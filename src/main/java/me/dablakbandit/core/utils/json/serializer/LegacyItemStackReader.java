package me.dablakbandit.core.utils.json.serializer;

import me.dablakbandit.core.utils.json.serializer.impl.MetaBlobUpgrader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Drop-in replacement for {@link BukkitObjectInputStream} for data written with BukkitObjectOutputStream.
 *
 * Bukkit resolves the nested ItemMeta before the ItemStack around it, so the meta never learns which DataVersion it
 * was saved on and its NBT blobs ("internal" / "unhandled") are parsed with the current codec, unconverted. This
 * reader holds the meta back until its ItemStack (which carries "v") is resolved, upgrades the blobs through the
 * server's DataFixer, then deserializes exactly like Bukkit would. Data that is already current is untouched.
 */
final class LegacyItemStackReader extends ObjectInputStream{

	private static final String	META_ALIAS	= "ItemMeta";
	private static final Class<?>	WRAPPER;
	private static final Field		WRAPPER_MAP;

	static{
		Class<?> wrapper = null;
		Field map = null;
		try{
			wrapper = Class.forName("org.bukkit.util.io.Wrapper", false, BukkitObjectInputStream.class.getClassLoader());
			map = wrapper.getDeclaredField("map");
			map.setAccessible(true);
		}catch(Exception ignored){
		}
		WRAPPER = wrapper;
		WRAPPER_MAP = map;
	}

	static boolean isAvailable(){
		return WRAPPER_MAP != null;
	}

	static Object read(byte[] bytes, MetaBlobUpgrader upgrader) throws IOException, ClassNotFoundException{
		try(LegacyItemStackReader in = new LegacyItemStackReader(bytes, upgrader)){
			Object result = in.readObject();
			if(result instanceof PendingMeta){
				result = ((PendingMeta)result).resolve(upgrader, -1, null);
			}
			return result;
		}
	}

	private final MetaBlobUpgrader upgrader;

	private LegacyItemStackReader(byte[] bytes, MetaBlobUpgrader upgrader) throws IOException{
		super(new ByteArrayInputStream(bytes));
		this.upgrader = upgrader;
		enableResolveObject(true);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object resolveObject(Object obj) throws IOException{
		if(!WRAPPER.isInstance(obj)){ return super.resolveObject(obj); }
		Map<String, Object> map;
		try{
			map = new LinkedHashMap<>((Map<String, ?>)WRAPPER_MAP.get(obj));
		}catch(IllegalAccessException e){
			throw new IOException(e);
		}
		if(META_ALIAS.equals(map.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY))){
			return new PendingMeta(map); // wait for the owning ItemStack to tell us its version
		}
		int version = map.get("v") instanceof Number ? ((Number)map.get("v")).intValue() : -1;
		Object type = map.get("type");
		for(Map.Entry<String, Object> e : map.entrySet()){
			if(e.getValue() instanceof PendingMeta){
				e.setValue(((PendingMeta)e.getValue()).resolve(upgrader, version, type instanceof String ? (String)type : null));
			}
		}
		return deserialize(map);
	}

	private static Object deserialize(Map<String, Object> map) throws IOException{
		Object result;
		try{
			result = ConfigurationSerialization.deserializeObject(map);
		}catch(Throwable t){
			throw new IOException("Failed to deserialize " + map.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY), t);
		}
		if(result == null){ throw new IOException("Failed to deserialize " + map.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY)); }
		return result;
	}

	private static final class PendingMeta{
		private final Map<String, Object> map;

		private PendingMeta(Map<String, Object> map){
			this.map = map;
		}

		private Object resolve(MetaBlobUpgrader upgrader, int version, String type) throws IOException{
			if(upgrader != null && version > 0){
				String itemId = itemId(type, version);
				for(String key : new String[]{ "internal", "unhandled" }){
					Object blob = map.get(key);
					if(blob instanceof String){
						try{
							map.put(key, upgrader.upgrade((String)blob, itemId, version));
						}catch(Throwable t){
							// leave the blob as-is; Bukkit will report it if it really can't be read
						}
					}
				}
			}
			return deserialize(map);
		}
	}

	private static String itemId(String type, int version){
		if(type == null){ return "minecraft:stone"; }
		try{
			Material material = Bukkit.getUnsafe().getMaterial(type, version);
			if(material != null){ return material.getKey().toString(); }
		}catch(Throwable ignored){
		}
		return "minecraft:" + type.toLowerCase(Locale.ROOT);
	}
}

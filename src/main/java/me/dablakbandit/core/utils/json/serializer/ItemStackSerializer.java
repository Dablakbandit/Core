package me.dablakbandit.core.utils.json.serializer;

import com.google.gson.*;
import me.dablakbandit.core.CoreLog;
import me.dablakbandit.core.json.JSONObject;
import me.dablakbandit.core.utils.ItemUtils;
import me.dablakbandit.core.utils.itemutils.IItemUtils;
import me.dablakbandit.core.utils.json.serializer.impl.MetaBlobUpgrader;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Base64;

/**
 * Stores ItemStacks as base64 strings.
 *
 * Paper and Spigot 26.x+: versioned item bytes ({@link ItemBytes}), upgraded by the server's DataFixer on load. Both
 * write the same format, so data moves freely between them. Older servers: Bukkit object serialization, as before.
 *
 * Reading accepts both. Old Bukkit-serialized data is upgraded in place on load (see {@link LegacyItemStackReader})
 * and is rewritten in the byte format the next time the owner saves.
 */
public class ItemStackSerializer implements JsonSerializer<Object>, JsonDeserializer<Object>{

	private static ItemStackSerializer serializer = new ItemStackSerializer();

	public static ItemStackSerializer getInstance(){
		return serializer;
	}

	private static IItemUtils		itemUtils;
	private static MetaBlobUpgrader	upgrader;
	private static boolean			upgraderChecked;

	private ItemStackSerializer(){

	}

	private static synchronized MetaBlobUpgrader upgrader(){
		if(!upgraderChecked){
			upgraderChecked = true;
			try{
				upgrader = new MetaBlobUpgrader();
			}catch(Throwable t){
				upgrader = null;
			}
		}
		return upgrader;
	}

	@Override
	public Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException{
		if(json.isJsonObject()){ return deserializeLegacyJSON(json); }
		if(!json.isJsonPrimitive()){ return null; }
		String asString = json.getAsString();
		try{
			byte[] bytes = Base64.getDecoder().decode(asString);
			if(ItemBytes.isItemBytes(bytes)){ return ItemBytes.deserialize(bytes); }
			Object item = LegacyItemStackReader.isAvailable() ? LegacyItemStackReader.read(bytes, upgrader()) : readBukkit(bytes);
			((ItemStack)item).getType();
			return item;
		}catch(Throwable t){
			Throwable root = t;
			while(root.getCause() != null){
				root = root.getCause();
			}
			CoreLog.error("Failed to load ItemStack (" + root.getClass().getSimpleName() + ": " + root.getMessage() + "), raw data: " + asString);
		}
		return null;
	}

	private static Object readBukkit(byte[] bytes) throws Exception{
		try(BukkitObjectInputStream bois = new BukkitObjectInputStream(new ByteArrayInputStream(bytes))){
			return bois.readObject();
		}
	}

	private static Object deserializeLegacyJSON(JsonElement json){
		try{
			if(itemUtils == null){
				itemUtils = ItemUtils.getInstance();
			}
			return itemUtils.convertJSONToItemStack(new JSONObject(json.getAsJsonObject().toString()));
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JsonElement serialize(Object item, Type type, JsonSerializationContext context){
		try{
			if(ItemBytes.isAvailable() && item instanceof ItemStack){
				ItemStack stack = (ItemStack)item;
				if(stack.getType() != Material.AIR && stack.getAmount() > 0){
					return new JsonPrimitive(Base64.getEncoder().encodeToString(ItemBytes.serialize(stack)));
				}
			}
		}catch(Throwable t){
			CoreLog.error("Failed to write ItemStack as bytes, falling back to Bukkit serialization: " + t);
		}
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);
			boos.writeObject(item);
			boos.flush();
			boos.close();
			return new JsonPrimitive(Base64.getEncoder().encodeToString(baos.toByteArray()));
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}

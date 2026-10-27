package me.dablakbandit.core.utils.json.serializer;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Paper's versioned item byte API ({@code ItemStack#serializeAsBytes} / {@code ItemStack.deserializeBytes}),
 * looked up by reflection because Core compiles against the Spigot API. Not present on Spigot.
 */
public final class ItemBytes{

	private static final Method	SERIALIZE;
	private static final Method	DESERIALIZE;

	static{
		Method serialize = null, deserialize = null;
		try{
			serialize = ItemStack.class.getMethod("serializeAsBytes");
			deserialize = ItemStack.class.getMethod("deserializeBytes", byte[].class);
		}catch(NoSuchMethodException ignored){
			serialize = null;
			deserialize = null;
		}
		SERIALIZE = serialize;
		DESERIALIZE = deserialize;
	}

	private ItemBytes(){
	}

	public static boolean isAvailable(){
		return SERIALIZE != null;
	}

	/** Gzip NBT with a DataVersion, upgraded by the server's DataFixer when read back. Item must not be empty. */
	public static byte[] serialize(ItemStack item) throws Exception{
		return (byte[])invoke(SERIALIZE, item);
	}

	public static ItemStack deserialize(byte[] bytes) throws Exception{
		return (ItemStack)invoke(DESERIALIZE, null, (Object)bytes);
	}

	/** True for data written by {@link #serialize} (gzip magic); Bukkit object streams start with 0xACED. */
	public static boolean isItemBytes(byte[] bytes){
		return bytes.length > 2 && (bytes[0] & 0xFF) == 0x1F && (bytes[1] & 0xFF) == 0x8B;
	}

	private static Object invoke(Method method, Object target, Object... args) throws Exception{
		if(method == null){ throw new UnsupportedOperationException("ItemStack byte serialization requires Paper"); }
		try{
			return method.invoke(target, args);
		}catch(InvocationTargetException e){
			Throwable cause = e.getCause();
			throw cause instanceof Exception ? (Exception)cause : e;
		}
	}
}

package me.dablakbandit.core.utils.json.serializer;

import me.dablakbandit.core.CoreLog;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Versioned item bytes: gzip NBT with a root DataVersion, upgraded by the server's DataFixer when read back.
 *
 * Paper: its API, {@code ItemStack#serializeAsBytes} / {@code ItemStack.deserializeBytes} (by reflection, since Core
 * compiles against the Spigot API). Spigot 26.x+: the same format through the vanilla codec
 * ({@link me.dablakbandit.core.utils.json.serializer.impl.VanillaItemBytes}). Data is interchangeable between the two.
 */
public final class ItemBytes{

	public interface Backend{
		byte[] serialize(ItemStack item) throws Exception;

		ItemStack deserialize(byte[] bytes) throws Exception;
	}

	private static final Backend BACKEND = load();

	private static Backend load(){
		try{
			Backend paper = new PaperBackend();
			CoreLog.info("ItemStack storage: Paper item byte API");
			return paper;
		}catch(Throwable ignored){
		}
		try{
			Backend vanilla = new me.dablakbandit.core.utils.json.serializer.impl.VanillaItemBytes();
			CoreLog.info("ItemStack storage: server item codec + DataFixer");
			return vanilla;
		}catch(Throwable ignored){
		}
		CoreLog.info("ItemStack storage: Bukkit object streams");
		return null;
	}

	private ItemBytes(){
	}

	public static boolean isAvailable(){
		return BACKEND != null;
	}

	/** Item must not be empty. */
	public static byte[] serialize(ItemStack item) throws Exception{
		return backend().serialize(item);
	}

	public static ItemStack deserialize(byte[] bytes) throws Exception{
		return backend().deserialize(bytes);
	}

	/** True for data written by {@link #serialize} (gzip magic); Bukkit object streams start with 0xACED. */
	public static boolean isItemBytes(byte[] bytes){
		return bytes.length > 2 && (bytes[0] & 0xFF) == 0x1F && (bytes[1] & 0xFF) == 0x8B;
	}

	private static Backend backend(){
		if(BACKEND == null){ throw new UnsupportedOperationException("ItemStack byte serialization is not supported on this server"); }
		return BACKEND;
	}

	private static final class PaperBackend implements Backend{
		private final Method	serialize	= ItemStack.class.getMethod("serializeAsBytes");
		private final Method	deserialize	= ItemStack.class.getMethod("deserializeBytes", byte[].class);

		private PaperBackend() throws NoSuchMethodException{
		}

		@Override
		public byte[] serialize(ItemStack item) throws Exception{
			return (byte[])invoke(serialize, item);
		}

		@Override
		public ItemStack deserialize(byte[] bytes) throws Exception{
			return (ItemStack)invoke(deserialize, null, (Object)bytes);
		}
	}

	private static Object invoke(Method method, Object target, Object... args) throws Exception{
		try{
			return method.invoke(target, args);
		}catch(InvocationTargetException e){
			Throwable cause = e.getCause();
			throw cause instanceof Exception ? (Exception)cause : e;
		}
	}
}

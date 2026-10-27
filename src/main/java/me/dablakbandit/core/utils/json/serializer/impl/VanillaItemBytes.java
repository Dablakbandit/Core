package me.dablakbandit.core.utils.json.serializer.impl;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import me.dablakbandit.core.utils.NMSUtils;
import me.dablakbandit.core.utils.json.serializer.ItemBytes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Spigot backend for {@link ItemBytes}: the same versioned format as Paper's {@code ItemStack#serializeAsBytes}
 * (gzip NBT from the vanilla item codec with a root DataVersion), upgraded on load by the server's DataFixer, exactly
 * as Paper's {@code ItemStack.deserializeBytes} does. Data written on either platform loads on the other.
 *
 * Uses vanilla (Mojang-named) classes, so it needs a 26.x+ server; constructed inside a try/catch, so older servers
 * fall back to Bukkit serialization.
 */
public class VanillaItemBytes implements ItemBytes.Backend{

	private final Method			asNMSCopy;
	private final Method			asBukkitCopy;
	private final MinecraftServer	server;

	public VanillaItemBytes() throws Exception{
		Class<?> craftItemStack = NMSUtils.getOBCClassSilent("inventory.CraftItemStack", "org.bukkit.craftbukkit.inventory.CraftItemStack");
		if(craftItemStack == null){ throw new ClassNotFoundException("CraftItemStack"); }
		asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
		asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class);
		// CraftServer#getServer: MinecraftServer.getServer() is a CraftBukkit addition, not in the vanilla jar
		Object craftServer = Bukkit.getServer();
		server = (MinecraftServer)craftServer.getClass().getMethod("getServer").invoke(craftServer);
		// Touch everything now so a missing class fails here, not mid-load
		ops();
		DataFixers.getDataFixer();
		References.ITEM_STACK.typeName();
		NbtAccounter.unlimitedHeap();
	}

	private DynamicOps<Tag> ops(){
		return server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
	}

	/** result() rather than getOrThrow(): getOrThrow's signature differs between DataFixerUpper versions. */
	private static <T> T orThrow(DataResult<T> result){
		return result.result().orElseThrow(() -> new IllegalStateException(String.valueOf(result)));
	}

	@Override
	public byte[] serialize(ItemStack item) throws Exception{
		net.minecraft.world.item.ItemStack nms = (net.minecraft.world.item.ItemStack)invoke(asNMSCopy, item);
		CompoundTag tag = (CompoundTag)orThrow(net.minecraft.world.item.ItemStack.CODEC.encodeStart(ops(), nms));
		NbtUtils.addCurrentDataVersion(tag);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		NbtIo.writeCompressed(tag, out);
		return out.toByteArray();
	}

	@Override
	public ItemStack deserialize(byte[] bytes) throws Exception{
		CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.unlimitedHeap());
		int from = NbtUtils.getDataVersion(tag, 0);
		int to = Bukkit.getUnsafe().getDataVersion();
		if(from < to){
			tag = (CompoundTag)DataFixers.getDataFixer().update(References.ITEM_STACK, new Dynamic<Tag>(NbtOps.INSTANCE, tag), from, to).getValue();
		}
		if(tag.getStringOr("id", "minecraft:air").equals("minecraft:air")){ return new ItemStack(Material.AIR); }
		net.minecraft.world.item.ItemStack nms = orThrow(net.minecraft.world.item.ItemStack.CODEC.parse(ops(), tag));
		return (ItemStack)invoke(asBukkitCopy, nms);
	}

	private static Object invoke(Method method, Object arg) throws Exception{
		try{
			return method.invoke(null, arg);
		}catch(InvocationTargetException e){
			Throwable cause = e.getCause();
			throw cause instanceof Exception ? (Exception)cause : e;
		}
	}
}

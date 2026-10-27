package me.dablakbandit.core.utils.json.serializer.impl;

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

	private final Method	asNMSCopy;
	private final Method	asBukkitCopy;

	public VanillaItemBytes() throws Exception{
		Class<?> craftItemStack = NMSUtils.getOBCClassSilent("inventory.CraftItemStack", "org.bukkit.craftbukkit.inventory.CraftItemStack");
		if(craftItemStack == null){ throw new ClassNotFoundException("CraftItemStack"); }
		asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
		asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", net.minecraft.world.item.ItemStack.class);
		// Touch everything now so a missing class fails here, not mid-load
		ops();
		MinecraftServer.getServer().getFixerUpper();
		References.ITEM_STACK.typeName();
		NbtAccounter.unlimitedHeap();
	}

	private static DynamicOps<Tag> ops(){
		return MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE);
	}

	@Override
	public byte[] serialize(ItemStack item) throws Exception{
		net.minecraft.world.item.ItemStack nms = (net.minecraft.world.item.ItemStack)invoke(asNMSCopy, item);
		CompoundTag tag = (CompoundTag)net.minecraft.world.item.ItemStack.CODEC.encodeStart(ops(), nms).getOrThrow();
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
			tag = (CompoundTag)MinecraftServer.getServer().getFixerUpper().update(References.ITEM_STACK, new Dynamic<Tag>(NbtOps.INSTANCE, tag), from, to).getValue();
		}
		if(tag.getStringOr("id", "minecraft:air").equals("minecraft:air")){ return new ItemStack(Material.AIR); }
		net.minecraft.world.item.ItemStack nms = net.minecraft.world.item.ItemStack.CODEC.parse(ops(), tag).getOrThrow();
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

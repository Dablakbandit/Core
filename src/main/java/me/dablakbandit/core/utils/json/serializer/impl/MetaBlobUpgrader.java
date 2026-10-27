package me.dablakbandit.core.utils.json.serializer.impl;

import me.dablakbandit.core.utils.json.serializer.ItemBytes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Upgrades the component data inside a base64 CraftMetaItem blob ("internal" / "unhandled") that was written on an
 * older version. Bukkit parses those blobs with the current codec and never runs the DataFixer on them, which is what
 * breaks e.g. shulker contents saved before 1.21.5.
 *
 * The upgrade itself is done by the server through {@code ItemStack.deserializeBytes}; the only server classes used
 * here are the vanilla NBT reader/writer needed to open the blob. Loaded inside a try/catch, so servers without them
 * (or without the item byte API) simply skip the upgrade.
 */
public class MetaBlobUpgrader{

	public MetaBlobUpgrader() throws Exception{
		if(!ItemBytes.isAvailable()){ throw new UnsupportedOperationException("ItemStack byte API not available"); }
		NbtAccounter.unlimitedHeap(); // fail here, not mid-load, if the NBT classes are missing
	}

	/** Returns the blob with its component data upgraded from {@code fromVersion}, or the input if nothing needed fixing. */
	public String upgrade(String base64, String itemId, int fromVersion) throws Exception{
		if(base64 == null || fromVersion <= 0 || fromVersion >= Bukkit.getUnsafe().getDataVersion()){ return base64; }
		CompoundTag root = read(Base64.getDecoder().decode(base64));
		if(!upgradeComponentMaps(root, itemId, fromVersion, 0)){ return base64; }
		return Base64.getEncoder().encodeToString(write(root));
	}

	/** Finds every component map (keys like "minecraft:container") in the blob and upgrades it in place. */
	private static boolean upgradeComponentMaps(CompoundTag tag, String itemId, int from, int depth) throws Exception{
		if(depth > 8){ return false; }
		if(isComponentMap(tag)){
			CompoundTag fixed = upgradeComponents(tag, itemId, from);
			for(String key : new ArrayList<>(tag.keySet())){
				tag.remove(key);
			}
			for(String key : fixed.keySet()){
				tag.put(key, fixed.get(key));
			}
			return true;
		}
		boolean changed = false;
		for(String key : new ArrayList<>(tag.keySet())){
			Tag child = tag.get(key);
			if(child instanceof CompoundTag){
				changed |= upgradeComponentMaps((CompoundTag)child, itemId, from, depth + 1);
			}
		}
		return changed;
	}

	private static boolean isComponentMap(CompoundTag tag){
		for(String key : tag.keySet()){
			if(key.startsWith("minecraft:") || key.startsWith("!minecraft:")){ return true; }
		}
		return false;
	}

	/**
	 * Wraps the components in an item stamped with the old DataVersion, lets the server load it (running the
	 * DataFixer), then reads the components back off the upgraded item.
	 */
	private static CompoundTag upgradeComponents(CompoundTag components, String itemId, int from) throws Exception{
		CompoundTag item = new CompoundTag();
		item.putString("id", itemId);
		item.putInt("count", 1);
		item.put("components", components.copy());
		item.putInt("DataVersion", from);
		ItemStack upgraded = ItemBytes.deserialize(write(item));
		return read(ItemBytes.serialize(upgraded)).getCompound("components").orElseGet(CompoundTag::new);
	}

	private static CompoundTag read(byte[] bytes) throws IOException{
		return NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.unlimitedHeap());
	}

	private static byte[] write(CompoundTag tag) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		NbtIo.writeCompressed(tag, out);
		return out.toByteArray();
	}
}

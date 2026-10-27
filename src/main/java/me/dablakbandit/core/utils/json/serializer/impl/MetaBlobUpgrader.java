package me.dablakbandit.core.utils.json.serializer.impl;

import me.dablakbandit.core.utils.json.serializer.ItemBytes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Upgrades the item data inside a base64 CraftMetaItem blob ("internal" / "unhandled") written on an older version.
 * Bukkit parses those blobs with the current codec and never runs the DataFixer on them, which is what breaks e.g.
 * shulker contents saved before 1.21.5.
 *
 * Handles both layouts: Paper's component maps ("block-entity-components" -> "minecraft:container" -> items) and
 * Spigot's block entity NBT ("BlockEntityTag" -> "Items" list). The upgrade itself is done by the server, by loading
 * the data through {@link ItemBytes} at its original DataVersion. The only server classes used here are the vanilla
 * NBT reader/writer needed to open the blob.
 */
public class MetaBlobUpgrader{

	/** Keys that make up an item stack in any version; anything else on an item entry (e.g. "Slot") is kept as-is. */
	private static final Set<String> ITEM_KEYS = new HashSet<>(Arrays.asList("id", "count", "Count", "tag", "components", "Damage", "DataVersion"));

	public MetaBlobUpgrader() throws Exception{
		if(!ItemBytes.isAvailable()){ throw new UnsupportedOperationException("Item byte serialization not available"); }
		NbtAccounter.unlimitedHeap(); // fail here, not mid-load, if the NBT classes are missing
	}

	/** Returns the blob with its item data upgraded from {@code fromVersion}, or the input if nothing needed fixing. */
	public String upgrade(String base64, String itemId, int fromVersion) throws Exception{
		if(base64 == null || fromVersion <= 0 || fromVersion >= Bukkit.getUnsafe().getDataVersion()){ return base64; }
		CompoundTag root = read(Base64.getDecoder().decode(base64));
		Tag fixed = fix(root, itemId, fromVersion, 0);
		if(fixed == null){ return base64; }
		return Base64.getEncoder().encodeToString(write((CompoundTag)fixed));
	}

	/** Returns the upgraded tag, or null if nothing under it changed. */
	private static Tag fix(Tag tag, String itemId, int from, int depth){
		if(depth > 64){ return null; }
		if(tag instanceof CompoundTag){
			CompoundTag compound = (CompoundTag)tag;
			// Upgrade as a whole where possible; if the server can't load it that way, fall through and fix what's inside
			if(isItem(compound)){
				CompoundTag upgraded = upgradeItem(compound, from);
				if(upgraded != null){ return upgraded; }
			}else if(isComponentMap(compound)){
				CompoundTag upgraded = upgradeComponents(compound, itemId, from);
				if(upgraded != null){ return upgraded; }
			}
			boolean changed = false;
			for(String key : new ArrayList<>(compound.keySet())){
				Tag fixed = fix(compound.get(key), itemId, from, depth + 1);
				if(fixed != null){
					compound.put(key, fixed);
					changed = true;
				}
			}
			return changed ? compound : null;
		}
		if(tag instanceof ListTag){
			ListTag list = (ListTag)tag;
			boolean changed = false;
			for(int i = 0; i < list.size(); i++){
				Tag fixed = fix(list.get(i), itemId, from, depth + 1);
				if(fixed != null){
					list.set(i, fixed);
					changed = true;
				}
			}
			return changed ? list : null;
		}
		return null;
	}

	/** An item stack entry in any version: {id, count|Count, ...}. Block entities and entities have no count. */
	private static boolean isItem(CompoundTag tag){
		return tag.getString("id").isPresent() && (tag.contains("count") || tag.contains("Count"));
	}

	/** A data component map / patch: keys like "minecraft:container" or "!minecraft:lore". */
	private static boolean isComponentMap(CompoundTag tag){
		for(String key : tag.keySet()){
			if(key.startsWith("minecraft:") || key.startsWith("!minecraft:")){ return true; }
		}
		return false;
	}

	/** Loads the item through the server at its old DataVersion and returns it in the current format. */
	private static CompoundTag upgradeItem(CompoundTag original, int from){
		CompoundTag item = original.copy();
		item.putInt("DataVersion", from);
		CompoundTag upgraded = roundTrip(item);
		if(upgraded == null){ return null; }
		for(String key : original.keySet()){
			if(!ITEM_KEYS.contains(key) && !upgraded.contains(key)){
				upgraded.put(key, original.get(key).copy());
			}
		}
		return upgraded;
	}

	/** Wraps the components in an item of the owner's type, loads it at the old DataVersion, and reads them back. */
	private static CompoundTag upgradeComponents(CompoundTag components, String itemId, int from){
		CompoundTag item = new CompoundTag();
		item.putString("id", itemId);
		item.putInt("count", 1);
		item.put("components", components.copy());
		item.putInt("DataVersion", from);
		CompoundTag upgraded = roundTrip(item);
		if(upgraded == null){ return null; }
		return upgraded.getCompound("components").orElseGet(CompoundTag::new);
	}

	/** Returns null (leave the data as it was) if the server can't make an item out of it. */
	private static CompoundTag roundTrip(CompoundTag item){
		try{
			ItemStack stack = ItemBytes.deserialize(write(item));
			if(stack == null || stack.getType().isAir() || stack.getAmount() <= 0){ return null; }
			CompoundTag upgraded = read(ItemBytes.serialize(stack));
			upgraded.remove("DataVersion");
			return upgraded;
		}catch(Throwable t){
			return null;
		}
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

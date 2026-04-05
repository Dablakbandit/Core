package me.dablakbandit.core.utils.anvil.impl;

import me.dablakbandit.core.utils.NMSUtils;
import me.dablakbandit.core.utils.PacketUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import static me.dablakbandit.core.utils.NMSUtils.*;

public class _16AnvilUtil implements IAnvilUtil{
    public static Class<?>			classEntity						= getNMSClassSilent("Entity");
    public static Class<?> classEntityHuman = getNMSClassSilent("EntityHuman");
    public static Class<?> classEntityPlayer = getNMSClassSilent("EntityPlayer");

    private static Class<?>			classWorld						= getNMSClassSilent("World");
    private static Class<?>			classContainer					= getNMSClassSilent("Container");
    private static Class<?>			classContainerAnvil				= getNMSClassSilent("ContainerAnvil");
    private static Class<?>			classBlockPosition				= getNMSClassSilent("BlockPosition");
    private static Class<?>			classPacketPlayOutOpenWindow	= getNMSClassSilent("PacketPlayOutOpenWindow");
    private static Class<?>			classChatMessage				= getNMSClassSilent("ChatMessage");
    private static Class<?>			classContainers					= getNMSClassSilent("Containers");

    private static Class<?>			classContainerAccess			= getNMSClassSilent("ContainerAccess");
    private static Method atContainerAccess				= getMethodSilent(classContainerAccess, "at", classWorld, classBlockPosition);
    private static final Method getBukkitView = getMethodSilent(classContainer, "getBukkitView");

    private static Constructor<?> conContainerAnvil				= getConContainerAnvil();

    private static Constructor<?> getConContainerAnvil(){
        if(classContainers != null){ return getConstructorSilent(classContainerAnvil, int.class, getNMSClass("PlayerInventory"), classContainerAccess); }
        if(classBlockPosition != null){ return getConstructorSilent(classContainerAnvil, getNMSClass("PlayerInventory"), classWorld, classBlockPosition, classEntityHuman); }
        return getConstructorSilent(classContainerAnvil, getNMSClassSilent("PlayerInventory"), classWorld, int.class, int.class, int.class, classEntityHuman);
    }

    private static Constructor<?>	conBlockPosition			= getConstructorSilent(classBlockPosition, int.class, int.class, int.class);
    private static Class<?>			classIChatBaseComponent		= getNMSClassSilent("IChatBaseComponent");
    private static Constructor<?>	conPacketPlayOutOpenWindow	= getConPacketPlayOutOpenWindow();

    public static Constructor<?> getConPacketPlayOutOpenWindow(){
        try{
            return getConstructorWithException(classPacketPlayOutOpenWindow, int.class, classContainers, classIChatBaseComponent);
        }catch(Exception e){

        }
        try{
            return getConstructorWithException(classPacketPlayOutOpenWindow, int.class, String.class, classIChatBaseComponent, int.class);
        }catch(Exception e){
            try{
                return getConstructorWithException(classPacketPlayOutOpenWindow, int.class, int.class, String.class, int.class, boolean.class);
            }catch(Exception e1){
            }
        }
        return null;
    }

    private static Constructor<?>	conChatMessage				= getConstructorSilent(classChatMessage, String.class, Object[].class);

    private static Field fieldCheckReachable			= getFieldSilent(classContainer, "checkReachable");
    private static Field			fieldTitle					= getFieldSilent(classContainer, "title");
    private static Field			fieldWindowID				= getFieldSilent(classContainer, "windowId");
    private static Field			fieldInventory				= getFieldSilent(classEntityHuman, "inventory");
    private static Field			fieldActiveContainer		= getFieldSilent(classEntityHuman, "activeContainer");
    private static Field			fieldWorld					= getFieldSilent(classEntity, "world");

    private static Method			nextContainerCounter		= getMethodSilent(classEntityPlayer, "nextContainerCounter");
    private static Method			addSlotListener				= getMethodSilent(classContainer, "addSlotListener", getNMSClassSilent("ICrafting"));

    private static Object			blockPosition				= classBlockPosition != null ? newInstance(conBlockPosition, 0, 0, 0) : null;

    private static Class			classItemStack				= getNMSClassSilent("ItemStack");

    private static Class			classPacketPlayOutSetSlot	= getNMSClassSilent("PacketPlayOutSetSlot");
    private static Constructor<?>	conPacketPlayOutSetSlot		= getConstructorSilent(classPacketPlayOutSetSlot, int.class, int.class, classItemStack);

    private static Class<?>			classCraftContainer			= getOBCClassSilent("inventory.CraftContainer");
    private static Method			getNotchInventoryType		= getMethodSilent(classCraftContainer, "getNotchInventoryType", InventoryType.class);
    private static Field			fieldANVIL					= getFieldSilent(classContainers, "ANVIL");
    private static Object			objectANVIL;

    static{
        try{
            objectANVIL = fieldANVIL.get(null);
        }catch(Exception e){

        }
    }

    public static Object getType(InventoryType type) throws Exception{
        if(objectANVIL != null){ return objectANVIL; }
        return getNotchInventoryType.invoke(null, type);
    }

    private static Object getPacketPlayOutOpenWindow(int id) throws Exception{
        return getPacketPlayOutOpenWindow("Enter", id);
    }

    private static Object getPacketPlayOutOpenWindow(String message, int id) throws Exception{
        switch(conPacketPlayOutOpenWindow.getParameterTypes().length){
            case 3: {
                Object type = getType(InventoryType.ANVIL);
                Object chatMessage = newInstance(conChatMessage, message, new Object[0]);
                return conPacketPlayOutOpenWindow.newInstance(id, type, chatMessage);
            }
            case 4: {
                Object chatMessage = newInstance(conChatMessage, message, new Object[0]);
                return conPacketPlayOutOpenWindow.newInstance(id, "minecraft:anvil", chatMessage, 0);
            }
            case 5:
                conPacketPlayOutOpenWindow.newInstance(id, 8, "Repairing", 9, true);
            default:
                throw new Exception("Constructor not found");
        }
    }

    public void open(Player player, Consumer<Inventory> after){
        open(player, "Enter", after);
    }

    public void open(Player player, String message, Consumer<Inventory> after){
        try{
            Object nmsPlayer = getHandle(player);
            Object anvilcon;
            if(classBlockPosition != null){
                if(conContainerAnvil.getParameterTypes().length == 3){
                    Object at = atContainerAccess.invoke(null, fieldWorld.get(nmsPlayer), blockPosition);
                    anvilcon = conContainerAnvil.newInstance(0, fieldInventory.get(nmsPlayer), at);
                    if(fieldTitle != null){
                        Object chatMessage = NMSUtils.newInstance(conChatMessage, message, new Object[0]);
                        fieldTitle.set(anvilcon, chatMessage);
                    }
                }else{
                    anvilcon = conContainerAnvil.newInstance(fieldInventory.get(nmsPlayer), fieldWorld.get(nmsPlayer), blockPosition, nmsPlayer);
                }
            }else{
                anvilcon = conContainerAnvil.newInstance(fieldInventory.get(nmsPlayer), fieldWorld.get(nmsPlayer), 0, 0, 0, nmsPlayer);
            }
            fieldCheckReachable.set(anvilcon, false);

            int c = (Integer)nextContainerCounter.invoke(nmsPlayer);
            PacketUtils.sendPacket(player, getPacketPlayOutOpenWindow(message, c));
            fieldActiveContainer.set(nmsPlayer, anvilcon);
            fieldWindowID.set(anvilcon, c);
            addSlotListener.invoke(anvilcon, nmsPlayer);
            after.accept(((InventoryView) getBukkitView.invoke(anvilcon)).getTopInventory());
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}

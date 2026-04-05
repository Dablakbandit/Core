package me.dablakbandit.core.utils.anvil.impl;

import me.dablakbandit.core.utils.NMSUtils;
import me.dablakbandit.core.utils.PacketUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import static me.dablakbandit.core.utils.NMSUtils.getConstructorSilent;

public class HistoricAnvilUtil implements IAnvilUtil {
    public static Class<?> classEntity = NMSUtils.getClassSilent("net.minecraft.world.entity.Entity");
    public static Class<?> classEntityHuman = NMSUtils.getClassSilent("net.minecraft.world.entity.player.EntityHuman");
    public static Class<?> classEntityPlayer = NMSUtils.getClassSilent("net.minecraft.server.level.EntityPlayer");

    private static final Class<?> classWorld = NMSUtils.getClassSilent("net.minecraft.world.level.World");
    private static final Class<?> classContainer = NMSUtils.getClassSilent("net.minecraft.world.inventory.Container");
    private static final Class<?> classPlayerInventory = NMSUtils.getClassSilent("net.minecraft.world.entity.player.PlayerInventory");
    private static final Class<?> classContainerAnvil = NMSUtils.getClassSilent("net.minecraft.world.inventory.ContainerAnvil");
    private static final Class<?> classBlockPosition = NMSUtils.getPossibleClass("net.minecraft.core.BlockPosition", "net.minecraft.core.BlockPos");
    private static final Class<?> classPacketPlayOutOpenWindow = NMSUtils.getPossibleClass("net.minecraft.network.protocol.game.PacketPlayOutOpenWindow", "net.minecraft.network.protocol.game.ClientboundOpenScreenPacket");
    private static final Class<?> classChatMessage = NMSUtils.getClassSilent("net.minecraft.network.chat.ChatMessage");
    private static final Class<?> classContainers = NMSUtils.getPossibleClass("net.minecraft.world.inventory.Containers", "net.minecraft.world.inventory.MenuType");
    private static final Class<?> classContainersSupplier = NMSUtils.getPossibleInnerClassSilent(classContainers, "Supplier", "MenuSupplier");
    private static final Class<?> classContainerSynchronizer = NMSUtils.getClassSilent("net.minecraft.world.inventory.ContainerSynchronizer");

    private static final Class<?> classContainerAccess = NMSUtils.getPossibleClass("net.minecraft.world.inventory.ContainerAccess", "net.minecraft.world.inventory.ContainerLevelAccess");
    private static final Method atContainerAccess = NMSUtils.getMethodSilent(classContainerAccess, new String[]{"a","at", "create"}, classWorld, classBlockPosition);
    private static final Method getBukkitView = NMSUtils.getMethodSilent(classContainer, "getBukkitView");

    private static final Constructor<?> conContainerAnvil = getConstructorSilent(classContainerAnvil, int.class, NMSUtils.getClassSilent("net.minecraft.world.entity.player.PlayerInventory"), classContainerAccess);

    private static final Constructor<?> conBlockPosition = getConstructorSilent(classBlockPosition, int.class, int.class, int.class);
    private static final Class<?> classIChatBaseComponent = NMSUtils.getPossibleClass("net.minecraft.network.chat.IChatBaseComponent", "net.minecraft.network.chat.Component");
    private static final Constructor<?> conPacketPlayOutOpenWindow = getConstructorSilent(classPacketPlayOutOpenWindow, int.class, classContainers, classIChatBaseComponent);

    private static final Constructor<?> conChatMessage = getConstructorSilent(classChatMessage, String.class, Object[].class);

    private static final Field fieldCheckReachable = NMSUtils.getFieldSilent(classContainer, "checkReachable");
    private static final Field fieldTitle = NMSUtils.getFieldSilent(classContainer, "title");
    private static final Field fieldInventory = NMSUtils.getFirstFieldOfTypeSilent(classEntityHuman, classPlayerInventory);
    private static final Field fieldActiveContainer = NMSUtils.getFirstFieldOfTypeSilent(classEntityHuman, classContainer);
    private static final Field fieldWorld = NMSUtils.getFirstFieldOfTypeSilent(classEntity, classWorld);

    private static final Method nextContainerCounter = NMSUtils.getMethodSilent(classEntityPlayer, "nextContainerCounter");
    private static final Method initMenu = NMSUtils.getMethodSilent(classEntityPlayer, new String[]{"a","initMenu"}, classContainer);

    private static final Object blockPosition = NMSUtils.newInstance(conBlockPosition, 0, 0, 0);

    private static final Class<?> classCraftChatMessage = NMSUtils.getOBCClassSilent("util.CraftChatMessage", "org.bukkit.craftbukkit.util.CraftChatMessage");
    private static final Method cccmfromString = NMSUtils.getMethodSilent(classCraftChatMessage, "fromString", String.class);
    private static final Field containersSupplier = NMSUtils.getFirstFieldOfTypeSilent(classContainers, classContainersSupplier);
    private static final Method containersSupplierCreate = NMSUtils.getMethodSilent(classContainersSupplier, "create", int.class, classPlayerInventory);
    private static Object objectANVIL;

    static {
        try {
            for (Field field : NMSUtils.getFields(classContainers)) {
                if(field.getType().equals(classContainers)){
                    Object object = field.get(null);
                    Object supplier = containersSupplier.get(object);
                    try {
                        containersSupplierCreate.invoke(supplier, 0, null);
                    }catch (Exception e){
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        if(sw.toString().contains("ContainerAnvil.java") || sw.toString().contains("AnvilMenu.java")){
                            objectANVIL = object;
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static Object getObjectAnvil() {
        return objectANVIL;
    }

    private static Object getPacketPlayOutOpenWindow(int id) throws Exception {
        return getPacketPlayOutOpenWindow("Enter", id);
    }

    private static Object getPacketPlayOutOpenWindow(String message, int id) throws Exception {
        Object chatMessage = NMSUtils.newInstance(conChatMessage, message, new Object[0]);
        if(chatMessage==null && cccmfromString!=null){
            chatMessage = ((Object[])cccmfromString.invoke(null, message))[0];
        }
        return conPacketPlayOutOpenWindow.newInstance(id, objectANVIL, chatMessage);
    }

    public void open(Player player, Consumer<Inventory> after) {
        open(player, "Enter", after);
    }

    public void open(Player player, String message, Consumer<Inventory> after) {
        try {
            player.closeInventory();
            Object nmsPlayer = NMSUtils.getHandle(player);
            Object anvilcon;
            int c = (Integer) nextContainerCounter.invoke(nmsPlayer);
            Object at = atContainerAccess.invoke(null, fieldWorld.get(nmsPlayer), blockPosition);
            anvilcon = conContainerAnvil.newInstance(c, fieldInventory.get(nmsPlayer), at);
            if (fieldTitle != null) {
                Object chatMessage = NMSUtils.newInstance(conChatMessage, message, new Object[0]);
                if(chatMessage==null && cccmfromString!=null){
                    chatMessage = ((Object[])cccmfromString.invoke(null, message))[0];
                }
                fieldTitle.set(anvilcon, chatMessage);
            }
            fieldCheckReachable.set(anvilcon, false);
            PacketUtils.sendPacket(player, getPacketPlayOutOpenWindow(message, c));
            fieldActiveContainer.set(nmsPlayer, anvilcon);
            initMenu.invoke(nmsPlayer, anvilcon);
            after.accept(((InventoryView) getBukkitView.invoke(anvilcon)).getTopInventory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void open(Player player, Runnable after) {
        open(player, (i)->{
            after.run();
        });
    }


}

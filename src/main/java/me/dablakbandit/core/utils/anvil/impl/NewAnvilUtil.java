package me.dablakbandit.core.utils.anvil.impl;

import me.dablakbandit.core.utils.NMSUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

public class NewAnvilUtil implements IAnvilUtil {

    public void open(Player player, Consumer<Inventory> after) {
        open(player, "Enter", after);
    }

    public void open(Player player, String message, Consumer<Inventory> after) {
        try {
            player.closeInventory();
            ServerPlayer nmsPlayer = (ServerPlayer) NMSUtils.getHandle(player);
            nmsPlayer.openMenu(new MenuProvider() {
                                   @Override
                                   public Component getDisplayName() {
                                        return Component.literal(message);
                                   }

                                   @Override
                                   public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player player) {
                                       return new CustomAnvilMenu(containerId, inventory, ContainerLevelAccess.create(nmsPlayer.level(), nmsPlayer.blockPosition()));
                                   }
                               });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class CustomAnvilMenu extends AnvilMenu {
        public CustomAnvilMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory, ContainerLevelAccess access) {
            super(containerId, inventory, access);
        }

        @Override
        protected boolean isValidBlock(BlockState state) {
            return true;
        }
    }

    public void open(Player player, Runnable after) {
        open(player, (i)->{
            after.run();
        });
    }


}

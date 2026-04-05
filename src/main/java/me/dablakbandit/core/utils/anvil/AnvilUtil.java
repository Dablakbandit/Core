package me.dablakbandit.core.utils.anvil;

import me.dablakbandit.core.CoreLog;
import me.dablakbandit.core.utils.anvil.impl.IAnvilUtil;
import me.dablakbandit.core.utils.anvil.impl.HistoricAnvilUtil;
import me.dablakbandit.core.utils.anvil.impl._16AnvilUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

public class AnvilUtil{

	private static IAnvilUtil anvilUtil = load();

	public static IAnvilUtil load(){
		if(anvilUtil!=null){
			return anvilUtil;
		}
		try{
			CoreLog.info("Attempting to load old AnvilUtil");
			HistoricAnvilUtil.getObjectAnvil().getClass();
			anvilUtil = new HistoricAnvilUtil();
			CoreLog.info("Loaded old, enjoy :)");
			return anvilUtil;
		}catch(Exception e){
		}
		try{
			CoreLog.info("Attempting to load 1.16 AnvilUtil");
			_16AnvilUtil.getConPacketPlayOutOpenWindow().getClass();
			anvilUtil = new _16AnvilUtil();
			CoreLog.info("Loaded 1.16, enjoy :)");
			return anvilUtil;
		}catch(Exception e){
		}
		try{
			CoreLog.info("Attempting to load propper AnvilUtil");
			anvilUtil = new me.dablakbandit.core.utils.anvil.impl.NewAnvilUtil();
			CoreLog.info("Loaded propper, enjoy :)");
			return anvilUtil;
		}catch(Exception e){
		}
		CoreLog.error("Failed to load AnvilUtil, no compatible version found :(");
		return null;
	}


	public static void open(Player player, Consumer<Inventory> after){
		anvilUtil.open(player, after);
	}
	public static void open(Player player, String message, Consumer<Inventory> after){
		anvilUtil.open(player, message, after);
	}
	public static void open(Player player, Runnable after){
		anvilUtil.open(player, (r) -> after.run());
	}
	public static void open(Player player, String message, Runnable after){
		anvilUtil.open(player, message, (r) -> after.run());
	}

}

package me.dablakbandit.core;

import org.bukkit.plugin.java.JavaPlugin;

import me.dablakbandit.core.commands.AbstractCommand;
import me.dablakbandit.core.database.DatabaseManager;
import me.dablakbandit.core.metrics.Metrics;
import me.dablakbandit.core.players.CorePlayerManager;
import me.dablakbandit.core.updater.PluginUpdater;

public class CorePlugin extends JavaPlugin{
	
	private static CorePlugin main;
	
	public static CorePlugin getInstance(){
		return main;
	}
	
	public void onLoad(){
		main = this;
		CorePluginConfiguration.getInstance().load();
		CoreLanguageConfiguration.setup(this, "language.yml");
		// Loads PlayerManager class
		CorePlayerManager.getInstance();
		PluginUpdater.getInstance().checkUpdate(this, "56780", "https://github.com/Dablakbandit/Core/raw/master/output/core-latest.jar", "core-latest.jar");
		new Metrics(this, "Core_");
	}
	
	public void onEnable(){
		// Enables PlayerManager class
		CorePlayerManager.getInstance().enable();
		
		// PluginUpdater
		PluginUpdater.getInstance().start();
		
		AbstractCommand.enable();
	}
	
	public void onDisable(){
		CorePlayerManager.getInstance().disable();
		
		DatabaseManager.getInstance().close();
	}
	
}

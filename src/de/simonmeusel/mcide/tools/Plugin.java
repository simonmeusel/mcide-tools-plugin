package de.simonmeusel.mcide.tools;

import org.bukkit.plugin.java.JavaPlugin;

import de.simonmeusel.mcide.tools.commands.GetCommandsCommand;

public class Plugin extends JavaPlugin {
	
	@Override
	public void onEnable() {
		
		getCommand("getcommands").setExecutor(new GetCommandsCommand(this));
		
	}
	
}

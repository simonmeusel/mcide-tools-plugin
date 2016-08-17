package de.simonmeusel.mcide.tools;

import org.bukkit.plugin.java.JavaPlugin;

import de.simonmeusel.mcide.tools.commands.GetCinematicCommand;
import de.simonmeusel.mcide.tools.commands.GetCommandsCommand;

public class Plugin extends JavaPlugin {
	
	@Override
	public void onEnable() {
		
		getCommand("getcinematic").setExecutor(new GetCinematicCommand(this));
		getCommand("getcommands").setExecutor(new GetCommandsCommand(this));
		
	}
	
}

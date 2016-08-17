package de.simonmeusel.mcide.tools.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.simonmeusel.mcide.tools.util.Outputer;

public class GetCinematicCommand implements CommandExecutor{
	
	JavaPlugin plugin;
	
	public GetCinematicCommand(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		
		if (!(commandSender instanceof Player) || args.length < 2) {
			return false;
		}

		Player player = (Player) commandSender;
		
		// Argument 1 - <ticks>
		int ticks = 0;
		try {
			ticks = Integer.parseInt(args[0]);
			if (ticks < 1) {
				throw new NumberFormatException();
			}
		} catch (Exception e) {
			commandSender.sendMessage("Ticks must be a number bigger than 0");
			return false;
		}
		
		// Argument 2 - <targetSelector>
		String targetSelector = args[1];
		
		// Argument 3 - [offsetTicks]
		int offsetTicks = 20;
		if (args.length > 2) {
			try {
				offsetTicks = Integer.parseInt(args[2]);
				if (offsetTicks < 1) {
					throw new NumberFormatException();
				}
			} catch (Exception e) {
				commandSender.sendMessage("OffsetTicks must be a number bigger than 0");
				return false;
			}
		}

		// Argument 4 - [gist.github|chat|file]
		String output = "chat";
		if (args.length > 3) {
			output = args[3];
		}
		
		final StringBuilder frames = new StringBuilder();
		
		final String outputFinal = output;
		final int ticksFinal = ticks;
		
		int pid[] = new int[1];
		
		pid[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			
			int currentTick = 0;
			
			@Override
			public void run() {
				if (currentTick == 0) {
					commandSender.sendMessage("Starting capture of cinematic!");
				}
				if (currentTick < ticksFinal) {
					Location loc = player.getLocation();
					frames.append("@tick " + currentTick + "\n"
							+ "/tp " + targetSelector + " "
							+  loc.getX() + " "
							+ loc.getBlockY() + " "
							+ loc.getZ() + " "
							+ loc.getYaw() + " "
							+ loc.getPitch() + "\n");
					currentTick++;
				} else {
					commandSender.sendMessage("Cinematic created!");
					Bukkit.getScheduler().cancelTask(pid[0]);
					new Outputer(outputFinal, "cinematic", frames.toString(), commandSender, plugin);
				}
			}
		}, offsetTicks, 1);
		
		return true;
	}
	
}

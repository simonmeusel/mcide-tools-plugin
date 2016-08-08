package de.simonmeusel.mcide.tools.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import de.simonmeusel.mcide.tools.Plugin;

public class GetCommandsCommand implements CommandExecutor {
	
	Plugin plugin;
	
	public GetCommandsCommand(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

		String method = "chat";

		if (args.length == 1) {
			method = args[0];
		}

		if (!(commandSender instanceof Player)) {
			commandSender.sendMessage("Sender needs to be a Player");
			return false;
		}

		Player player = (Player) commandSender;

		org.bukkit.plugin.Plugin plugin;

		if (!((plugin = Bukkit.getPluginManager().getPlugin("WorldEdit")) instanceof WorldEditPlugin)) {
			commandSender.sendMessage("WorldEdit needs to be installed");
			return false;
		}

		WorldEditPlugin worldEdit = (WorldEditPlugin) plugin;

		Selection selection = worldEdit.getSelection(player);

		if (selection == null) {
			commandSender.sendMessage("You need to have a Selection");
			return false;
		}

		World world = selection.getWorld();

		int lx, hx, ly, hy, lz, hz;

		if (selection.getMinimumPoint().getBlockX() > selection.getMaximumPoint().getBlockX()) {
			lx = selection.getMaximumPoint().getBlockX();
			hx = selection.getMinimumPoint().getBlockX();
		} else {
			hx = selection.getMaximumPoint().getBlockX();
			lx = selection.getMinimumPoint().getBlockX();
		}

		if (selection.getMinimumPoint().getBlockY() > selection.getMaximumPoint().getBlockY()) {
			ly = selection.getMaximumPoint().getBlockY();
			hy = selection.getMinimumPoint().getBlockY();
		} else {
			hy = selection.getMaximumPoint().getBlockY();
			ly = selection.getMinimumPoint().getBlockY();
		}

		if (selection.getMinimumPoint().getBlockZ() > selection.getMaximumPoint().getBlockZ()) {
			lz = selection.getMaximumPoint().getBlockZ();
			hz = selection.getMinimumPoint().getBlockZ();
		} else {
			hz = selection.getMaximumPoint().getBlockZ();
			lz = selection.getMinimumPoint().getBlockZ();
		}

		ArrayList<String> commands = new ArrayList<>();

		for (int z = lz; z <= hz; z++) {
			for (int x = lx; x <= hx; x++) {
				for (int y = ly; y <= hy; y++) {
					Block block = world.getBlockAt(x, y, z);
					if (block.getType().equals(Material.COMMAND) || block.getType().equals(Material.COMMAND_CHAIN)
							|| block.getType().equals(Material.COMMAND_REPEATING)) {
						CommandBlock commandBlock = (CommandBlock) block.getState();
						commands.add(commandBlock.getCommand());
					}
				}
			}
		}

		switch (method) {
		case "gist.github":
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					try {
						commandSender.sendMessage("Connecting to GitHub, please wait!");
						
						String commandString = "";
						
						for (String string : commands) {
							commandString += string + "\n";
						}
						
						JSONObject data = new JSONObject();
						
						data.put("description", "McIDE generated commands");
						data.put("public", false);
						
						JSONObject commandsMcPhP = new JSONObject();
						commandsMcPhP.put("content", commandString);
						
						JSONObject files = new JSONObject();
						files.put("commands.mc.php", commandsMcPhP);
						
						data.put("files", files);
						
						HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.github.com/gists").openConnection();
						
						connection.setRequestProperty("Content-Type", "application/json");
						connection.setDoOutput(true);
						
						connection.getOutputStream().write(data.toJSONString().getBytes());
						connection.getOutputStream().flush();
						
						int responseCode = -1;
						
						try {
							responseCode = connection.getResponseCode();
						} catch (Exception e) {
							e.printStackTrace();
						}
						commandSender.sendMessage("GitHub responded with " + responseCode);
						
						String response = "";
						String lastLine;
						
						BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						while ((lastLine = br.readLine()) != null) {
							response += lastLine;
						}
						
						JSONObject object = (JSONObject) new JSONParser().parse(response);
						commandSender.sendMessage("§6" + object.get("html_url"));
						
						
					} catch (Exception e) {
						commandSender.sendMessage("Failed!");
						e.printStackTrace();
					}
				}
			});

			break;

		case "book":
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			BookMeta bookMeta = (BookMeta) book.getItemMeta();
			bookMeta.setAuthor("Simon Meusel");
			bookMeta.setTitle("McIDE Command generation");

			ArrayList<String> pages = new ArrayList<>();

			int letters = 0;
			int newLines = 0;

			String currentPage = "";

			for (String string : commands) {
				if (string.length() > 254) {
					commandSender.sendMessage("Command is to long for book: " + command);
					continue;
				}

				if (newLines > 9 || letters + newLines + string.length() > 254) {
					pages.add(currentPage);
					letters = 0;
					newLines = 0;
				} else {
					letters += string.length();
					newLines++;
					currentPage += string + "\n\n";
				}

			}

			bookMeta.setPages(pages.toArray(new String[0]));
			book.setItemMeta(bookMeta);
			player.getInventory().addItem(book);

			break;

		default:
			for (String string : commands) {
				commandSender.sendMessage(string);
			}

			break;
		}

		return true;

	}

}

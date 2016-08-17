package de.simonmeusel.mcide.tools.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Outputer {
	
	public Outputer(String method, String header, String content, CommandSender commandSender, JavaPlugin plugin) {
		
		switch (method) {
		case "gist.github":
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					try {
						commandSender.sendMessage("Connecting to GitHub, please wait!");
						
						JSONObject data = new JSONObject();
						
						data.put("description", "McIDE " + header);
						data.put("public", false);
						
						JSONObject commandsMcPhP = new JSONObject();
						commandsMcPhP.put("content", content);
						
						JSONObject files = new JSONObject();
						files.put("mc.php", commandsMcPhP);
						
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

		case "chat":
			
			commandSender.sendMessage(content);
			
			break;

		case "file":
			
			if (commandSender.hasPermission("mcide.tools.file")) {
				File directory = new File(plugin.getDataFolder(), header);
				
				File file = new File(directory, System.currentTimeMillis() + ".php");
				try {
					directory.mkdirs();
					file.createNewFile();
					OutputStream out = new FileOutputStream(file);
					out.write(content.getBytes());
					out.close();
				} catch (Exception e) {
					commandSender.sendMessage("Failed writing the file!");
					e.printStackTrace();
				}
			} else {
				commandSender.sendMessage("Yout don't have the Permission to write to files!");
			}
			
			break;
			
		default:
			break;
		}
		
	}
	
}

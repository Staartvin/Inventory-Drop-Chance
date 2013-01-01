package Staartvin.InventoryDropChance;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryDropChance extends JavaPlugin {

	IDCEvents events = new IDCEvents(this);
	Files files = new Files(this);
	boolean verboseLogging;
	protected FileConfiguration inventoriesConfig;
	protected File inventoriesConfigFile;
	String[] array = {"ExampleGroup"};
	List<String> groups = new ArrayList<String>();
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(events, this);
		loadConfiguration();
		checkGroups();
		System.out.println("[" + getDescription().getName()
				+ "] has been enabled!");
	}

	public void onDisable() {
		reloadConfig();
		saveConfig();
		System.out.println("[" + getDescription().getName()
				+ "] has been disabled!");
	}

	protected void loadConfiguration() {
		getConfig().options().header(
				"Inventory Drop Chance v" + getDescription().getVersion()
						+ " Config"
						+ "\nMake sure that a group listed in 'Group List' is also defined as a group in 'Groups'!");

		getConfig().addDefault("verboseLogging", true);
		getConfig().addDefault("Use XP Loss Percentage", false);
		getConfig().addDefault("Group List", Arrays.asList(array));
		
		getConfig().addDefault("Groups.ExampleGroup.retain percentage", 50);
		getConfig().addDefault("Groups.ExampleGroup.xp loss", 25);
		getConfig().addDefault("Groups.ExampleGroup.use xp loss", false);
		
		getConfig().options().copyDefaults(true);
		saveConfig();

		verboseLogging = getConfig().getBoolean("verboseLogging");

		if (verboseLogging) {
			System.out.print("[Inventory Drop Chance] "
					+ getConfig().getStringList("Group List").size() + " groups found!");
		}
	}
	protected boolean checkGroups() {
		groups = getConfig().getStringList("Group List");
	
		if (verboseLogging) {
			if (groups == null) {
				System.out.print("[Inventory Drop Chance] Group list is not found!");
				System.out.print("[Inventory Drop Chance] Disabling IDC because group list is not found!");
				onDisable();
				return false;
			}
			for (String group:groups) {
				System.out.print("[Inventory Drop Chance] Group: " + group);
			}
		}
		return true;
	}
}

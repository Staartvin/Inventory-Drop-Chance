package Staartvin.InventoryDropChance;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryDropChance extends JavaPlugin {

	IDCEvents events = new IDCEvents(this);
	Files files = new Files(this);
	boolean XPLoss;
	int XPLossPercentage;
	int retainPercentage;
	boolean verboseLogging;
	protected FileConfiguration inventoriesConfig;
	protected File inventoriesConfigFile;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(events, this);
		loadConfiguration();
		System.out.println("[" + getDescription().getName()
				+ "] has been enabled!");
	}

	public void onDisable() {
		reloadConfig();
		saveConfig();
		System.out.println("[" + getDescription().getName()
				+ "] has been disabled!");
	}

	public void loadConfiguration() {
		getConfig().options().header(
				"Inventory Drop Chance v" + getDescription().getVersion()
						+ " Config");

		getConfig().addDefault("verboseLogging", true);
		getConfig().addDefault("Retain percentage", 50);
		getConfig().addDefault("Use XP Loss Percentage", false);
		getConfig().addDefault("XP Loss percentage", 50);
		getConfig().options().copyDefaults(true);
		saveConfig();

		XPLoss = getConfig().getBoolean("Use XP Loss Percentage");
		XPLossPercentage = getConfig().getInt("XP Loss percentage");
		retainPercentage = getConfig().getInt("Retain percentage");
		verboseLogging = getConfig().getBoolean("verboseLogging");

		if (verboseLogging) {
			System.out.print("[Inventory Drop Chance] XP loss percentage is "
					+ XPLossPercentage);
			System.out.print("[Inventory Drop Chance] Retain percentage is "
					+ retainPercentage);
		}
 // Small test
	}
}

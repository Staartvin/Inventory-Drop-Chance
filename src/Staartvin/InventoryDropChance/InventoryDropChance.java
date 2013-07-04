package Staartvin.InventoryDropChance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import Staartvin.InventoryDropChance.updater.Updater;

public class InventoryDropChance extends JavaPlugin {

	IDCEvents events = new IDCEvents(this);
	Files files = new Files(this);
	boolean verboseLogging;
	protected FileConfiguration languageConfig;
	protected File languageConfigFile;
	List<String> groups = new ArrayList<String>();
	WorldGuardClass wgClass = new WorldGuardClass(this);
	WorldHandlers wHandlers = new WorldHandlers(this);
	public Methods methods = new Methods(this);
	public Updater updater;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(events, this);
		files.loadConfiguration();
		checkGroups();

		if (!wgClass.checkWorldGuard()) {
			System.out
					.print("[Inventory Drop Chance] WorldGuard has not been found. Custom flags cannot be used!");
		}
		if (!wgClass.checkWGCustomFlags()) {
			System.out
					.print("[Inventory Drop Chance] WGCustomFlags has not been found. Custom flags cannot be used!");
		}
		if (wgClass.checkWorldGuard() && wgClass.checkWGCustomFlags()) {
			wgClass.initialiseWGHandler(this);
			System.out
					.print("[Inventory Drop Chance] Hooked into WorldGuard and WGCustomFlags!");
			System.out
					.print("[Inventory Drop Chance] WorldGuard custom flags can be used!");
			wgClass.wgHandler.getWGCustomFlags();
			wgClass.wgHandler.getWorldGuard();
			wgClass.wgHandler.registerFlags();
		}

		// Initialize worlds
		wHandlers.getWorlds();

		System.out.print("[Inventory Drop Chance] Configuring worlds!");

		// Initialize enabled worlds
		System.out.print("[Inventory Drop Chance] Checking "
				+ wHandlers.getEnabledWorlds().size() + " worlds!");

		// Check if the config is set up correctly
		if (!files.hasAllOptions()) {
			getLogger().severe("The config is not setup correctly!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		// Check for a newer version
		if (getConfig().getBoolean("Updater.doCheckUpdate")) {
			updater = new Updater(this, "inventory-drop-chance", this.getFile(),
					Updater.UpdateType.NO_DOWNLOAD, false);
		}

		System.out.println("[" + getDescription().getName()
				+ "] has been enabled!");
	}

	public void onDisable() {
		files.reloadLanguageConfig();
		files.saveLanguageConfig();
		reloadConfig();
		saveConfig();
		System.out.println("[" + getDescription().getName()
				+ "] has been disabled!");
	}

	protected boolean checkGroups() {
		groups.addAll(getConfig().getConfigurationSection("Groups").getKeys(
				false));

		if (verboseLogging) {
			if (groups == null) {
				System.out
						.print("[Inventory Drop Chance] Group list is not found!");
				System.out
						.print("[Inventory Drop Chance] Disabling IDC because group list is not found!");
				onDisable();
				return false;
			}
			for (String group : groups) {
				System.out.print("[Inventory Drop Chance] Group: " + group);
			}
		}
		return true;
	}

	protected List<String> getWhitelistedItems(String group) {
		return getConfig().getStringList("Groups." + group + ".whitelist");
	}

	protected List<String> getBlacklistedItems(String group) {
		return getConfig().getStringList("Groups." + group + ".blacklist");
	}
	
	public void getUpdaterStatus() {
		if (!getConfig().getBoolean("Updater.doCheckUpdate")) return;
		updater = new Updater(this, "inventory-drop-chance", this.getFile(),
				Updater.UpdateType.NO_DOWNLOAD, false);
	}
}

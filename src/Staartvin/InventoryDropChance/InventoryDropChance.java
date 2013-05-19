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
	protected FileConfiguration languageConfig;
	protected File languageConfigFile;
	String[] array = { "ExampleGroup" };
	String[] worldArray = { "DisabledWorld", "DisabledWorld_nether",
			"DisabledWorld_the_end" };
	List<String> groups = new ArrayList<String>();
	WorldGuardClass wgClass = new WorldGuardClass(this);
	WorldHandlers wHandlers = new WorldHandlers(this);

	public void onEnable() {
		getServer().getPluginManager().registerEvents(events, this);
		loadConfiguration();
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

	protected void loadConfiguration() {
		getConfig()
				.options()
				.header("Inventory Drop Chance v"
						+ getDescription().getVersion()
						+ " Config"
						+ "\nMake sure that a group listed in 'Group List' is also defined as a group in 'Groups'!"
						+ "\nAn item on the whitelist will always be kept."
						+ "\nAn item on the blacklist will always be dropped.");

		getConfig().addDefault("verboseLogging", true);
		getConfig().addDefault("Group List", Arrays.asList(array));
		getConfig().addDefault("DisabledWorlds", Arrays.asList(worldArray));
		getConfig().addDefault("Groups.ExampleGroup.retain percentage", 50);
		getConfig().addDefault("Groups.ExampleGroup.xp loss", 25);
		getConfig().addDefault("Groups.ExampleGroup.use xp loss", false);

		if (getConfig().getStringList("Groups.ExampleGroup.blacklist").isEmpty()) {
			getConfig().set("Groups.ExampleGroup.blacklist",
					Arrays.asList(new String[] { "35:7", "273" }));
		}
		
		if (getConfig().getStringList("Groups.ExampleGroup.whitelist").isEmpty()) {
			getConfig().set("Groups.ExampleGroup.whitelist",
					Arrays.asList(new String[] { "276", "25" }));
		}

		files.getLanguageConfig().addDefault("ITEMS_MESSAGE_ON_RESPAWN",
				"{0} items have survived your death!");
		files.getLanguageConfig().addDefault("PERCENTAGE_MESSAGE_ON_RESPAWN",
				"That is {0} of your old inventory.");

		files.loadConfigVariables();

		getConfig().options().copyDefaults(true);
		saveConfig();
		files.getLanguageConfig().options().copyDefaults(true);
		files.saveLanguageConfig();

		verboseLogging = getConfig().getBoolean("verboseLogging");

		if (verboseLogging) {
			System.out.print("[Inventory Drop Chance] "
					+ getConfig().getStringList("Group List").size()
					+ " groups found!");
		}
	}

	protected boolean checkGroups() {
		groups = getConfig().getStringList("Group List");

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
}

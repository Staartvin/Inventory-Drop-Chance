package staartvin.inventorydropchance.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import staartvin.inventorydropchance.InventoryDropChance;

public class Files {

	InventoryDropChance plugin;

	public Files(InventoryDropChance plugin) {
		this.plugin = plugin;
	}

	public String PERCENTAGE_MESSAGE_ON_RESPAWN = "";
	public String INVERTED_PERCENTAGE_MESSAGE_ON_RESPAWN = "";
	public String ITEMS_MESSAGE_ON_RESPAWN = "";
	public String ALL_ITEMS_SURVIVED = "";
	public String PER_STACK_CHECK_MESSAGE_ON_RESPAWN = "";
	public String INVERTED_PER_STACK_CHECK_MESSAGE_ON_RESPAWN = "";

	private FileConfiguration config;
	private FileConfiguration languageConfig;
	private File languageConfigFile;

	// Player Config Methods
	public void reloadLanguageConfig() {
		if (languageConfigFile == null) {
			languageConfigFile = new File(plugin.getDataFolder(),
					"language.yml");
		}
		languageConfig = YamlConfiguration
				.loadConfiguration(languageConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = plugin.getResource("language.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			languageConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getLanguageConfig() {
		if (languageConfig == null) {
			this.reloadLanguageConfig();
		}
		return languageConfig;
	}

	public void saveLanguageConfig() {
		if (languageConfig == null || languageConfigFile == null) {
			return;
		}
		try {
			getLanguageConfig().save(languageConfigFile);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE,
					"Could not save config to " + languageConfigFile, ex);
		}
	}

	public void loadConfigVariables() {
		PERCENTAGE_MESSAGE_ON_RESPAWN = languageConfig
				.getString("PERCENTAGE_MESSAGE_ON_RESPAWN");
		INVERTED_PERCENTAGE_MESSAGE_ON_RESPAWN = languageConfig
				.getString("INVERTED_PERCENTAGE_MESSAGE_ON_RESPAWN");
		ITEMS_MESSAGE_ON_RESPAWN = languageConfig
				.getString("ITEMS_MESSAGE_ON_RESPAWN");
		ALL_ITEMS_SURVIVED = languageConfig.getString("ALL_ITEMS_SURVIVED");
		PER_STACK_CHECK_MESSAGE_ON_RESPAWN = languageConfig
				.getString("PER_STACK_CHECK_MESSAGE_ON_RESPAWN");
		INVERTED_PER_STACK_CHECK_MESSAGE_ON_RESPAWN = languageConfig
				.getString("INVERTED_PER_STACK_CHECK_MESSAGE_ON_RESPAWN");
	}

	public void loadConfiguration() {
		config = plugin.getConfig();

		config.options()
				.header("Inventory Drop Chance v"
						+ plugin.getDescription().getVersion()
						+ " Config"
						+ "\nAn item on the whitelist will always be kept."
						+ "\nAn item on the blacklist will always be dropped."
						+ "\nRetain percentage is the percentage of the inv that will be kept. If this is 0, nothing will be kept and everything is dropped"
						+ "\nDelete percentage is the percentage of the inv that will be deleted. If this is 0, nothing will be deleted"
						+ "\nXp loss is the percentage of the xp that will be lost. The rest will be given back to you when you respawn"
						+ "\nCheck first is the first thing to check:"
						+ "\n    'save' = First the save check will be run, then the delete check. This way the delete check will only check from the saved items"
						+ "\n    'delete' = First the delete check will be run, then the save check. This way the save check will only check the not-deleted items"
						+ "\nWhen 'per-stack-check' is true, IDC will check every stack and will keep a % of it and delete a % of that. (Follows check-first rules)"
						+ "\n	When you have a stack of 64 diamonds and your retain and delete percentage is 50%, IDC will calculate: "
						+ "\n		stack amount * (retain percentage / 100) = kept amount. (Amount a player keeps)"
						+ "\n		kept amount * (delete percentage / 100) = delete amount. (Amount of the kept amount that will be deleted)"
						+ "\n		stack amount - kept amount = drop amount. (Amount that is dropped on the ground)"
						+ "\n	In the example: "
						+ "\n		kept amount = 64 * (50 / 100) = 32"
						+ "\n		delete amount = 32 * (50 / 100) = 16"
						+ "\n		drop amount = 64 - 32 = 32"
						+ "\n	A player will keep 16 diamonds after his death. 32 will be dropped at his death location."
						+ "\nYou don't need to touch the default values. These are used when a player is not in a group.");

		config.addDefault("verboseLogging", true);
		config.addDefault(
				"DisabledWorlds",
				Arrays.asList(new String[] { "DisabledWorld",
						"DisabledWorld_nether", "DisabledWorld_the_end" }));
		config.addDefault("Groups.ExampleGroup.retain percentage", 50);
		config.addDefault("Groups.ExampleGroup.delete percentage", 0);
		//config.addDefault("Groups.ExampleGroup.xp loss", 25);
		//config.addDefault("Groups.ExampleGroup.use xp loss", false);
		config.addDefault("Groups.ExampleGroup.check first", "save");
		config.addDefault("Groups.ExampleGroup.per-stack-check", false);
		
		// Default values
		config.addDefault("Default values.retain percentage", 50);
		config.addDefault("Default values.delete percentage", 50);
		config.addDefault("Default values.xp loss", 50);

		config.addDefault("Updater.doCheckUpdate", true);

		if (config.getStringList("Groups.ExampleGroup.blacklist").isEmpty()) {
			config.set("Groups.ExampleGroup.blacklist",
					Arrays.asList(new String[] { "35:7", "273" }));
		}

		if (config.getStringList("Groups.ExampleGroup.whitelist").isEmpty()) {
			config.set("Groups.ExampleGroup.whitelist",
					Arrays.asList(new String[] { "276", "25" }));
		}

		getLanguageConfig().addDefault("ITEMS_MESSAGE_ON_RESPAWN",
				"{0} items have survived your death!");
		getLanguageConfig()
				.addDefault("PERCENTAGE_MESSAGE_ON_RESPAWN",
						"{0} of your old inventory has been saved and {1} of that has been deleted.");
		getLanguageConfig()
				.addDefault("INVERTED_PERCENTAGE_MESSAGE_ON_RESPAWN",
						"{1} of your old inventory has been deleted and {0} of that has been saved.");
		getLanguageConfig().addDefault("ALL_ITEMS_SURVIVED",
				"All your items survived your death!");
		getLanguageConfig().addDefault("PER_STACK_CHECK_MESSAGE_ON_RESPAWN",
				"{0} of each stack of your old inventory has been saved and {1} of that has been deleted.");
		getLanguageConfig().addDefault("INVERTED_PER_STACK_CHECK_MESSAGE_ON_RESPAWN",
				"{1} of each stack of your old inventory has been deleted and {0 of that has been saved.");

		loadConfigVariables();

		config.options().copyDefaults(true);
		plugin.saveConfig();
		getLanguageConfig().options().copyDefaults(true);
		saveLanguageConfig();
	}

	public int getRetainPercentage(Player player) {

		if (plugin.getWorldGuardClass().isWorldGuardReady()) {
			return plugin.getWorldGuardClass().wgHandler
					.getRetainPercentage(player);
		}
		
		else {
			String group = plugin.getFiles().getGroup(player);
			
			if (group == null) return config.getInt("Default values.retain percentage", 50);
			else return plugin.getConfig().getInt(
					"Groups." + group + ".retain percentage");
		}
	}

	public int getDeletePercentage(Player player) {

		if (plugin.getWorldGuardClass().isWorldGuardReady()) {
			return plugin.getWorldGuardClass().wgHandler
					.getDeletePercentage(player);
		}
		
		else {
			String group = plugin.getFiles().getGroup(player);
			
			if (group == null) return config.getInt("Default values.delete percentage", 50);
			else return plugin.getConfig().getInt(
					"Groups." + group + ".delete percentage");
		}
	}

	public String getGroup(Player player) {
		for (String groupName : plugin.getConfig()
				.getConfigurationSection("Groups").getKeys(false)) {
			if (player.hasPermission("idc.group." + groupName)) {
				return groupName;
			}
		}
		return null;
	}

	public boolean logVerbose() {
		return config.getBoolean("verboseLogging");
	}

	public boolean hasAllOptions() {
		Set<String> groupSet = config.getConfigurationSection("Groups")
				.getKeys(false);
		boolean allIsRight = true;

		// Check every single option
		for (String group : groupSet) {
			if (config.getInt("Groups." + group + ".retain percentage", -1) == -1) {
				plugin.getLogger().warning(
						"Retain Percentage for group '" + group
								+ "' is not found!");
				allIsRight = false;
			}

			if (config.getInt("Groups." + group + ".delete percentage", -1) == -1) {
				plugin.getLogger().warning(
						"Delete Percentage for group '" + group
								+ "' is not found!");
				allIsRight = false;
			}

			if (config.getString("Groups." + group + ".check first", null) == null) {
				plugin.getLogger().warning(
						"Check first for group '" + group + "' is not found!");
				allIsRight = false;
			}

			if (config.getList("Groups." + group + ".blacklist") == null) {
				plugin.getLogger().warning(
						"Blacklist for group '" + group + "' is not found!");
				allIsRight = false;
			}

			if (config.getList("Groups." + group + ".whitelist") == null) {
				plugin.getLogger().warning(
						"Whitelist for group '" + group + "' is not found!");
				allIsRight = false;
			}
		}

		return allIsRight;
	}

	public boolean doCheckUpdate() {
		return config.getBoolean("Updater.doCheckUpdate");
	}

	public boolean doPerStackCheck(Player player) {
		String group = getGroup(player);

		if (group == null)
			return false;

		return config.getBoolean("Groups." + group + ".per-stack-check");
	}

	public String getCheckFirst(Player player) {

		String group = getGroup(player);

		if (group == null)
			return "save";

		return config.getString("Groups." + group + ".check first");
	}
	
	
}

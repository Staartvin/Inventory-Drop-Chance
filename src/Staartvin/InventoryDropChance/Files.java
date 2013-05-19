package Staartvin.InventoryDropChance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Files {

	InventoryDropChance plugin;
	
	public Files(InventoryDropChance plugin) {
		this.plugin = plugin;
	}
	
	public String PERCENTAGE_MESSAGE_ON_RESPAWN = "";
	public String ITEMS_MESSAGE_ON_RESPAWN = "";
	
	// Player Config Methods
			protected void reloadLanguageConfig() {
				if (plugin.languageConfigFile == null) {
					plugin.languageConfigFile = new File(plugin.getDataFolder(),
							"language.yml");
				}
				plugin.languageConfig = YamlConfiguration
						.loadConfiguration(plugin.languageConfigFile);

				// Look for defaults in the jar
				InputStream defConfigStream = plugin.getResource("language.yml");
				if (defConfigStream != null) {
					YamlConfiguration defConfig = YamlConfiguration
							.loadConfiguration(defConfigStream);
					plugin.languageConfig.setDefaults(defConfig);
				}
			}

			protected FileConfiguration getLanguageConfig() {
				if (plugin.languageConfig == null) {
					this.reloadLanguageConfig();
				}
				return plugin.languageConfig;
			}

			protected void saveLanguageConfig() {
				if (plugin.languageConfig == null || plugin.languageConfigFile == null) {
					return;
				}
				try {
					getLanguageConfig().save(plugin.languageConfigFile);
				} catch (IOException ex) {
					plugin.getLogger().log(Level.SEVERE,
							"Could not save config to " + plugin.languageConfigFile, ex);
				}
			}
			
			protected void loadConfigVariables() {
				PERCENTAGE_MESSAGE_ON_RESPAWN = plugin.languageConfig.getString("PERCENTAGE_MESSAGE_ON_RESPAWN");
				ITEMS_MESSAGE_ON_RESPAWN = plugin.languageConfig.getString("ITEMS_MESSAGE_ON_RESPAWN");
			}
}

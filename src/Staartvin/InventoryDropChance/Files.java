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
	
	// Player Config Methods
			protected void reloadInventoriesConfig() {
				if (plugin.inventoriesConfigFile == null) {
					plugin.inventoriesConfigFile = new File(plugin.getDataFolder(),
							"inventories.yml");
				}
				plugin.inventoriesConfig = YamlConfiguration
						.loadConfiguration(plugin.inventoriesConfigFile);

				// Look for defaults in the jar
				InputStream defConfigStream = plugin.getResource("inventories.yml");
				if (defConfigStream != null) {
					YamlConfiguration defConfig = YamlConfiguration
							.loadConfiguration(defConfigStream);
					plugin.inventoriesConfig.setDefaults(defConfig);
				}
			}

			protected FileConfiguration getInventoriesConfig() {
				if (plugin.inventoriesConfig == null) {
					this.reloadInventoriesConfig();
				}
				return plugin.inventoriesConfig;
			}

			protected void saveInventoriesConfig() {
				if (plugin.inventoriesConfig == null || plugin.inventoriesConfigFile == null) {
					return;
				}
				try {
					getInventoriesConfig().save(plugin.inventoriesConfigFile);
				} catch (IOException ex) {
					plugin.getLogger().log(Level.SEVERE,
							"Could not save config to " + plugin.inventoriesConfigFile, ex);
				}
			}
}

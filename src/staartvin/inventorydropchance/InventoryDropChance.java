package staartvin.inventorydropchance;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import staartvin.inventorydropchance.files.Files;
import staartvin.inventorydropchance.listed.ListHandler;
import staartvin.inventorydropchance.listeners.IDCListeners;
import staartvin.inventorydropchance.updater.Updater;
import staartvin.inventorydropchance.worldhandler.WorldGuardClass;
import staartvin.inventorydropchance.worldhandler.WorldHandlers;


public class InventoryDropChance extends JavaPlugin {

	private IDCListeners events = new IDCListeners(this);
	private Files files = new Files(this);
	private WorldGuardClass wgClass = new WorldGuardClass(this);
	private WorldHandlers wHandlers = new WorldHandlers(this);
	private Methods methods = new Methods(this);
	public Updater updater;
	private ListHandler listHandler = new ListHandler(this);

	public void onEnable() {
		getServer().getPluginManager().registerEvents(getEvents(), this);
		files.loadConfiguration();

		if (!wgClass.checkWorldGuard()) {
			this.getLogger().info("WorldGuard has not been found. Custom flags cannot be used!");
		}
		if (!wgClass.checkWGCustomFlags()) {
			this.getLogger().info("WGCustomFlags has not been found. Custom flags cannot be used!");
		}
		
		if (wgClass.isWorldGuardReady()) {
			
			// Get WorldGuardHandler
			wgClass.initialiseWGHandler(this);
			
			this.getLogger().info("Hooked into WorldGuard and WGCustomFlags!");
			this.getLogger().info("WorldGuard custom flags can be used!");
			wgClass.wgHandler.getWGCustomFlags();
			wgClass.wgHandler.getWorldGuard();
			wgClass.wgHandler.registerFlags();
		}

		// Initialize worlds
		wHandlers.getWorlds();

		this.getLogger().info("Configuring worlds!");

		// Initialize enabled worlds
		this.getLogger().info("Checking "
				+ wHandlers.getEnabledWorlds().size() + " worlds!");

		// Check if the config is set up correctly
		if (!files.hasAllOptions()) {
			getLogger().severe("The config is not setup correctly!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		// Check for a newer version IF we are allowed to
		if (getConfig().getBoolean("Updater.doCheckUpdate")) {
			updater = new Updater(this, "inventory-drop-chance", this.getFile(),
					Updater.UpdateType.NO_DOWNLOAD, false);
		}
		
		this.getLogger().info(getDescription().getName()
				+ "] has been enabled!");
	}

	public void onDisable() {
		files.reloadLanguageConfig();
		files.saveLanguageConfig();
		reloadConfig();
		saveConfig();
		this.getLogger().info(getDescription().getName()
				+ "] has been disabled!");
	}

	public List<String> getWhitelistedItems(String group) {
		return getConfig().getStringList("Groups." + group + ".whitelist");
	}

	public List<String> getBlacklistedItems(String group) {
		return getConfig().getStringList("Groups." + group + ".blacklist");
	}
	
	public void getUpdaterStatus() {
		if (!getConfig().getBoolean("Updater.doCheckUpdate")) return;
		updater = new Updater(this, "inventory-drop-chance", this.getFile(),
				Updater.UpdateType.NO_DOWNLOAD, false);
	}

	public Methods getMethods() {
		return methods;
	}
	
	public WorldHandlers getWorldHandlers() {
		return wHandlers;
	}
	
	public WorldGuardClass getWorldGuardClass() {
		return wgClass;
	}
	
	public Files getFiles() {
		return files;
	}

	public IDCListeners getEvents() {
		return events;
	}
	
	public ListHandler getListHandler() {
		return listHandler;
	}
}

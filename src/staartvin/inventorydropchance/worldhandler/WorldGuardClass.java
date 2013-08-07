package staartvin.inventorydropchance.worldhandler;

import staartvin.inventorydropchance.InventoryDropChance;

public class WorldGuardClass {

	private InventoryDropChance plugin;
	public WorldGuardHandler wgHandler;
	
	public WorldGuardClass(InventoryDropChance instance) {
		plugin = instance;
		
	}
	
	public boolean checkWorldGuard() {
		if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
			return true;
		}
		return false;
	}
	
	public boolean checkWGCustomFlags() {
		if (plugin.getServer().getPluginManager().getPlugin("WGCustomFlags") != null) {
			return true;
		}
		return false;
	}
	
	public void initialiseWGHandler(InventoryDropChance instance) {
		wgHandler = new WorldGuardHandler(instance);
	}
	
	public boolean isWorldGuardReady() {
		if (checkWorldGuard() && checkWGCustomFlags()) {
			return true;
		}
		return false;
	}
}

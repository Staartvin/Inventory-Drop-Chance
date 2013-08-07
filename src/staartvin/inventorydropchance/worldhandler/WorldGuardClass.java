package staartvin.inventorydropchance.worldhandler;

import staartvin.inventorydropchance.InventoryDropChance;

public class WorldGuardClass {

	private InventoryDropChance plugin;
	public WorldGuardHandler wgHandler;
	
	public WorldGuardClass(InventoryDropChance instance) {
		plugin = instance;
	}
	
	public boolean checkWorldGuard() {
		return plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null;
	}
	
	public boolean checkWGCustomFlags() {
		return plugin.getServer().getPluginManager().getPlugin("WGCustomFlags") != null;
	}
	
	public void initialiseWGHandler(InventoryDropChance instance) {
		wgHandler = new WorldGuardHandler(instance);
	}
	
	public boolean isWorldGuardReady() {
		return checkWorldGuard() && checkWGCustomFlags(); 
	}
}

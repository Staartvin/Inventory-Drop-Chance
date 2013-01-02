package Staartvin.InventoryDropChance;

public class WorldGuardClass {

	private InventoryDropChance plugin;
	protected WorldGuardHandler wgHandler;
	
	public WorldGuardClass(InventoryDropChance instance) {
		plugin = instance;
		
	}
	
	protected boolean checkWorldGuard() {
		if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
			return true;
		}
		return false;
	}
	
	protected boolean checkWGCustomFlags() {
		if (plugin.getServer().getPluginManager().getPlugin("WGCustomFlags") != null) {
			return true;
		}
		return false;
	}
	
	protected void initialiseWGHandler(InventoryDropChance instance) {
		wgHandler = new WorldGuardHandler(instance);
	}
	
	protected boolean isWorldGuardReady() {
		if (checkWorldGuard() && checkWGCustomFlags()) {
			return true;
		}
		return false;
	}
}

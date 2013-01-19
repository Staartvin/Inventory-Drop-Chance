package Staartvin.InventoryDropChance;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

public class WorldHandlers {

	private InventoryDropChance plugin;
	private List<World> worlds = new ArrayList<World>();
	private List<World> disabledWorlds = new ArrayList<World>();
	private List<World> enabledWorlds = new ArrayList<World>();
	
	public WorldHandlers(InventoryDropChance instance) {
		plugin = instance;	
	}
	
	public List<World> getWorlds() {
		worlds = plugin.getServer().getWorlds();
		return worlds;
	}
	
	public boolean worldIsEnabled(String world) {
		if (plugin.getConfig().getList("DisabledWorlds").contains(world)) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public List<World> getEnabledWorlds() {
		worlds = plugin.getServer().getWorlds();
		disabledWorlds = (List<World>) plugin.getConfig().getList("DisabledWorlds");
		worlds.removeAll(disabledWorlds);
		enabledWorlds = worlds;
		return enabledWorlds;
	}
}

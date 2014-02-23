package staartvin.inventorydropchance.worldhandler;

import org.bukkit.entity.Player;

import staartvin.inventorydropchance.InventoryDropChance;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class WorldGuardHandler {

	private InventoryDropChance plugin;

	WorldGuardPlugin wgPlugin;
	WGCustomFlagsPlugin customWGFlags;
	public static IntegerFlag RETAIN_PERCENTAGE;
	public static IntegerFlag DELETE_PERCENTAGE;

	public WorldGuardHandler(InventoryDropChance instance) {
		plugin = instance;
	}

	public void registerFlags() {
		if (!plugin.getWorldGuardClass().isWorldGuardReady())
			return;

		RETAIN_PERCENTAGE = new IntegerFlag("retain-percentage");
		DELETE_PERCENTAGE = new IntegerFlag("lose-percentage");

		customWGFlags.addCustomFlag(RETAIN_PERCENTAGE);
		customWGFlags.addCustomFlag(DELETE_PERCENTAGE);
	}

	public int getRetainPercentage(Player player) {

		if (plugin.getWorldGuardClass().isWorldGuardReady()) {
			RegionManager regionManager = wgPlugin.getRegionManager(player
					.getWorld());
			if (regionManager != null) {
				ApplicableRegionSet set = regionManager
						.getApplicableRegions(player.getLocation());
				if (set != null) {
					if (set.getFlag(RETAIN_PERCENTAGE) != null) {
						return set.getFlag(RETAIN_PERCENTAGE);
					}
				}
			}
		}
		String group = plugin.getFiles().getGroup(player);

		if (group == null)
			return plugin.getConfig().getInt("Default values.retain percentage", 50);
		else
			return plugin.getConfig().getInt(
					"Groups." + group + ".retain percentage");
	}

	public int getDeletePercentage(Player player) {

		if (plugin.getWorldGuardClass().isWorldGuardReady()) {
			RegionManager regionManager = wgPlugin.getRegionManager(player
					.getWorld());
			if (regionManager != null) {
				ApplicableRegionSet set = regionManager
						.getApplicableRegions(player.getLocation());
				if (set != null) {
					if (set.getFlag(DELETE_PERCENTAGE) != null) {
						return set.getFlag(DELETE_PERCENTAGE);
					}
				}
			}
		}
		String group = plugin.getFiles().getGroup(player);

		if (group == null)
			return plugin.getConfig().getInt("Default values.delete percentage", 50);
		else
			return plugin.getConfig().getInt(
					"Groups." + group + ".delete percentage");
	}

	public WorldGuardPlugin getWorldGuard() {
		wgPlugin = (WorldGuardPlugin) plugin.getServer().getPluginManager()
				.getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (wgPlugin == null || !(wgPlugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) wgPlugin;
	}

	public WGCustomFlagsPlugin getWGCustomFlags() {
		customWGFlags = (WGCustomFlagsPlugin) plugin.getServer()
				.getPluginManager().getPlugin("WGCustomFlags");

		if (customWGFlags == null
				|| !(customWGFlags instanceof WGCustomFlagsPlugin)) {
			return null;
		}

		return (WGCustomFlagsPlugin) customWGFlags;
	}
}

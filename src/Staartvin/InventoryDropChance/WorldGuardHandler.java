package Staartvin.InventoryDropChance;

import org.bukkit.entity.Player;

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
	public static IntegerFlag XPLOSS_PERCENTAGE;

	public WorldGuardHandler(InventoryDropChance instance) {
		plugin = instance;
	}

	protected void registerFlags() {
		if (!plugin.wgClass.isWorldGuardReady())
			return;

		XPLOSS_PERCENTAGE = new IntegerFlag("xploss-percentage");
		RETAIN_PERCENTAGE = new IntegerFlag("retain-percentage");
		DELETE_PERCENTAGE = new IntegerFlag("lose-percentage");

		customWGFlags.addCustomFlag(XPLOSS_PERCENTAGE);
		customWGFlags.addCustomFlag(RETAIN_PERCENTAGE);
		customWGFlags.addCustomFlag(DELETE_PERCENTAGE);
	}

	protected int getRetainPercentage(Player player) {

		if (plugin.wgClass.isWorldGuardReady()) {
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

		for (String groupName : plugin.groups) {
			if (player.hasPermission("idc.group." + groupName)) {
				return plugin.getConfig().getInt(
						"Groups." + groupName + ".retain percentage");
			}
		}
		return 50;
	}

	protected int getDeletePercentage(Player player) {

		if (plugin.wgClass.isWorldGuardReady()) {
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

		for (String groupName : plugin.groups) {
			if (player.hasPermission("idc.group." + groupName)) {
				return plugin.getConfig().getInt(
						"Groups." + groupName + ".lose percentage");
			}
		}
		return 50;
	}

	protected int getExpPercentage(Player player) {

		if (plugin.wgClass.isWorldGuardReady()) {
			RegionManager regionManager = wgPlugin.getRegionManager(player
					.getWorld());
			if (regionManager != null) {
				ApplicableRegionSet set = regionManager
						.getApplicableRegions(player.getLocation());
				if (set != null) {
					if (set.getFlag(XPLOSS_PERCENTAGE) != null) {
						return set.getFlag(XPLOSS_PERCENTAGE);
					}
				}
			}
		}

		for (String groupName : plugin.groups) {
			if (player.hasPermission("idc.group." + groupName)) {
				return plugin.getConfig().getInt(
						"Groups." + groupName + ".xp loss");
			}
		}
		return 50;
	}

	protected WorldGuardPlugin getWorldGuard() {
		wgPlugin = (WorldGuardPlugin) plugin.getServer().getPluginManager()
				.getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (wgPlugin == null || !(wgPlugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) wgPlugin;
	}

	protected WGCustomFlagsPlugin getWGCustomFlags() {
		customWGFlags = (WGCustomFlagsPlugin) plugin.getServer()
				.getPluginManager().getPlugin("WGCustomFlags");

		if (customWGFlags == null
				|| !(customWGFlags instanceof WGCustomFlagsPlugin)) {
			return null;
		}

		return (WGCustomFlagsPlugin) customWGFlags;
	}
}

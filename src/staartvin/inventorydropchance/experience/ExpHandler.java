package staartvin.inventorydropchance.experience;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import staartvin.inventorydropchance.InventoryDropChance;

public class ExpHandler {

	private InventoryDropChance plugin;
	
	public ExpHandler(InventoryDropChance instance) {
		plugin = instance;
	}
	
	public int calculateExp(int Exp, Player player) {
		// Calculate amount of xp not being lost
		int expLoss;
		if (plugin.getWorldGuardClass().isWorldGuardReady()) {
			expLoss = (int) Math
					.round(Exp
							* (plugin.getWorldGuardClass().wgHandler
									.getExpPercentage(player) / 100d));
		} else {
			expLoss = (int) Math.round(Exp
					* (plugin.getFiles().getExpPercentage(player) / 100d));
		}
		return Exp - expLoss;
	}
	
	public void doEXPCheck(Player player, PlayerDeathEvent event) {
		if (player.hasPermission("idc.keepxp")) {
			event.setDroppedExp(0);
			event.setKeepLevel(true);
			return;
		}

		if (plugin.getFiles().getExpLossUsage(player)) {
			//ExperienceManager expMan = plugin.events.expManHandler.get(player.getName());
			
			int calEXP = calculateExp(player.getTotalExperience(), player);

			// Dropped exp = total exp of player - (total exp * xploss percentage)
			event.setDroppedExp(player.getTotalExperience() - calEXP);

			// Save the exp to give back
			plugin.getEvents().ExpToKeep.put(player.getName(), calEXP);
		}
	}
}

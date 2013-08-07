package staartvin.inventorydropchance.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import staartvin.inventorydropchance.InventoryDropChance;
import staartvin.inventorydropchance.updater.Updater;

public class ServerJoinListener implements Listener {

	private InventoryDropChance plugin;
	
	public ServerJoinListener(InventoryDropChance instance) {
		plugin = instance;
	}
	
	@EventHandler
	protected void onServerJoin(PlayerJoinEvent event) {

		final Player player = event.getPlayer();

		/*		if (!expManHandler.containsKey(player.getName())) {
					ExperienceManager expMan = new ExperienceManager(player);
					
					expManHandler.put(player.getName(), expMan);
				} */

		plugin.getUpdaterStatus();

		if (player.hasPermission("idc.noticeonupdate")) {
			if (plugin.updater != null
					&& plugin.updater.getResult().equals(
							Updater.UpdateResult.UPDATE_AVAILABLE)) {
				plugin.getServer().getScheduler()
						.runTaskLaterAsynchronously(plugin, new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								player.sendMessage(ChatColor.GREEN
										+ plugin.updater
												.getLatestVersionString()
										+ ChatColor.GOLD
										+ " is now available for download!");
							}

						}, 10L);
			}
		}
	}
}

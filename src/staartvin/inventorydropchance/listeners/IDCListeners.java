package staartvin.inventorydropchance.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import staartvin.inventorydropchance.InventoryDropChance;

//import staartvin.inventorydropchance.experience.ExperienceManager;

public class IDCListeners implements Listener {

	InventoryDropChance plugin;

	// Amount of items a player had
	protected HashMap<String, Integer> count = new HashMap<String, Integer>();

	// Items that will be given back on respawn
	protected HashMap<String, List<ItemStack>> items = new HashMap<String, List<ItemStack>>();

	// Is the player dead or not?
	//protected HashMap<String, Boolean> dead = new HashMap<String, Boolean>();

	// The amount of EXP to give back on respawn
	public HashMap<String, Integer> ExpToKeep = new HashMap<String, Integer>();

	// A hashmap containing all ExperienceManager objects
	//protected HashMap<String, ExperienceManager> expManHandler = new HashMap<String, ExperienceManager>();

	public IDCListeners(InventoryDropChance plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	protected void onPlayerDeath(PlayerDeathEvent event) {

		Player player = event.getEntity().getPlayer();

		if (!plugin.getWorldHandlers().worldIsEnabled(
				player.getWorld().getName()))
			return;

		count.put(player.getName(), player.getInventory().getContents().length);
		
		// Run EXP check
		//plugin.getExpHandler().doEXPCheck(player, event);
		
		if (player.hasPermission("idc.keepxp")) {
			event.setNewTotalExp(event.getDroppedExp());
			event.setDroppedExp(0);
			event.setKeepLevel(true);
			return;
		}

		/*if (plugin.getFiles().getExpLossUsage(player)) {
			//ExperienceManager expMan = plugin.events.expManHandler.get(player.getName());
			
			int calEXP = plugin.getExpHandler().calculateExp(player.getTotalExperience(), player);
			
			event.setNewTotalExp(calEXP);
			
			// Dropped exp = total exp of player - (total exp * xploss percentage)
			event.setDroppedExp(player.getTotalExperience() - calEXP);
			
			// Save the exp to give back
			plugin.getEvents().ExpToKeep.put(player.getName(), calEXP);
		}*/

		if (player.hasPermission("idc.keepallitems")) {
			List<ItemStack> itemStackArray = new ArrayList<ItemStack>();
			List<ItemStack> armourStackArray = new ArrayList<ItemStack>();

			// Give full inventory back
			for (int i = 0; i < player.getInventory().getContents().length; i++) {
				ItemStack item = player.getInventory().getContents()[i];

				itemStackArray.add(item);
			}

			// Give all armour back
			for (ItemStack armour : player.getInventory().getArmorContents()) {
				armourStackArray.add(armour);
			}

			// Clear drops
			event.getDrops().clear();

			// Save items and armour for other methods
			items.put(player.getName(), itemStackArray);
			plugin.getMethods().armour.put(player.getName(), armourStackArray);

			return;
		}

		if (plugin.getFiles().doPerStackCheck(player)) {
			// Do per stack check

			List<ItemStack> drops = event.getDrops();

			List<ItemStack> givenItems = plugin.getMethods().doPerStackCheck(
					player, drops);
			
			List<ItemStack> droppedItems = plugin.getMethods().getDroppedItems(
					player, drops);

			// Save given items
			items.put(player.getName(), givenItems);

			// Clear drops
			drops.clear();

			// Add drops
			for (ItemStack droppedItem : droppedItems) {
				drops.add(droppedItem);
			}

			return;
		}

		String checkFirst = plugin.getFiles().getCheckFirst(player);

		if (checkFirst.equalsIgnoreCase("save")) {

			// Run save check first
			items.put(player.getName(),
					plugin.getMethods().doSaveCheck(player, event.getDrops()));

			// Run delete check afterwards
			// Remove deleted items from items so they are not given back
			List<ItemStack> givenItems = items.get(player.getName());
			List<ItemStack> deletedItems = plugin.getMethods().doDeleteCheck(
					player, givenItems);

			// Remove all deleted items from the given items
			for (ItemStack deletedItem : deletedItems) {
				for (ItemStack givenItem : givenItems) {
					if (givenItem.isSimilar(deletedItem)) {
						givenItems.remove(givenItem);
						break;
					}
				}
			}
			// Remove the deleted items from the drops so they are not dropped as well
			/*for (ItemStack deletedItem: deletedItems) {
				for (ItemStack drop: event.getDrops()) {
					if (drop.isSimilar(deletedItem)) {
						event.getDrops().remove(drop);
						break;
					}
				}
			}*/

			// Update given items
			items.put(player.getName(), givenItems);

		} else if (checkFirst.equalsIgnoreCase("delete")) {

			// Run delete check first
			List<ItemStack> deletedItems = plugin.getMethods().doDeleteCheck(
					player, event.getDrops());

			// Remove deleted items from dropped items
			event.getDrops().removeAll(deletedItems);

			// Run save check afterwards
			items.put(player.getName(),
					plugin.getMethods().doSaveCheck(player, event.getDrops()));
		}
	}

	@EventHandler
	protected void onPlayerRespawn(PlayerRespawnEvent event) {

		final Player player = event.getPlayer();
		final String playerName = player.getName();

		if (!plugin.getWorldHandlers().worldIsEnabled(
				player.getWorld().getName()))
			return;

		// Give player saved EXP
		// Redundant: see line 41 of ExpHandler
		/*if (plugin.getFiles().getExpLossUsage(player)) {
			if (!player.hasPermission("idc.keepxp")) {
				if (ExpToKeep.get(playerName) == null)
					return;

				plugin.getServer().getScheduler()
						.runTaskLater(plugin, new Runnable() {

							public void run() {
								if (player.getExp() <= 0) {
									System.out.print("give exp!");
									player.giveExp(ExpToKeep.get(playerName));	
								}
								//System.out.print("Exp to keep: " + ExpToKeep.get(playerName));
								//ExpToKeep.put(playerName, null);
							}
						}, 20L);
			}
		}*/

		plugin.getMethods().returnItems(player, items.get(playerName));

		// Set everything to null so GC can do his work
		count.put(playerName, null);
		items.put(playerName, null);
		plugin.getMethods().armour.put(playerName, null);
		plugin.getMethods().whitelistedItems.put(playerName, null);
		plugin.getMethods().randomUsed.put(playerName, null);
	}
}

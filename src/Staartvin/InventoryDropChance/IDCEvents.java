package Staartvin.InventoryDropChance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class IDCEvents implements Listener {

	InventoryDropChance plugin;
	protected HashMap<String, ItemStack[]> inventory = new HashMap<String, ItemStack[]>();
	protected HashMap<String, Integer> count = new HashMap<String, Integer>();
	protected HashMap<String, ItemStack[]> items = new HashMap<String, ItemStack[]>();
	protected HashMap<String, List<Integer>> randomUsed = new HashMap<String, List<Integer>>();
	protected HashMap<String, Integer> orgItems = new HashMap<String, Integer>();
	Player player;

	public IDCEvents(InventoryDropChance plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	protected void onPlayerDeath(PlayerDeathEvent event) {
		player = event.getEntity().getPlayer();
		String playername = player.getName();
		List<ItemStack> drops = event.getDrops();
		List<ItemStack> remove = new ArrayList<ItemStack>();
	//	int playerExp = event.getEntity().getTotalExperience();
		
		if (player.hasPermission("idc.keepxp")) {
			event.setDroppedExp(0);
			event.setKeepLevel(true);
		}

/*		// Calculate amount of xp not being lost
			int xpLoss = (int) Math.round(playerExp
					* (plugin.XPLossPercentage / 100d));
			System.out.print("XPLoss: " + xpLoss);
			System.out.print("Dropped XP: " + playerExp);
			event.setDroppedExp(playerExp - xpLoss); */
				
		if (player.hasPermission("idc.keepallitems")) {
			inventory
					.put(playername, player.getInventory().getContents());
			count.put(playername, drops.size());
			drops.clear();
			return;
		}

		if (player.hasPermission("idc.percentageloss")) {

			if (orgItems.get(playername) == null) {
				orgItems.put(playername, drops.size());
			}
			
			// Store number of itemstacks in inventory
			orgItems.put(playername, drops.size());
			
			
			// Count the size of total drops
			count.put(playername, drops.size());

			// Calculate amount of items not being dropped
			double calculated = count.get(playername)
					* (plugin.retainPercentage / 100d);

			// Initialize new ItemStack array
			ItemStack[] itemstackarray = new ItemStack[36];

			// Create for loop to loop all not drops
			for (int i = 0; i < Math.round(calculated); i++) {
				// Create a random number
				int slot = generateRandomUnique(drops.size(), playername);

				itemstackarray[i] = drops.get(slot);
				remove.add(drops.get(slot));
			}
			// Clear specific player list
			randomUsed.put(playername, new ArrayList<Integer>());

			drops.removeAll(remove);
			items.put(playername, itemstackarray);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		String playername = player.getName();
		if (player != event.getPlayer())
			return;

		if (player.hasPermission("idc.keepallitems")) {
			Inventory replacement = player.getInventory();
			replacement.setContents(inventory.get(playername));
			return;
		}

		if (player.hasPermission("idc.percentageloss")) {
			int count = 0;
			Inventory replacement = player.getInventory();
			ItemStack[] newinv = new ItemStack[36];
			for (int i = 0; i < items.get(playername).length; i++) {
				newinv[i] = items.get(playername)[i];
				if (newinv[i] == null) {
					break;
				}
				count++;
			}
			replacement.setContents(newinv);
			player.sendMessage(ChatColor.GOLD
					+ (count + " items have survived your death!"));
			if (count == 0 && orgItems.get(playername) == 0) {
				player.sendMessage(ChatColor.RED + "That's 100% of your old inventory.");
				return;
			}
			player.sendMessage(ChatColor.RED + "That's "
					+ plugin.retainPercentage + "% of your old inventory.");
		}
	}

	protected int generateRandomUnique(Integer listsize, String playername) {

		int random = 0;
		boolean randomVerify = false;
		List<Integer> list;

		if (randomUsed.get(playername) == null) {
			list = new ArrayList<Integer>();
		} else {
			list = randomUsed.get(playername);
		}

		while (!randomVerify) {
			random = (int) Math.floor((Math.random() * listsize));

			if (randomUsed.get(playername) == null) {
				list.add(random);
				randomUsed.put(playername, list);
				randomVerify = true;
			} else if (!randomUsed.get(playername).contains(random)) {
				list.add(random);
				randomUsed.put(playername, list);
				randomVerify = true;
			}
		}
		return random;
	}
}

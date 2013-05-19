package Staartvin.InventoryDropChance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
	protected HashMap<String, Boolean> dead = new HashMap<String, Boolean>();
	protected HashMap<String, Integer> ExpToKeep = new HashMap<String, Integer>();
	protected HashMap<String, List<ItemStack>> whitelistedItems= new HashMap<String, List<ItemStack>>();

	public IDCEvents(InventoryDropChance plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	protected void onPlayerDeath(PlayerDeathEvent event) {

		Player player = event.getEntity().getPlayer();

		if (!plugin.wHandlers.worldIsEnabled(player.getWorld().getName()))
			return;
		dead.put(player.getName(), true);

		String playerName = player.getName();
		List<ItemStack> drops = event.getDrops();
		List<ItemStack> remove = new ArrayList<ItemStack>();

		if (player.hasPermission("idc.keepxp")) {
			event.setDroppedExp(0);
			event.setKeepLevel(true);
		}

		// TODO: FIX BUG: ARMOR IS NOT BEING KEPT OR CHECKED OR WHATEVER. THIS IS BECAUSE IT'S NOT PART OF inv.getContents()
		if (player.hasPermission("idc.keepallitems")) {
			inventory.put(playerName, player.getInventory().getContents());
			count.put(playerName, drops.size());
			drops.clear();
			return;
		}

		if (getExpLossUsage(player)) {
			int calEXP = calculateExp(player.getTotalExperience(), player);
			//	System.out.print("Dropped EXP: " + (player.getTotalExperience() - calEXP));
			//	System.out.print("Given back EXP: " + calEXP);
			event.setDroppedExp(player.getTotalExperience() - calEXP);
			ExpToKeep.put(playerName, calEXP);
		}

		// Initialise a new array that will hold all items that will be forced to drop (I.E. Blacklisted)
		List<ItemStack> blacklisted = new ArrayList<ItemStack>();

		// Initialise a new array that will hold all items that will be forced to keep (I.E. Whitelisted)
		List<ItemStack> whitelisted = new ArrayList<ItemStack>();

		// Group of the player
		String group = getGroup(player);
		System.out.print("Group: " + group);

		// Remove the blacklisted items from inventory so they are not counted
		for (ItemStack item : player.getInventory()) {
			if (group != null && item != null) {
				if (isBlacklistedItem(item, group)) {
					blacklisted.add(item);
					// We don't want to keep an item that is forced to be dropped.
					drops.remove(item);
					continue;
				} else if (isWhitelistedItem(item, group)) {
					whitelisted.add(item);
					
					// We don't want the same item given more than once, so we remove it from the drops
					drops.remove(item);
					continue;
				}
			}
		}
		
		// Make the whitelisted items global so returnItems() can add them to the inventory
		whitelistedItems.put(playerName, whitelisted);
		

		if (orgItems.get(playerName) == null) {
			orgItems.put(playerName, drops.size());
		}

		// Store number of itemstacks in inventory
		orgItems.put(playerName, drops.size());

		// Count the size of total drops
		count.put(playerName, drops.size());

		// Calculate amount of items not being dropped
		double calculated;
		if (plugin.wgClass.isWorldGuardReady()) {
			calculated = count.get(playerName)
					* (plugin.wgClass.wgHandler.getRetainPercentage(player) / 100d);
		} else {
			calculated = count.get(playerName)
					* (getRetainPercentage(player) / 100d);
		}

		// Initialize new ItemStack array
		ItemStack[] itemstackarray = new ItemStack[36];

		// Create for loop to loop all not drops
		for (int i = 0; i < Math.round(calculated); i++) {
			// Create a random number
			int slot = generateRandomUnique(drops.size(), playerName);
			
			itemstackarray[i] = drops.get(slot);
			remove.add(drops.get(slot));
		}
		// Clear specific player list
		randomUsed.put(playerName, new ArrayList<Integer>());

		drops.removeAll(remove);
		drops.addAll(blacklisted);
		
		for (ItemStack item:drops) {
			System.out.print("Dropped items: " + item);
		}
		
		// Items that are kept
		items.put(playerName, itemstackarray);
	}

	@EventHandler
	protected void onPlayerRespawn(PlayerRespawnEvent event) {

		final Player player = event.getPlayer();
		final String playerName = player.getName();

		if (dead.get(playerName) == null) {
			dead.put(playerName, false);
		}
		if (!dead.get(playerName))
			return;

		dead.put(playerName, false);

		if (!plugin.wHandlers.worldIsEnabled(player.getWorld().getName()))
			return;

		if (getExpLossUsage(player)) {
			if (!player.hasPermission("idc.keepxp")) {
				if (ExpToKeep.get(playerName) == null)
					return;
				plugin.getServer().getScheduler()
						.runTaskLater(plugin, new Runnable() {

							public void run() {
								//	System.out.print("Player already had: " + player.getTotalExperience());
								//    System.out.print("Giving player " + ExpToKeep.get(playerName) + " EXP!");
								player.giveExp(ExpToKeep.get(playerName));
								//	System.out.print("Player now has: " + player.getTotalExperience());
							}
						}, 3L);
			}
		}

		if (player.hasPermission("idc.keepallitems")) {
			Inventory replacement = player.getInventory();
			replacement.setContents(inventory.get(playerName));
			return;
		}

		returnItems(player, items.get(playerName));
		inventory.put(playerName, null);
		count.put(playerName, null);
		items.put(playerName, null);
		orgItems.put(playerName, null);
		whitelistedItems.put(playerName, null);
	}

	protected boolean isBlacklistedItem(ItemStack item, String group) {
		List<String> blacklist = plugin.getBlacklistedItems(group);

		int dataValue = item.getTypeId();
		int damageValue = item.getData().getData();

		for (String blacklistedItem : blacklist) {

			Integer dataValueBlack = null;
			Integer damageValueBlack = null;

			if (blacklistedItem.contains(":")) {
				// For items such as wool. 35:1
				String[] temp = blacklistedItem.split(":");
				dataValueBlack = Integer.parseInt(temp[0]);
				damageValueBlack = Integer.parseInt(temp[1]);
			} else {
				dataValueBlack = Integer.parseInt(blacklistedItem);
			}

			if (dataValueBlack != null && damageValueBlack != null) {
				if (dataValue == dataValueBlack
						&& damageValue == damageValueBlack) {
					return true;
				}
					
			} else {
				if (dataValue == dataValueBlack) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isWhitelistedItem(ItemStack item, String group) {
		List<String> whitelist = plugin.getWhitelistedItems(group);

		int dataValue = item.getTypeId();
		int damageValue = item.getData().getData();

		for (String whitelistedItem : whitelist) {

			Integer dataValueBlack = null;
			Integer damageValueBlack = null;

			if (whitelistedItem.contains(":")) {
				// For items such as wool. 35:1
				String[] temp = whitelistedItem.split(":");
				dataValueBlack = Integer.parseInt(temp[0]);
				damageValueBlack = Integer.parseInt(temp[1]);
			} else {
				dataValueBlack = Integer.parseInt(whitelistedItem);
			}

			if (dataValueBlack != null && damageValueBlack != null) {
				if (dataValue == dataValueBlack
						&& damageValue == damageValueBlack) {
					return true;
				}
			} else {
				if (dataValue == dataValueBlack) {
					return true;
				}
			}
		}
		return false;
	}

	@EventHandler
	protected void onServerQuit(PlayerQuitEvent event) {

		Player player = event.getPlayer();
		String playerName = player.getName();

		if (dead.get(playerName) == null) {
			dead.put(playerName, false);
		}
		if (!dead.get(playerName))
			return;
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

	protected void returnItems(Player player, ItemStack[] items) {

		String playerName = player.getName();

		int count = 0;
		Inventory replacement = player.getInventory();
		ItemStack[] newinv = new ItemStack[36];
		
		for (int i = 0; i < items.length; i++) {
			newinv[i] = items[i];
			if (newinv[i] == null) {
				break;
			}
			count++;
		}

		replacement.setContents(newinv);
		
		// Give whitelisted items
		for (ItemStack item:whitelistedItems.get(playerName)) {
			replacement.addItem(item);
		}
		
		String itemMessage = plugin.files.ITEMS_MESSAGE_ON_RESPAWN.replace(
				"{0}", count + "");
		String percentageMessage = plugin.files.PERCENTAGE_MESSAGE_ON_RESPAWN;

		player.sendMessage(ChatColor.GOLD + itemMessage);
		if (count == 0 && orgItems.get(playerName) == 0) {
			percentageMessage = percentageMessage.replace("{0}", 100 + "%");
			player.sendMessage(ChatColor.RED + percentageMessage);
			return;
		}
		if (plugin.wgClass.isWorldGuardReady()) {
			percentageMessage = percentageMessage.replace("{0}",
					plugin.wgClass.wgHandler.getRetainPercentage(player) + "%");
			player.sendMessage(ChatColor.RED + percentageMessage);
		} else {
			percentageMessage = percentageMessage.replace("{0}",
					getRetainPercentage(player) + "%");
			player.sendMessage(ChatColor.RED + percentageMessage);
		}

	}

	protected int calculateExp(int Exp, Player player) {
		// Calculate amount of xp not being lost
		int expLoss;
		if (plugin.wgClass.isWorldGuardReady()) {
			expLoss = (int) Math
					.round(Exp
							* (plugin.wgClass.wgHandler
									.getExpPercentage(player) / 100d));
		} else {
			expLoss = (int) Math.round(Exp * (getExpPercentage(player) / 100d));
		}
		return Exp - expLoss;
	}

	protected boolean getExpLossUsage(Player player) {

		String group = getGroup(player);

		if (group == null)
			return false;
		else
			return plugin.getConfig().getBoolean(
					"Groups." + group + ".use xp loss");
	}

	protected int getRetainPercentage(Player player) {

		String group = getGroup(player);

		if (group == null)
			return 50;
		else
			return plugin.getConfig().getInt(
					"Groups." + group + ".retain percentage");
	}

	protected int getExpPercentage(Player player) {
		String group = getGroup(player);

		if (group == null)
			return 50;
		else
			return plugin.getConfig().getInt("Groups." + group + ".xp loss");
	}

	protected String getGroup(Player player) {
		for (String groupName : plugin.groups) {
			if (player.hasPermission("idc.group." + groupName)) {
				return groupName;
			}
		}
		return null;
	}
}

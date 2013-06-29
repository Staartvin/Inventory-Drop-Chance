package Staartvin.InventoryDropChance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	// Amount of items a player had
	protected HashMap<String, Integer> count = new HashMap<String, Integer>();

	// Items that will be given back on respawn
	protected HashMap<String, List<ItemStack>> items = new HashMap<String, List<ItemStack>>();

	// Slots that are already used and can't be checked again
	protected HashMap<String, List<Integer>> randomUsed = new HashMap<String, List<Integer>>();

	// Is the player dead or not?
	protected HashMap<String, Boolean> dead = new HashMap<String, Boolean>();

	// The amount of EXP to give back on respawn
	protected HashMap<String, Integer> ExpToKeep = new HashMap<String, Integer>();

	// A list of items that are whitelisted from the inv and should be given back
	protected HashMap<String, List<ItemStack>> whitelistedItems = new HashMap<String, List<ItemStack>>();

	public IDCEvents(InventoryDropChance plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	protected void onPlayerDeath(PlayerDeathEvent event) {

		Player player = event.getEntity().getPlayer();

		dead.put(player.getName(), true);

		if (!plugin.wHandlers.worldIsEnabled(player.getWorld().getName()))
			return;

		count.put(player.getName(), player.getInventory().getContents().length);

		// Run EXP check
		doEXPCheck(player, event);

		if (player.hasPermission("idc.keepallitems")) {
			List<ItemStack> itemStackArray = new ArrayList<ItemStack>();

			// Give full inventory back
			for (int i = 0; i < player.getInventory().getContents().length; i++) {
				ItemStack item = player.getInventory().getContents()[i];

				itemStackArray.add(item);
			}

			// Clear drops
			event.getDrops().clear();

			items.put(player.getName(), itemStackArray);

			return;
		}

		String checkFirst = plugin.getConfig().getString(
				"Groups." + getGroup(player) + ".check first", "save");

		if (checkFirst.equalsIgnoreCase("save")) {

			// Run save check first
			items.put(player.getName(), doSaveCheck(player, event.getDrops()));

			// Run delete check afterwards
			// Remove deleted items from items so they are not given back
			List<ItemStack> givenItems = items.get(player.getName());
			List<ItemStack> deletedItems = doDeleteCheck(player, givenItems);

			givenItems.removeAll(deletedItems);

			// Update given items
			items.put(player.getName(), givenItems);

			// Remove the deleted items from the drops so they are not dropped as well
			event.getDrops().removeAll(deletedItems);

		} else if (checkFirst.equalsIgnoreCase("delete")) {

			// Run delete check first
			List<ItemStack> deletedItems = doDeleteCheck(player,
					event.getDrops());

			// Remove deleted items from dropped items
			event.getDrops().removeAll(deletedItems);

			// Run save check afterwards
			items.put(player.getName(), doSaveCheck(player, event.getDrops()));
		}
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

		// Give player saved EXP
		if (getExpLossUsage(player)) {
			if (!player.hasPermission("idc.keepxp")) {
				if (ExpToKeep.get(playerName) == null) return;
				
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

		returnItems(player, items.get(playerName));
		count.put(playerName, null);
		items.put(playerName, null);
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

	protected void returnItems(Player player, List<ItemStack> items) {

		String playerName = player.getName();

		int count = 0;
		Inventory replacement = player.getInventory();
		ItemStack[] newinv = new ItemStack[36];

		for (int i = 0; i < newinv.length; i++) {
			if (i >= items.size()) {
				break;
			}

			newinv[i] = items.get(i);
			if (newinv[i] == null) {
				break;
			}
			count++;
		}

		replacement.setContents(newinv);

		// Give whitelisted items
		for (ItemStack item : whitelistedItems.get(playerName)) {
			replacement.addItem(item);
		}

		String itemMessage = plugin.files.ITEMS_MESSAGE_ON_RESPAWN.replace(
				"{0}", count + "");
		String percentageMessage = "";
		
		if (plugin.getConfig().getString("Groups." + getGroup(player) + ".check first").equalsIgnoreCase("save")) {
			percentageMessage = plugin.files.PERCENTAGE_MESSAGE_ON_RESPAWN;
		} else if (plugin.getConfig().getString("Groups." + getGroup(player) + ".check first").equalsIgnoreCase("delete")){
			percentageMessage = plugin.files.INVERTED_PERCENTAGE_MESSAGE_ON_RESPAWN;
		} else {
			percentageMessage = plugin.files.PERCENTAGE_MESSAGE_ON_RESPAWN;
		}
		
		
		player.sendMessage(ChatColor.GOLD + itemMessage);
		if (plugin.wgClass.isWorldGuardReady()) {
			percentageMessage = percentageMessage.replace("{0}",
					plugin.wgClass.wgHandler.getRetainPercentage(player) + "%").replace("{1}", plugin.wgClass.wgHandler.getDeletePercentage(player) + "%");
			player.sendMessage(ChatColor.RED + percentageMessage);
		} else {
			percentageMessage = percentageMessage.replace("{0}",
					getRetainPercentage(player) + "%").replace("{1}", getDeletePercentage(player) + "%");
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

	protected int getDeletePercentage(Player player) {

		String group = getGroup(player);

		if (group == null)
			return 50;
		else
			return plugin.getConfig().getInt(
					"Groups." + group + ".delete percentage");
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

	// Get items that are saved
	protected List<ItemStack> doSaveCheck(Player player,
			List<ItemStack> itemsToCheck) {

		String playerName = player.getName();
		List<ItemStack> drops = itemsToCheck;

		// FIXME: ARMOR IS NOT BEING KEPT OR CHECKED OR WHATEVER. THIS IS BECAUSE IT'S NOT PART OF inv.getContents()

		// Initialise a new array that will hold all items that will be forced to drop (I.E. Blacklisted)
		List<ItemStack> blacklisted = new ArrayList<ItemStack>();

		// Initialise a new array that will hold all items that will be forced to keep (I.E. Whitelisted)
		List<ItemStack> whitelisted = new ArrayList<ItemStack>();

		// Group of the player
		String group = getGroup(player);

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

		// Calculate amount of items not being dropped
		double calculated;
		if (plugin.wgClass.isWorldGuardReady()) {
			calculated = drops.size()
					* (plugin.wgClass.wgHandler.getRetainPercentage(player) / 100d);
		} else {
			calculated = drops.size() * (getRetainPercentage(player) / 100d);
		}

		// Initialize new ItemStack array
		List<ItemStack> itemstackarray = new ArrayList<ItemStack>();

		// Clear slots used
		randomUsed.put(playerName, new ArrayList<Integer>());

		// Keep a list of items that are kept and should be removed from the drops
		List<ItemStack> keptItems = new ArrayList<ItemStack>();

		// Create for loop to loop all not drops
		for (int i = 0; i < Math.round(calculated); i++) {
			// Create a random number
			int slot = generateRandomUnique(drops.size(), playerName);

			itemstackarray.add(drops.get(slot));
			keptItems.add(drops.get(slot));
		}
		// Clear slots used
		randomUsed.put(playerName, new ArrayList<Integer>());

		// Remove all kept items from the drops
		drops.removeAll(keptItems);

		// Drop all blacklisted items (They are forced to drop)
		drops.addAll(blacklisted);

		// Items that are kept
		return itemstackarray;
	}

	protected void doEXPCheck(Player player, PlayerDeathEvent event) {
		if (player.hasPermission("idc.keepxp")) {
			event.setDroppedExp(0);
			event.setKeepLevel(true);
			return;
		}

		if (getExpLossUsage(player)) {
			int calEXP = calculateExp(player.getTotalExperience(), player);

			// Dropped exp = total exp of player - (total exp * xploss percentage)
			event.setDroppedExp(player.getTotalExperience() - calEXP);

			// Save the exp to give back
			ExpToKeep.put(player.getName(), calEXP);
		}
	}

	protected List<ItemStack> doDeleteCheck(Player player,
			List<ItemStack> itemsToCheck) {

		String playerName = player.getName();

		// Calculate amount of items being deleted
		double calculated;
		if (plugin.wgClass.isWorldGuardReady()) {
			calculated = itemsToCheck.size()
					* (plugin.wgClass.wgHandler.getDeletePercentage(player) / 100d);
		} else {
			calculated = itemsToCheck.size()
					* (getDeletePercentage(player) / 100d);
		}

		List<ItemStack> deletedItems = new ArrayList<ItemStack>();

		// Clear slots used
		randomUsed.put(playerName, new ArrayList<Integer>());

		// Create for loop to loop all not drops
		for (int i = 0; i < Math.round(calculated); i++) {
			// Create a random number
			int slot = generateRandomUnique(itemsToCheck.size(), playerName);

			deletedItems.add(itemsToCheck.get(slot));
		}

		// Clear slots used
		randomUsed.put(playerName, new ArrayList<Integer>());

		// Return the deleted items
		return deletedItems;
	}
}

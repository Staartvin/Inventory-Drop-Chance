package staartvin.inventorydropchance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

//import staartvin.inventorydropchance.experience.ExperienceManager;

public class Methods {
	private InventoryDropChance plugin;

	// Slots that are already used and can't be checked again
	public HashMap<String, List<Integer>> randomUsed = new HashMap<String, List<Integer>>();

	// A list of items that are whitelisted from the inv and should be given back
	public HashMap<String, List<ItemStack>> whitelistedItems = new HashMap<String, List<ItemStack>>();

	// Armour that will be given back on respawn
	public HashMap<String, List<ItemStack>> armour = new HashMap<String, List<ItemStack>>();

	public Methods(InventoryDropChance instance) {
		plugin = instance;
	}

	// Returns items that are saved
	public List<ItemStack> doSaveCheck(Player player,
			List<ItemStack> itemsToCheck) {

		String playerName = player.getName();
		List<ItemStack> drops = itemsToCheck;

		// Initialise a new array that will hold all items that will be forced to drop (I.E. Blacklisted)
		List<ItemStack> blacklisted = new ArrayList<ItemStack>();

		// Initialise a new array that will hold all items that will be forced to keep (I.E. Whitelisted)
		List<ItemStack> whitelisted = new ArrayList<ItemStack>();

		// Group of the player
		String group = plugin.getFiles().getGroup(player);

		// Remove the blacklisted items from inventory so they are not counted
		for (ItemStack item : player.getInventory()) {
			if (group != null && item != null) {
				if (plugin.getListHandler().isBlacklistedItem(item, group)) {
					blacklisted.add(item);
					// We don't want to keep an item that is forced to be dropped.
					drops.remove(item);
					continue;
				} else if (plugin.getListHandler().isWhitelistedItem(item,
						group)) {
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

		calculated = drops.size()
				* (plugin.getFiles().getRetainPercentage(player) / 100d);

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
		// Can't remove drops.removeAll() because that would delete everything when there is only 1 item.
		for (ItemStack keptItem : keptItems) {
			for (ItemStack drop : drops) {
				if (drop.isSimilar(keptItem)) {
					drops.remove(drop);
					break;
				}
			}
		}

		// Drop all blacklisted items (They are forced to drop)
		drops.addAll(blacklisted);

		// Items that are kept
		return itemstackarray;
	}

	// Return items that are deleted
	public List<ItemStack> doDeleteCheck(Player player,
			List<ItemStack> itemsToCheck) {

		String playerName = player.getName();

		// Do we need to check?
		boolean doCheck = true;

		// Calculate amount of items being deleted
		double calculated;
		calculated = itemsToCheck.size()
				* (plugin.getFiles().getDeletePercentage(player) / 100d);
		
		if (plugin.getFiles().getDeletePercentage(player) == 0)
			doCheck = false;

		// If delete percentage is 0, return nothing.
		if (!doCheck)
			return new ArrayList<ItemStack>();

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

	public int generateRandomUnique(Integer listsize, String playername) {

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

	public void returnItems(Player player, List<ItemStack> items) {

		String playerName = player.getName();

		int count = 0;
		PlayerInventory replacement = player.getInventory();

		
		if (items != null) {
			// Give kept items
			for (ItemStack item: items) {
				
				// Check if item is not null
				if (item != null) {
					replacement.addItem(item);
					count++;
				}
			}
		}
		

		// Give whitelisted items
		if (whitelistedItems.get(playerName) != null) {
			for (ItemStack item : whitelistedItems.get(playerName)) {

				// If inv is full
				if (replacement.firstEmpty() < 0) {
					player.getWorld().dropItem(player.getLocation(), item);
					continue;
				}

				replacement.addItem(item);
			}
		}

		// Give armour back (This will only give armour when a player has idc.keepallitems)
		if (this.armour.get(playerName) != null) {
			for (ItemStack armour : this.armour.get(playerName)) {

				if (isHelmet(armour)) {
					if (replacement.getHelmet() != null) {
						player.getWorld()
								.dropItem(player.getLocation(), armour);
						continue;
					} else {
						replacement.setHelmet(armour);
						continue;
					}
				}

				if (isChestplate(armour)) {
					if (replacement.getChestplate() != null) {
						player.getWorld()
								.dropItem(player.getLocation(), armour);
						continue;
					} else {
						replacement.setChestplate(armour);
						continue;
					}
				}

				if (isLeggings(armour)) {
					if (replacement.getLeggings() != null) {
						player.getWorld()
								.dropItem(player.getLocation(), armour);
						continue;
					} else {
						replacement.setLeggings(armour);
						continue;
					}
				}

				if (isBoots(armour)) {
					if (replacement.getBoots() != null) {
						player.getWorld()
								.dropItem(player.getLocation(), armour);
						continue;
					} else {
						replacement.setBoots(armour);
						continue;
					}
				} else {
					// If inv is full
					if (replacement.firstEmpty() < 0) {
						player.getWorld()
								.dropItem(player.getLocation(), armour);
						continue;
					}

					replacement.addItem(armour);
				}
			}
		}

		sendCorrectMessages(player, count);
	}

	boolean isHelmet(ItemStack item) {
		@SuppressWarnings("deprecation")
		int ID = item.getTypeId();

		return (ID == 298 || ID == 302 || ID == 306 || ID == 310 || ID == 314);
	}

	boolean isChestplate(ItemStack item) {
		@SuppressWarnings("deprecation")
		int ID = item.getTypeId();

		return (ID == 299 || ID == 303 || ID == 307 || ID == 311 || ID == 315);
	}

	boolean isLeggings(ItemStack item) {
		@SuppressWarnings("deprecation")
		int ID = item.getTypeId();

		return (ID == 300 || ID == 304 || ID == 308 || ID == 312 || ID == 316);
	}

	boolean isBoots(ItemStack item) {
		@SuppressWarnings("deprecation")
		int ID = item.getTypeId();

		return (ID == 301 || ID == 305 || ID == 309 || ID == 313 || ID == 317);
	}

	// Returns kept items
	public List<ItemStack> doPerStackCheck(Player player,
			List<ItemStack> itemsToCheck) {
		// Initialise empty array that will hold all kept items
		List<ItemStack> keptItems = new ArrayList<ItemStack>();

		// Automatically corrected when WorldGuard is found
		int rPercentage = plugin.getFiles().getRetainPercentage(player), dPercentage = plugin
				.getFiles().getDeletePercentage(player);

		String checkFirst = plugin.getFiles().getCheckFirst(player);

		// Go through every stack
		for (ItemStack item : itemsToCheck) {
			int amount = item.getAmount();

			@SuppressWarnings("unused")
			// dropAmount is just for human logic
			int saveAmount = 0, deleteAmount = 0, dropAmount = 0, keptAmount = 0;
			// Copy of item
			ItemStack newItem = item.clone();

			// First save, then delete
			if (checkFirst.equalsIgnoreCase("save")) {

				saveAmount = (int) Math.round(amount * (rPercentage / 100d));

				dropAmount = amount - saveAmount;

				deleteAmount = (int) Math.round(saveAmount
						* (dPercentage / 100d));

				keptAmount = (saveAmount - deleteAmount);

			} else if (checkFirst.equalsIgnoreCase("delete")) {
				deleteAmount = (int) Math.round(amount * (dPercentage / 100d));

				saveAmount = (int) Math.round((amount - deleteAmount)
						* (rPercentage / 100d));

				dropAmount = (amount - deleteAmount) - saveAmount;

				keptAmount = saveAmount;
			}
			
			newItem.setAmount(keptAmount);

			keptItems.add(newItem);
		}

		return keptItems;
	}

	// Returns dropped items
	public List<ItemStack> getDroppedItems(Player player,
			List<ItemStack> itemsToCheck) {
		// Initialise empty array that will hold all (soon to be) dropped items
		List<ItemStack> droppedItems = new ArrayList<ItemStack>();

		// Automatically corrected when WorldGuard is found
		int rPercentage = plugin.getFiles().getRetainPercentage(player), dPercentage = plugin
				.getFiles().getDeletePercentage(player);

		String checkFirst = plugin.getFiles().getCheckFirst(player);

		// Go through every stack
		for (ItemStack item : itemsToCheck) {
			int amount = item.getAmount();

			int saveAmount = 0, deleteAmount = 0, dropAmount = 0;

			// First save, then delete
			if (checkFirst.equalsIgnoreCase("save")) {

				saveAmount = (int) Math.round(amount * (rPercentage / 100d));

				dropAmount = amount - saveAmount;

				deleteAmount = (int) Math.round(saveAmount
						* (dPercentage / 100d));

			} else if (checkFirst.equalsIgnoreCase("delete")) {
				deleteAmount = (int) Math.round(amount * (dPercentage / 100d));

				saveAmount = (int) Math.round((amount - deleteAmount)
						* (rPercentage / 100d));

				dropAmount = (amount - deleteAmount) - saveAmount;
			}

			if (dropAmount > 0) {
				ItemStack newItem = item.clone();
				newItem.setAmount(dropAmount);

				droppedItems.add(newItem);
			}
		}

		return droppedItems;
	}

	public void sendCorrectMessages(Player player, int count) {
		if (player.hasPermission("idc.keepallitems")) {
			player.sendMessage(ChatColor.GOLD
					+ plugin.getFiles().ALL_ITEMS_SURVIVED);
			return;
		}

		String itemMessage = plugin.getFiles().ITEMS_MESSAGE_ON_RESPAWN
				.replace("{0}", count + "");

		player.sendMessage(ChatColor.GOLD + itemMessage);

		String percentageMessage = "";

		String checkFirst = plugin.getFiles().getCheckFirst(player);

		if (checkFirst == null)
			checkFirst = "save";

		if (checkFirst.equalsIgnoreCase("save")) {

			if (plugin.getFiles().doPerStackCheck(player)) {
				percentageMessage = plugin.getFiles().PER_STACK_CHECK_MESSAGE_ON_RESPAWN;
			} else {
				percentageMessage = plugin.getFiles().PERCENTAGE_MESSAGE_ON_RESPAWN;
			}

		} else if (checkFirst.equalsIgnoreCase("delete")) {

			if (plugin.getFiles().doPerStackCheck(player)) {
				percentageMessage = plugin.getFiles().INVERTED_PER_STACK_CHECK_MESSAGE_ON_RESPAWN;
			} else {
				percentageMessage = plugin.getFiles().INVERTED_PERCENTAGE_MESSAGE_ON_RESPAWN;
			}

		} else {

			if (plugin.getFiles().doPerStackCheck(player)) {
				percentageMessage = plugin.getFiles().PER_STACK_CHECK_MESSAGE_ON_RESPAWN;
			} else {
				percentageMessage = plugin.getFiles().PERCENTAGE_MESSAGE_ON_RESPAWN;
			}

		}

		percentageMessage = percentageMessage.replace("{0}",
				plugin.getFiles().getRetainPercentage(player) + "%").replace(
				"{1}", plugin.getFiles().getDeletePercentage(player) + "%");

		player.sendMessage(ChatColor.RED + percentageMessage);
	}
}

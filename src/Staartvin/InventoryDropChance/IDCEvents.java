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
	protected Boolean doneWorking = false;

	public IDCEvents(InventoryDropChance plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	protected void onPlayerDeath(PlayerDeathEvent event) {
		
		Player player = event.getEntity().getPlayer();
		
		dead.put(player.getName(), true);
		
		String playerName = player.getName();
		List<ItemStack> drops = event.getDrops();
		List<ItemStack> remove = new ArrayList<ItemStack>();
		
		if (player.hasPermission("idc.keepxp")) {
			event.setDroppedExp(0);
			event.setKeepLevel(true);
		}
				
		if (player.hasPermission("idc.keepallitems")) {
			inventory
					.put(playerName, player.getInventory().getContents());
			count.put(playerName, drops.size());
			drops.clear();
			return;
		}
		if (getExpLossUsage(player)){
		int calEXP = calculateExp(player.getTotalExperience(), player);
		event.setDroppedExp(player.getTotalExperience() - calEXP);
		ExpToKeep.put(playerName, calEXP);
		}

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
			}
			else {
				calculated = count.get(playerName) * (getRetainPercentage(player) / 100d);
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
			items.put(playerName, itemstackarray);
	}

	@EventHandler
	protected void onPlayerRespawn(PlayerRespawnEvent event) {
		
		final Player player = event.getPlayer();
		final String playerName = player.getName();
	
		if (dead.get(playerName) == null) {
			dead.put(playerName, false);
		}
		if (!dead.get(playerName)) return;
		
		dead.put(playerName, false);

		if (getExpLossUsage(player)) {
			if (!player.hasPermission("idc.keepxp")) {
				if (ExpToKeep.get(playerName) == null) return;
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
				    @Override 
				    public void run() {
				    	player.giveExp(ExpToKeep.get(playerName));
				    	doneWorking = true;
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
		if (!doneWorking) {
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			    @Override 
			    public void run() {
			    	ExpToKeep.put(playerName, null);
			    }
			}, 10L);
		}
		else {
			ExpToKeep.put(playerName, null);
		}
	}

	@EventHandler
	protected void onServerQuit(PlayerQuitEvent event) {
	
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		if (dead.get(playerName) == null) {
			dead.put(playerName, false);
		}
		if (!dead.get(playerName)) return;
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
		player.sendMessage(ChatColor.GOLD
				+ (count + " items have survived your death!"));
		if (count == 0 && orgItems.get(playerName) == 0) {
			player.sendMessage(ChatColor.RED + "That's 100% of your old inventory.");
			return;
		}
		if (plugin.wgClass.isWorldGuardReady()) {
			player.sendMessage(ChatColor.RED + "That's "
					+ plugin.wgClass.wgHandler.getRetainPercentage(player) + "% of your old inventory.");	
		}
		else {
			player.sendMessage(ChatColor.RED + "That's "
					+ getRetainPercentage(player) + "% of your old inventory.");
		}
		
	}

	protected int calculateExp(int Exp, Player player) {
		 // Calculate amount of xp not being lost
		int expLoss;
		if (plugin.wgClass.isWorldGuardReady()) {
			expLoss = (int) Math.round(Exp
					* (plugin.wgClass.wgHandler.getExpPercentage(player) / 100d));
		} else {
			expLoss = (int) Math.round(Exp
					* (getExpPercentage(player) / 100d));
		}
		return Exp - expLoss;
	}
	
	protected boolean getExpLossUsage(Player player) {
		
		for (String groupName: plugin.groups) {
			if (player.hasPermission("idc.group." + groupName)) {
				return plugin.getConfig().getBoolean("Groups." + groupName + ".use xp loss");
			}
		}
		return false;
	}
	
	protected int getRetainPercentage(Player player) {
		
		for (String groupName: plugin.groups) {
			if (player.hasPermission("idc.group." + groupName)) {
				return plugin.getConfig().getInt("Groups." + groupName + ".retain percentage");
			}
		}
		return 50;
	}
	
	protected int getExpPercentage(Player player) {
		
		for (String groupName: plugin.groups) {
			if (player.hasPermission("idc.group." + groupName)) {
				return plugin.getConfig().getInt("Groups." + groupName + ".xp loss");
			}
		}
		return 50;
	}
}

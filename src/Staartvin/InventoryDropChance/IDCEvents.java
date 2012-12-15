package Staartvin.InventoryDropChance;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class IDCEvents implements Listener{
	
	InventoryDropChance plugin;
	protected HashMap<String, ItemStack[]> inventory = new HashMap<String, ItemStack[]>();
	protected HashMap<String, Integer> count = new HashMap<String, Integer>();
	protected HashMap<String, ItemStack[]> items = new HashMap<String, ItemStack[]>();
	protected HashMap<String, List<Integer>> randomUsed = new HashMap<String, List<Integer>>();
	Player player;
	
	public IDCEvents (InventoryDropChance plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	protected void onPlayerDeath(PlayerDeathEvent event)
	{	
		player = event.getEntity().getPlayer();
		List<ItemStack> drops = event.getDrops();
		List<ItemStack> remove = new ArrayList<ItemStack>();
		
			if (player.hasPermission("idc.keepitems"))
			{	
				inventory.put(player.getName(), player.getInventory().getContents());
				count.put(player.getName(), drops.size());
				drops.clear();
				return;
			} 
			
			if (player.hasPermission("idc.keepxp"))
			{
				event.setDroppedExp(0);
				event.setKeepLevel(true);
			}
			
			if (player.hasPermission("idc.percentageloss")) {
				
				// Count the size of total drops
				count.put(player.getName(), drops.size());			
				
				// Calculate amount of items not being dropped
				double calculated = count.get(player.getName()) * (plugin.retainPercentage / 100d);
				
				// Initialize new ItemStack array
				ItemStack[] itemstackarray = new ItemStack[36]; 
				
				// Create for loop to loop all not drops
				for (int i=0; i<Math.round(calculated); i++) {
					// Create a random number
					int slot = generateRandomUnique(drops.size(), player.getName());
					
					itemstackarray[i] = drops.get(slot);
					remove.add(drops.get(slot));
				}
				// Clear specific player list
				randomUsed.get(player.getName()).clear();
				
				drops.removeAll(remove);
				items.put(player.getName(), itemstackarray);
			}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if (player != event.getPlayer()) return;
		
		if (player.hasPermission("idc.keepitems"))
		{
			Inventory replacement = player.getInventory();
			replacement.setContents(inventory.get(player.getName()));
			return;
		} 
		
		if (player.hasPermission("idc.percentageloss"))
		{
			Inventory replacement = player.getInventory();
			ItemStack[] newinv = new ItemStack[36];
			for (int i=0; i<items.get(player.getName()).length;i++) {
				newinv[i] = items.get(player.getName())[i];
			} 
			replacement.setContents(newinv);
		}
	}
	
	protected int generateRandomUnique(Integer listsize, String playername) {
	
		int random = 0;
		boolean randomVerify = false;
		List<Integer> list;
		
		if (randomUsed.get(playername) == null) {
			list = new ArrayList<Integer>();
		}
		else {
			list = randomUsed.get(playername);
		}
		
		while (!randomVerify) {
			random = (int) Math.floor((Math.random()*listsize));
			
			if (randomUsed.get(playername) == null) {
				list.add(random);
				randomUsed.put(playername, list);
				randomVerify = true;
			}
			else if (!randomUsed.get(playername).contains(random)) {
				list.add(random);
				randomUsed.put(playername, list);
				randomVerify = true;
			}
		}
		return random;
	}
}

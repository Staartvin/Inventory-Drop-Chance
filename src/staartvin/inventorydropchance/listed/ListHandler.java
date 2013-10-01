package staartvin.inventorydropchance.listed;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import staartvin.inventorydropchance.InventoryDropChance;

public class ListHandler {

	private InventoryDropChance plugin;
	
	public ListHandler(InventoryDropChance instance) {
		plugin = instance;
	}
	
	@SuppressWarnings("deprecation")
	public boolean isBlacklistedItem(ItemStack item, String group) {
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
	
	public boolean isWhitelistedItem(ItemStack item, String group) {
		List<String> whitelist = plugin.getWhitelistedItems(group);

		@SuppressWarnings("deprecation")
		int dataValue = item.getTypeId();
		@SuppressWarnings("deprecation")
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
}

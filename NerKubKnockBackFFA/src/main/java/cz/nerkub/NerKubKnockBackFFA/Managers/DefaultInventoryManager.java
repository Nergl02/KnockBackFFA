package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.Items.*;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DefaultInventoryManager {

	private final NerKubKnockBackFFA plugin;
	private final DatabaseManager databaseManager;
	private final KnockBackStickItem knockBackStickItem;
	private final PunchBowItem punchBowItem;
	private final LeatherTunicItem leatherTunicItem;
	private final BuildBlockItem buildBlockItem;

	public DefaultInventoryManager(NerKubKnockBackFFA plugin, DatabaseManager databaseManager) {
		this.plugin = plugin;
		this.databaseManager = databaseManager;
		this.knockBackStickItem = new KnockBackStickItem(plugin);
		this.punchBowItem = new PunchBowItem(plugin);
		this.leatherTunicItem = new LeatherTunicItem(plugin);
		this.buildBlockItem = new BuildBlockItem(plugin);
	}

	// 🌟 Nastavení inventáře pro hráče
	public void setPlayerInventory(Player player) {
		// Načtení uloženého inventáře z DB
		ItemStack[] mainInventory = databaseManager.loadMainInventory(player.getUniqueId());
		ItemStack[] hotbar = databaseManager.loadHotbar(player.getUniqueId());

		player.getInventory().clear(); // Vyčištění inventáře

		// 🔄 **Kontrola Extra Punch Bow eventu**
		boolean isExtraPunchBowActive = plugin.getCustomEventManager().isEventActive("ExtraPunchBow");

		for (int i = 0; i < 9; i++) {
			if (i == 0) {
				// ✅ KnockBack Stick se přidá pouze pokud není aktivní NoKnockBackStick event
				if (!plugin.getCustomEventManager().isEventActive("NoKnockBackStick")) {
					player.getInventory().setItem(i, hotbar[i] != null ? hotbar[i] : knockBackStickItem.createKnockBackStickItem());
				} else {
					player.getInventory().setItem(i, new ItemStack(Material.AIR));
				}
			} else if (i == 2) {
				if (isExtraPunchBowActive) {
					// ✅ Přidání Extra Punch Bow pokud je event aktivní
					ItemStack punchBow = new ItemStack(Material.BOW);
					ItemMeta meta = punchBow.getItemMeta();
					if (meta != null) {
						meta.addEnchant(Enchantment.ARROW_KNOCKBACK, 5, true);
						meta.setDisplayName(ChatColor.GOLD + "Extra Punch Bow");
						punchBow.setItemMeta(meta);
					}
					player.getInventory().setItem(i, punchBow);
				} else {
					// 🌟 **Po skončení eventu hráč dostane zpět normální luk**
					player.getInventory().setItem(i, hotbar[i] != null ? hotbar[i] : punchBowItem.createBowItem());
				}
			} else {
				player.getInventory().setItem(i, hotbar[i] != null ? hotbar[i] : new ItemStack(Material.AIR));
			}
		}

		// Nastavení hlavního inventáře (sloty 9–35)
		for (int i = 0; i < 27; i++) {
			player.getInventory().setItem(9 + i, (mainInventory[i] != null) ? mainInventory[i] : new ItemStack(Material.AIR));
		}
	}

	// 🌟 Výchozí inventář
	public void setDefaultInventory(Player player) {
		player.getInventory().clear();

		// 🔲 Výchozí hotbar
		ItemStack[] defaultHotbar = getDefaultHotbar();
		for (int i = 0; i < 9; i++) {
			player.getInventory().setItem(i, defaultHotbar[i]);
		}

		// 🔳 Výchozí hlavní inventář (sloty 9–35)
		ItemStack[] defaultMainInventory = getDefaultMainInventory();
		for (int i = 0; i < 27; i++) {
			player.getInventory().setItem(9 + i, defaultMainInventory[i]);
		}

		// 🥋 Výchozí brnění
		player.getInventory().setChestplate(leatherTunicItem.createLeatherTunicItem());
	}

	// ✅ Pomocná metoda pro kontrolu, zda inventář obsahuje nějaké itemy
	private boolean hasItems(ItemStack[] inventory) {
		if (inventory == null) return false;
		for (ItemStack item : inventory) {
			if (item != null && item.getType() != Material.AIR) {
				return true;
			}
		}
		return false;
	}

	// 🟩 Výchozí hlavní inventář (27 slotů)
	public ItemStack[] getDefaultMainInventory() {
		ItemStack[] mainInventory = new ItemStack[27];

		// Příklad výchozích itemů (můžeš přidat další)
		mainInventory[0] = new ItemStack(Material.ARROW, 1);

		return mainInventory;
	}

	// 🟨 Výchozí hotbar (9 slotů)
	public ItemStack[] getDefaultHotbar() {
		ItemStack[] hotbar = new ItemStack[9];

		// ✅ KnockBack Stick se přidá pouze pokud není aktivní NoKnockBackStick event
		if (!plugin.getCustomEventManager().isEventActive("NoKnockBackStick")) {
			hotbar[0] = knockBackStickItem.createKnockBackStickItem();
		} else {
			hotbar[0] = new ItemStack(Material.AIR);
		}

		hotbar[1] = new ItemStack(Material.ENDER_PEARL, 1);

		// ✅ Pokud je aktivní ExtraPunchBow event, přidá se luk s vyšším Punch
		if (plugin.getCustomEventManager().isEventActive("ExtraPunchBow")) {
			ItemStack punchBow = new ItemStack(Material.BOW);
			ItemMeta meta = punchBow.getItemMeta();
			if (meta != null) {
				meta.addEnchant(Enchantment.ARROW_KNOCKBACK, 5, true);
				punchBow.setItemMeta(meta);
			}
			hotbar[2] = punchBow;
		} else {
			hotbar[2] = punchBowItem.createBowItem();
		}

		hotbar[8] = buildBlockItem.createBuildBlockItem(plugin.getConfig().getInt("build-blocks.default-amount"));
		return hotbar;
	}

}

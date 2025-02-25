package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.Items.*;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
		// 🟢 Načtení hlavního inventáře a hotbaru z DB
		ItemStack[] mainInventory = databaseManager.loadMainInventory(player.getUniqueId());
		ItemStack[] hotbar = databaseManager.loadHotbar(player.getUniqueId());

		if (hasItems(mainInventory) || hasItems(hotbar)) {
			// Pokud existují uloženy předměty, načteme je
			player.getInventory().clear();

			// 🔲 Nastavení hlavního inventáře (sloty 9–35)
			for (int i = 0; i < 27; i++) {
				player.getInventory().setItem(9 + i, mainInventory[i] != null ? mainInventory[i] : new ItemStack(Material.AIR));
			}

			// 🔳 Nastavení hotbaru (sloty 0–8)
			for (int i = 0; i < 9; i++) {
				player.getInventory().setItem(i, hotbar[i] != null ? hotbar[i] : new ItemStack(Material.AIR));
			}

		} else {
			// Pokud neexistuje vlastní inventář, nastav výchozí
			setDefaultInventory(player);
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

		hotbar[0] = knockBackStickItem.createKnockBackStickItem();
		hotbar[1] = new ItemStack(Material.ENDER_PEARL, 1);
		hotbar[2] = punchBowItem.createBowItem();
		hotbar[8] = buildBlockItem.createBuildBlockItem(plugin.getConfig().getInt("build-blocks.default-amount"));

		return hotbar;
	}
}

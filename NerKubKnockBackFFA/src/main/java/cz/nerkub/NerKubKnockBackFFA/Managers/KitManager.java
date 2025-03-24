package cz.nerkub.NerKubKnockBackFFA.Managers;

import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class KitManager {

	private final NerKubKnockBackFFA plugin;
	private final DefaultInventoryManager defaultInventoryManager;

	public KitManager(NerKubKnockBackFFA plugin, DefaultInventoryManager defaultInventoryManager) {
		this.plugin = plugin;
		this.defaultInventoryManager = defaultInventoryManager;
	}

	public Set<String> getKits() {
		return plugin.getKits().getConfig().getConfigurationSection("kits").getKeys(false);
	}

	public boolean doesKitExist(String kitName) {
		return plugin.getKits().getConfig().contains("kits." + kitName);
	}

	public int getKitPrice(String kitName) {
		return plugin.getKits().getConfig().getInt("kits." + kitName + ".price", 0);
	}

	public String getKitDisplayItem(String kitName) {
		return plugin.getKits().getConfig().getString("kits." + kitName + ".display-item", "STONE");
	}

	public void applyKit(Player player, String kitName) {
		if (!doesKitExist(kitName)) return;

		// 🚫 Zabrání aplikaci kitu v safezóně
		if (plugin.getSafeZoneManager().isInSafeZone(player.getLocation(), plugin.getArenaManager().getArenaSpawn(plugin.getArenaManager().getCurrentArenaName()))) {
			return;
		}

		// ✅ Vymazání aktuálního inventáře
		player.getInventory().clear();

		// 📌 Načtení hlavních předmětů kitu a brnění
		ItemStack[] defaultMainInventory = defaultInventoryManager.getDefaultMainInventory();
		ItemStack[] mainInventory = getKitItems(kitName);
		ItemStack[] armor = getKitArmor(kitName);
		ItemStack[] hotbar = defaultInventoryManager.getDefaultHotbar(); // 🟨 Načteme výchozí hotbar

		// ✅ Přidání výchozích věcí do hotbaru (např. KnockBack Stick, Punch Bow, Ender Pearl)
		for (int i = 0; i < defaultMainInventory.length; i++) {
			if (mainInventory[i] == null || mainInventory[i].getType() == Material.AIR) {
				mainInventory[i] = defaultMainInventory[i]; // Pokud je slot prázdný, doplníme výchozí itemy (např. šíp)
			}
		}

		for (int i = 0; i < mainInventory.length; i++) {
			if (mainInventory[i] != null && mainInventory[i].getType() != Material.AIR) {
				player.getInventory().setItem(i + 9, mainInventory[i]); // Přidáváme věci do hlavního inv
			}
		}

		player.getInventory().setArmorContents(armor);


		// ✅ Automatické uložení výchozího kitu do databáze, pokud ho tam hráč nemá
		if (!plugin.getDatabaseManager().hasCustomKit(player.getUniqueId(), kitName)) {
			plugin.getDatabaseManager().saveCustomKit(player.getUniqueId(), kitName, mainInventory, hotbar, armor);
		}

	}


	public ItemStack[] getKitItems(String kitName) {
		ConfigurationSection kitSection = plugin.getKits().getConfig().getConfigurationSection("kits." + kitName + ".items");

		if (kitSection == null) {
			return new ItemStack[27]; // Vrátí prázdný inventář, pokud kit neobsahuje předměty
		}

		ItemStack[] items = new ItemStack[27]; // Max 27 slotů pro hlavní inventář
		int index = 0;

		for (String key : kitSection.getKeys(false)) {
			if (index >= 27) break; // Pokud by bylo víc než 27 itemů, zastavíme

			Material material = Material.getMaterial(kitSection.getString(key + ".material", "AIR").toUpperCase());
			int amount = kitSection.getInt(key + ".amount", 1);

			if (material != null && material != Material.AIR) {
				ItemStack item = new ItemStack(material, amount);

				// Zpracování efektů pro lektvary
				if (material == Material.POTION || material == Material.SPLASH_POTION) {
					if (kitSection.contains(key + ".effects")) {
						PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

						ConfigurationSection effectsSection = kitSection.getConfigurationSection(key + ".effects");
						for (String effectKey : effectsSection.getKeys(false)) {
							try {
								PotionEffectType effectType = PotionEffectType.getByName(effectKey.toUpperCase());
								if (effectType != null) {
									int duration = effectsSection.getInt(effectKey, 600); // Default 30 sekund
									potionMeta.addCustomEffect(new PotionEffect(effectType, duration, 1), true); // Přidání efektu
								}
							} catch (Exception e) {
								if (plugin.getConfig().getBoolean("debug")) {
									Bukkit.getLogger().warning("⚠️ Neplatný efekt '" + effectKey + "' pro lektvar v kitu '" + kitName + "'");
								}
							}
						}

						// Kontrola pro splash lektvar
						if (kitSection.contains(key + ".splash")) {
							boolean splash = kitSection.getBoolean(key + ".splash", false);
							if (splash) {
								item.setType(Material.SPLASH_POTION);
							}
						}

						item.setItemMeta(potionMeta);
					}
				} else {
					// Zkontrolujeme, zda má item enchantments
					if (kitSection.contains(key + ".enchantments")) {
						ConfigurationSection enchantsSection = kitSection.getConfigurationSection(key + ".enchantments");
						for (String enchantKey : enchantsSection.getKeys(false)) {
							try {
								item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.getByName(enchantKey.toUpperCase()), enchantsSection.getInt(enchantKey));
							} catch (Exception e) {
								if (plugin.getConfig().getBoolean("debug")) {
									Bukkit.getLogger().warning("⚠️ Neplatný enchantment '" + enchantKey + "' pro item '" + material + "' v kitu '" + kitName + "'");
								}
							}
						}
					}
				}

				// Přidáme item do inventáře
				items[index++] = item;
			}
		}

		return items;
	}


	public ItemStack[] getKitArmor(String kitName) {
		ConfigurationSection kitSection = plugin.getKits().getConfig().getConfigurationSection("kits." + kitName + ".armor");

		if (kitSection == null) {
			return new ItemStack[4]; // Vrátí prázdné brnění, pokud kit nemá definované brnění
		}

		ItemStack[] armor = new ItemStack[4];

		for (String key : kitSection.getKeys(false)) {
			Material material = Material.matchMaterial(kitSection.getString(key + ".material", "AIR").toUpperCase());
			int slot = getArmorSlotFromType(material);

			if (slot != -1 && material != Material.AIR) {
				ItemStack item = new ItemStack(material);

				// Načtení enchantmentů
				ConfigurationSection enchantmentsSection = kitSection.getConfigurationSection(key + ".enchantments");
				if (enchantmentsSection != null) {
					for (String enchantKey : enchantmentsSection.getKeys(false)) {
						try {
							item.addUnsafeEnchantment(
									org.bukkit.enchantments.Enchantment.getByName(enchantKey.toUpperCase()),
									enchantmentsSection.getInt(enchantKey)
							);
						} catch (Exception e) {
							// TODO: DEBUG PŘIDAT
							Bukkit.getLogger().warning("⚠ Chyba při aplikaci enchantmentu " + enchantKey + " pro " + material.name());
						}
					}
				}

				armor[slot] = item;
			}
		}

		return armor;
	}

	/**
	 * Vrátí index slotu pro brnění podle jeho typu:
	 * - 0: boty
	 * - 1: kalhoty
	 * - 2: chestplate
	 * - 3: helma
	 */
	private int getArmorSlotFromType(Material material) {
		if (material.name().endsWith("_BOOTS")) return 0;
		if (material.name().endsWith("_LEGGINGS")) return 1;
		if (material.name().endsWith("_CHESTPLATE")) return 2;
		if (material.name().endsWith("_HELMET")) return 3;
		return -1;
	}



}

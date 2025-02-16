package cz.nerkub.NerKubKnockBackFFA.Listeners;

import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.NerKubKnockBackFFA;
import org.bukkit.*;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class FireBallLauncherListener implements Listener {

	private final NerKubKnockBackFFA plugin;
	private final DamagerMap damagerMap;
	private final double power;
	private final double explosionRadius;

	public FireBallLauncherListener(NerKubKnockBackFFA plugin, DamagerMap damagerMap) {
		this.plugin = plugin;
		this.damagerMap = damagerMap;
		this.power = plugin.getItems().getConfig().getDouble("fireball-launcher.power", 1.5); // Síla knockbacku
		this.explosionRadius = plugin.getItems().getConfig().getDouble("fireball-launcher.explosion-radius", 4.0); // Poloměr knockbacku
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();

		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (item.getType() == Material.BLAZE_ROD && item.hasItemMeta()) {

				// Debug zpráva
				player.sendMessage(ChatColor.GREEN + "🔥 Fireball Launched!");

				// **Vytvoření fireballu a zpomalení rychlosti**
				Fireball fireball = player.launchProjectile(Fireball.class);
				fireball.setVelocity(player.getEyeLocation().getDirection().multiply(0.6)); // **Zpomalená rychlost**
				fireball.setIsIncendiary(false);
				fireball.setYield(0); // Nezničí bloky
				fireball.setGravity(false);

				player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);

				// Odebrání jednoho kusu z inventáře
				if (item.getAmount() > 1) {
					item.setAmount(item.getAmount() - 1);
				} else {
					player.getInventory().setItemInMainHand(null);
				}
			}
		}
	}

	@EventHandler
	public void onFireballHit(ProjectileHitEvent event) {
		if (!(event.getEntity() instanceof Fireball fireball)) return;
		if (!(fireball.getShooter() instanceof Player shooter)) return;

		// 💥 Vizuální exploze (bez ničení bloků)
		Location explosionLocation = fireball.getLocation();
		fireball.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, explosionLocation, 2);
		fireball.getWorld().playSound(explosionLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);

		// 🔄 Najdi hráče v okruhu exploze
		for (Player victim : Bukkit.getOnlinePlayers()) {
			if (victim.equals(shooter)) continue; // Neodhazuj střelce

			double distance = victim.getLocation().distance(explosionLocation);
			if (distance > explosionRadius) continue; // Hráč je mimo dosah exploze

			// 🔹 Knockback síla (stejná pro všechny v okruhu)
			Vector knockbackVector = victim.getLocation().toVector()
					.subtract(explosionLocation.toVector()) // Směr od exploze
					.normalize()
					.multiply(power) // Konstanta knockbacku
					.setY(0.3 * power); // Výhoz nahoru

			victim.setVelocity(knockbackVector);
			damagerMap.putDamager(victim.getUniqueId(), shooter.getUniqueId());
		}

		fireball.remove(); // Zničíme fireball po dopadu
	}

	@EventHandler
	public void onFireballDirectHit(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player victim)) return;
		if (!(event.getDamager() instanceof Fireball fireball)) return;
		if (!(fireball.getShooter() instanceof Player shooter)) return;

		// 🚀 Extra knockback pro přímý zásah
		Vector knockbackVector = victim.getLocation().toVector()
				.subtract(fireball.getLocation().toVector()) // Směr od střely
				.normalize()
				.multiply(power * 1.2) // O něco silnější knockback pro přímý zásah
				.setY(0.5 * power); // Výhoz nahoru

		// 🎯 Asynchronní knockback po 1 ticku
		Bukkit.getScheduler().runTaskLater(plugin, () -> victim.setVelocity(knockbackVector), 1L);

		fireball.remove();
		event.setDamage(0); // Nezpůsobí poškození
		damagerMap.putDamager(victim.getUniqueId(), shooter.getUniqueId());
	}






}

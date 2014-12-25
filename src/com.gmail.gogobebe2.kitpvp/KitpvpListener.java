package com.gmail.gogobebe2.kitpvp;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KitpvpListener implements Listener {

	JavaPlugin plugin;

	public KitpvpListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onKill(PlayerDeathEvent e) {
		// Just incase a spider or hostile mob kills. Cus it'll just display an
		// error which is annoying:
		String killed = e.getEntity().getName();
		if (e.getEntity().getKiller() instanceof Player) {
			String killer = e.getEntity().getKiller().getName();
			e.setDeathMessage(ChatColor.AQUA + killed + ChatColor.YELLOW
					+ " has been killed by " + ChatColor.AQUA + killer);
			e.getEntity()
					.getKiller()
					.sendMessage(
							ChatColor.DARK_GREEN
									+ "$1 has been added to your account");
			Kitpvp.econ.depositPlayer(e.getEntity().getKiller(), 1);
			e.getEntity()
					.getKiller()
					.sendMessage(
							ChatColor.GOLD
									+ "A strength and regen kill bonus has been given for 20 seconds!");
			e.getEntity().getKiller().setHealth(20);
			e.getEntity()
					.getKiller()
					.addPotionEffect(
							new PotionEffect(PotionEffectType.INCREASE_DAMAGE,
									20 * 20, 2));
			e.getEntity()
					.getKiller()
					.addPotionEffect(
							new PotionEffect(PotionEffectType.REGENERATION,
									20 * 20, 2));
			return;
		}
		e.setDeathMessage(ChatColor.AQUA + killed + ChatColor.YELLOW
				+ " has been killed!");
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		if (!(action.equals(Action.RIGHT_CLICK_AIR) || action
				.equals(Action.RIGHT_CLICK_BLOCK))) {
			return;
		}
		if (player.getItemInHand().getType() != Material.MUSHROOM_SOUP) {
			return;
		}
		if (player.getHealth() >= player.getMaxHealth()) {
			return;
		}
		double amount = 4.5;

		player.setHealth(player.getHealth() + amount > player.getMaxHealth() ? player
				.getMaxHealth() : player.getHealth() + amount);
		event.getItem().setType(Material.BOWL);
		event.setCancelled(true);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (!plugin.getConfig().getBoolean(
				"Enable drop/pick of other items (not just soup)")) {
			if (event.getItem().getItemStack().getType() != Material.MUSHROOM_SOUP) {
				event.setCancelled(true);
			}
		}

	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		if (!plugin.getConfig().getBoolean(
				"Enable drop/pick of other items (not just soup)")) {
			if ((event.getItemDrop().getItemStack().getType() != Material.MUSHROOM_SOUP)
					&& (event.getItemDrop().getItemStack().getType() != Material.BOWL)) {
				event.setCancelled(true);
			}
		}
	}
}

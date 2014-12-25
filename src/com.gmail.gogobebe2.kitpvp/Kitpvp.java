package com.gmail.gogobebe2.kitpvp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kitpvp extends JavaPlugin {

	public static Economy econ = null;
	private static final Logger log = Logger.getLogger("Minecraft");

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			econ = economyProvider.getProvider();
		}

		return (econ != null);
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(new KitpvpListener(this),
				this);

		if (!setupEconomy()) {
			log.severe(String.format(
					"[%s] - Disabled due to no Vault dependency found!",
					getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		try {
			saveConfig();
			setupConfig(getConfig());
			saveConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {

		if (commandLabel.equalsIgnoreCase("soup")) {
			if (sender instanceof Player) {
				final Player player = (Player) sender;
				final double originalHealth = player.getHealth();
				player.setHealth(1);
				player.addPotionEffect(new PotionEffect(
						PotionEffectType.CONFUSION, 20 * 10, 0));
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
						20 * 10, 0));
				player.addPotionEffect(new PotionEffect(
						PotionEffectType.WEAKNESS, 20 * 10, 0));
				player.addPotionEffect(new PotionEffect(
						PotionEffectType.BLINDNESS, 20 * 10, 0));
				player.sendMessage(ChatColor.DARK_RED
						+ "You are vulnerable for 10 seconds...");
				final ItemStack soup = new ItemStack(Material.MUSHROOM_SOUP, 1);
				Bukkit.getScheduler().scheduleSyncDelayedTask(this,
						new Runnable() {
							public void run() {
								player.setHealth(originalHealth);
								player.sendMessage(ChatColor.DARK_GREEN
										+ "Vulnerability removed!");
								player.sendMessage(ChatColor.GREEN
										+ "Soup refilled!");
								while (player.getInventory().firstEmpty() != -1) {
									player.getInventory().addItem(soup);
								}
								player.updateInventory();
							}
						}, 20 * 10L);

			}
			return true;
		}

		if (commandLabel.equalsIgnoreCase("kit")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length == 0) {
					String serverName = getConfig().getString("Name of your server displayed when players type /kit", "Server Name - set this in config.");
					player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
							+ serverName);
					String[] classes = getConfig().getString("Kits.Names")
							.split(",");
					for (String s : classes) {
						if (s != null) {
							double price = getConfig().getDouble(
									"Kits." + s + ".Price");
							player.sendMessage(ChatColor.GRAY + "- "
									+ ChatColor.LIGHT_PURPLE + s
									+ ChatColor.GRAY + " " + ChatColor.GREEN
									+ "$" + price);
						}
					}
					return true;
				} else {

					for (String s : getConfig().getConfigurationSection("Kits")
							.getKeys(false)) {

						if (args[0].equalsIgnoreCase(s)) {

							double price = getConfig().getDouble(
									"Kits." + s + ".Price");

							EconomyResponse r = econ.withdrawPlayer(
									player.getName(), price);

							if (r.transactionSuccess()) {

								player.getInventory().clear();
								player.sendMessage(ChatColor.DARK_GREEN + s
										+ " selected");
								player.getInventory().setHelmet(
										new ItemStack(Material.AIR));
								player.getInventory().setChestplate(
										new ItemStack(Material.AIR));
								player.getInventory().setLeggings(
										new ItemStack(Material.AIR));
								player.getInventory().setBoots(
										new ItemStack(Material.AIR));

								try {
									String items = getConfig().getString(
											"Kits." + s + ".Items");
									String[] indiItems = items.split(",");

									for (String s1 : indiItems) {
										String[] itemAmounts = s1.split("-");
										ItemStack item = new ItemStack(
												Integer.valueOf(itemAmounts[0]),
												Integer.valueOf(itemAmounts[1]));

										player.getInventory().addItem(item);
									}
									player.updateInventory();

								} catch (Exception e) {
									e.printStackTrace();
								}
								player.performCommand("soup");

							}

							else {
								player.sendMessage(ChatColor.RED
										+ "You do not have enough money for that kit. Type "
										+ ChatColor.DARK_AQUA
										+ "/money"
										+ ChatColor.RED
										+ " to see how much money you have and "
										+ ChatColor.DARK_AQUA + "/kit"
										+ ChatColor.RED + " to see kit prices");
							}

						}

					}
				}
				return true;
			}

		}
		return true;
	}

	private void setupConfig(FileConfiguration config) throws IOException {
		if (!new File(getDataFolder(), "RESET.FILE").exists()) {
			new File(getDataFolder(), "RESET.FILE").createNewFile();
			
			config.set("Name of your server displayed when players type /kit", "Server Name - set this in config.");
			config.set("Enable drop/pick of other items (not just soup)", false);
			config.set("Kits.Warrior.Items", "276-1,306-1,307-1,308-1,309-1");
			config.set("Kits.Warrior.Price", 2);
			config.set("Kits.ExampleKit1.Items", "2-64,1-1,5-4");
			config.set("Kits.ExampleKit1.Price", 4);
			config.set("Kits.ExampleKit2.Items", "4-64,1-1,5-4");
			config.set("Kits.ExampleKit2.Price", 1);
			config.set("Kits.ExampleKit3.Items", "4-64,4-1,4-4");
			config.set("Kits.ExampleKit3.Price", 0);
			config.set("Kits.Names", "Warrior,ExampleKit1,ExampleKit2,ExampleKit3");
		}

	}

	public void onDisable() {

	}

}

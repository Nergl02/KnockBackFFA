package cz.nerkub.NerKubKnockBackFFA;

import cz.nerkub.NerKubKnockBackFFA.CustomFiles.CustomConfig;
import cz.nerkub.NerKubKnockBackFFA.HashMaps.DamagerMap;
import cz.nerkub.NerKubKnockBackFFA.Items.KnockBackStickItem;
import cz.nerkub.NerKubKnockBackFFA.Items.PunchBowItem;
import cz.nerkub.NerKubKnockBackFFA.Listeners.*;
import cz.nerkub.NerKubKnockBackFFA.Managers.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class NerKubKnockBackFFA extends JavaPlugin {

	private static NerKubKnockBackFFA plugin;

	private CustomConfig messages;
	private CustomConfig arenas;
	private CustomConfig items;

	private final DamagerMap damagerMap = new DamagerMap(); //Nejlepší řešení místo getInstance();
	private final KnockBackStickItem knockBackStickItem = new KnockBackStickItem(this);
	private final PunchBowItem punchBowItem = new PunchBowItem(this);


	@Override
	public void onEnable() {
		// Plugin startup logic

		plugin = this;

		Bukkit.getConsoleSender().sendMessage("");
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|\\   |  | /	&aPlugin: &6NerKub KnockBackFFA"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3| \\  |  |/	&aVersion: &bv1.0.0"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|  \\ |  |\\	&aAuthor: &3NerKub Studios"));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3|   \\|  | \\	&aPremium: &bThis plugin is a premium resource."));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3| Visit our Discord for more! &ahttps://discord.gg/YXm26egK6g"));
		Bukkit.getConsoleSender().sendMessage("");

		getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
		getServer().getPluginManager().registerEvents(new FallDamageListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerDamageListener(this, damagerMap), this);
		getServer().getPluginManager().registerEvents(new PlayerMoveListener(this, new Random(), damagerMap), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, knockBackStickItem, punchBowItem), this);
		getServer().getPluginManager().registerEvents(new PlayerSwapperListener(this, damagerMap), this);
		getServer().getPluginManager().registerEvents(new DropItemListener(), this);
		getServer().getPluginManager().registerEvents(new CancelBlockDestroyListener(this), this);

		getCommand("knbffa").setExecutor(new CommandManager(this));


		saveDefaultConfig();
		reloadConfig();

		// Custom ConfigFiles
		messages = new CustomConfig("messages", "messages.yml", this); // Directory can be "" to create file in the main plugin folder
		messages.saveConfig();
		arenas = new CustomConfig("arenas", "arenas.yml", this); // Directory can be "" to create file in the main plugin folder
		arenas.saveConfig();
		items = new CustomConfig("items", "items.yml", this);
		items.saveConfig();

	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}

	public static NerKubKnockBackFFA getPlugin() {
		return plugin;
	}

	public CustomConfig getMessages() {
		return messages;
	}

	public CustomConfig getArenas() {
		return arenas;
	}

	public CustomConfig getItems() {
		return items;
	}

}

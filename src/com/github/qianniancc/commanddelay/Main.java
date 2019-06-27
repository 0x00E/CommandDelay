package com.github.qianniancc.commanddelay;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {
	public String prefix = "§e[CommandDelay]";
	public ConfigurationSection cdlist = getConfig().getConfigurationSection("cdlist");
	public ConfigurationSection pcdlist = getConfig().getConfigurationSection("pcdlist");
	public File file = new File(getDataFolder(), "config.yml");

	public void onEnable() {

		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		if (!file.exists()) {
			saveDefaultConfig();
		}
		reloadConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
		if (cmd.getName().equals("cdreload")) {
			if ((!sender.isOp()) && (!sender.hasPermission("cd.reload")) && (!sender.hasPermission("cd.*"))) {
				sender.sendMessage(prefix + "§c§l你没有权限");
				return false;
			}

			if (!getDataFolder().exists()) {
				getDataFolder().mkdir();
			}
			if (!file.exists()) {
				saveDefaultConfig();
			}
			reloadConfig();
			sender.sendMessage(prefix + "§a§l重置成功");
			return true;
		}
		return false;

	}

	@EventHandler
	public void onCmd(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		if (getConfig().getBoolean("exempt")) {
			if ((!p.isOp()) && (!p.hasPermission("cd.exempt")) && (!p.hasPermission("cd.*"))) {

			} else {
				return;
			}
		}

		String cmd = e.getMessage().trim();
		cmd = cmd.toLowerCase();
		if (cmd.startsWith("/")) {
			cmd = cmd.substring(1).trim();
		}
		final String fcmd = cmd;
		int firstSpace = cmd.indexOf(' ');
		if (firstSpace < 0) {
			firstSpace = cmd.length();
		}
		cmd = cmd.substring(0, firstSpace);
		if (getConfig().getInt("all") != 0) {
			int delay = getConfig().getInt("all");
			p.sendMessage("本指令已延迟，将在" + delay + "秒后自动执行");
			e.setCancelled(true);
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "sudo " + p.getName() + " " + fcmd);
					p.sendMessage("/" + fcmd + "已执行");

				}
			}.runTaskLaterAsynchronously(this, 20 * delay);
			return;
		}
		for (String str : cdlist.getKeys(false)) {
			if (!str.contains(".")) {
				if (str.equals(fcmd)) {
					int delay = cdlist.getInt(str);
					p.sendMessage("本指令已延迟，将在" + delay + "秒后自动执行");
					e.setCancelled(true);
					new BukkitRunnable() {
						@Override
						public void run() {
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
									"sudo " + p.getName() + " " + fcmd);
							p.sendMessage("/" + fcmd + "已执行");
						}
					}.runTaskLaterAsynchronously(this, 20 * delay);
					return;
				}
			}
		}
		for (String str : pcdlist.getKeys(false)) {
			if (!str.contains(".")) {
				if (fcmd.startsWith(str)) {
					int delay = pcdlist.getInt(str);
					p.sendMessage("本指令已延迟，将在" + delay + "秒后自动执行");
					e.setCancelled(true);
					new BukkitRunnable() {
						@Override
						public void run() {
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
									"sudo " + p.getName() + " " + fcmd);
							p.sendMessage("/" + fcmd + "已执行");
						}
					}.runTaskLaterAsynchronously(this, 20 * delay);
					return;
				}
			}
		}
	}
}

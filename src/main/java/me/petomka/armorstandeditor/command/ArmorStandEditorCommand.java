package me.petomka.armorstandeditor.command;

import com.google.common.collect.Maps;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.inventory.InventoryMenu;
import me.petomka.armorstandeditor.inventory.MenuItem;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ArmorStandEditorCommand implements TabExecutor {

	private Map<String, String> subCommands = Main.mapOf(Maps::newHashMap,
			"reload", "",
			"off", "<Player> {<Player>}",
			"on", "<Player> {<Player>}");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			onReload(sender);
			return true;
		}

		if (args.length >= 1 && args[0].equalsIgnoreCase("off")) {
			String[] remainingArgs = new String[args.length - 1];
			if (remainingArgs.length > 0) {
				System.arraycopy(args, 1, remainingArgs, 0, remainingArgs.length);
			}
			onToggle(sender, label, remainingArgs, false);
			return true;
		}

		if (args.length >= 1 && args[0].equalsIgnoreCase("on")) {
			String[] remainingArgs = new String[args.length - 1];
			if (remainingArgs.length > 0) {
				System.arraycopy(args, 1, remainingArgs, 0, remainingArgs.length);
			}
			onToggle(sender, label, remainingArgs, true);
			return true;
		}

		onSyntax(sender, label);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
		String lastArg = args[args.length - 1];

		if (args.length == 1) {
			return Main.copySubstringMatches(subCommands.keySet(), lastArg);
		}

		return Main.copySubstringMatches(Bukkit.getOnlinePlayers().stream()
				.map(Player::getName)
				.collect(Collectors.toList()), lastArg);
	}

	private void onSyntax(CommandSender sender, String label) {
		Main main = Main.getInstance();
		PluginDescriptionFile description = main.getDescription();
		sender.sendMessage(ChatColor.GOLD + description.getName() + " version " + description.getVersion());
		sender.sendMessage(ChatColor.GOLD + "by " + String.join(", ", description.getAuthors()));
		sender.sendMessage(ChatColor.GOLD + description.getWebsite());

		if (!sender.hasPermission(Main.getInstance().getDefaultConfig().getEditPermission())) {
			return;
		}

		sender.sendMessage(ChatColor.RED + "Usage:");
		subCommands.forEach((cmd, syntax) -> sender.sendMessage(ChatColor.RED + "/" + label + " " + cmd + " " + syntax));
	}

	private void onReload(CommandSender sender) {
		Main main = Main.getInstance();
		if (!sender.hasPermission(main.getDefaultConfig().getReloadPermission())) {
			sender.sendMessage(Main.colorString(main.getMessages().getNoPermissionMessage()));
			return;
		}
		InventoryMenu.reloadItemNames();
		MenuItem.reloadMenuItems();
		try {
			main.getMessages().reload();
			sender.sendMessage(Main.colorString(main.getMessages().getMessagesSuccessfullyReloaded()));
		} catch (InvalidConfigurationException e) {
			main.getLogger().log(Level.SEVERE, "Error trying to reload messages.yml", e);
			sender.sendMessage(Main.colorString(main.getMessages().getMessagesReloadError()));
		}
		try {
			main.getDefaultConfig().reload();
			sender.sendMessage(Main.colorString(main.getMessages().getConfigSuccessfullyReloaded()));
		} catch (InvalidConfigurationException e) {
			main.getLogger().log(Level.SEVERE, "Error trying to reload config.yml", e);
			sender.sendMessage(Main.colorString(main.getMessages().getConfigReloadError()));
		}
	}

	private void onToggle(CommandSender sender, String alias, String[] args, boolean on) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Use: /" + alias + " <Player> {<Player>}");
				return;
			}
			onToggle(sender, alias, new String[]{sender.getName()}, on);
			return;
		}

		if (!sender.hasPermission(Main.getInstance().getDefaultConfig().getToggleOthersPermission())) {
			if (args.length != 1 || !args[0].equalsIgnoreCase(sender.getName())) {
				sender.sendMessage(Main.colorString(Main.getInstance().getMessages().getNoPermissionMessage()));
				return;
			}
		}

		String msg;
		if (on) {
			msg = Main.getInstance().getMessages().getToggle_on();
		} else {
			msg = Main.getInstance().getMessages().getToggle_off();
		}

		for (String name : args) {
			Player player = Bukkit.getPlayerExact(name);
			if (player == null) {
				sender.sendMessage(Main.colorString(Main.getInstance().getMessages()
						.getPlayerNotFound().replace("{name}", name)));
				return;
			}
			if (!on) {
				Main.getInstance().getDisabledPlayersStorage().getDisabledPlayers().add(player.getUniqueId());
			} else {
				Main.getInstance().getDisabledPlayersStorage().getDisabledPlayers().remove(player.getUniqueId());
			}
			player.sendMessage(Main.colorString(msg.replace("{name}", player.getName())));
		}
	}

}

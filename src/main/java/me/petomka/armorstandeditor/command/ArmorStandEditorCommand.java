package me.petomka.armorstandeditor.command;

import com.google.common.collect.Maps;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.inventory.InventoryMenu;
import me.petomka.armorstandeditor.inventory.MenuItem;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ArmorStandEditorCommand implements TabExecutor {

	private Map<String, String> subCommands = Main.mapOf(Maps::newHashMap,
			"reload", "");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			onReload(sender);
			return true;
		}

		onSyntax(sender, label);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
		String lastArg = args[args.length - 1];
		if (args.length == 1 && commandSender.hasPermission(Main.getInstance().getDefaultConfig().getReloadPermission())) {
			return Main.copySubstringMatches(subCommands.keySet(), lastArg);
		}
		return Collections.emptyList();
	}

	private void onSyntax(CommandSender sender, String label) {
		subCommands.forEach((cmd, syntax) -> sender.sendMessage(ChatColor.RED + "/" + label + " " + cmd + " " + syntax));
	}

	private void onReload(CommandSender sender) {
		Main main = Main.getInstance();
		if(!sender.hasPermission(main.getDefaultConfig().getReloadPermission())) {
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

}

package me.petomka.armorstandeditor.command;

import com.google.common.collect.Maps;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.config.Messages;
import me.petomka.armorstandeditor.handler.AttachedCommandsHandler;
import me.petomka.armorstandeditor.inventory.InventoryMenu;
import me.petomka.armorstandeditor.inventory.MenuItem;
import me.petomka.armorstandeditor.util.ExceptionToNull;
import me.petomka.armorstandeditor.util.Menu;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ArmorStandEditorCommand implements TabExecutor {

	private final Map<String, String> subCommands = Main.mapOf(LinkedHashMap::new,
			"reload", "",
			"off", "<Player> {<Player>}",
			"on", "<Player> {<Player>}",
			"commands", "<UUID> (add <Command>)|(remove <index>)|removeall|(set <index> command)|list");

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

		//asa commands <uuid> (add|(set <id>) <command>)|(remove <id>)
		//asa commands uuid actions
		if (args.length >= 3 && args[0].equalsIgnoreCase("commands")) {
			if (!sender.hasPermission(Main.getInstance().getDefaultConfig().getAttachCommandsPermission())) {
				sender.sendMessage(Main.getInstance().getMessages().getNoPermissionMessage());
				return true;
			}
			String[] remainingArgs = new String[args.length - 2];
			System.arraycopy(args, 2, remainingArgs, 0, remainingArgs.length);
			UUID uuid = ExceptionToNull.get(() -> UUID.fromString(args[1]));
			if (uuid == null) {
				onSyntax(sender, label);
				return true;
			}
			Entity entity = Bukkit.getServer().getEntity(uuid);
			if (!(entity instanceof ArmorStand)) {
				sender.sendMessage(Main.colorString(Main.getInstance().getMessages().getEntityIsNotArmorStand()));
				return true;
			}
			if (!onCommands(sender, label, (ArmorStand) entity, remainingArgs)) {
				onSyntax(sender, label);
			}
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

		if (args[0].equalsIgnoreCase("commands")) {
			return Collections.emptyList(); //Noone should type this command by hand...
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

	private boolean onCommands(CommandSender sender, String label, ArmorStand armorStand, String[] args) {
		if (sender instanceof Player && Main.getInstance().isInteractCancelled((Player) sender,
				Collections.singleton(armorStand), new Vector(0, 0, 0))) {
			return true; // Message sent by plugin that cancelled..?
		}
		if (args[0].equalsIgnoreCase("add") && args.length > 1) {
			String cmd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			AttachedCommandsHandler.getInstance().appendCommand(armorStand.getUniqueId(), cmd);
			sender.sendMessage(Main.colorString(Main.getInstance().getMessages().getCommands_addedCommand()));
			return true;
		}
		if (args[0].equalsIgnoreCase("remove") && args.length == 2) {
			Integer index = ExceptionToNull.get(() -> Integer.parseInt(args[1]));
			if (index == null) {
				return false;
			}
			Messages messages = Main.getInstance().getMessages();
			if (AttachedCommandsHandler.getInstance().removeCommandByIndex(armorStand.getUniqueId(), index)) {
				sender.sendMessage(Main.colorString(messages.getCommands_removedCommand()));
			} else {
				sender.sendMessage(Main.colorString(messages.getCommands_invalidCommand()));
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("set") && args.length >= 3) {
			Integer index = ExceptionToNull.get(() -> Integer.parseInt(args[1]));
			if (index == null) { //set 1 a
				return false;
			}
			String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
			Messages messages = Main.getInstance().getMessages();
			if (AttachedCommandsHandler.getInstance().setCommandByIndex(armorStand.getUniqueId(), index, cmd)) {
				sender.sendMessage(Main.colorString(messages.getCommands_updatedCommand()));
			} else {
				sender.sendMessage(Main.colorString(messages.getCommands_invalidCommand()));
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("insert") && args.length >= 3) {
			Integer index = ExceptionToNull.get(() -> Integer.parseInt(args[1]));
			if (index == null) { //insert 1 a
				return false;
			}
			String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
			Messages messages = Main.getInstance().getMessages();
			if (AttachedCommandsHandler.getInstance().insertCommandByIndex(armorStand.getUniqueId(), index, cmd)) {
				sender.sendMessage(Main.colorString(messages.getCommands_insertedCommand()));
			} else {
				sender.sendMessage(Main.colorString(messages.getCommands_invalidCommand()));
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("removeall") && args.length == 1) {
			AttachedCommandsHandler.getInstance().removeAll(armorStand.getUniqueId());
			sender.sendMessage(Main.colorString(Main.getInstance().getMessages().getCommands_removedAll()));
			return true;
		}
		if (args[0].equalsIgnoreCase("list")) {
			sendActionList(sender, armorStand, null, false);
			return true;
		}
		if (args[0].equalsIgnoreCase("actions")) {
			if (args.length == 1) {
				onActionsOverview(sender, armorStand);
				return true;
			}
			sendActionList(sender, armorStand, args[1], args[1].equalsIgnoreCase("set"));
			return true;
		}
		return false;
	}

	private void onActionsOverview(CommandSender sender, ArmorStand armorStand) {
		Messages messages = Main.getInstance().getMessages();
		Menu menu = new Menu(Main.colorString(messages.getCommands_actions_head()));
		menu.addSub(
				new Menu(Main.colorString(messages.getCommands_actions_add()), ClickEvent.Action.SUGGEST_COMMAND,
						"/asa commands " + armorStand.getUniqueId() + " add ")
		);
		menu.addSub(
				new Menu(Main.colorString(messages.getCommands_actions_remove()), ClickEvent.Action.RUN_COMMAND,
						"/asa commands " + armorStand.getUniqueId() + " actions remove")
		);
		menu.addSub(
				new Menu(Main.colorString(messages.getCommands_actions_update()), ClickEvent.Action.RUN_COMMAND,
						"/asa commands " + armorStand.getUniqueId() + " actions set")
		);
		menu.addSub(
				new Menu(Main.colorString(messages.getCommands_insert()), ClickEvent.Action.RUN_COMMAND,
						"/asa commands " + armorStand.getUniqueId() + " actions insert")
		);
		menu.addSub(
				new Menu(Main.colorString(messages.getCommands_actions_list()), ClickEvent.Action.RUN_COMMAND,
						"/asa commands " + armorStand.getUniqueId() + " list")
		);
		menu.addSub(
				new Menu(Main.colorString(messages.getCommands_actions_removeAll()), ClickEvent.Action.SUGGEST_COMMAND,
						"/asa commands " + armorStand.getUniqueId() + " removeall")
		);
		menu.send(sender);
	}

	private void sendActionList(CommandSender sender, ArmorStand armorStand, String action, boolean appendOld) {
		List<String> commands = AttachedCommandsHandler.getInstance().getAttachedCommands(armorStand.getUniqueId());
		Messages messages = Main.getInstance().getMessages();
		String armorStandName = armorStand.getCustomName() != null ? armorStand.getCustomName() : armorStand.getUniqueId().toString();
		Menu list = new Menu(Main.colorString(messages.getCommands_list_head()).replace("<armorstand>", armorStandName));

		if (commands.isEmpty()) {
			list.addSub(new Menu(Main.colorString(messages.getCommands_list_none())));
			list.send(sender);
			return;
		}

		int index = 0;
		for (String command : commands) {
			String entry = Main.colorString(messages.getCommands_list_entry().replace("<index>", "" + index)
					.replace("<command>", command));
			String actionString = "/asa commands " + armorStand.getUniqueId() + " " + action + " " + index + " ";
			if (appendOld) {
				actionString += commands.get(index);
			}
			Menu sub = action == null ? new Menu(entry) : new Menu(entry, ClickEvent.Action.SUGGEST_COMMAND, actionString);
			list.addSub(sub);
			index++;
		}

		list.send(sender);
	}

}

package me.petomka.armorstandeditor.handler;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.config.CommandStorage;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.*;
import java.util.logging.Level;

public class AttachedCommandsHandler {

	@Getter
	private static AttachedCommandsHandler instance;

	public static void init(Main plugin) {
		if (instance == null) {
			instance = new AttachedCommandsHandler(plugin);
		}
	}

	private final Main plugin;


	private AttachedCommandsHandler(Main plugin) {
		this.plugin = plugin;
	}

	public List<String> getAttachedCommands(UUID armorStand) {
		CommandStorage storage = plugin.getCommandStorage();
		return ImmutableList.copyOf(storage.getMappedCommands()
				.getOrDefault(armorStand.toString(), Collections.emptyList()));
	}

	public void setAttachedCommands(UUID armorStand, List<String> commands) {
		if (commands.isEmpty()) {
			removeAll(armorStand);
			return;
		}
		CommandStorage storage = plugin.getCommandStorage();
		List<String> copy = ImmutableList.copyOf(commands);
		storage.getMappedCommands().put(armorStand.toString(), copy);
		try {
			storage.save();
		} catch (InvalidConfigurationException e) {
			plugin.getLogger().log(Level.SEVERE, "Error saving as-commands.yml", e);
		}
	}

	public void removeAll(UUID armorStand) {
		if (!hasCommands(armorStand)) {
			return;
		}
		CommandStorage storage = plugin.getCommandStorage();
		storage.getMappedCommands().remove(armorStand.toString());
		try {
			storage.save();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public boolean hasCommands(UUID armorStand) {
		return plugin.getCommandStorage().getMappedCommands().containsKey(armorStand.toString());
	}

	public void appendCommand(UUID armorStand, String commandLine) {
		List<String> commands = new ArrayList<>(getAttachedCommands(armorStand));
		commands.add(commandLine);
		setAttachedCommands(armorStand, commands);
	}

	public boolean removeCommandByIndex(UUID armorStand, int index) {
		List<String> commands = new ArrayList<>(getAttachedCommands(armorStand));
		if (index < 0 || index >= commands.size()) {
			return false;
		}
		commands.remove(index);
		setAttachedCommands(armorStand, commands);
		return true;
	}

	public boolean setCommandByIndex(UUID armorStand, int index, String command) {
		List<String> commands = new ArrayList<>(getAttachedCommands(armorStand));
		if (index < 0 || index >= commands.size()) {
			return false;
		}
		commands.set(index, command);
		setAttachedCommands(armorStand, commands);
		return true;
	}

	public boolean insertCommandByIndex(UUID armorStand, int index, String command) {
		List<String> commands = new ArrayList<>(getAttachedCommands(armorStand));
		if (index < 0 || index >= commands.size()) {
			return false;
		}
		commands.add(index, command);
		setAttachedCommands(armorStand, commands);
		return true;
	}

	public void performCommands(Player player, UUID armorStand) {
		List<String> commands = getAttachedCommands(armorStand);
		if (commands.isEmpty()) {
			return;
		}
		for (String command : commands) {
			String replaced = command.replace("@p", player.getName());
			player.performCommand(replaced);
		}
	}

}

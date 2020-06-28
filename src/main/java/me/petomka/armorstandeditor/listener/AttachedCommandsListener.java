package me.petomka.armorstandeditor.listener;

import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.handler.ArmorStandEditHandler;
import me.petomka.armorstandeditor.handler.AttachedCommandsHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.HashSet;
import java.util.Set;

public class AttachedCommandsListener implements Listener {

	public AttachedCommandsListener(Main plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/*
	 * ArmorStandEditListener already checks for destroyed armor stands.
	 * It removes destroyed armor stands from the attached commands listener.
	 * */

	//Object because Cancellable does not extend event and not every event is cancellable
	private static final Set<Object> eventsToIgnore = new HashSet<>();

	public static void addIgnoreEvent(Object event) {
		eventsToIgnore.add(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractAtEntityEvent event) {
		if (eventsToIgnore.remove(event)) {
			return;
		}
		if (Main.getEventsToIgnore().contains(event)) {
			return;
		}
		if (ArmorStandEditHandler.getInstance().isEditingPlayer(event.getPlayer().getUniqueId())) {
			return;
		}
		Entity rightClicked = event.getRightClicked();
		if (!(rightClicked instanceof ArmorStand)) {
			return;
		}
		if (!AttachedCommandsHandler.getInstance().hasCommands(rightClicked.getUniqueId())) {
			return;
		}
		AttachedCommandsHandler.getInstance().performCommands(event.getPlayer(), rightClicked.getUniqueId());
		event.setCancelled(true);
	}

}

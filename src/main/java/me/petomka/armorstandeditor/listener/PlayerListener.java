package me.petomka.armorstandeditor.listener;

import me.petomka.armorstandeditor.handler.ArmorStandEditHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		ArmorStandEditHandler.getInstance().removeEditingPlayer(event.getPlayer().getUniqueId());
	}

}

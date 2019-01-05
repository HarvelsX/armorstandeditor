package me.petomka.armorstandeditor.handler;

import com.google.common.base.Preconditions;
import me.petomka.armorstandeditor.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ActionBarTask extends BukkitRunnable {

	@Override
	public void run() {
		ArmorStandEditHandler.getInstance().editingPlayers.forEach((uuid, part) -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return;
			}
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					TextComponent.fromLegacyText(part.getEditingString()));
		});
	}

	public void onEditingStop(@Nonnull UUID player) {
		Preconditions.checkNotNull(player, "player");

		Player thePlayer = Bukkit.getPlayer(player);
		if (thePlayer != null) {
			thePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					TextComponent.fromLegacyText(
							ChatColor.translateAlternateColorCodes('&',
									Main.getInstance().getMessages().getEditingDone())));
		}
	}

}

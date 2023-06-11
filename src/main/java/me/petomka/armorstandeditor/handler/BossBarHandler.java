package me.petomka.armorstandeditor.handler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.Getter;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.config.DefaultConfig;
import me.petomka.armorstandeditor.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class BossBarHandler {

	@Getter(lazy = true)
	private static final BossBarHandler instance = new BossBarHandler();

	private Map<UUID, BossBar> playerBossBars = Maps.newHashMap();

	public boolean registerEditorPlayer(@Nonnull UUID player) {
		Preconditions.checkNotNull(player, "player");

		if (isRegistered(player)) {
			return updatePlayerEditBar(player);
		}

		Player thePlayer = Bukkit.getPlayer(player);
		if (thePlayer == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Cannot register bossbar for unknown player " +
					"with unknown UUID " + player);
			return false;
		}

		BossBar bar = createNewEditBossBar();
		if (bar != null) {
			playerBossBars.put(player, bar);
			bar.addPlayer(thePlayer);
			updatePlayerEditBar(player);
			return true;
		}
		return false;
	}

	public boolean registerSearchPlayer(@Nonnull UUID player, int count) {
		Preconditions.checkNotNull(player, "player");

		if (isRegistered(player)) {
			return updatePlayerSearchBar(player, count);
		}

		Player thePlayer = Bukkit.getPlayer(player);
		if (thePlayer == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Cannot register bossbar for unknown player " +
					"with unknown UUID " + player);
			return false;
		}

		BossBar bar = createNewSearchBossBar();
		if (bar != null) {
			playerBossBars.put(player, bar);
			bar.addPlayer(thePlayer);
			updatePlayerSearchBar(player, count);
			return true;
		}
		return false;
	}

	public void unregisterPlayer(@Nonnull UUID player) {
		Preconditions.checkNotNull(player, "player");

		Optional.ofNullable(playerBossBars.remove(player)).ifPresent(BossBar::removeAll);
	}

	public boolean isRegistered(@Nonnull UUID player) {
		Preconditions.checkNotNull(player, "player");

		return playerBossBars.containsKey(player);
	}

	public boolean updatePlayerEditBar(@Nonnull UUID player) {
		Preconditions.checkNotNull(player, "player");

		BossBar bar = playerBossBars.get(player);
		if (bar == null) {
			return false;
		}

		Accuracy playerAccuracy = ArmorStandEditHandler.getInstance().editingAccuracy.get(player);

		String title = Main.getInstance().getMessages().getBossBarTitle();
		title = ChatColor.translateAlternateColorCodes('&', title);
		title = title.replace("{size}",
				ChatColor.translateAlternateColorCodes('&', playerAccuracy.getName()));

		bar.setProgress((playerAccuracy.ordinal() + 1) / (double) Accuracy.values().length);
		bar.setTitle(title);
		return true;
	}

	private BossBar createNewEditBossBar() {
		Messages messages = Main.getInstance().getMessages();
		DefaultConfig config = Main.getInstance().getDefaultConfig();

		BarColor barColor;
		BarStyle barStyle;

		try {
			barColor = BarColor.valueOf(config.getBossBarColor());
		} catch (Exception exc) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Invalid barColor \"" +
					config.getBossBarColor() + "\"", exc);
			return null;
		}
		try {
			barStyle = BarStyle.valueOf(config.getBossBarStyle());
		} catch (Exception exc) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Invalid barStyle \"" +
					config.getBossBarStyle() + "\"", exc);
			return null;
		}

		return Bukkit.createBossBar(
				messages.getBossBarTitle(),
				barColor,
				barStyle
		);
	}

	public boolean updatePlayerSearchBar(@Nonnull UUID player, int count) {
		Preconditions.checkNotNull(player, "player");

		BossBar bar = playerBossBars.get(player);
		if (bar == null) {
			return false;
		}

		String title = Main.getInstance().getMessages().getSearchBarTitle();
		title = title.replace("{count}", String.valueOf(count));

		bar.setProgress(1.0f);
		bar.setTitle(Main.colorString(title));
		return true;
	}

	private BossBar createNewSearchBossBar() {
		Messages messages = Main.getInstance().getMessages();
		DefaultConfig config = Main.getInstance().getDefaultConfig();

		BarColor barColor;
		BarStyle barStyle;

		try {
			barColor = BarColor.valueOf(config.getSearchBarColor());
		} catch (Exception exc) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Invalid barColor \"" +
					config.getBossBarColor() + "\"", exc);
			return null;
		}

		try {
			barStyle = BarStyle.valueOf(config.getSearchBarStyle());
		} catch (Exception exc) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Invalid barStyle \"" +
					config.getBossBarStyle() + "\"", exc);
			return null;
		}

		return Bukkit.createBossBar(
				Main.colorString(messages.getSearchBarTitle()),
				barColor,
				barStyle
		);
	}

}

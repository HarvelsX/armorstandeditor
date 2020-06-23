package me.petomka.armorstandeditor;

import com.google.common.base.Preconditions;
import lombok.Getter;
import me.petomka.armorstandeditor.command.ArmorStandEditorCommand;
import me.petomka.armorstandeditor.config.DefaultConfig;
import me.petomka.armorstandeditor.config.DisabledPlayersStorage;
import me.petomka.armorstandeditor.config.Messages;
import me.petomka.armorstandeditor.handler.ArmorStandEditHandler;
import me.petomka.armorstandeditor.inventory.InventoryMenu;
import me.petomka.armorstandeditor.inventory.MenuItem;
import me.petomka.armorstandeditor.listener.ArmorStandEditListener;
import me.petomka.armorstandeditor.listener.PlayerListener;
import me.petomka.armorstandeditor.util.EntityLocationProxy;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Main extends JavaPlugin {

	public static final int PRO_MODE_SLOT = 4;

	@Getter
	private static Main instance;

	@Getter
	private Messages messages;

	@Getter
	private DefaultConfig defaultConfig;

	@Getter
	private DisabledPlayersStorage disabledPlayersStorage;

	@Override
	public void onEnable() {
		instance = this;

		try {
			defaultConfig = new DefaultConfig(this);
		} catch (InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Your defaultConfig.yml was wrong", e);
			panic();
			return;
		}

		try {
			messages = new Messages(this);
		} catch (InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Your messages.yml was wrong", e);
			panic();
			return;
		}

		try {
			disabledPlayersStorage = new DisabledPlayersStorage(this);
		} catch (InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Your disabled_players.yml is invalid.", e);
			panic();
			return;
		}

		new ArmorStandEditHandler(this);

		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new ArmorStandEditListener(this), this);
		manager.registerEvents(new PlayerListener(), this);

		getCommand("armorstandeditor").setExecutor(new ArmorStandEditorCommand());

		InventoryMenu.reloadItemNames();
		MenuItem.reloadMenuItems();
	}

	@Override
	public void onDisable() {
		messages = null;
		defaultConfig = null;

		if (disabledPlayersStorage != null) {
			try {
				disabledPlayersStorage.save();
			} catch (InvalidConfigurationException e) {
				getLogger().log(Level.SEVERE, "Error saving disabled_players.yml!", e);
			}
		}

		disabledPlayersStorage = null;
		instance = null;
	}

	private void panic() {
		getLogger().log(Level.SEVERE, "Disabling plugin due to startup error.");
		Bukkit.getPluginManager().disablePlugin(this);
	}

	public boolean isInteractCancelled(Player player, Collection<ArmorStand> entities, Vector delta) {
		double x = delta.getX(), y = delta.getY(), z = delta.getZ();
		return entities.stream()
				.map(as -> {
					EntityLocationProxy proxy = new EntityLocationProxy(as, as.getLocation().add(x, y, z));
					PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(player, proxy);
					ArmorStandEditListener.getEventsToIgnore().add(event);
					Bukkit.getPluginManager().callEvent(event);
					return event;
				})
				.anyMatch(Cancellable::isCancelled);
	}

	public static String colorString(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public static List<String> copySubstringMatches(Collection<String> strings, String startsWith) {
		return strings.stream()
				.filter(s -> s.startsWith(startsWith))
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked") //let's just trust this one
	public static <K, V> Map<K, V> mapOf(Supplier<Map> mapSupplier, Object... objects) {
		Preconditions.checkNotNull(mapSupplier, "mapSupplier");
		Preconditions.checkNotNull(objects, "objects");
		Preconditions.checkArgument(objects.length % 2 == 0, "objects must be of even size");

		Map result = mapSupplier.get();

		for (int a = 0; a < objects.length; a += 2) {
			result.put(objects[a], objects[a + 1]);
		}

		return result;
	}

	public static int normalizeYaw(float yaw) {
		return Math.floorMod((int) yaw, 360);
	}

	public static String formatDouble(double value) {
		return String.format("%.2f%n", value).trim();
	}

}

package me.petomka.armorstandeditor.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InventoryMenu implements Listener {

	private static final ItemStack DEFAULT_ITEM = hideDisplayName(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE));
	private static final ItemStack DARK_ITEM = hideDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));

	private static final Set<InventoryAction> LEGIT_ACTIONS = ImmutableSet.of(
			InventoryAction.PICKUP_ALL,
			InventoryAction.PICKUP_HALF,
			InventoryAction.PICKUP_ONE,
			InventoryAction.PICKUP_SOME,
			InventoryAction.CLONE_STACK,
			InventoryAction.PLACE_ALL,
			InventoryAction.SWAP_WITH_CURSOR,
			InventoryAction.PLACE_ONE,
			InventoryAction.PLACE_SOME
	);

	private static ItemStack BACK_ITEM;

	public static void reloadItemNames() {
		Messages messages = Main.getInstance().getMessages();

		BACK_ITEM = namedItemStack(Material.IRON_DOOR, messages.getInventory_backItem());
	}


	public static ItemStack namedItemStack(Material material, String name) {
		ItemStack stack = new ItemStack(material);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(Main.colorString(name));
		stack.setItemMeta(meta);
		return stack;
	}

	private static ItemStack hideDisplayName(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(ChatColor.BLACK + "");
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	private Map<Integer, BiConsumer<Player, ItemStack>> clickHandlers = Maps.newHashMap();
	private Set<Integer> modifiableSlots = Sets.newHashSet();

	@Getter
	private Inventory inventory;

	@Getter
	@Setter
	private boolean locked = false;

	@Getter
	private BiConsumer<Player, ItemStack> defaultClickHandler;

	@Getter
	private Runnable backHandler;

	@Getter
	@Setter
	private Runnable closeHandler;

	private final int rows;

	public InventoryMenu(@Nonnull String title, final int rows, @Nullable Consumer<Player> defaultClickHandler,
						 @Nullable Runnable backHandler) {
		this(title, rows, (player, itemStack) -> defaultClickHandler.accept(player), backHandler);
	}

	public InventoryMenu(@Nonnull String title, final int rows, @Nullable BiConsumer<Player, ItemStack> defaultClickHandler,
						 @Nullable Runnable backHandler) {
		Preconditions.checkNotNull(title, "title");
		Preconditions.checkArgument(rows <= 6, "Only 6 rows are possible!");

		if(defaultClickHandler == null) {
			defaultClickHandler = (player, itemStack) -> this.unlock();
		}

		this.rows = rows;

		this.defaultClickHandler = defaultClickHandler;
		this.backHandler = backHandler;

		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());

		inventory = Bukkit.createInventory(null, 9 * rows, title);

		for (int i = 0; i < inventory.getSize(); i++) {
			inventory.setItem(i, DEFAULT_ITEM);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onClick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null) {
			return;
		}
		if (!inventory.getViewers().contains(event.getWhoClicked())) {
			return;
		}
		if (!event.getClickedInventory().equals(inventory)) {
			return;
		}
		if (!LEGIT_ACTIONS.contains(event.getAction())) {
			event.setCancelled(true);
		}
		if (locked) {
			event.setCancelled(true);
			return;
		}
		if (!modifiableSlots.contains(event.getSlot())) {
			event.setCancelled(true);
		}
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () ->
				clickHandlers.getOrDefault(event.getSlot(), defaultClickHandler)
						.accept((Player) event.getWhoClicked(), event.getCurrentItem()), 1L);
		locked = true;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onClose(InventoryCloseEvent event) {
		if (!event.getInventory().equals(inventory)) {
			return;
		}
		if (closeHandler != null) {
			Bukkit.getScheduler().runTaskLater(Main.getInstance(), closeHandler, 1L);
		}
	}

	public void open(Player player) {
		player.openInventory(inventory);
	}

	public void fillBorder() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < rows; j++) {
				if (j == 0 || j == rows - 1 || i == 0 || i == 8) {
					addItemAndClickHandler(DARK_ITEM, j, i, defaultClickHandler);
				}
			}
		}
		if (backHandler != null) {
			addItemAndClickHandler(BACK_ITEM, rows - 1, 8, (p, i) -> backHandler.run());
		} else {
			addItemAndClickHandler(DARK_ITEM, rows - 1, 8, defaultClickHandler);
		}
	}

	public void fill(ItemStack itemStack) {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < rows; j++) {
				addItemAndClickHandler(itemStack, j, i, null);
			}
		}
	}

	public void unlock() {
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> this.setLocked(false), 1L);
	}

	public void addModifiableSlot(int row, int column) {
		modifiableSlots.add(row * 9 + column);
	}

	public void addModifiableSlot(int indexedSlot) {
		modifiableSlots.add(indexedSlot);
	}

	public void addItemAndClickHandler(@Nullable ItemStack itemStack, int row, int column, @Nullable BiConsumer<Player, ItemStack> clickHandler) {
		addItemAndClickHandler(itemStack, 9 * row + column, clickHandler);
	}

	public void addItemAndClickHandler(@Nullable ItemStack itemStack, int indexedSlot, @Nullable BiConsumer<Player, ItemStack> clickHandler) {
		inventory.setItem(indexedSlot, itemStack);

		if (clickHandler != null) {
			clickHandlers.put(indexedSlot, clickHandler);
		}
	}

}

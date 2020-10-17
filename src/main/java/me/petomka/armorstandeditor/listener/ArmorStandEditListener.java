package me.petomka.armorstandeditor.listener;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.config.DefaultConfig;
import me.petomka.armorstandeditor.config.Messages;
import me.petomka.armorstandeditor.handler.Accuracy;
import me.petomka.armorstandeditor.handler.ArmorStandEditHandler;
import me.petomka.armorstandeditor.handler.AttachedCommandsHandler;
import me.petomka.armorstandeditor.handler.Part;
import me.petomka.armorstandeditor.inventory.InventoryMenu;
import me.petomka.armorstandeditor.inventory.MenuItem;
import me.petomka.armorstandeditor.util.ArmorStandUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ArmorStandEditListener implements Listener {

	private Set<UUID> doubleClicks = Sets.newHashSet();

	private final Main plugin;

	private Map<ArmorStand, Set<UUID>> armorStandDamagers = Maps.newHashMap();

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAtEntityClick(PlayerInteractAtEntityEvent event) {
		if (Main.getEventsToIgnore().contains(event)) {
			return;
		}

		if (event.getRightClicked().getType() != EntityType.ARMOR_STAND) {
			return;
		}

		ArmorStand armorStand = (ArmorStand) event.getRightClicked();
		if (ArmorStandEditHandler.getInstance().getArmorStandEditor(armorStand) != null) {
			event.setCancelled(true); // If an armorstand is being edited, cancel interactions with it, always.
		}

		if (Main.getInstance().getDisabledPlayersStorage().getDisabledPlayers().contains(event.getPlayer().getUniqueId())) {
			return;
		}

		ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
		if (hand != null && handleArmorstandItemClick(event, event.getPlayer(), (ArmorStand) event.getRightClicked())) {
			return;
		}

		if (!event.getPlayer().hasPermission(Main.getInstance().getDefaultConfig().getEditPermission())) {
			return;
		}

		if (!event.getPlayer().isSneaking()) {
			if (ArmorStandEditHandler.getInstance().isEditingPlayer(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
			}
			if (ArmorStandEditHandler.getInstance().isProModeEditor(event.getPlayer().getUniqueId())) {
				ArmorStandEditHandler.getInstance().getSingleArmorstand(event.getPlayer().getUniqueId())
						.ifPresent(as -> openArmorStandMenu(event.getPlayer(), null));
			}
			return;
		}

		if (!doubleClicks.add(event.getPlayer().getUniqueId()) &&
				ArmorStandEditHandler.getInstance().isEditingPlayer(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			openArmorStandMenu(event.getPlayer(), null);
			return;
		}

		doubleClicks.add(event.getPlayer().getUniqueId());
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
			doubleClicks.remove(event.getPlayer().getUniqueId());
		}, 10L);

		boolean proMode = false;
		if (hand != null) {
			proMode = hand.getType() == Material.STICK;
		}
		ArmorStandEditHandler.getInstance().addEditingPlayer(event.getPlayer().getUniqueId(),
				calculateClickedPart(event.getClickedPosition(), armorStand), armorStand);

		if (proMode && !ArmorStandEditHandler.getInstance().isProModeEditor(event.getPlayer().getUniqueId())) {
			handleToggleProMode(event, event.getPlayer(), false);
		}

		if (Main.getInstance().getDefaultConfig().isPlaySounds()) {
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1f, 1f);
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemHeld(PlayerItemHeldEvent event) {
		if (!ArmorStandEditHandler.getInstance().isEditingPlayer(event.getPlayer().getUniqueId())) {
			return;
		}

		Accuracy accuracy = ArmorStandEditHandler.getInstance().getPlayerAccuracy(event.getPlayer().getUniqueId());
		if (accuracy == null) {
			return; //error already logged in getPlayerAccuracy
		}

		if (ArmorStandEditHandler.getInstance().isProModeEditor(event.getPlayer().getUniqueId())) {
			if (event.getNewSlot() == Main.PRO_MODE_SLOT) {
				return;
			}
			event.setCancelled(true);
		}

		boolean playSounds = Main.getInstance().getDefaultConfig().isPlaySounds();

		if (event.getPlayer().isSneaking()) {
			boolean moreAccurate = !isIncreaseSlot(event.getPreviousSlot(), event.getNewSlot());
			ArmorStandEditHandler.getInstance().updateAccuracy(event.getPlayer().getUniqueId(), moreAccurate);
			if (!playSounds) {
				return;
			}
			if (moreAccurate) {
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_OUT, SoundCategory.MASTER, 1f, 1f);
			} else {
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_UP, SoundCategory.MASTER, 1f, 2f);
			}
			return;
		}

		Vector adjustment = new Vector(0, 0, 0);
		Location playerLocation = event.getPlayer().getLocation();

		int yaw = Math.round(playerLocation.getYaw() + 45);

		if (yaw < 0) {
			yaw += 360;
		}

		yaw = yaw / 90;

		if (playerLocation.getPitch() <= -60 || playerLocation.getPitch() >= 70) {
			adjustment.setY(1);
		} else if (yaw % 2 == 0) {
			adjustment.setZ(1);
		} else {
			adjustment.setX(1);
		}

		if (!isIncreaseSlot(event.getPreviousSlot(), event.getNewSlot())) {
			adjustment = adjustment.multiply(-1);
		}

		adjustment = adjustment.multiply(accuracy.getAdjustmentSize());

		ArmorStandEditHandler.getInstance().onAdjustmentMade(event.getPlayer().getUniqueId(),
				adjustment.getX(), adjustment.getY(), adjustment.getZ());

		if (playSounds) {
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundCategory.MASTER, 1f, 1f);
		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onLeftOrRightClick(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}

		if (!ArmorStandEditHandler.getInstance().isEditingPlayer(event.getPlayer().getUniqueId())) {
			handleNonEditingBlockClick(event, event.getPlayer(), event.getAction(), event.getClickedBlock());
			return;
		}

		boolean playSounds = Main.getInstance().getDefaultConfig().isPlaySounds();
		boolean sneaking = event.getPlayer().isSneaking();
		boolean iAirLeftClick = event.getAction() == Action.LEFT_CLICK_AIR;
		boolean iBlockLeftClick = event.getAction() == Action.LEFT_CLICK_BLOCK;
		boolean isRightClick = event.getAction() == Action.RIGHT_CLICK_BLOCK ||
				event.getAction() == Action.RIGHT_CLICK_AIR;

		if (iAirLeftClick && !sneaking) {
			handleQuitEditing(event.getPlayer(), playSounds);
			return;
		}

		boolean isProMode = ArmorStandEditHandler.getInstance().isProModeEditor(event.getPlayer().getUniqueId());
		if (isRightClick && isProMode && sneaking) {
			ArmorStandEditHandler.getInstance().editPreviousPart(event.getPlayer().getUniqueId());
			if (playSounds) {
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f);
			}
			return;
		}

		if (isRightClick && isProMode) {
			openArmorStandMenu(event.getPlayer(), null);
			return;
		}

		if ((iAirLeftClick || isProMode && iBlockLeftClick) && sneaking) {
			handleEditNextPart(event.getPlayer(), playSounds);
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) {
			return;
		}
		if (!(event.getEntity() instanceof ArmorStand)) {
			return;
		}
		handleArmorStandLeftClick(event, (Player) event.getDamager(), (ArmorStand) event.getEntity());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBuild(BlockPlaceEvent event) {
		if (ArmorStandEditHandler.getInstance().isProModeEditor(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBreak(BlockBreakEvent event) {
		if (ArmorStandEditHandler.getInstance().isProModeEditor(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!ArmorStandEditHandler.getInstance().isProModeEditor(event.getWhoClicked().getUniqueId())) {
			return;
		}
		if (event.getClickedInventory() == null) {
			return;
		}
		PlayerInventory inventory = event.getWhoClicked().getInventory();
		if (event.getClickedInventory().equals(inventory) && event.getSlot() == inventory.getHeldItemSlot()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (event.getItemDrop() == null) {
			return;
		}
		if (event.getItemDrop().getItemStack().getType() != Material.STICK) {
			return;
		}
		if (event.getPlayer().getOpenInventory().getTopInventory() != null &&
				event.getPlayer().getOpenInventory().getTopInventory().getType() == InventoryType.CHEST) {
			return;
		}
		if (ArmorStandEditHandler.getInstance().isEditingPlayer(player.getUniqueId())) {
			handleToggleProMode(event, player, Main.getInstance().getDefaultConfig().isPlaySounds());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemSwap(PlayerSwapHandItemsEvent event) {
		if (ArmorStandEditHandler.getInstance().isProModeEditor(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			DefaultConfig config = Main.getInstance().getDefaultConfig();
			Messages messages = Main.getInstance().getMessages();
			if (!event.getPlayer().hasPermission(config.getGlowingPermission())) {
				event.getPlayer().sendMessage(Main.colorString(messages.getProModeGlowingNotAllowed()));
				return;
			}
			ArmorStandEditHandler.getInstance().getSingleArmorstand(event.getPlayer().getUniqueId())
					.ifPresent(armorStand -> armorStand.setGlowing(!armorStand.isGlowing()));
			if (Main.getInstance().getDefaultConfig().isPlaySounds()) {
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT,
						SoundCategory.MASTER, 1f, 1f);
			}
		}
	}

	private void handleEditNextPart(Player player, boolean playSounds) {
		ArmorStandEditHandler.getInstance().editNextPart(player.getUniqueId());
		if (playSounds) {
			player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f);
		}
	}

	private Part calculateClickedPart(Vector clicked, ArmorStand armorStand) {
		if (!armorStand.isSmall() && clicked.getY() >= 1.55 || armorStand.isSmall() && clicked.getY() > 0.75) {
			return Part.HEAD;
		}

		return calcLegOrArmPart(clicked, armorStand.getLocation().getYaw(), armorStand.isSmall());
	}

	//small is half sized
	private Part calcLegOrArmPart(Vector clicked, float yaw, boolean small) {
		yaw = Math.round(yaw + 45);

		if (yaw < 0) {
			yaw += 360;
		}

		yaw = yaw / 90;

		switch ((int) yaw) {
			case 4:
			case 0:
				if (clicked.getX() > 0) {
					return upperOrLower(clicked, Part.LEFT_ARM, Part.LEFT_LEG, small);
				}
				return upperOrLower(clicked, Part.RIGHT_ARM, Part.RIGHT_LEG, small);
			case 1:
				if (clicked.getZ() > 0) {
					return upperOrLower(clicked, Part.LEFT_ARM, Part.LEFT_LEG, small);
				}
				return upperOrLower(clicked, Part.RIGHT_ARM, Part.RIGHT_LEG, small);
			case 2:
				if (clicked.getX() < 0) {
					return upperOrLower(clicked, Part.LEFT_ARM, Part.LEFT_LEG, small);
				}
				return upperOrLower(clicked, Part.RIGHT_ARM, Part.RIGHT_LEG, small);
			case 3:
				if (clicked.getZ() < 0) {
					return upperOrLower(clicked, Part.LEFT_ARM, Part.LEFT_LEG, small);
				}
				return upperOrLower(clicked, Part.RIGHT_ARM, Part.RIGHT_LEG, small);
			default:
				return Part.BODY;
		}
	}

	private Part upperOrLower(Vector clicked, Part upper, Part lower, boolean small) {
		if (!small) {
			if (clicked.getY() <= 0.875) {
				return lower;
			}
			if (clicked.getY() >= 1.25) {
				return upper;
			}
		} else {
			if (clicked.getY() <= 0.375) {
				return lower;
			}
			if (clicked.getY() >= 0.625) {
				return upper;
			}
		}

		return Part.BELLY;
	}

	private boolean handleArmorstandItemClick(Cancellable event, Player player, ArmorStand armorStand) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null) {
			return false;
		}
		switch (hand.getType()) {
			case NAME_TAG:
				handleNameTagRename(event, player, armorStand);
				break;
			case LEATHER:
				handleLeatherRotate(event, player, armorStand, false);
				break;
		}
		return event.isCancelled();
	}

	private void handleNameTagRename(Cancellable event, Player player, ArmorStand armorStand) {
		ItemStack nameTag = player.getInventory().getItemInMainHand();
		if (nameTag == null) {
			return;
		}
		if (nameTag.getType() != Material.NAME_TAG) {
			return;
		}
		if (!player.hasPermission(Main.getInstance().getDefaultConfig().getColorNameTagsPermission())) {
			return;
		}
		if (plugin.isInteractCancelled(player, Collections.singleton(armorStand), new Vector(0, 0, 0))) {
			return;
		}
		AttachedCommandsListener.addIgnoreEvent(event);
		ItemMeta meta = nameTag.getItemMeta();
		if (!meta.hasDisplayName()) { //cannot be null -> Name_tag has item meta (checked)
			return;
		}
		if (player.getGameMode() != GameMode.CREATIVE) {
			nameTag.setAmount(nameTag.getAmount() - 1);
		}
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
			String name = Main.colorString(meta.getDisplayName());
			armorStand.setCustomName(name);
		}, 1L);
		event.setCancelled(true);
	}

	private void handleArmorStandLeftClick(Cancellable event, Player player, ArmorStand armorStand) {
		if (!ArmorStandEditHandler.getInstance().isEditingPlayer(player.getUniqueId())) {
			handleNonEditingArmorStandLeftClick(event, player, armorStand);
			return;
		}
		event.setCancelled(true);
		if (player.isSneaking()) {
			handleEditNextPart(player, Main.getInstance().getDefaultConfig().isPlaySounds());
		}
	}

	private void handleNonEditingArmorStandLeftClick(Cancellable event, Player player, ArmorStand armorStand) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null) {
			handleArmorstandDestroy(event, player, armorStand);
			return;
		}
		switch (hand.getType()) {
			case LEATHER:
				handleLeatherRotate(event, player, armorStand, true);
				return;
		}
		handleArmorstandDestroy(event, player, armorStand);
	}

	private void handleArmorstandDestroy(Cancellable event, Player player, ArmorStand armorStand) {
		if (player.getGameMode() == GameMode.SURVIVAL &&
				!armorStandDamagers.getOrDefault(armorStand, ImmutableSet.of()).contains(player.getUniqueId())) {
			if (armorStand.isInvulnerable()) {
				return;
			}
			armorStandDamagers.compute(armorStand, (a, uuids) -> {
				if (uuids == null) {
					uuids = Sets.newHashSet();
				}
				uuids.add(player.getUniqueId());
				return uuids;
			});
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
				armorStandDamagers.compute(armorStand, (a, uuids) -> {
					if (uuids == null) {
						return null;
					}
					uuids.remove(player.getUniqueId());
					if (uuids.isEmpty()) {
						return null;
					}
					return uuids;
				});
			}, 8L);
			armorStand.setLastDamage(0);
			armorStand.setLastDamageCause(null);
			return;
		}
		AttachedCommandsHandler.getInstance().removeAll(armorStand.getUniqueId());
		UUID armorStandEditor = ArmorStandEditHandler.getInstance().getArmorStandEditor(armorStand);
		if (armorStandEditor == null) {
			handleDropCopy(event, player, armorStand);
			return;
		}
		if (!player.hasPermission(Main.getInstance().getDefaultConfig().getBreakEditedArmorStandPermission())) {
			event.setCancelled(true);
			return;
		}
		Player editor = Bukkit.getPlayer(armorStandEditor);
		if (editor == null) {
			return;
		}

		boolean playSounds = Main.getInstance().getDefaultConfig().isPlaySounds();
		handleQuitEditing(editor, playSounds);
		handleDropCopy(event, player, armorStand);
	}

	private void handleDropCopy(Cancellable event, Player player, ArmorStand armorStand) {
		if (!Main.getInstance().getDefaultConfig().isDropCopyOfDestroyedArmorStand()) {
			return;
		}
		if (!player.hasPermission(Main.getInstance().getDefaultConfig().getCopyArmorStandPermission())) {
			return;
		}
		if (!ArmorStandUtils.isCopyWorthIt(armorStand)) {
			return;
		}
		if (armorStand.isDead() || !armorStand.isValid()) {
			return;
		}
		ItemStack copy = ArmorStandUtils.saveToItem(armorStand, player);
		armorStand.getWorld().dropItem(armorStand.getLocation().add(0, 0.2, 0), copy);
		event.setCancelled(true);
		armorStand.remove();
		armorStandDamagers.remove(armorStand);
	}

	private void handleLeatherRotate(Cancellable event, Player player, ArmorStand armorStand, boolean clockWise) {
		if (!player.hasPermission(Main.getInstance().getDefaultConfig().getRotateLeatherPermission())) {
			return;
		}
		if (plugin.isInteractCancelled(player, Collections.singleton(armorStand), new Vector(0, 0, 0))) {
			return;
		}
		event.setCancelled(true);

		int angle = Main.getInstance().getDefaultConfig().getRotateLeatherDegrees();

		if (!clockWise) {
			angle *= -1;
		}

		Location location = armorStand.getLocation();
		location.setYaw(location.getYaw() + angle);
		armorStand.teleport(location);
	}


	private void handleNonEditingBlockClick(Cancellable event, Player player, Action action, Block block) {
		switch (action) {
			case RIGHT_CLICK_BLOCK:
				handleBlockRightClick(event, player, block);
				break;
		}
	}

	private void handleBlockRightClick(Cancellable event, Player player, Block block) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null) {
			return;
		}
		switch (hand.getType()) {
			case ARMOR_STAND:
				handlePlaceCopiedArmorStand(event, player, hand, block);
				break;
		}
	}

	private void handlePlaceCopiedArmorStand(Cancellable event, Player player, ItemStack itemStack, Block block) {
		if (!player.hasPermission(Main.getInstance().getDefaultConfig().getPlaceNBTArmorStandPermission())) {
			return;
		}
		if (!ArmorStandUtils.isCopiedArmorStand(itemStack)) {
			return;
		}

		ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(block.getLocation()
				.add(0.5, 1, 0.5), EntityType.ARMOR_STAND);

		ArmorStandUtils.loadFromItem(itemStack, armorStand);

		if (player.getGameMode() != GameMode.CREATIVE) {
			itemStack.setAmount(itemStack.getAmount() - 1);
		}

		event.setCancelled(true);
	}

	private void handleQuitEditing(Player player, boolean playSounds) {
		ArmorStandEditHandler.getInstance().removeEditingPlayer(player.getUniqueId());
		if (playSounds) {
			player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_CLOSE, 1f, 1f);
		}
	}

	private boolean isIncreaseSlot(int old, int nu) {
		if (old == 0 && nu == 8) {
			return true;
		}
		if (old == 8 && nu == 0) {
			return false;
		}
		return nu < old;
	}

	private void handleToggleProMode(Cancellable event, Player player, boolean playSound) {
		if (!player.hasPermission(Main.getInstance().getDefaultConfig().getProEditPermission())) {
			return;
		}
		event.setCancelled(true);

		Messages messages = Main.getInstance().getMessages();
		String subTitle;

		PlayerInventory inventory = player.getInventory();
		ItemStack proItem = inventory.getItem(inventory.getHeldItemSlot());
		ItemStack swapItem = inventory.getItem(Main.PRO_MODE_SLOT);
		inventory.setItem(Main.PRO_MODE_SLOT, proItem);
		inventory.setItem(inventory.getHeldItemSlot(), swapItem);
		inventory.setHeldItemSlot(Main.PRO_MODE_SLOT);

		if (ArmorStandEditHandler.getInstance().toggleProMode(player.getUniqueId())) {
			subTitle = messages.getProModeStart();
			if (playSound) {
				player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1f, 2f);
			}
		} else {
			subTitle = messages.getProModeEnd();
			if (playSound) {
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1f, 1f);
			}
		}
		player.sendTitle(ChatColor.BLACK + "", Main.colorString(subTitle), 5, 50, 5);
	}

	private void openArmorStandMenu(Player player, Runnable backHandler) {
		ArmorStand armorStand = ArmorStandEditHandler.getInstance()
				.getSingleArmorstand(player.getUniqueId())
				.orElse(null);

		if (armorStand == null) {
			return;
		}

		Messages messages = Main.getInstance().getMessages();
		InventoryMenu menu = new InventoryMenu(Main.colorString(messages.getInventory_title()), 6,
				p -> openArmorStandMenu(player, backHandler), backHandler);

		menu.fillBorder();

		DefaultConfig config = Main.getInstance().getDefaultConfig();

		int menuIndex = 1;

		/*
		 * 1 -> 1, 1
		 * 2 -> 1, 2
		 * ...
		 * 7 -> 1, 7
		 * 8 -> 3 2*(x // 7) + 1 , 1 ((x - 1) % 7) + 1
		 * */
		IntUnaryOperator rowFunction = x -> 2 * Math.floorDiv(x - 1, 7) + 1;
		IntUnaryOperator columnFunction = x -> ((x - 1) % 7) + 1;

		//Arms
		if (player.hasPermission(config.getShowArmsPermission())) {
			menu.addItemAndClickHandler(MenuItem.TOGGLE_SHOW_ARMS, rowFunction.applyAsInt(menuIndex), columnFunction.applyAsInt(menuIndex), (p, i) -> {
				armorStand.setArms(!armorStand.hasArms());
				openArmorStandMenu(player, backHandler);
				playToggleSound(player);
			});
			addBoolItem(menu, rowFunction.applyAsInt(menuIndex) + 1, columnFunction.applyAsInt(menuIndex), armorStand::hasArms);
			menuIndex++;
		}

		//Baseplate
		if (player.hasPermission(config.getShowBasePlatePermission())) {
			menu.addItemAndClickHandler(MenuItem.TOGGLE_SHOW_BASEPLATE, rowFunction.applyAsInt(menuIndex), columnFunction.applyAsInt(menuIndex), (p, i) -> {
				armorStand.setBasePlate(!armorStand.hasBasePlate());
				openArmorStandMenu(player, backHandler);
				playToggleSound(player);
			});
			addBoolItem(menu, rowFunction.applyAsInt(menuIndex) + 1, columnFunction.applyAsInt(menuIndex), armorStand::hasBasePlate);
			menuIndex++;
		}

		//Small Armorstand
		if (player.hasPermission(config.getSmallArmorStandPermission())) {
			menu.addItemAndClickHandler(MenuItem.TOGGLE_SMALL_ARMORSTAND, rowFunction.applyAsInt(menuIndex), columnFunction.applyAsInt(menuIndex), (p, i) -> {
				armorStand.setSmall(!armorStand.isSmall());
				openArmorStandMenu(player, backHandler);
				playToggleSound(player);
			});
			addBoolItem(menu, rowFunction.applyAsInt(menuIndex) + 1, columnFunction.applyAsInt(menuIndex), armorStand::isSmall);
			menuIndex++;
		}

		//Invulnerability
		if (player.hasPermission(config.getInvulnerableArmorStandPermission())) {
			menu.addItemAndClickHandler(MenuItem.TOGGLE_INVULNERABILITY, rowFunction.applyAsInt(menuIndex), columnFunction.applyAsInt(menuIndex), (p, i) -> {
				armorStand.setInvulnerable(!armorStand.isInvulnerable());
				openArmorStandMenu(player, backHandler);
				playToggleSound(player);
			});
			addBoolItem(menu, rowFunction.applyAsInt(menuIndex) + 1, columnFunction.applyAsInt(menuIndex), armorStand::isInvulnerable);
			menuIndex++;
		}

		//Gravity
		if (player.hasPermission(config.getGravityPermission())) {
			menu.addItemAndClickHandler(MenuItem.TOGGLE_GRAVITY, rowFunction.applyAsInt(menuIndex), columnFunction.applyAsInt(menuIndex), (p, i) -> {
				armorStand.setGravity(!armorStand.hasGravity());
				openArmorStandMenu(player, backHandler);
				playToggleSound(player);
			});
			addBoolItem(menu, rowFunction.applyAsInt(menuIndex) + 1, columnFunction.applyAsInt(menuIndex), armorStand::hasGravity);
			menuIndex++;
		}

		//Visibility
		if (player.hasPermission(config.getVisibilityPermission())) {
			menu.addItemAndClickHandler(MenuItem.TOGGLE_VISIBILITY, rowFunction.applyAsInt(menuIndex), columnFunction.applyAsInt(menuIndex), (p, i) -> {
				armorStand.setVisible(!armorStand.isVisible());
				openArmorStandMenu(player, backHandler);
				playToggleSound(player);
			});
			addBoolItem(menu, rowFunction.applyAsInt(menuIndex) + 1, columnFunction.applyAsInt(menuIndex), armorStand::isVisible);
			menuIndex++;
		}

		//Custom name
		if (player.hasPermission(config.getCustomNamePermission())) {
			menu.addItemAndClickHandler(MenuItem.TOGGLE_SHOW_CUSTOM_NAME, rowFunction.applyAsInt(menuIndex), columnFunction.applyAsInt(menuIndex), (p, i) -> {
				armorStand.setCustomNameVisible(!armorStand.isCustomNameVisible());
				openArmorStandMenu(player, backHandler);
				playToggleSound(player);
			});
			addBoolItem(menu, rowFunction.applyAsInt(menuIndex) + 1, columnFunction.applyAsInt(menuIndex), armorStand::isCustomNameVisible);
			menuIndex++;
		}

		//Glowing
		if (player.hasPermission(config.getGlowingPermission())) {
			menu.addItemAndClickHandler(MenuItem.TOGGLE_GLOWING, rowFunction.applyAsInt(menuIndex), columnFunction.applyAsInt(menuIndex), (p, i) -> {
				armorStand.setGlowing(!armorStand.isGlowing());
				openArmorStandMenu(player, backHandler);
				playToggleSound(player);
			});
			addBoolItem(menu, rowFunction.applyAsInt(menuIndex) + 1, columnFunction.applyAsInt(menuIndex), armorStand::isGlowing);
			menuIndex++;
		}

		if (player.hasPermission(config.getAttachCommandsPermission())) {
			menu.addItemAndClickHandler(MenuItem.ATTACH_COMMANDS, rowFunction.applyAsInt(menuIndex), columnFunction.applyAsInt(menuIndex), (p, i) -> {
				p.closeInventory();
				p.performCommand("asa commands " + armorStand.getUniqueId() + " actions");
			});
//			menuIndex++;
		}

		if (player.hasPermission(config.getSetEquipPermission())) {
			menu.addItemAndClickHandler(MenuItem.SET_EQUIP, 4, 6, (p, i) -> {
				openEquipMenu(player, () -> openArmorStandMenu(player, backHandler), armorStand);
				playClickSound(player);
			});
		}

		if (player.hasPermission(Main.getInstance().getDefaultConfig().getCopyArmorStandPermission())) {
			menu.addItemAndClickHandler(MenuItem.CREATE_COPY, 4, 7, (p, i) -> {
				openArmorStandMenu(player, backHandler);
				ItemStack copy = ArmorStandUtils.saveToItem(armorStand, player);
				player.getInventory().addItem(copy);
				playClickSound(player);
			});
		}

		playClickSound(player);
		menu.open(player);
	}

	private void addBoolItem(InventoryMenu menu, int row, int column, Supplier<Boolean> booleanSupplier) {
		if (booleanSupplier.get().equals(Boolean.TRUE)) {
			menu.addItemAndClickHandler(MenuItem.GLASS_ENABLED, row, column, null);
		} else {
			menu.addItemAndClickHandler(MenuItem.GLASS_DISABLED, row, column, null);
		}
	}

	private void openEquipMenu(Player player, Runnable backHandler, ArmorStand armorStand) {
		InventoryMenu menu = new InventoryMenu(Main.colorString(Main.getInstance().getMessages()
				.getInventory_equipTitle()), 6, (BiConsumer<Player, ItemStack>) null,
				backHandler);

		menu.fill(MenuItem.GLASS_NOT_SO_DARK);
		menu.fillBorder();

		menu.addItemAndClickHandler(MenuItem.HELMET_SLOT, 1, 2, null);
		menu.addItemAndClickHandler(MenuItem.CHEST_SLOT, 2, 2, null);
		menu.addItemAndClickHandler(MenuItem.LEG_SLOT, 3, 2, null);
		menu.addItemAndClickHandler(MenuItem.BOOTS_SLOT, 4, 2, null);

		menu.addItemAndClickHandler(MenuItem.MAINHAND_SLOT, 2, 6, null);
		menu.addItemAndClickHandler(MenuItem.OFFHAND_SLOT, 3, 6, null);

		menu.addItemAndClickHandler(armorStand.getHelmet(), 1, 3, (p, itemStack) -> {
			armorStand.setHelmet(itemStack);
			menu.unlock();
			playEquipSound(player);
		});
		menu.addModifiableSlot(1, 3);

		menu.addItemAndClickHandler(armorStand.getChestplate(), 2, 3, (p, itemStack) -> {
			armorStand.setChestplate(itemStack);
			menu.unlock();
			playEquipSound(player);
		});
		menu.addModifiableSlot(2, 3);

		menu.addItemAndClickHandler(armorStand.getLeggings(), 3, 3, (p, itemStack) -> {
			armorStand.setLeggings(itemStack);
			menu.unlock();
			playEquipSound(player);
		});
		menu.addModifiableSlot(3, 3);

		menu.addItemAndClickHandler(armorStand.getBoots(), 4, 3, (p, itemStack) -> {
			armorStand.setBoots(itemStack);
			menu.unlock();
			playEquipSound(player);
		});
		menu.addModifiableSlot(4, 3);

		menu.addItemAndClickHandler(armorStand.getItemInHand(), 2, 5, (p, itemStack) -> {
			armorStand.setItemInHand(itemStack);
			menu.unlock();
			playEquipSound(player);
		});
		menu.addModifiableSlot(2, 5);

		menu.addItemAndClickHandler(armorStand.getEquipment().getItemInOffHand(), 3, 5, (p, itemStack) -> {
			armorStand.getEquipment().setItemInOffHand(itemStack);
			menu.unlock();
			playEquipSound(player);
		});
		menu.addModifiableSlot(3, 5);

		menu.setCloseHandler(() -> {
			if (player.getOpenInventory().getTopInventory() == null) {
				playClickSound(player);
			}
			Inventory inventory = menu.getInventory();
			armorStand.setHelmet(inventory.getItem(9 + 3));
			armorStand.setChestplate(inventory.getItem(18 + 3));
			armorStand.setLeggings(inventory.getItem(27 + 3));
			armorStand.setBoots(inventory.getItem(36 + 3));
			armorStand.setItemInHand(inventory.getItem(18 + 5));
			armorStand.getEquipment().setItemInOffHand(inventory.getItem(27 + 5));
		});

		menu.open(player);
	}

	private void playClickSound(Player player) {
		if (!Main.getInstance().getDefaultConfig().isPlaySounds()) {
			return;
		}
		player.playSound(player.getLocation(), Sound.ENTITY_DONKEY_CHEST, 1f, 1f);
	}

	private void playToggleSound(Player player) {
		if (!Main.getInstance().getDefaultConfig().isPlaySounds()) {
			return;
		}
		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f);
	}

	private void playEquipSound(Player player) {
		if (!Main.getInstance().getDefaultConfig().isPlaySounds()) {
			return;
		}
		player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1f);
	}

}

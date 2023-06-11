package me.petomka.armorstandeditor.inventory;

import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.config.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MenuItem {

	public static ItemStack TOGGLE_SHOW_ARMS;
	public static ItemStack TOGGLE_SHOW_BASEPLATE;
	public static ItemStack TOGGLE_SMALL_ARMORSTAND;
	public static ItemStack TOGGLE_INVULNERABILITY;
	public static ItemStack TOGGLE_GRAVITY;
	public static ItemStack TOGGLE_VISIBILITY;
	public static ItemStack TOGGLE_SHOW_CUSTOM_NAME;
	public static ItemStack TOGGLE_GLOWING;
	public static ItemStack TOGGLE_MARKER;
	public static ItemStack ATTACH_COMMANDS;
	public static ItemStack TOGGLE_LOCK_ARMOR_STAND;
	public static ItemStack SET_EQUIP;
	public static ItemStack CHANGE_EQUIP_LOCKS;
	public static ItemStack HELMET_SLOT;
	public static ItemStack CHEST_SLOT;
	public static ItemStack LEG_SLOT;
	public static ItemStack BOOTS_SLOT;
	public static ItemStack MAINHAND_SLOT;
	public static ItemStack OFFHAND_SLOT;
	public static ItemStack CREATE_COPY;

	public static ItemStack GLASS_ENABLED;
	public static ItemStack GLASS_DISABLED;

	public static ItemStack GLASS_NOT_SO_DARK = InventoryMenu.namedItemStack(Material.GRAY_STAINED_GLASS_PANE, ChatColor.BLACK + "");
	public static ItemStack GLASS_DARK = InventoryMenu.namedItemStack(Material.BLACK_STAINED_GLASS_PANE, ChatColor.BLACK + "");

	public static ItemStack NO_ITEM = new ItemStack(Material.AIR);

	public static void reloadMenuItems() {
		Messages messages = Main.getInstance().getMessages();
		TOGGLE_SHOW_ARMS = InventoryMenu.namedItemStack(Material.STICK, messages.getInventory_toggleShowArms());
		TOGGLE_SHOW_BASEPLATE = InventoryMenu.namedItemStack(Material.STONE_PRESSURE_PLATE, messages.getInventory_toggleShowBaseplate());
		TOGGLE_SMALL_ARMORSTAND = InventoryMenu.namedItemStack(Material.TOTEM_OF_UNDYING, messages.getInventory_toggleSmallArmorstand());
		TOGGLE_INVULNERABILITY = InventoryMenu.namedItemStack(Material.ENCHANTED_GOLDEN_APPLE, messages.getInventory_toggleInvulnerability());
		TOGGLE_GRAVITY = InventoryMenu.namedItemStack(Material.FEATHER, messages.getInventory_toggleGravity());
		TOGGLE_VISIBILITY = InventoryMenu.namedItemStack(Material.GLASS, messages.getInventory_toggleVisibility());
		TOGGLE_SHOW_CUSTOM_NAME = InventoryMenu.namedItemStack(Material.NAME_TAG, messages.getInventory_toggleShowCustomName());
		TOGGLE_GLOWING = InventoryMenu.namedItemStack(Material.SEA_LANTERN, messages.getInventory_toggleGlowing());
		ATTACH_COMMANDS = InventoryMenu.namedItemStack(Material.COMMAND_BLOCK, messages.getInventory_attachCommands());
		TOGGLE_LOCK_ARMOR_STAND = InventoryMenu.namedItemStack(Material.TRIPWIRE_HOOK, messages.getInventory_toggleLockArmorStnad());
		TOGGLE_MARKER = InventoryMenu.namedItemStack(Material.LIGHT, messages.getInventory_toggleMarker());

		SET_EQUIP = InventoryMenu.namedItemStack(Material.IRON_CHESTPLATE, messages.getInventory_setEquip());
		CHANGE_EQUIP_LOCKS = InventoryMenu.namedItemStack(Material.GOLDEN_HELMET, messages.getInventory_equipLock_menuItem());
		HELMET_SLOT = InventoryMenu.namedItemStack(Material.LEATHER_HELMET, messages.getInventory_setEquipHelmet());
		CHEST_SLOT = InventoryMenu.namedItemStack(Material.LEATHER_CHESTPLATE, messages.getInventory_setEquipChest());
		LEG_SLOT = InventoryMenu.namedItemStack(Material.LEATHER_LEGGINGS, messages.getInventory_setEquipLegs());
		BOOTS_SLOT = InventoryMenu.namedItemStack(Material.LEATHER_BOOTS, messages.getInventory_setEquipBoots());
		MAINHAND_SLOT = InventoryMenu.namedItemStack(Material.IRON_SWORD, messages.getInventory_setEquipMainHand());
		OFFHAND_SLOT = InventoryMenu.namedItemStack(Material.TORCH, messages.getInventory_setEquipOffHand());

		CREATE_COPY = InventoryMenu.namedItemStack(Material.ARMOR_STAND, messages.getInventory_createCopy());

		GLASS_ENABLED = InventoryMenu.namedItemStack(Material.LIME_STAINED_GLASS_PANE, messages.getInventory_enabledName());
		GLASS_DISABLED = InventoryMenu.namedItemStack(Material.RED_STAINED_GLASS_PANE, messages.getInventory_disabledName());
	}

	public static ItemStack getEquipLockItem(ArmorStand armorStand, EquipmentSlot slot, ArmorStand.LockType type) {
		Messages messages = Main.getInstance().getMessages();
		boolean hasLock = armorStand.hasEquipmentLock(slot, type);
		Material glassMaterial = hasLock ? Material.RED_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE;
		String lockName = switch (type) {
			case ADDING -> messages.getInventory_equipLock_adding();
			case ADDING_OR_CHANGING -> messages.getInventory_equipLock_addingChanging();
			case REMOVING_OR_CHANGING -> messages.getInventory_equipLock_removingChanging();
		};
		lockName = Main.colorString(lockName);
		String lockedName = hasLock ? messages.getInventory_equipLock_locked() : messages.getInventory_equipLock_unlocked();
		lockedName = Main.colorString(lockedName);
		ItemStack itemStack = InventoryMenu.namedItemStack(glassMaterial, lockName);
		ItemMeta meta = itemStack.getItemMeta();
		if (meta != null) {
			meta.setLore(List.of(lockedName));
			itemStack.setItemMeta(meta);
		}
		return itemStack;
	}

}

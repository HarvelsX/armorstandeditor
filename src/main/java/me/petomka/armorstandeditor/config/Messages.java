package me.petomka.armorstandeditor.config;

import lombok.Getter;
import lombok.Setter;
import me.petomka.armorstandeditor.Main;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.checkerframework.checker.units.qual.C;

import java.io.File;

@Getter
@Setter
public class Messages extends Config {

	public Messages(Main main) throws InvalidConfigurationException {
		CONFIG_HEADER = new String[]{"Message configuration"};
		CONFIG_FILE = new File(main.getDataFolder(), "messages.yml");
		CONFIG_MODE = ConfigMode.DEFAULT;
		init();
	}

	private String playerNotFound = "&cA player called \"{name}\" was not found on this server.";

	@Comment("Message when trying to execute the armorstandeditor command without permission")
	private String noPermissionMessage = "&cYou do not have permission to do this.";

	@Comment("Message upon successful messages relaod")
	private String messagesSuccessfullyReloaded = "&amessages.yml successfully reloaded";

	@Comment("Message if reloading messages.yml fails")
	private String messagesReloadError = "&cError trying to reload messages.yml, take a look at the console for further detail.";

	@Comment("Message upon successful configuration reload")
	private String configSuccessfullyReloaded = "&aconfig.yml successfully reloaded";

	@Comment("Message if reloading config.yml fails")
	private String configReloadError = "&cError trying to reload config.yml, take a look at the console for further detail.";

	@Comment("What is to be displayed in action bar when editing, {part} will be replaced by the part currently being edited")
	private String editingNow = "&6Now editing {part}";

	@Comment("What is to be displayed as scoreboard title when editing")
	private String editingInfo = "&cEditing {part}";

	private String proModeGlowingNotAllowed = "&cSorry, but you are not allowed to toggle glowing effect.";

	private String cannotChangeYAxis = "&cSorry, but you are not allowed to move the armorstand along the y axis.";

	private String entityIsNotArmorStand = "&cThe provided entity is invalid or not an Armor Stand.";

	private String commands_addedCommand = "&aThe command was successfully appended.";

	private String commands_removedCommand = "&aThe command was successfully removed.";

	private String commands_invalidCommand = "&cThis command does not exist.";

	private String commands_updatedCommand = "&aSuccessfully updated the command.";

	private String commands_insertedCommand = "&aSuccessfully inserted the command.";

	private String commands_removedAll = "&aAll commands were removed from this armorstand.";

	@Comment("The headline of the list. <armorstand> will be replaced by the armor stand's custom name (if set) or uuid.")
	private String commands_list_head = "&6Attached commands to <armorstand>";

	private String commands_list_none = "&cNone";

	@Comment("List entry. <index> will be replaced by the respective index, <command> by the command line")
	private String commands_list_entry = "&7<index>: &f<command>";

	private String commands_actions_head = "&6What do you want to do? (Click an option)";

	private String commands_actions_add = "&2Add a command";

	private String commands_actions_remove = "&2Remove a command";

	private String commands_actions_update = "&2Update a command";

	private String commands_insert = "&2Insert a command before another command";

	private String commands_actions_list = "&2View all commands";

	private String commands_actions_removeAll = "&2Remove &call &2commands";

	@Comment("Name for each part")
	private String headName = "head";
	private String leftArmName = "left arm";
	private String rightArmName = "right arm";
	private String bellyName = "belly";
	private String leftLegName = "left leg";
	private String rightLegName = "right leg";
	private String bodyName = "body";
	private String rotationName = "rotation";

	private String editingDone = "&6You are no longer editing the armor stand.";

	@Comment("Scoreboard format for everything except rotation")
	private String singlePartScoreboardFormat = "&bX Axis:\\n{x}\\n\\n&bY Axis:\\n{y}\\n\\n&bZ Axis:\\n{z}";

	@Comment("Scoreboard format for editing the rotation angle")
	private String rotationPartScoreboardFormat = "&bYaw:\\n{yaw}";

	@Comment("What the BossBar displaying the adjustment size should say")
	private String bossBarTitle = "&6Current adjustment size: {size}";

	@Comment("What the BossBar displaying the search progress should say")
	private String searchBarTitle = "&9Searching for armor stands: {count} nearby";
	
	private String smallestAdjustmentName = "smallest (0.01)";
	private String smallerAdjustmentName = "smaller (0.05)";
	private String smallAdjustmentName = "small (0.1)";
	private String largeAdjustmentName = "large (0.3)";
	private String largerAdjustmentName = "larger (1)";
	private String largestAdjustmentName = "largest (5)";

	private String proModeStart = "&2Editing in pro mode";
	private String proModeEnd = "&2Returned to normal mode";

	private String inventory_title = "&2ArmorStand Properties";
	private String inventory_equipTitle = "&2ArmorStand Equipment";
	private String inventory_backItem = "&cBack";
	private String inventory_toggleShowArms = "&6Show arms";
	private String inventory_toggleShowBaseplate = "&6Show baseplate";
	private String inventory_toggleSmallArmorstand = "&6Small armorstand";
	private String inventory_toggleInvulnerability = "&6Invulnerability";
	private String inventory_toggleGravity = "&6Gravity";
	private String inventory_toggleVisibility = "&6Visible";
	private String inventory_toggleShowCustomName = "&6Show custom name";
	private String inventory_toggleGlowing = "&6Glowing";
	private String inventory_attachCommands = "&6Manage commands";
	private String inventory_toggleLockArmorStnad = "&6Editor locked";
	private String inventory_toggleMarker = "&6Marker";
	private String inventory_setEquip = "&6Set equipment";
	private String inventory_setEquipHelmet = "&6Helmet";
	private String inventory_setEquipChest = "&6Chestplate";
	private String inventory_setEquipLegs = "&6Leggings";
	private String inventory_setEquipBoots = "&6Boots";
	private String inventory_setEquipMainHand = "&6Main hand";
	private String inventory_setEquipOffHand = "&6Off hand";
	private String inventory_createCopy = "&6Create copy";
	private String inventory_enabledName = "&aEnabled";
	private String inventory_disabledName = "&cDisabled";
	private String inventory_equipLock_menuItem = "&6Set equipment locks";
	private String inventory_equipLock_title = "&2ArmorStand Equipment Locks";
	private String inventory_equipLock_adding = "&6Adding";
	private String inventory_equipLock_addingChanging = "&6Adding or changing";
	private String inventory_equipLock_removingChanging = "&6Removing or changing";
	private String inventory_equipLock_locked = "&clocked";
	private String inventory_equipLock_unlocked = "&aunlocked";

	private String copiedArmorStandItemName = "&rCopy of {name}";
	private String copyDefaultName = "Armorstand";
	private String copiedArmorStandLore = "&6&o(+ArmorStandEditor NBT)";

	private String toggle_on = "&2Enabled &aarmor stand editing for &6{name}";
	private String toggle_off = "&cDisabled &aarmor stand editing for &6{name}";

	private String armorStandLocked = "&cThis armor stand has been locked. You cannot edit it.";

	private String axisLocked = "&aYou have locked your axis.";
	private String axisUnlocked = "&aYou have unlocked your axis.";

	private String searchEnabled = "&aYou have enabled armor stand search.";
	private String searchDisabled = "&cYou have disabled armor stand search.";
}

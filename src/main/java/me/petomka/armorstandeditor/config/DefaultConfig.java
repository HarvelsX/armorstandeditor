package me.petomka.armorstandeditor.config;

import lombok.Getter;
import lombok.Setter;
import me.petomka.armorstandeditor.Main;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.Yamler.Config.Path;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@Setter
public class DefaultConfig extends Config {

	private static String bossBarColors = Arrays.stream(BarColor.values())
			.map(Enum::name)
			.collect(Collectors.joining(", "));

	private static String bossBarStyles = Arrays.stream(BarStyle.values())
			.map(Enum::name)
			.collect(Collectors.joining(", "));

	public DefaultConfig(Main main) throws InvalidConfigurationException {
		CONFIG_HEADER = new String[]{"Default configuration"};
		CONFIG_FILE = new File(main.getDataFolder(), "config.yml");
		addComment("bossBarColor", "What color the bar should have, available colors: " + bossBarColors);
		addComment("bossBarStyle", "What style the bar should have, available styles: " + bossBarStyles);

		init();
	}

	@Comment("Permission required to start editing an armorstand")
	private String editPermission = "armorstandedit.use";

	@Comment("Permission required to start editing an armorstand with more options")
	private String proEditPermission = "armorstandedit.pro";

	@Comment("Permission required to perform a reload of config.yml and messages.yml")
	private String reloadPermission = "armorstandedit.reload";

	@Comment("Permission required to enable or disable editing for other players")
	private String toggleOthersPermission = "armorstandedit.toggle_others";

	@Comment("Permission required to place armorstands with saved NBT data")
	private String placeNBTArmorStandPermission = "armorstandedit.placecopy";

	@Comment("Permission required to create a copy of an armor stand in the gui")
	private String copyArmorStandPermission = "armorstandedit.copy";

	@Comment("Permission similar to the above, but does not allow for GUI copies, just for dropping copies when destroyed.")
	private String dropCopyArmorStandPermission = "armorstandedit.drop_copy";

	@Comment("Permission required to give a colored name to an armorstand with a nametag")
	private String colorNameTagsPermission = "armorstandedit.colornametag";

	@Comment("Permission required to rotate an armorstand by left/right clicking with leather")
	private String rotateLeatherPermission = "armorstandedit.rotateleather";

	@Comment("Permission required to break an armorstand that is currently being edited")
	private String breakEditedArmorStandPermission = "armorstandedit.breakedited";

	@Comment("Whether a user can toggle an armorstand's arms")
	private String showArmsPermission = "armorstandedit.flag.show_arms";

	@Comment("Whether a user can toggle an armorstand's baseplate")
	private String showBasePlatePermission = "armorstandedit.flag.show_base_plate";

	@Comment("Whether a user can make an armorstand small")
	private String smallArmorStandPermission = "armorstandedit.flag.small_armorstand";

	@Comment("Whether a user can make an armorstand invulnerable")
	private String invulnerableArmorStandPermission = "armorstandedit.flag.invulnerable";

	@Comment("Whether a user can toggle an armorstand's gravity")
	private String gravityPermission = "armorstandedit.flag.gravity";

	@Comment("Whether a user can toggle an armorstand's visibility")
	private String visibilityPermission = "armorstandedit.flag.visibility";

	@Comment("Whether a user can toggle an armorstand's custom name")
	private String customNamePermission = "armorstandedit.flag.custom_name";

	@Comment("Whether a user can toggle an armorstand's glowing effect, also affects pro-mode!")
	private String glowingPermission = "armorstandedit.flag.glowing";

	@Comment("Whether a user can modify the marker flag of an armor stand.")
	private String markerArmorStandPermission = "armorstandedit.flag.marker";

	@Comment("Whether a user can directly set an armorstand's equipment via gui - does not affect normal equipping.")
	private String setEquipPermission = "armorstandedit.set_equip";

	@Comment("Whether a user can modify an armor stand's equipment locks")
	private String setEquipLocksPermission = "armorstandedit.set_equip_locks";

	@Comment("Whether a user can modify the commands attached to an armor stand.")
	private String attachCommandsPermission = "armorstandedit.attach_commands";

	@Comment("Whether a user can modify the locked state of an armor stand.")
	private String lockArmorStandPermission = "armorstandedit.lock_armorstand";

	@Comment("Whether a user can search for armor stands packed close together with a compass.")
	private String armorStandSearchPermission = "armorstandedit.search";

	@Comment("Whether custom armor stands should drop a copy of themselves when they are destroyed")
	private boolean dropCopyOfDestroyedArmorStand = true;

	@Comment("Angle to rotate armor stand in when clicked with leather")
	private int rotateLeatherDegrees = 45;

	@Comment("Display additional information about current armorstand in sidebar scoreboard?")
	private boolean scoreboardEnabled = true;

	@Comment("Display information about current editing precision in boss bar?")
	private boolean bossBarEnabled = true;

	@Comment("Provide audio feedback for actions")
	private boolean playSounds = true;

	@Comment("Whether an armor stands gravity should automatically be disabled on Y axis change. If true, users\n" +
			"# without the permission to disable the gravity will not be able to move the armor stand on the y axis.")
	private boolean disableGravityOnYPositionChange = true;

	@Comment("The maximum distance an armorstand will be able to be moved away from the editor")
	private float maximumEditDistance = 5.f;

	//Comment added manually in constructor
	@Path("bossBarColor")
	private String bossBarColor = BarColor.RED.name();

	//Comment added manually in constructor
	@Path("bossBarStyle")
	private String bossBarStyle = BarStyle.SEGMENTED_6.name();

	@Path("searchBarColor")
	private String searchBarColor = BarColor.BLUE.name();

	@Path("searchBarStyle")
	private String searchBarStyle = BarStyle.SOLID.name();

	@Comment("Default accuracy to begin editing with, possible values are: SMALLEST, SMALLER, SMALL, LARGE, LARGER, LARGEST")
	private String defaultAccuracy = "LARGE";

}

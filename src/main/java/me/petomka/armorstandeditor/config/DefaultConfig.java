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

	@Comment("Permission required to place armorstands with saved NBT data")
	private String placeNBTArmorStandPermission = "armorstandedit.placecopy";

	@Comment("Permission required to create a copy of an armor stand in the gui")
	private String copyArmorStandPermission = "armorstandedit.copy";

	@Comment("Permission required to give a colored name to an armorstand with a nametag")
	private String colorNameTagsPermission = "armorstandedit.colornametag";

	@Comment("Permission required to rotate an armorstand by left/right clicking with leather")
	private String rotateLeatherPermission = "armorstandedit.rotateleather";

	@Comment("Permission required to break an armorstand that is currently being edited")
	private String breakEditedArmorStandPermission = "armorstandedit.breakedited";

	@Comment("Wheterh custom armor stands should drop a copy of themselves when they are destroyed")
	private boolean dropCopyOfDestroyedArmorStand = true;

	@Comment("Angle to rotate armor stand in when clicked with leather")
	private int rotateLeatherDegrees = 45;

	@Comment("Display additional information about current armorstand in sidebar scoreboard?")
	private boolean scoreboardEnabled = true;

	@Comment("Display information about current editing precision in boss bar?")
	private boolean bossBarEnabled = true;

	@Comment("Provide audio feedback for actions")
	private boolean playSounds = true;

	//Comment added manually in constructor
	@Path("bossBarColor")
	private String bossBarColor = BarColor.RED.name();

	//Comment added manually in constructor
	@Path("bossBarStyle")
	private String bossBarStyle = BarStyle.SEGMENTED_6.name();

	@Comment("Default accuracy to begin editing with, possible values are: SMALLEST, SMALLER, SMALL, LARGE, LARGER, LARGEST")
	private String defaultAccuracy = "LARGE";

}

package me.petomka.armorstandeditor.handler;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.util.Scoreboardable;
import me.petomka.armorstandeditor.util.XYZ;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public enum Part {

	HEAD(Main.getInstance().getMessages()::getHeadName, a -> Part.fromEuler(a.getHeadPose()),
			(armorStand, xyz) -> armorStand.setHeadPose(addXYZ(armorStand.getHeadPose(), xyz))),

	LEFT_ARM(Main.getInstance().getMessages()::getLeftArmName, a -> Part.fromEuler(a.getLeftArmPose()),
			(armorStand, xyz) -> armorStand.setLeftArmPose(addXYZ(armorStand.getLeftArmPose(), xyz))),

	RIGHT_ARM(Main.getInstance().getMessages()::getRightArmName, a -> Part.fromEuler(a.getRightArmPose()),
			(armorStand, xyz) -> armorStand.setRightArmPose(addXYZ(armorStand.getRightArmPose(), xyz))),

	BELLY(Main.getInstance().getMessages()::getBellyName, a -> Part.fromEuler(a.getBodyPose()),
			(armorStand, xyz) -> armorStand.setBodyPose(addXYZ(armorStand.getBodyPose(), xyz))),

	LEFT_LEG(Main.getInstance().getMessages()::getLeftLegName, a -> Part.fromEuler(a.getLeftLegPose()),
			(armorStand, xyz) -> armorStand.setLeftLegPose(addXYZ(armorStand.getLeftLegPose(), xyz))),

	RIGHT_LEG(Main.getInstance().getMessages()::getRightLegName, a -> Part.fromEuler(a.getRightLegPose()),
			(armorStand, xyz) -> armorStand.setRightLegPose(addXYZ(armorStand.getRightLegPose(), xyz))),

	BODY(Main.getInstance().getMessages()::getBodyName, a -> Part.fromLocation(a.getLocation()),
			(armorStand, xyz) -> {
				Location loc = armorStand.getLocation();
				loc.add(xyz.x, xyz.y, xyz.z);
				armorStand.teleport(loc);
			}),

	ROTATION(Main.getInstance().getMessages()::getRotationName, a -> (
			() -> {
				String format[] = Main.getInstance().getMessages().getRotationPartScoreboardFormat().split("\\\\n");

				int emptyLine = 0;

				for (int i = 0; i < format.length; i++) {
					String line = format[i];
					if (line.equals("")) {
						format[i] = ChatColor.values()[emptyLine++] + "";
					} else {
						format[i] = line.replace("{yaw}", Main.formatDouble(a.getLocation().getYaw()));
					}
				}
				int lineNum = format.length;

				Map<String, Integer> map = Maps.newHashMap();
				for (String line : format) {
					map.putIfAbsent(line, lineNum--);
				}
				return map;
			}),
			(armorStand, xyz) -> {
				Location loc = armorStand.getLocation();
				loc.setYaw((float) (loc.getYaw() + (xyz.sum() > 0 ? xyz.length() : xyz.length() * -1) * 9f));
				armorStand.teleport(loc);
			});

	private static XYZ fromEuler(EulerAngle eulerAngle) {
		return XYZ.of(eulerAngle.getX(), eulerAngle.getY(), eulerAngle.getZ());
	}

	private static XYZ fromLocation(Location location) {
		return XYZ.of(location.getX(), location.getY(), location.getZ());
	}

	private static EulerAngle addXYZ(EulerAngle angle, XYZ xyz) {
		return angle.add(xyz.x, xyz.y, xyz.z);
	}

	private final Supplier<String> partNameSupplier;

	private final Function<ArmorStand, Scoreboardable> toScoreboardable;

	private final BiConsumer<ArmorStand, XYZ> addFunction;

	public String getEditingString() {
		return Main.colorString(Main.getInstance().getMessages().getEditingNow())
				.replace("{part}", partNameSupplier.get());
	}

	public String getEditingInfo() {
		return Main.colorString(Main.getInstance().getMessages().getEditingInfo())
				.replace("{part}", partNameSupplier.get());
	}

	public Part nextPart() {
		return Part.values()[(this.ordinal() + 1) % Part.values().length];
	}

	public Part previousPart() {
		return Part.values()[Math.floorMod(this.ordinal() - 1, Part.values().length)];
	}

	public void add(ArmorStand armorStand, double x, double y, double z) {
		addFunction.accept(armorStand, XYZ.of(x, y, z));
	}

	public Map<String, Integer> getScoreboardInfo(ArmorStand armorStand) {
		return toScoreboardable.apply(armorStand).toScoreboardInfo();
	}

}

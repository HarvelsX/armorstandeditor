package me.petomka.armorstandeditor.util;

import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.config.Messages;
import me.petomka.armorstandeditor.inventory.InventoryMenu;
import me.petomka.armorstandeditor.nbt.NbtItem;
import me.petomka.armorstandeditor.nbt.NbtTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

@UtilityClass
public class ArmorStandUtils {

	private final String NBT_TAG_KEY = "armorStandEdit";

	public ItemStack saveToItem(ArmorStand armorStand, Player player) {
		boolean hasCustomName = armorStand.getCustomName() != null && !armorStand.getCustomName().isEmpty();
		Messages messages = Main.getInstance().getMessages();
		String nameReplacement;

		double y = armorStand.getLocation().getY() - player.getLocation().getY();

		if (hasCustomName) {
			nameReplacement = armorStand.getCustomName();
		} else {
			nameReplacement = messages.getCopyDefaultName();
		}

		ItemStack copyItem = InventoryMenu.namedItemStack(Material.ARMOR_STAND,
				Main.colorString(messages.getCopiedArmorStandItemName().replace("{name}", nameReplacement)));
		ItemMeta meta = copyItem.getItemMeta();
		List<String> lore = Arrays.stream(messages.getCopiedArmorStandLore().split("\\\\n"))
				.map(Main::colorString)
				.collect(Collectors.toList());
		meta.setLore(lore);
		copyItem.setItemMeta(meta);

		NbtItem nbtItem = new NbtItem(copyItem);
		NbtTag nbtItemTag = nbtItem.getNbtTagCompound();

		NbtTag nbtTag = new NbtTag();

		nbtTag.setBoolean("arms", armorStand.hasArms());
		nbtTag.setBoolean("baseplate", armorStand.hasBasePlate());
		nbtTag.setBoolean("small", armorStand.isSmall());
		nbtTag.setBoolean("invulnerable", armorStand.isInvulnerable());
		nbtTag.setBoolean("gravity", armorStand.hasGravity());
		nbtTag.setBoolean("visible", armorStand.isVisible());
		nbtTag.setBoolean("customNameVisible", armorStand.isCustomNameVisible());
		nbtTag.setBoolean("glowing", armorStand.isGlowing());

		nbtTag.setByteArray("headPose", ArmorStandUtils.serializeEulerAngle(armorStand.getHeadPose()));
		nbtTag.setByteArray("leftArmPose", ArmorStandUtils.serializeEulerAngle(armorStand.getLeftArmPose()));
		nbtTag.setByteArray("rightArmPose", ArmorStandUtils.serializeEulerAngle(armorStand.getRightArmPose()));
		nbtTag.setByteArray("leftLegPose", ArmorStandUtils.serializeEulerAngle(armorStand.getLeftLegPose()));
		nbtTag.setByteArray("rightLegPose", ArmorStandUtils.serializeEulerAngle(armorStand.getRightLegPose()));
		nbtTag.setByteArray("bodyPose", ArmorStandUtils.serializeEulerAngle(armorStand.getBodyPose()));
		nbtTag.setDouble("rotation", armorStand.getLocation().getYaw());
		nbtTag.setDouble("y", y);

		if (hasCustomName) {
			nbtTag.setString("customName", armorStand.getCustomName());
		}

		if (armorStand.getHelmet() != null) {
			nbtTag.setByteArray("helmet", Objects.requireNonNull(ArmorStandUtils.serializeConfigurationSerializable(armorStand.getHelmet())));
		}
		if (armorStand.getChestplate() != null) {
			nbtTag.setByteArray("chestplate", Objects.requireNonNull(ArmorStandUtils.serializeConfigurationSerializable(armorStand.getChestplate())));
		}
		if (armorStand.getLeggings() != null) {
			nbtTag.setByteArray("leggings", Objects.requireNonNull(ArmorStandUtils.serializeConfigurationSerializable(armorStand.getLeggings())));
		}
		if (armorStand.getBoots() != null) {
			nbtTag.setByteArray("boots", Objects.requireNonNull(ArmorStandUtils.serializeConfigurationSerializable(armorStand.getBoots())));
		}
		if (armorStand.getItemInHand() != null) {
			nbtTag.setByteArray("itemInHand", Objects.requireNonNull(ArmorStandUtils.serializeConfigurationSerializable(armorStand.getItemInHand())));
		}
		if (armorStand.getEquipment().getItemInOffHand() != null) {
			nbtTag.setByteArray("itemInOffHand", Objects.requireNonNull(ArmorStandUtils.serializeConfigurationSerializable(armorStand.getEquipment().getItemInOffHand())));
		}

		nbtItemTag.set(ArmorStandUtils.NBT_TAG_KEY, nbtTag);
		nbtItem.setTag(nbtItemTag);

		return nbtItem.toBukkitItem();
	}

	public boolean isCopiedArmorStand(ItemStack itemStack) {
		NbtItem item = new NbtItem(itemStack);
		return item.getNbtTagCompound().hasKey(NBT_TAG_KEY);
	}

	public boolean isCopyWorthIt(ArmorStand armorStand) {
		boolean euler = isEulerNormal(armorStand.getHeadPose(), 0.2)
				&& isEulerNormal(armorStand.getLeftArmPose(), 0.3)
				&& isEulerNormal(armorStand.getRightArmPose(), 0.3)
				&& isEulerNormal(armorStand.getLeftLegPose(), 0.1)
				&& isEulerNormal(armorStand.getRightLegPose(), 0.1)
				&& isEulerNormal(armorStand.getBodyPose(), 0.2);
		if (!euler) {
			return true;
		}
		boolean item = isItemThere(armorStand.getHelmet())
				|| isItemThere(armorStand.getChestplate())
				|| isItemThere(armorStand.getLeggings())
				|| isItemThere(armorStand.getBoots())
				|| isItemThere(armorStand.getEquipment().getItemInMainHand())
				|| isItemThere(armorStand.getEquipment().getItemInOffHand());
		if (item) {
			return true;
		}
		return armorStand.isGlowing()
				|| armorStand.hasArms()
				|| !armorStand.hasBasePlate()
				|| !armorStand.hasGravity()
				|| armorStand.isInvulnerable()
				|| armorStand.isCustomNameVisible()
				|| armorStand.isSmall();
	}

	private boolean isEulerNormal(EulerAngle angle, double threshold) {
		double x = Math.abs(angle.getX());
		double y = Math.abs(angle.getY());
		double z = Math.abs(angle.getZ());
		return x <= threshold && x >= -threshold
				&& y <= threshold && y >= -threshold
				&& z <= threshold && z >= -threshold;
	}

	private boolean isItemThere(ItemStack itemStack) {
		return itemStack != null && itemStack.getType() != Material.AIR;
	}

	public void loadFromItem(ItemStack itemStack, ArmorStand armorStand) {
		NbtItem nbtItem = new NbtItem(itemStack);
		NbtTag nbtTag = nbtItem.getNbtTagCompound();
		if (!nbtTag.hasKey(ArmorStandUtils.NBT_TAG_KEY)) {
			return;
		}

		nbtTag = nbtTag.getNbtTag(ArmorStandUtils.NBT_TAG_KEY);
		if (nbtTag == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Wrong value stored for NBT key + \"" + ArmorStandUtils.NBT_TAG_KEY + "\"");
			return;
		}

		armorStand.setArms(nbtTag.getBoolean("arms"));
		armorStand.setBasePlate(nbtTag.getBoolean("baseplate"));
		armorStand.setSmall(nbtTag.getBoolean("small"));
		armorStand.setInvulnerable(nbtTag.getBoolean("invulnerable"));
		armorStand.setGravity(nbtTag.getBoolean("gravity"));
		armorStand.setVisible(nbtTag.getBoolean("visible"));
		armorStand.setCustomNameVisible(nbtTag.getBoolean("customNameVisible"));
		armorStand.setGlowing(nbtTag.getBoolean("glowing"));

		armorStand.setHeadPose(ArmorStandUtils.deserializeToEuler(nbtTag.getByteArray("headPose")));
		armorStand.setLeftArmPose(ArmorStandUtils.deserializeToEuler(nbtTag.getByteArray("leftArmPose")));
		armorStand.setRightArmPose(ArmorStandUtils.deserializeToEuler(nbtTag.getByteArray("rightArmPose")));
		armorStand.setLeftLegPose(ArmorStandUtils.deserializeToEuler(nbtTag.getByteArray("leftLegPose")));
		armorStand.setRightLegPose(ArmorStandUtils.deserializeToEuler(nbtTag.getByteArray("rightLegPose")));
		armorStand.setBodyPose(ArmorStandUtils.deserializeToEuler(nbtTag.getByteArray("bodyPose")));

		final float yaw = (float) nbtTag.getDouble("rotation");
		final double y = nbtTag.getDouble("y");

		if (nbtTag.hasKey("customName")) {
			armorStand.setCustomName(nbtTag.getString("customName"));
		}

		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
			Location location = armorStand.getLocation();
			location.add(0, y, 0);
			armorStand.teleport(location);
		}, 1L);
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
			Location location = armorStand.getLocation();
			location.setYaw(yaw);
			armorStand.teleport(location);
		}, 2L);

		if (nbtTag.hasKey("helmet")) {
			armorStand.setHelmet(ArmorStandUtils.deserializeItemStack(nbtTag.getByteArray("helmet")));
		}
		if (nbtTag.hasKey("chestplate")) {
			armorStand.setChestplate(ArmorStandUtils.deserializeItemStack(nbtTag.getByteArray("chestplate")));
		}
		if (nbtTag.hasKey("leggings")) {
			armorStand.setLeggings(ArmorStandUtils.deserializeItemStack(nbtTag.getByteArray("leggings")));
		}
		if (nbtTag.hasKey("boots")) {
			armorStand.setBoots(ArmorStandUtils.deserializeItemStack(nbtTag.getByteArray("boots")));
		}
		if (nbtTag.hasKey("itemInHand")) {
			armorStand.setItemInHand(ArmorStandUtils.deserializeItemStack(nbtTag.getByteArray("itemInHand")));
		}
		if (nbtTag.hasKey("itemInOffHand")) {
			armorStand.getEquipment().setItemInOffHand(ArmorStandUtils.deserializeItemStack(nbtTag.getByteArray("itemInOffHand")));
		}
	}

	private byte[] serializeEulerAngle(EulerAngle angle) {
		XYZ xyz = new XYZ(angle.getX(), angle.getY(), angle.getZ());
		return ArmorStandUtils.serializeObject(xyz);
	}

	private EulerAngle deserializeToEuler(byte[] bytes) {
		XYZ xyz = (XYZ) ArmorStandUtils.deserializeObject(bytes);
		if (xyz == null) {
			return new EulerAngle(0, 0, 0);
		}
		return new EulerAngle(xyz.x, xyz.y, xyz.z);
	}

	private byte[] serializeConfigurationSerializable(ConfigurationSerializable itemStack) {
		return ArmorStandUtils.serializeObject((Serializable) ArmorStandUtils.fullySerialize(itemStack));
	}

	private Map<String, Object> fullySerialize(ConfigurationSerializable serializable) {
		Map<String, Object> objectMap = Maps.newHashMap(serializable.serialize());
		objectMap.put("==", ConfigurationSerialization.getAlias(serializable.getClass()));
		for (Map.Entry<String, Object> objectEntry : objectMap.entrySet()) {
			if (objectEntry.getValue() instanceof ConfigurationSerializable) {
				objectEntry.setValue(serializeConfigurationSerializable((ConfigurationSerializable) objectEntry.getValue()));
			}
		}
		return objectMap;
	}

	private byte[] serializeObject(Serializable serializable) {
		ObjectOutput out = null;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			out = new ObjectOutputStream(bos);
			out.writeObject(serializable);
			out.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object deserializeObject(byte[] bytes) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
			try (ObjectInput in = new ObjectInputStream(bis)) {
				return in.readObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ItemStack deserializeItemStack(byte[] bytes) {
		return (ItemStack) ArmorStandUtils.fullyDeserializeConfigurationSerializable(bytes);
	}

	private Object fullyDeserializeConfigurationSerializable(byte[] bytes) {
		Map<String, Object> o = (Map<String, Object>) deserializeObject(bytes);
		for (Map.Entry<String, Object> objectEntry : o.entrySet()) {
			if (objectEntry.getValue() instanceof byte[]) {
				objectEntry.setValue(fullyDeserializeConfigurationSerializable((byte[]) objectEntry.getValue()));
			}
		}
		return ConfigurationSerialization.deserializeObject(o);
	}
}

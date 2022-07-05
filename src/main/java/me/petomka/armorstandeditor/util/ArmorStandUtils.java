package me.petomka.armorstandeditor.util;

import lombok.experimental.UtilityClass;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.config.Messages;
import me.petomka.armorstandeditor.inventory.InventoryMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@UtilityClass
public class ArmorStandUtils {

    private final String NBT_TAG_KEY = "armorStandEdit";

    private static NamespacedKey key(String key) {
        return new NamespacedKey(Main.getInstance(), key);
    }

    private static void setBoolean(PersistentDataContainer container, String key, boolean value) {
        container.set(key(key), PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }

    private static boolean getBoolean(PersistentDataContainer container, String key) {
        return container.getOrDefault(key(key), PersistentDataType.BYTE, (byte) 0) != 0;
    }

    private static void setByteArray(PersistentDataContainer container, String key, byte[] value) {
        container.set(key(key), PersistentDataType.BYTE_ARRAY, value);
    }

    private static byte[] getByteArray(PersistentDataContainer container, String key) {
        return container.get(key(key), PersistentDataType.BYTE_ARRAY);
    }

    private static void setDouble(PersistentDataContainer container, String key, double value) {
        container.set(key(key), PersistentDataType.DOUBLE, value);
    }

    private static double getDouble(PersistentDataContainer container, String key) {
        return container.getOrDefault(key(key), PersistentDataType.DOUBLE, 0.);
    }

    private static void setString(PersistentDataContainer container, String key, String value) {
        container.set(key(key), PersistentDataType.STRING, value);
    }

    private static String getString(PersistentDataContainer container, String key) {
        return container.get(key(key), PersistentDataType.STRING);
    }

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
        assert meta != null;
        List<String> lore = Arrays.stream(messages.getCopiedArmorStandLore().split("\\\\n"))
                .map(Main::colorString)
                .collect(Collectors.toList());
        meta.setLore(lore);
        copyItem.setItemMeta(meta);

        PersistentDataContainer parentContainer = meta.getPersistentDataContainer();

        PersistentDataContainer dataContainer = parentContainer.getAdapterContext().newPersistentDataContainer();

        setBoolean(dataContainer, "arms", armorStand.hasArms());
        setBoolean(dataContainer, "baseplate", armorStand.hasBasePlate());
        setBoolean(dataContainer, "small", armorStand.isSmall());
        setBoolean(dataContainer, "invulnerable", armorStand.isInvulnerable());
        setBoolean(dataContainer, "gravity", armorStand.hasGravity());
        setBoolean(dataContainer, "visible", armorStand.isVisible());
        setBoolean(dataContainer, "customNameVisible", armorStand.isCustomNameVisible());
        setBoolean(dataContainer, "glowing", armorStand.isGlowing());

        setByteArray(dataContainer, "headPose", ArmorStandUtils.serializeEulerAngle(armorStand.getHeadPose()));
        setByteArray(dataContainer, "leftArmPose", ArmorStandUtils.serializeEulerAngle(armorStand.getLeftArmPose()));
        setByteArray(dataContainer, "rightArmPose", ArmorStandUtils.serializeEulerAngle(armorStand.getRightArmPose()));
        setByteArray(dataContainer, "leftLegPose", ArmorStandUtils.serializeEulerAngle(armorStand.getLeftLegPose()));
        setByteArray(dataContainer, "rightLegPose", ArmorStandUtils.serializeEulerAngle(armorStand.getRightLegPose()));
        setByteArray(dataContainer, "bodyPose", ArmorStandUtils.serializeEulerAngle(armorStand.getBodyPose()));

        setDouble(dataContainer, "rotation", armorStand.getLocation().getYaw());
        setDouble(dataContainer, "y", y);

        if (hasCustomName) {
            setString(dataContainer, "customName", armorStand.getCustomName());
        }

        EntityEquipment equipment = armorStand.getEquipment();

        if (equipment != null) {
            if (equipment.getHelmet() != null) {
                setByteArray(dataContainer, "helmet", serializeBukkitObject(equipment.getHelmet()));
            }
            if (equipment.getChestplate() != null) {
                setByteArray(dataContainer, "chestplate", serializeBukkitObject(equipment.getChestplate()));
            }
            if (equipment.getLeggings() != null) {
                setByteArray(dataContainer, "leggings", serializeBukkitObject(equipment.getLeggings()));
            }
            if (equipment.getBoots() != null) {
                setByteArray(dataContainer, "boots", serializeBukkitObject(equipment.getBoots()));
            }
            if (equipment.getItemInMainHand() != null) {
                setByteArray(dataContainer, "itemInHand", serializeBukkitObject(equipment.getItemInMainHand()));
            }
            if (equipment.getItemInOffHand() != null) {
                setByteArray(dataContainer, "itemInOffHand", serializeBukkitObject(equipment.getItemInOffHand()));
            }
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                boolean locked = armorStand.hasEquipmentLock(equipmentSlot, lockType);
                setBoolean(dataContainer, getLockTypeKey(equipmentSlot, lockType), locked);
            }
        }

        parentContainer.set(key(NBT_TAG_KEY), PersistentDataType.TAG_CONTAINER, dataContainer);

        copyItem.setItemMeta(meta);

        return copyItem;
    }

    public boolean isCopiedArmorStand(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        return container.has(key(NBT_TAG_KEY), PersistentDataType.TAG_CONTAINER);
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
        if (!isCopiedArmorStand(itemStack)) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        PersistentDataContainer parentContainer = itemMeta.getPersistentDataContainer();
        PersistentDataContainer dataContainer = parentContainer.get(key(NBT_TAG_KEY), PersistentDataType.TAG_CONTAINER);
        if (dataContainer == null) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Wrong value stored for NBT key + \"" + ArmorStandUtils.NBT_TAG_KEY + "\"");
            return;
        }

        armorStand.setArms(getBoolean(dataContainer, "arms"));
        armorStand.setBasePlate(getBoolean(dataContainer, "baseplate"));
        armorStand.setSmall(getBoolean(dataContainer, "small"));
        armorStand.setInvulnerable(getBoolean(dataContainer, "invulnerable"));
        armorStand.setGravity(getBoolean(dataContainer, "gravity"));
        armorStand.setVisible(getBoolean(dataContainer, "visible"));
        armorStand.setCustomNameVisible(getBoolean(dataContainer, "customNameVisible"));
        armorStand.setGlowing(getBoolean(dataContainer, "glowing"));

        armorStand.setHeadPose(ArmorStandUtils.deserializeToEuler(getByteArray(dataContainer, "headPose")));
        armorStand.setLeftArmPose(ArmorStandUtils.deserializeToEuler(getByteArray(dataContainer, "leftArmPose")));
        armorStand.setRightArmPose(ArmorStandUtils.deserializeToEuler(getByteArray(dataContainer, "rightArmPose")));
        armorStand.setLeftLegPose(ArmorStandUtils.deserializeToEuler(getByteArray(dataContainer, "leftLegPose")));
        armorStand.setRightLegPose(ArmorStandUtils.deserializeToEuler(getByteArray(dataContainer, "rightLegPose")));
        armorStand.setBodyPose(ArmorStandUtils.deserializeToEuler(getByteArray(dataContainer, "bodyPose")));

        final float yaw = (float) getDouble(dataContainer, "rotation");
        final double y = getDouble(dataContainer, "y");

        if (dataContainer.has(key("customName"), PersistentDataType.STRING)) {
            armorStand.setCustomName(getString(dataContainer, "customName"));
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

        EntityEquipment equipment = armorStand.getEquipment();

        if (equipment != null) {
            if (dataContainer.has(key("helmet"), PersistentDataType.BYTE_ARRAY)) {
                equipment.setHelmet((ItemStack) deserializeBukkitObject(getByteArray(dataContainer, "helmet")));
            }
            if (dataContainer.has(key("chestplate"), PersistentDataType.BYTE_ARRAY)) {
                equipment.setChestplate((ItemStack) deserializeBukkitObject(getByteArray(dataContainer, "chestplate")));
            }
            if (dataContainer.has(key("leggings"), PersistentDataType.BYTE_ARRAY)) {
                equipment.setLeggings((ItemStack) deserializeBukkitObject(getByteArray(dataContainer, "leggings")));
            }
            if (dataContainer.has(key("boots"), PersistentDataType.BYTE_ARRAY)) {
                equipment.setBoots((ItemStack) deserializeBukkitObject(getByteArray(dataContainer, "boots")));
            }
            if (dataContainer.has(key("itemInHand"), PersistentDataType.BYTE_ARRAY)) {
                equipment.setItemInMainHand((ItemStack) deserializeBukkitObject(getByteArray(dataContainer, "itemInHand")));
            }
            if (dataContainer.has(key("itemInOffHand"), PersistentDataType.BYTE_ARRAY)) {
                equipment.setItemInOffHand((ItemStack) deserializeBukkitObject(getByteArray(dataContainer, "itemInOffHand")));
            }
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (ArmorStand.LockType lockType : ArmorStand.LockType.values()) {
                boolean locked = getBoolean(dataContainer, getLockTypeKey(slot, lockType));
                if (locked) {
                    armorStand.addEquipmentLock(slot, lockType);
                } else {
                    armorStand.removeEquipmentLock(slot, lockType);
                }
            }
        }
    }

    private String getLockTypeKey(EquipmentSlot slot, ArmorStand.LockType lockType) {
        return slot.name() + "." + lockType.name();
    }

    private byte[] serializeEulerAngle(EulerAngle angle) {
        XYZ xyz = new XYZ(angle.getX(), angle.getY(), angle.getZ());
        return ArmorStandUtils.serializeObject(xyz);
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

    private EulerAngle deserializeToEuler(byte[] bytes) {
        XYZ xyz = (XYZ) ArmorStandUtils.deserializeObject(bytes);
        if (xyz == null) {
            return new EulerAngle(0, 0, 0);
        }
        return new EulerAngle(xyz.x, xyz.y, xyz.z);
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

    private byte[] serializeBukkitObject(ConfigurationSerializable serializable) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream bukkitOut = new BukkitObjectOutputStream(byteOut)) {
                bukkitOut.writeObject(serializable);
            }
            return byteOut.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ConfigurationSerializable deserializeBukkitObject(byte[] bytes) {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes)) {
            try (BukkitObjectInputStream bukkitIn = new BukkitObjectInputStream(byteIn)) {
                return (ConfigurationSerializable) bukkitIn.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}

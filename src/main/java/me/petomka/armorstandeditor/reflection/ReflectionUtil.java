package me.petomka.armorstandeditor.reflection;

import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;
import me.petomka.armorstandeditor.Main;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@UtilityClass
public class ReflectionUtil {

	private Class<?> craftItemStack;
	private Class<?> nbtTagCompound;
	private Class<?> nbtBase;
	private Class<?> nmsItemStack;

	private Constructor<?> nbtTagCompoundConstructor;

	private Method craftItemStackAsNmsCopy;
	private Method craftItemStackAsBukkitCopy;

	private Method nbtTagCompoundSetString;
	private Method nbtTagCompoundSetInt;
	private Method nbtTagCompoundSetBoolean;
	private Method nbtTagCompoundSetByteArray;
	private Method nbtTagCompoundSetDouble;
	private Method nbtTagCompoundSet;
	private Method nbtTagCompoundGetKeys;
	private Method nbtTagCompoundGetString;
	private Method nbtTagCompoundGetInt;
	private Method nbtTagCompoundGetBoolean;
	private Method nbtTagCompoundGetByteArray;
	private Method nbtTagCompoundGetDouble;
	private Method nbtTagCompoundGet;
	private Method nbtTagCompoundHasKey;
	private Method nmsItemStackGetOrCreateTag;
	private Method nmsItemStackSetTag;

	private final String NMS_NAMESPACE = "net.minecraft.server";
	private final String CRAFTBUKKIT_NAMESPACE = "org.bukkit.craftbukkit";

	private final String VERSION;

	private final Class[] NO_PARAMETERS = new Class[0];
	private final Object[] NO_ARGUMENTS = new Object[0];

	static {
		String path = Bukkit.getServer().getClass().getPackage().getName();
		VERSION = path.substring(path.lastIndexOf('.') + 1);

		try {
			nbtTagCompound = getNmsClass("NBTTagCompound");
			nbtBase = getNmsClass("NBTBase");
			nmsItemStack = getNmsClass("ItemStack");

			nbtTagCompoundConstructor = nbtTagCompound.getConstructor(NO_PARAMETERS);

			craftItemStack = getCraftBukkitClass("inventory.CraftItemStack");

			craftItemStackAsNmsCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
			craftItemStackAsBukkitCopy = craftItemStack.getMethod("asBukkitCopy", nmsItemStack);

			nbtTagCompoundSetString = nbtTagCompound.getMethod("setString", String.class, String.class);
			nbtTagCompoundSetInt = nbtTagCompound.getMethod("setInt", String.class, int.class);
			nbtTagCompoundSetBoolean = nbtTagCompound.getMethod("setBoolean", String.class, boolean.class);
			nbtTagCompoundSetByteArray = nbtTagCompound.getDeclaredMethod("setByteArray", String.class, byte[].class);
			nbtTagCompoundSetDouble = nbtTagCompound.getMethod("setDouble", String.class, double.class);
			nbtTagCompoundSet = nbtTagCompound.getMethod("set", String.class, nbtBase);

			nbtTagCompoundGetKeys = nbtTagCompound.getMethod("getKeys", NO_PARAMETERS);
			nbtTagCompoundGetString = nbtTagCompound.getMethod("getString", String.class);
			nbtTagCompoundGetInt = nbtTagCompound.getMethod("getInt", String.class);
			nbtTagCompoundGetBoolean = nbtTagCompound.getMethod("getBoolean", String.class);
			nbtTagCompoundGetByteArray = nbtTagCompound.getDeclaredMethod("getByteArray", String.class);
			nbtTagCompoundGetDouble = nbtTagCompound.getMethod("getDouble", String.class);
			nbtTagCompoundGet = nbtTagCompound.getMethod("get", String.class);
			nbtTagCompoundHasKey = nbtTagCompound.getMethod("hasKey", String.class);

			nmsItemStackGetOrCreateTag = nmsItemStack.getMethod("getOrCreateTag");
			nmsItemStackSetTag = nmsItemStack.getMethod("setTag", nbtTagCompound);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE,
					"Error loading NMS Classes, are you using the right version?", e);
		}
	}

	public Class<?> getNmsClass(String name) throws ClassNotFoundException {
		return Class.forName(NMS_NAMESPACE + "." + VERSION + "." + name);
	}

	public Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
		return Class.forName(CRAFTBUKKIT_NAMESPACE + "." + VERSION + "." + name);
	}

	public Object itemStackAsNmsCopy(ItemStack itemStack) {
		try {
			return craftItemStackAsNmsCopy.invoke(null, itemStack);
		} catch (IllegalAccessException | InvocationTargetException e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Error creating NMS ItemStack", e);
		}
		return null;
	}


	public static Object newNbtTagCompound() {
		try {
			return nbtTagCompoundConstructor.newInstance(NO_ARGUMENTS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean nbtTagCompoundHasKey(Object nbtTagCompound, String key) {
		try {
			return (boolean) nbtTagCompoundHasKey.invoke(nbtTagCompound, key);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return false;
	}

	public static Set<String> nbtTagCompoundGetKeySet(Object nbtTagCompound) {
		try {
			return (Set<String>) nbtTagCompoundGetKeys.invoke(nbtTagCompound);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return ImmutableSet.of();
	}

	public static Object nbtTagCompoundGet(Object nbtTagCompound, String key) {
		try {
			return nbtTagCompoundGet.invoke(nbtTagCompound, key);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return null;
	}

	public static String nbtTagCompoundGetString(Object nbtTagCompound, String key) {
		try {
			return (String) nbtTagCompoundGetString.invoke(nbtTagCompound, key);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return null;
	}

	public static int nbtTagCompoundGetInt(Object nbtTagCompound, String key) {
		try {
			return (int) nbtTagCompoundGetInt.invoke(nbtTagCompound, key);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return 0;
	}

	public static boolean nbtTagCompoundGetBoolean(Object nbtTagCompound, String key) {
		try {
			return (boolean) nbtTagCompoundGetBoolean.invoke(nbtTagCompound, key);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return false;
	}

	public static byte[] nbtTagCompoundGetByteArray(Object nbtTagCompound, String key) {
		try {
			return (byte[]) nbtTagCompoundGetByteArray.invoke(nbtTagCompound, key);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return null;
	}

	public static double nbtTagCompoundGetDouble(Object nbtTagCompound, String key) {
		try {
			return (double) nbtTagCompoundGetDouble.invoke(nbtTagCompound, key);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return 0D;
	}

	public static void nbtTagCompoundSetString(Object nbtTagCompound, String key, String value) {
		try {
			nbtTagCompoundSetString.invoke(nbtTagCompound, key, value);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
	}

	public static void nbtTagCompoundSetInt(Object nbtTagCompound, String key, int value) {
		try {
			nbtTagCompoundSetInt.invoke(nbtTagCompound, key, value);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
	}

	public static void nbtTagCompoundSetBoolean(Object nbtTagCompound, String key, boolean value) {
		try {
			nbtTagCompoundSetBoolean.invoke(nbtTagCompound, key, value);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
	}

	public static void nbtTagCompoundSetByteArray(Object nbtTagCompound, String key, byte[] bytes) {
		try {
			nbtTagCompoundSetByteArray.invoke(nbtTagCompound, key, bytes);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
	}

	public static void nbtTagCompoundSetDouble(Object nbtTagCompound, String key, double value) {
		try {
			nbtTagCompoundSetDouble.invoke(nbtTagCompound, key, value);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
	}

	public static void nbtTagCompoundSet(Object nbtTagCompound, String key, Object nbtTagCompound1) {
		try {
			nbtTagCompoundSet.invoke(nbtTagCompound, key, nbtTagCompound1);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
	}

	public static Object craftItemStackAsNmsCopy(ItemStack itemStack) {
		try {
			return craftItemStackAsNmsCopy.invoke(null, itemStack);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return null;
	}

	public static Object itemStackGetOrCreateTag(Object nmsStack) {
		try {
			return nmsItemStackGetOrCreateTag.invoke(nmsStack, NO_ARGUMENTS);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return null;
	}

	public static void itemStackSetTag(Object nmsStack, Object nbtTagCompound) {
		try {
			nmsItemStackSetTag.invoke(nmsStack, nbtTagCompound);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
	}

	public static ItemStack craftItemStackAsBukkitCopy(Object nmsStack) {
		try {
			return (ItemStack) craftItemStackAsBukkitCopy.invoke(null, nmsStack);
		} catch (Exception e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "NMS Error", e);
		}
		return null;
	}
}

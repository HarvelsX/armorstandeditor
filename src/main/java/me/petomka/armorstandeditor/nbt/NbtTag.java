package me.petomka.armorstandeditor.nbt;

import lombok.Getter;
import me.petomka.armorstandeditor.reflection.ReflectionUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class NbtTag {

	@Getter
	private @Nonnull
	Object nbtTagCompound;

	public NbtTag(@Nullable Object nbtTagCompound) {
		if (nbtTagCompound != null) {
			this.nbtTagCompound = nbtTagCompound;
		} else {
			Object nbtTagCompound2 = ReflectionUtil.newNbtTagCompound();
			assert nbtTagCompound2 != null;
			this.nbtTagCompound = nbtTagCompound2;
		}
	}

	public NbtTag() {
		this(null);
	}

	public boolean hasKey(@Nonnull String key) {
		return ReflectionUtil.nbtTagCompoundHasKey(nbtTagCompound, key);
	}

	public Set<String> getKeySet() {
		return ReflectionUtil.nbtTagCompoundGetKeySet(nbtTagCompound);
	}

	public Object get(@Nonnull String key) {
		return ReflectionUtil.nbtTagCompoundGet(nbtTagCompound, key);
	}

	public @Nullable
	String getString(@Nonnull String key) {
		if (!hasKey(key)) {
			return null;
		}
		return ReflectionUtil.nbtTagCompoundGetString(nbtTagCompound, key);
	}

	public @Nullable
	NbtTag getNbtTag(@Nonnull String key) {
		if (!hasKey(key)) {
			return null;
		}
		return new NbtTag(ReflectionUtil.nbtTagCompoundGet(nbtTagCompound, key));
	}

	public int getInt(@Nonnull String key) {
		if (!hasKey(key)) {
			return 0;
		}
		return ReflectionUtil.nbtTagCompoundGetInt(nbtTagCompound, key);
	}

	public boolean getBoolean(@Nonnull String key) {
		if (!hasKey(key)) {
			return false;
		}
		return ReflectionUtil.nbtTagCompoundGetBoolean(nbtTagCompound, key);
	}

	public byte[] getByteArray(@Nonnull String key) {
		if (!hasKey(key)) {
			return null;
		}
		return ReflectionUtil.nbtTagCompoundGetByteArray(nbtTagCompound, key);
	}

	public double getDouble(@Nonnull String key) {
		if (!hasKey(key)) {
			return 0D;
		}
		return ReflectionUtil.nbtTagCompoundGetDouble(nbtTagCompound, key);
	}

	public void setString(@Nonnull String key, @Nonnull String value) {
		ReflectionUtil.nbtTagCompoundSetString(nbtTagCompound, key, value);
	}

	public void setInt(@Nonnull String key, int value) {
		ReflectionUtil.nbtTagCompoundSetInt(nbtTagCompound, key, value);
	}

	public void setBoolean(@Nonnull String key, boolean value) {
		ReflectionUtil.nbtTagCompoundSetBoolean(nbtTagCompound, key, value);
	}

	public void setByteArray(@Nonnull String key, @Nonnull byte[] bytes) {
		ReflectionUtil.nbtTagCompoundSetByteArray(nbtTagCompound, key, bytes);
	}

	public void setDouble(@Nonnull String key, double value) {
		ReflectionUtil.nbtTagCompoundSetDouble(nbtTagCompound, key, value);
	}

	public void set(@Nonnull String key, @Nonnull NbtTag tag) {
		ReflectionUtil.nbtTagCompoundSet(nbtTagCompound, key, tag.getNbtTagCompound());
	}

}

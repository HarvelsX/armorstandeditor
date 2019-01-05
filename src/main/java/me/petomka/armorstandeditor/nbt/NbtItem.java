package me.petomka.armorstandeditor.nbt;

import lombok.Getter;
import me.petomka.armorstandeditor.reflection.ReflectionUtil;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class NbtItem {

	@Getter
	private final @Nonnull
	ItemStack itemStack;

	@Getter
	private final @Nonnull
	Object nmsStack;

	@Getter
	private @Nonnull
	NbtTag nbtTagCompound;

	public NbtItem(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
		Object nmsStack = ReflectionUtil.craftItemStackAsNmsCopy(itemStack);
		assert nmsStack != null;
		this.nmsStack = nmsStack;
		this.nbtTagCompound = new NbtTag(ReflectionUtil.itemStackGetOrCreateTag(nmsStack));
	}

	public void setTag(@Nonnull NbtTag nbtTagCompound) {
		setTag(nbtTagCompound.getNbtTagCompound());
	}

	public void setTag(@Nonnull Object nbtTagCompound) {
		ReflectionUtil.itemStackSetTag(nmsStack, nbtTagCompound);
	}

	public ItemStack toBukkitItem() {
		return ReflectionUtil.craftItemStackAsBukkitCopy(nmsStack);
	}

}

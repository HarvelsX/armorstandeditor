package me.petomka.armorstandeditor.util;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class ExceptionToNull {

	public <T> T get(Supplier<T> tSupplier) {
		try {
			return tSupplier.get();
		} catch (Exception e) {
			return null;
		}
	}

}

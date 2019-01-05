package me.petomka.armorstandeditor.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.petomka.armorstandeditor.Main;

import java.util.function.Supplier;

@RequiredArgsConstructor
public enum Accuracy {

	SMALLEST(0.01, Main.getInstance().getMessages()::getSmallestAdjustmentName),
	SMALLER(0.05, Main.getInstance().getMessages()::getSmallerAdjustmentName),
	SMALL(0.1, Main.getInstance().getMessages()::getSmallAdjustmentName),
	LARGE(0.3, Main.getInstance().getMessages()::getLargeAdjustmentName),
	LARGER(1, Main.getInstance().getMessages()::getLargerAdjustmentName),
	LARGEST(5, Main.getInstance().getMessages()::getLargestAdjustmentName);

	@Getter
	private final double adjustmentSize;
	private final Supplier<String> nameSupplier;

	public String getName() {
		return nameSupplier.get();
	}

	public Accuracy moreAccurate() {
		if (this == SMALLEST) {
			return SMALLEST;
		}
		return Accuracy.values()[(this.ordinal() - 1) % Accuracy.values().length];
	}

	public Accuracy lessAccurate() {
		if (this == LARGEST) {
			return LARGEST;
		}
		return Accuracy.values()[(this.ordinal() + 1) % Accuracy.values().length];
	}

}

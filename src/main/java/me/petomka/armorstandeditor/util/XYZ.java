package me.petomka.armorstandeditor.util;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.config.Messages;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@AllArgsConstructor
public class XYZ implements Scoreboardable, Serializable {
	public double x, y, z;

	public static XYZ of(double x, double y, double z) {
		return new XYZ(x, y, z);
	}

	public double sum() {
		return x + y + z;
	}

	public double length() {
		return new Vector(x, y, z).length();
	}

	@Override
	public Map<String, Integer> toScoreboardInfo() {
		Messages messages = Main.getInstance().getMessages();

		String scoreboardFormat[] = messages.getSinglePartScoreboardFormat().split("\\\\n");

		int sbLine = 0;

		for (int i = 0; i < scoreboardFormat.length; i++) {
			String line = scoreboardFormat[i];
			String unique = ChatColor.values()[sbLine++] + "";
			if (line.equals("")) {
				scoreboardFormat[i] = unique;
			} else {
				scoreboardFormat[i] = line.replace("{x}", Main.formatDouble(x))
						.replace("{y}", Main.formatDouble(y))
						.replace("{z}", Main.formatDouble(z))
						+ unique;
			}
		}

		AtomicInteger lineNum = new AtomicInteger(scoreboardFormat.length);

		return Arrays.stream(scoreboardFormat)
				.collect(Collectors.toMap(line -> line, line -> lineNum.getAndDecrement(),
						(o1, o2) -> o1, Maps::newHashMap));
	}
}

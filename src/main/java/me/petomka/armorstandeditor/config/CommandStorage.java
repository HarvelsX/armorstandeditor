package me.petomka.armorstandeditor.config;

import lombok.Getter;
import lombok.Setter;
import me.petomka.armorstandeditor.Main;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class CommandStorage extends Config {

	public CommandStorage(Main plugin) throws InvalidConfigurationException {
		CONFIG_FILE = new File(plugin.getDataFolder(), "as-commands.yml");
		CONFIG_MODE = ConfigMode.DEFAULT;

		init();
	}

	private Map<String, List<String>> mappedCommands = new HashMap<>();

}

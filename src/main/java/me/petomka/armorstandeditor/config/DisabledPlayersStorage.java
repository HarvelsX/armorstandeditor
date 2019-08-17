package me.petomka.armorstandeditor.config;

import com.google.common.collect.Sets;
import lombok.Getter;
import me.petomka.armorstandeditor.Main;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.InvalidConfigurationException;

import java.io.File;
import java.util.Set;
import java.util.UUID;

@Getter
public class DisabledPlayersStorage extends Config {

	public DisabledPlayersStorage(Main main) throws InvalidConfigurationException {
		CONFIG_FILE = new File(main.getDataFolder(), "disabled_players.yml");
		CONFIG_MODE = ConfigMode.DEFAULT;

		init();
	}

	private Set<UUID> disabledPlayers = Sets.newHashSet();

}

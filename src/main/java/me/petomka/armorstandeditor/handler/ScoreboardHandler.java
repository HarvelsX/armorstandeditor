package me.petomka.armorstandeditor.handler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import me.petomka.armorstandeditor.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;

public class ScoreboardHandler {

	@Getter(lazy = true)
	private static final ScoreboardHandler instance = new ScoreboardHandler();

	private static final String SCORBOARD_OBJECTIVE_NAME = "asEdit";
	private static final String SCOREBOARD_TEAM_START = "T";

	private Map<UUID, Scoreboard> registeredBoards = Maps.newHashMap();
	private Map<UUID, Scoreboard> previousScoreboards = new WeakHashMap<>();

	public boolean registerPlayer(@Nonnull UUID player) {
		Preconditions.checkNotNull(player, "player");

		Player thePlayer = Bukkit.getPlayer(player);
		if (thePlayer == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Cannot register scoreboard for unknown player " +
					"with UUID " + player);
			return false;
		}

		Part editedPart = ArmorStandEditHandler.getInstance().getEditingPart(player);
		if (editedPart == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Cannot register scoreboard for not editing player " +
					thePlayer.getName());
			return false;
		}

		Scoreboard scoreboard = thePlayer.getScoreboard();
		Objective objective;
		if (!registeredBoards.containsValue(scoreboard)) {
			previousScoreboards.put(player, thePlayer.getScoreboard());
			scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

			objective = scoreboard.registerNewObjective(SCORBOARD_OBJECTIVE_NAME, "dummy",
					editedPart.getEditingInfo());
		} else {
			scoreboard = registeredBoards.get(player);
			objective = scoreboard.getObjective(SCORBOARD_OBJECTIVE_NAME);
		}

		registeredBoards.put(player, scoreboard);

		if(objective == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Objective for registered scoreboard was missing!");
			return false;
		}

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		thePlayer.setScoreboard(scoreboard);

		updatePlayer(player);
		return true;
	}

	public void unregisterPlayer(@Nonnull UUID player) {
		Preconditions.checkNotNull(player, "player");

		Player thePlayer = Bukkit.getPlayer(player);
		if (thePlayer == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Cannot unregister Scoreboard for non-existent " +
					"player with UUID " + player);
			return;
		}

		registeredBoards.remove(player);

		Scoreboard scoreboard = previousScoreboards.get(player);
		if (scoreboard != null) {
			thePlayer.setScoreboard(scoreboard);
		} else {
			thePlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}

	public boolean updatePlayer(@Nonnull UUID player) {
		Preconditions.checkNotNull(player, "player");

		Player thePlayer = Bukkit.getPlayer(player);
		if (thePlayer == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Cannot update scoreboard for unknown player " +
					"with UUID " + player);
			return false;
		}

		Scoreboard scoreboard = thePlayer.getScoreboard();
		if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Tried updating foreign scoreboard!");
			return false;
		}

		Objective objective = scoreboard.getObjective(SCORBOARD_OBJECTIVE_NAME);
		if (objective == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Scoreboard used to display info for " +
					thePlayer.getName() + " is missing it's objective!");
			return false;
		}

		Part editedPart = ArmorStandEditHandler.getInstance().getEditingPart(player);
		if (editedPart == null) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Tried updating scoreboard for non editing player!");
			return false;
		}

		int teamNo = 0;

		Set<ArmorStand> armorStands = ArmorStandEditHandler.getInstance().editedArmorStands.get(player);
		if (armorStands == null || armorStands.size() != 1) {
			Main.getInstance().getLogger().log(Level.SEVERE, "Player " + thePlayer.getName() + " does not have " +
					"exactly one armorstand to edit, but has " + (armorStands == null ? "NULL" : armorStands.size()));
			return false;
		}

		objective.setDisplayName(editedPart.getEditingInfo());
		Map<String, Integer> scoreboardInfo = editedPart.getScoreboardInfo(armorStands.stream().findAny().get());
		Set<Team> unusedTeams = Sets.newHashSet(scoreboard.getTeams());
		for (Map.Entry<String, Integer> scoreboardLine : scoreboardInfo.entrySet()) {
			String teamName = SCOREBOARD_TEAM_START + teamNo++;
			String teamEntry = ChatColor.values()[teamNo] + "";
			Team team = scoreboard.getTeam(teamName);
			if (team == null) {
				team = scoreboard.registerNewTeam(teamName);
				team.addEntry(teamEntry);
			} else {
				unusedTeams.remove(team);
			}
			team.setPrefix(ChatColor.translateAlternateColorCodes('&', scoreboardLine.getKey()));
			objective.getScore(teamEntry).setScore(scoreboardLine.getValue());
		}
		for (Team unusedTeam : unusedTeams) {
			unusedTeam.getEntries().forEach(scoreboard::resetScores);
			unusedTeam.unregister();
		}
		return true;
	}

}

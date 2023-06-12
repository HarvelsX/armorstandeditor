package me.petomka.armorstandeditor.handler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.inventory.InventoryMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static me.petomka.armorstandeditor.util.ArmorStandUtils.handleLockedArmorStand;

public class ArmorStandEditHandler {

    @Getter
    private static ArmorStandEditHandler instance;

    private ActionBarTask actionBarTask = new ActionBarTask();

    private Main plugin;

    public ArmorStandEditHandler(Main main) {
        instance = this;
        this.plugin = main;

        actionBarTask.runTaskTimerAsynchronously(main, 30L, 30L);
    }

    private Set<UUID> proEditors = Sets.newHashSet();
    Map<UUID, Part> editingPlayers = Maps.newHashMap();
    Map<UUID, Set<ArmorStand>> editedArmorStands = Maps.newHashMap();
    Map<UUID, Accuracy> editingAccuracy = Maps.newHashMap();
    Map<UUID, Vector> playerLockedAxis = Maps.newHashMap();
    Map<UUID, Collection<ArmorStand>> searchingPlayers = Maps.newHashMap();

    public boolean isEditingPlayer(@Nullable UUID player) {
        return editingPlayers.containsKey(player);
    }

    public boolean isSearchingPlayer(@Nullable UUID player) {
        return searchingPlayers.containsKey(player);
    }

    public boolean isProModeEditor(@Nullable UUID player) {
        if (!isEditingPlayer(player)) {
            return false;
        }
        return proEditors.contains(player);
    }

    /**
     * Toggles pro mode
     *
     * @param player UUID of the target player
     * @return true if player is now in pro mode, false otherwise
     */
    public boolean toggleProMode(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");
        if (proEditors.add(player)) {
            return true;
        }
        proEditors.remove(player);
        return false;
    }

    public @Nullable
    Part getEditingPart(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");

        return editingPlayers.get(player);
    }

    public void addSearchingPlayer(@Nonnull UUID player, @Nonnull Collection<ArmorStand> armorStands) {
        Preconditions.checkNotNull(player, "player");
        searchingPlayers.put(player, armorStands);

        if (Main.getInstance().getDefaultConfig().isBossBarEnabled()
                && !BossBarHandler.getInstance().registerSearchPlayer(player, armorStands.size())) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Error trying to register a player boss bar!");
        }
    }

    public @Nullable Collection<ArmorStand> getSearchedArmorStands(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");

        if (searchingPlayers.get(player) == null) {
            return null;
        }

        return searchingPlayers.get(player);
    }

    public Set<UUID> getSearchingPlayers() {
        return searchingPlayers.keySet();
    }

    public void addEditingPlayer(@Nonnull UUID player, @Nonnull Part part, @Nonnull ArmorStand armorStand) {
        Preconditions.checkNotNull(armorStand, "armorStand");

        addEditingPlayer(player, part, Collections.singleton(armorStand));
    }

    public void addEditingPlayer(@Nonnull UUID player, @Nonnull Part part, @Nonnull Set<ArmorStand> armorStands) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(part, "part");
        Preconditions.checkNotNull(armorStands, "armorStands");

        editingPlayers.put(player, part);

        Accuracy accuracy = editingAccuracy.get(player);
        String accuracyName = Main.getInstance().getDefaultConfig().getDefaultAccuracy();

        if (accuracy == null) {
            try {
                accuracy = Accuracy.valueOf(accuracyName);
            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.SEVERE, "Could not load accuracy from config default " +
                        accuracyName, e);
                return;
            }
        }

        editingAccuracy.put(player, accuracy);
        editedArmorStands.put(player, armorStands);

        if (Main.getInstance().getDefaultConfig().isScoreboardEnabled() && !ScoreboardHandler.getInstance().registerPlayer(player)) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Error trying to register a player scoreboard!");
        }

        if (Main.getInstance().getDefaultConfig().isBossBarEnabled() && !BossBarHandler.getInstance().registerEditorPlayer(player)) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Error trying to register a player boss bar!");
        }

        CompletableFuture.runAsync(actionBarTask);
    }

    public @Nonnull
    Set<ArmorStand> getEditedArmorstands(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");

        if (editedArmorStands.get(player) == null) {
            return ImmutableSet.of();
        }

        return ImmutableSet.copyOf(editedArmorStands.get(player));
    }

    public boolean removeEditingPlayer(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");

        BossBarHandler.getInstance().unregisterPlayer(player);
        ScoreboardHandler.getInstance().unregisterPlayer(player);
        actionBarTask.onEditingStop(player);

        //editing accuracy will be preserved
        editedArmorStands.remove(player);
        proEditors.remove(player);

        if (InventoryMenu.getOpenedInventories().get(player) != null) {
            Player thePlayer = Bukkit.getPlayer(player);
            if (thePlayer != null) {
                thePlayer.closeInventory();
            }
        }

        return editingPlayers.remove(player) != null;
    }

    public boolean removeSearchingPlayer(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");
        boolean contained = searchingPlayers.containsKey(player);
        searchingPlayers.remove(player);
        BossBarHandler.getInstance().unregisterPlayer(player);
        return contained;
    }

    public void editNextPart(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");

        Part part = getEditingPart(player);
        if (part == null) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Tried to update a non editing player's part with " +
                    "UUID " + player);
            return;
        }

        addEditingPlayer(player, part.nextPart(), getEditedArmorstands(player));
    }

    public void editPreviousPart(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");

        Part part = getEditingPart(player);
        if (part == null) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Tried to update a non editing player's part with " +
                    "UUID " + player);
            return;
        }

        addEditingPlayer(player, part.previousPart(), getEditedArmorstands(player));
    }

    public void updateAccuracy(@Nonnull UUID player, boolean moreAccurate) {
        Preconditions.checkNotNull(player, "player");

        Accuracy oldAccuracy = editingAccuracy.get(player);
        Accuracy newAccuracy = moreAccurate ? oldAccuracy.moreAccurate() : oldAccuracy.lessAccurate();

        editingAccuracy.put(player, newAccuracy);
        if (Main.getInstance().getDefaultConfig().isBossBarEnabled() && !BossBarHandler.getInstance().updatePlayerEditBar(player)) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Could not update player boss bar");
        }
    }

    public @Nullable
    Accuracy getPlayerAccuracy(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");

        Accuracy accuracy = editingAccuracy.get(player);
        if (accuracy == null) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Tried to query accuracy for non editing player with UUID " + player);
            return null;
        }

        return accuracy;
    }

    public void lockPlayerAxis(Player player, Vector axis) {
        playerLockedAxis.put(player.getUniqueId(), axis);
    }

    public void unlockPlayerAxis(Player player) {
        playerLockedAxis.remove(player.getUniqueId());
    }

    public @Nullable
    Vector getPlayerLockedAxis(Player player) {
        return playerLockedAxis.get(player.getUniqueId());
    }

    public boolean isPlayerAxisLocked(Player player) {
        return playerLockedAxis.containsKey(player.getUniqueId());
    }

    public Optional<ArmorStand> getSingleArmorstand(@Nonnull UUID player) {
        Preconditions.checkNotNull(player, "player");

        if (getEditedArmorstands(player).size() != 1) {
            return Optional.empty();
        }
        return getEditedArmorstands(player).stream().findAny();
    }

    public @Nullable
    UUID getArmorStandEditor(@Nonnull ArmorStand armorStand) {
        Preconditions.checkNotNull(armorStand, "armorStand");

        return editedArmorStands.entrySet()
                .stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(armorStand::equals))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public Vector getAdjustmentVector(Player player) {
        Vector lockedAxis = getPlayerLockedAxis(player);
        if (lockedAxis != null) {
            return lockedAxis.clone();
        }
        Vector adjustment = new Vector(0, 0, 0);
        Location playerLocation = player.getLocation();

        int yaw = Math.round(playerLocation.getYaw() + 45);

        if (yaw < 0) {
            yaw += 360;
        }

        yaw = yaw / 90;

        if (playerLocation.getPitch() <= -60 || playerLocation.getPitch() >= 70) {
            adjustment.setY(1);
        } else if (yaw % 2 == 0) {
            adjustment.setZ(1);
        } else {
            adjustment.setX(1);
        }

        return adjustment;
    }

    public void onAdjustmentMade(@Nonnull UUID player, double x, double y, double z) {
        Preconditions.checkNotNull(player, "player");

        if (!isEditingPlayer(player)) {
            Main.getInstance().getLogger().log(Level.WARNING, "Tried to make armor stand adjustment for a player currently not editing");
            return;
        }

        Part part = getEditingPart(player);
        if (part == null) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Editing player with UUID " + player + " has no corresponding part.");
            return;
        }

        Set<ArmorStand> armorStands = editedArmorStands.get(player);
        if (armorStands == null || armorStands.isEmpty()) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Editing player with UUID " + player + " has no armor stands to adjust!");
            return;
        }

        Player thePlayer = Bukkit.getPlayer(player);
        if (thePlayer == null) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Unknown player with UUID " + player + " tried to make an" +
                    " adjustment");
            return;
        }

        boolean anyLocked = armorStands.stream().anyMatch(armorStand -> handleLockedArmorStand(thePlayer, armorStand));
        if (anyLocked) {
            return;
        }

        if (part == Part.BODY && plugin.willBeTooFar(thePlayer, armorStands, new Vector(x, y, z))) {
            return;
        }

        if (part == Part.BODY && plugin.isInteractCancelled(thePlayer, armorStands, new Vector(x, y, z))) {
            return;
        }

        if (part == Part.BODY && plugin.getDefaultConfig().isDisableGravityOnYPositionChange() && y != 0) {
            if (!thePlayer.hasPermission(plugin.getDefaultConfig().getGravityPermission())) {
                thePlayer.sendMessage(Main.colorString(plugin.getMessages().getCannotChangeYAxis()));
                return;
            }
            armorStands.forEach(armorStand -> armorStand.setGravity(false));
        }

        armorStands.forEach(armorStand -> part.add(armorStand, x, y, z));

        if (Main.getInstance().getDefaultConfig().isScoreboardEnabled() && !ScoreboardHandler.getInstance().updatePlayer(player)) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Could not update scoreboard for player with UUID " + player);
        }
    }

}

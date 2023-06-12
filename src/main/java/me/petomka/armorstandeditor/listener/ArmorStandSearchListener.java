package me.petomka.armorstandeditor.listener;

import lombok.RequiredArgsConstructor;
import me.petomka.armorstandeditor.Main;
import me.petomka.armorstandeditor.handler.ArmorStandEditHandler;
import me.petomka.armorstandeditor.handler.BossBarHandler;
import me.petomka.armorstandeditor.handler.Part;
import me.petomka.armorstandeditor.util.ArmorStandUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

@RequiredArgsConstructor
public class ArmorStandSearchListener implements Listener, Runnable {

    private static final Material SEARCH_ITEM = Material.RECOVERY_COMPASS;

    private final Main main;

    private final Map<UUID, Integer> playerSearchOffset = new HashMap<>();

    private final Map<ArmorStand, Set<UUID>> touchedArmorStands = new HashMap<>();
    private final Map<UUID, ArmorStand> activeArmorStand = new HashMap<>();

    public void scheduleTask() {
        Bukkit.getScheduler().runTaskTimer(main, this, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (Main.getEventsToIgnore().contains(event)) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Player player = event.getPlayer();
        boolean isSearching = ArmorStandEditHandler.getInstance().isSearchingPlayer(player.getUniqueId());
        ItemStack itemStack = event.getItem();
        boolean isLeftClick = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
        if (isSearching && isLeftClick && !event.getPlayer().isSneaking()) {
            disableSearch(event.getPlayer());
            event.setCancelled(true);
            return;
        }
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        if (isSearching && isLeftClick && itemStack.getType() == SEARCH_ITEM) {
            pickArmorStand(event.getPlayer());
            event.setCancelled(true);
            return;
        }
        if (itemStack == null) {
            return;
        }
        if (itemStack.getType() != SEARCH_ITEM) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            String permission = main.getDefaultConfig().getArmorStandSearchPermission();
            if (!player.hasPermission(permission)) {
                return;
            }
            enableSearch(event.getPlayer());
            event.setCancelled(true);
            return;
        }
        disableSearch(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!ArmorStandEditHandler.getInstance().isSearchingPlayer(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        pickArmorStand(player);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        if (Main.getEventsToIgnore().contains(event)) {
            return;
        }
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if (itemStack.getType() != SEARCH_ITEM) {
            return;
        }
        event.setCancelled(true);
        enableSearch(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        disableSearch(event.getPlayer());
    }

    private boolean isIncreaseSlot(int old, int nu) {
        if (old == 0 && nu == 8) {
            return true;
        }
        if (old == 8 && nu == 0) {
            return false;
        }
        return nu < old;
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (!ArmorStandEditHandler.getInstance().isSearchingPlayer(event.getPlayer().getUniqueId())) {
            return;
        }
        event.getPlayer().getInventory().setHeldItemSlot(Main.PRO_MODE_SLOT);
        if (event.getNewSlot() == Main.PRO_MODE_SLOT) {
            return;
        }
        event.setCancelled(true);
        if (isIncreaseSlot(event.getPreviousSlot(), event.getNewSlot())) {
            playerSearchOffset.compute(event.getPlayer().getUniqueId(), (uuid, integer) -> integer == null ? 1 : integer + 1);
        } else {
            playerSearchOffset.compute(event.getPlayer().getUniqueId(), (uuid, integer) -> integer == null ? -1 : integer - 1);
        }
    }

    @EventHandler
    public void onLookAround(PlayerMoveEvent event) {
        Location pre = event.getFrom();
        Location post = event.getTo();
        var preDir = pre.getDirection();
        var postDir = post.getDirection();
        if (Math.abs(preDir.dot(postDir)) < 0.1) {
            return;
        }
        if (!ArmorStandEditHandler.getInstance().isSearchingPlayer(event.getPlayer().getUniqueId())) {
            return;
        }
        playerSearchOffset.remove(event.getPlayer().getUniqueId());
    }

    private void enableSearch(Player player) {
        String permission = main.getDefaultConfig().getArmorStandSearchPermission();
        if (!player.hasPermission(permission)) {
            return;
        }
        if (ArmorStandEditHandler.getInstance().isSearchingPlayer(player.getUniqueId())) {
            return;
        }
        if (ArmorStandEditHandler.getInstance().isEditingPlayer(player.getUniqueId())) {
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() != SEARCH_ITEM) {
            return;
        }

        ArmorStandEditHandler.getInstance().addSearchingPlayer(player.getUniqueId(), new ArrayList<>());
        String message = main.getMessages().getSearchEnabled();
        player.sendMessage(Main.colorString(message));

        int heldSlot = player.getInventory().getHeldItemSlot();
        if (heldSlot == Main.PRO_MODE_SLOT) {
            return;
        }

        ItemStack proSlotItem = player.getInventory().getItem(Main.PRO_MODE_SLOT);
        player.getInventory().setHeldItemSlot(Main.PRO_MODE_SLOT);
        player.getInventory().setItem(Main.PRO_MODE_SLOT, handItem);
        player.getInventory().setItem(heldSlot, proSlotItem);
    }

    private void disableSearch(Player player) {
        if (!ArmorStandEditHandler.getInstance().removeSearchingPlayer(player.getUniqueId())) {
            return;
        }
        String message = main.getMessages().getSearchDisabled();
        player.sendMessage(Main.colorString(message));
        clearPlayerArmorStand(player.getUniqueId());
    }

    private void clearPlayerArmorStand(UUID playerId) {
        ArmorStand prevActiveStand = activeArmorStand.get(playerId);
        if (prevActiveStand != null) {
            prevActiveStand.setGlowing(false);
            activeArmorStand.remove(playerId);
            // search and remove from touched list
            for (Map.Entry<ArmorStand, Set<UUID>> entry : touchedArmorStands.entrySet()) {
                entry.getValue().remove(playerId);
            }
            // remove touched armor stands where set size is 0
            touchedArmorStands.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
    }

    private void pickArmorStand(Player player) {
        var armorStands = ArmorStandEditHandler.getInstance().getSearchedArmorStands(player.getUniqueId());
        if (armorStands == null || armorStands.isEmpty()) {
            return;
        }
        if (!(armorStands instanceof List<ArmorStand> list)) {
            return;
        }
        int offset = playerSearchOffset.getOrDefault(player.getUniqueId(), 0);
        if (offset < 0) {
            offset = list.size() + offset;
        }
        int index = offset % list.size();
        ArmorStand armorStand = list.get(index);
        disableSearch(player);
        ArmorStandEditHandler.getInstance().addEditingPlayer(player.getUniqueId(), Part.BELLY, armorStand);

        int stickIndex = player.getInventory().first(Material.STICK);
        if (stickIndex == -1) {
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        ItemStack stick = player.getInventory().getItem(stickIndex);
        player.getInventory().setItem(stickIndex, handItem);
        player.getInventory().setItem(Main.PRO_MODE_SLOT, stick);
        player.getInventory().setHeldItemSlot(Main.PRO_MODE_SLOT);

        if (ArmorStandEditHandler.getInstance().isProModeEditor(player.getUniqueId())) {
            return;
        }
        ArmorStandEditHandler.getInstance().toggleProMode(player.getUniqueId());
    }

    @Override
    public void run() {
        for (UUID playerId : ArmorStandEditHandler.getInstance().getSearchingPlayers()) {
            clearPlayerArmorStand(playerId);

            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            int offset = playerSearchOffset.getOrDefault(playerId, 0);
            List<ArmorStand> armorStands = new ArrayList<>();
            for (Entity nearby : player.getWorld().getNearbyEntities(player.getLocation(), 6, 6, 6)) {
                if (nearby instanceof ArmorStand as && ArmorStandUtils.canEditArmorStand(player, as)) {
                    armorStands.add(as);
                }
            }
            // sort armor stands by distance viewing angle to player
            armorStands.sort(Comparator.comparingDouble(as -> {
                Location loc = as.getLocation();
                Vector dir = loc.toVector().subtract(player.getLocation().toVector()).normalize();
                return -dir.dot(player.getLocation().getDirection().normalize());
            }));
            ArmorStandEditHandler.getInstance().addSearchingPlayer(playerId, armorStands);
            if (armorStands.isEmpty()) {
                continue;
            }

            if (offset < 0) {
                offset = armorStands.size() + offset;
            }
            int index = offset % armorStands.size();
            if (index < 0) {
                index = 0;
            }
            ArmorStand armorStand = armorStands.get(index);
            if (armorStand.isGlowing()) {
                return;
            }
            armorStand.setGlowing(true);
            touchedArmorStands.compute(armorStand, (armorStand1, uuids) -> {
                if (uuids == null) {
                    uuids = new HashSet<>();
                }
                uuids.add(playerId);
                return uuids;
            });
            activeArmorStand.put(playerId, armorStand);
        }
    }
}



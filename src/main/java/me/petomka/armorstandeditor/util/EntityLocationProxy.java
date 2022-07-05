package me.petomka.armorstandeditor.util;

import lombok.RequiredArgsConstructor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pose;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class EntityLocationProxy implements Entity {

	private final Entity originalEntity;

	private final Location location;

	@Override

	public Location getLocation() {
		return location;
	}

	@Override
	public Location getLocation(Location location) {
		if (location == null) {
			return null;
		}
		Location l = this.location;
		location.setDirection(l.getDirection());
		location.setX(l.getX());
		location.setY(l.getY());
		location.setZ(l.getZ());
		location.setYaw(l.getYaw());
		location.setPitch(l.getPitch());
		location.setWorld(l.getWorld());
		return location;
	}

	@Override
	public void setVelocity(Vector vector) {
		originalEntity.setVelocity(vector);
	}

	@Override
	public Vector getVelocity() {
		return originalEntity.getVelocity();
	}

	@Override
	public double getHeight() {
		return originalEntity.getHeight();
	}

	@Override
	public double getWidth() {
		return originalEntity.getWidth();
	}

	@Override
	public BoundingBox getBoundingBox() {
		return originalEntity.getBoundingBox();
	}

	@Override
	public boolean isOnGround() {
		return originalEntity.isOnGround();
	}

	@Override
	public World getWorld() {
		return originalEntity.getWorld();
	}

	@Override
	public void setRotation(float v, float v1) {
		originalEntity.setRotation(v, v1);
	}

	@Override
	public boolean teleport(Location location) {
		return originalEntity.teleport(location);
	}

	@Override
	public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause teleportCause) {
		return originalEntity.teleport(location, teleportCause);
	}

	@Override
	public boolean teleport(Entity entity) {
		return originalEntity.teleport(entity);
	}

	@Override
	public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause teleportCause) {
		return originalEntity.teleport(entity, teleportCause);
	}

	@Override
	public List<Entity> getNearbyEntities(double v, double v1, double v2) {
		return originalEntity.getNearbyEntities(v, v1, v2);
	}

	@Override
	public int getEntityId() {
		return originalEntity.getEntityId();
	}

	@Override
	public int getFireTicks() {
		return originalEntity.getFireTicks();
	}

	@Override
	public int getMaxFireTicks() {
		return originalEntity.getMaxFireTicks();
	}

	@Override
	public void setFireTicks(int i) {
		originalEntity.setFireTicks(i);
	}

	@Override
	public void remove() {
		originalEntity.remove();
	}

	@Override
	public boolean isDead() {
		return originalEntity.isDead();
	}

	@Override
	public boolean isValid() {
		return originalEntity.isValid();
	}

	@Override
	public Server getServer() {
		return originalEntity.getServer();
	}

	@Override
	@Deprecated
	public boolean isPersistent() {
		return originalEntity.isPersistent();
	}

	@Override
	@Deprecated
	public void setPersistent(boolean b) {
		originalEntity.setPersistent(b);
	}

	@Override
	@Deprecated
	public Entity getPassenger() {
		return originalEntity.getPassenger();
	}

	@Override
	@Deprecated
	public boolean setPassenger(Entity entity) {
		return originalEntity.setPassenger(entity);
	}

	@Override
	public List<Entity> getPassengers() {
		return originalEntity.getPassengers();
	}

	@Override
	public boolean addPassenger(Entity entity) {
		return originalEntity.addPassenger(entity);
	}

	@Override
	public boolean removePassenger(Entity entity) {
		return originalEntity.removePassenger(entity);
	}

	@Override
	public boolean isEmpty() {
		return originalEntity.isEmpty();
	}

	@Override
	public boolean eject() {
		return originalEntity.eject();
	}

	@Override
	public float getFallDistance() {
		return originalEntity.getFallDistance();
	}

	@Override
	public void setFallDistance(float v) {
		originalEntity.setFallDistance(v);
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent entityDamageEvent) {
		originalEntity.setLastDamageCause(entityDamageEvent);
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		return originalEntity.getLastDamageCause();
	}

	@Override
	public UUID getUniqueId() {
		return originalEntity.getUniqueId();
	}

	@Override
	public int getTicksLived() {
		return originalEntity.getTicksLived();
	}

	@Override
	public void setTicksLived(int i) {
		originalEntity.setTicksLived(i);
	}

	@Override
	public void playEffect(EntityEffect entityEffect) {
		originalEntity.playEffect(entityEffect);
	}

	@Override
	public EntityType getType() {
		return originalEntity.getType();
	}

	@Override
	public boolean isInsideVehicle() {
		return originalEntity.isInsideVehicle();
	}

	@Override
	public boolean leaveVehicle() {
		return originalEntity.leaveVehicle();
	}

	@Override
	public Entity getVehicle() {
		return originalEntity.getVehicle();
	}

	@Override
	public void setCustomNameVisible(boolean b) {
		originalEntity.setCustomNameVisible(b);
	}

	@Override
	public boolean isCustomNameVisible() {
		return originalEntity.isCustomNameVisible();
	}

	@Override
	public void setGlowing(boolean b) {
		originalEntity.setGlowing(b);
	}

	@Override
	public boolean isGlowing() {
		return originalEntity.isGlowing();
	}

	@Override
	public void setInvulnerable(boolean b) {
		originalEntity.setInvulnerable(b);
	}

	@Override
	public boolean isInvulnerable() {
		return originalEntity.isInvulnerable();
	}

	@Override
	public boolean isSilent() {
		return originalEntity.isSilent();
	}

	@Override
	public void setSilent(boolean b) {
		originalEntity.setSilent(b);
	}

	@Override
	public boolean hasGravity() {
		return originalEntity.hasGravity();
	}

	@Override
	public void setGravity(boolean b) {
		originalEntity.setGravity(b);
	}

	@Override
	public int getPortalCooldown() {
		return originalEntity.getPortalCooldown();
	}

	@Override
	public void setPortalCooldown(int i) {
		originalEntity.setPortalCooldown(i);
	}

	@Override
	public Set<String> getScoreboardTags() {
		return originalEntity.getScoreboardTags();
	}

	@Override
	public boolean addScoreboardTag(String s) {
		return originalEntity.addScoreboardTag(s);
	}

	@Override
	public boolean removeScoreboardTag(String s) {
		return originalEntity.removeScoreboardTag(s);
	}

	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		return originalEntity.getPistonMoveReaction();
	}

	@Override
	public BlockFace getFacing() {
		return originalEntity.getFacing();
	}

	@Override
	public Spigot spigot() {
		return originalEntity.spigot();
	}

	@Override
	public void setMetadata(String s, MetadataValue metadataValue) {
		originalEntity.setMetadata(s, metadataValue);
	}

	@Override
	public List<MetadataValue> getMetadata(String s) {
		return originalEntity.getMetadata(s);
	}

	@Override
	public boolean hasMetadata(String s) {
		return originalEntity.hasMetadata(s);
	}

	@Override
	public void removeMetadata(String s, Plugin plugin) {
		originalEntity.removeMetadata(s, plugin);
	}

	@Override
	public void sendMessage(String s) {
		originalEntity.sendMessage(s);
	}

	@Override
	public void sendMessage(String[] strings) {
		originalEntity.sendMessage(strings);
	}

	@Override
	public String getName() {
		return originalEntity.getName();
	}

	@Override
	public boolean isPermissionSet(String s) {
		return originalEntity.isPermissionSet(s);
	}

	@Override
	public boolean isPermissionSet(Permission permission) {
		return originalEntity.isPermissionSet(permission);
	}

	@Override
	public boolean hasPermission(String s) {
		return originalEntity.hasPermission(s);
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return originalEntity.hasPermission(permission);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
		return originalEntity.addAttachment(plugin, s, b);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin) {
		return originalEntity.addAttachment(plugin);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
		return originalEntity.addAttachment(plugin, s, b, i);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, int i) {
		return originalEntity.addAttachment(plugin, i);
	}

	@Override
	public void removeAttachment(PermissionAttachment permissionAttachment) {
		originalEntity.removeAttachment(permissionAttachment);
	}

	@Override
	public void recalculatePermissions() {
		originalEntity.recalculatePermissions();
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return originalEntity.getEffectivePermissions();
	}

	@Override
	public boolean isOp() {
		return originalEntity.isOp();
	}

	@Override
	public void setOp(boolean b) {
		originalEntity.setOp(b);
	}

	@Override
	public String getCustomName() {
		return originalEntity.getCustomName();
	}

	@Override
	public void setCustomName(String s) {
		originalEntity.setCustomName(s);
	}

	@Override
	public boolean isInWater() {
		return originalEntity.isInWater();
	}

	@Override
	public void setVisualFire(boolean fire) {
		originalEntity.setVisualFire(fire);
	}

	@Override
	public boolean isVisualFire() {
		return originalEntity.isVisualFire();
	}

	@Override
	public int getFreezeTicks() {
		return originalEntity.getFreezeTicks();
	}

	@Override
	public int getMaxFreezeTicks() {
		return originalEntity.getMaxFreezeTicks();
	}

	@Override
	public void setFreezeTicks(int ticks) {
		originalEntity.setFreezeTicks(ticks);
	}

	@Override
	public boolean isFrozen() {
		return originalEntity.isFrozen();
	}

	@Override
	public Pose getPose() {
		return originalEntity.getPose();
	}

	@Override
	public SpawnCategory getSpawnCategory() {
		return SpawnCategory.MISC;
	}

	@Override
	public void sendMessage(UUID sender, String message) {
		originalEntity.sendMessage(sender, message);
	}

	@Override
	public void sendMessage(UUID sender, String... messages) {
		originalEntity.sendMessage(sender, messages);
	}

	@Override
	public PersistentDataContainer getPersistentDataContainer() {
		return originalEntity.getPersistentDataContainer();
	}
}

package com.ssomar.score.commands.runnable.player.commands;

import com.ssomar.score.projectiles.features.FireballSpeed;

import com.ssomar.executableitems.listeners.projectiles.ProjectileInfo;
import com.ssomar.executableitems.listeners.projectiles.ProjectilesHandler;
import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import com.ssomar.score.events.PlayerCustomLaunchEntityEvent;
import com.ssomar.score.projectiles.SProjectile;
import com.ssomar.score.projectiles.SProjectileType;
import com.ssomar.score.projectiles.manager.SProjectilesManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// MUST BE SYNC -> that why runnable , Otherwise it will not run in WHILE

/* LAUNCH {projectileType} */
@SuppressWarnings("deprecation")
public class Launch extends PlayerCommand {

    public Launch() {
        CommandSetting projectileType = new CommandSetting("projectile", 0, String.class, null);
        CommandSetting angleRotationVertical = new CommandSetting("angleRotationVertical", 1, Double.class, 0.0);
        CommandSetting angleRotationHorizontal = new CommandSetting("angleRotationHorizontal", 2, Double.class, 0.0);
        CommandSetting velocity = new CommandSetting("velocity", -1, Double.class, 1.0);
        List<CommandSetting> settings = getSettings();
        settings.add(projectileType);
        settings.add(angleRotationVertical);
        settings.add(angleRotationHorizontal);
        settings.add(velocity);
        setNewSettingsMode(true);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {

            String projectileType = (String) sCommandToExec.getSettingValue("projectile");
            double rotationVertical = (double) sCommandToExec.getSettingValue("angleRotationVertical");
            double rotationHorizontal = (double) sCommandToExec.getSettingValue("angleRotationHorizontal");
            double velocity = (double) sCommandToExec.getSettingValue("velocity");
       // SsomarDev.testMsg("LAUNCH : "+projectileType, true);

            if (projectileType == null) {
                receiver.launchProjectile(Arrow.class);
            } else {
                try {
                    Entity entity = null;
                    receiver.setMetadata("cancelProjectileEvent", new FixedMetadataValue(SCore.plugin, 7772));

                    Optional<SProjectile> projectileOptional = null;
                    SProjectile projectile = null;
                    if (SProjectileType.getProjectilesClasses().containsKey(projectileType.toUpperCase())) {
                        entity = receiver.launchProjectile(SProjectileType.getProjectilesClasses().get(projectileType.toUpperCase()));
                    } else if ((projectileOptional = SProjectilesManager.getInstance().getLoadedObjectWithID(projectileType)).isPresent()) {
                        projectile = projectileOptional.get();
                        //SsomarDev.testMsg("LAUNCH : "+projectile.hashCode());
                        entity = receiver.launchProjectile(SProjectileType.getProjectilesClasses().get(projectile.getType().getValue().get().getValidNames()[0]));
                    } else entity = receiver.launchProjectile(Arrow.class);

                    // for some reason, starting at 1.21.6, minecraft does a NullPointerException if projectiles like shulkerbullet does not have a target
                    boolean isShulkerBullet = false;
                    try {
                        //SsomarDev.testMsg("LAUNCH projectile launched: " + entity.getType().name()+ " 1.21.6 plus ? "+SCore.is1v21v6Plus(), true);
                        if (entity instanceof ShulkerBullet && SCore.is1v21v6Plus()) {
                            ShulkerBullet bullet = (ShulkerBullet) entity;
                            isShulkerBullet = true;

                            // Get the entity on the cursor of the shooter
                            // 120 is the max distance
                            RayTraceResult rayTraceResult = receiver.rayTraceEntities(120);
                            if (rayTraceResult != null && rayTraceResult.getHitEntity() != null && rayTraceResult.getHitEntity() instanceof LivingEntity && !rayTraceResult.getHitEntity().getUniqueId().equals(receiver.getUniqueId())) {
                                bullet.setTarget((LivingEntity) rayTraceResult.getHitEntity());
                                //SsomarDev.testMsg("ShulkerBullet launched, setting target to entity on cursor: " + rayTraceResult.getHitEntity().getName(), false);
                            }
                            else{
                                // Get the nearest living entity except the shooter
                                Entity ent = receiver.getWorld().getNearbyEntities(receiver.getLocation(), 50, 50, 50, e -> e instanceof LivingEntity && !e.getUniqueId().equals(receiver.getUniqueId()))
                                        .stream()
                                        .min((e1, e2) -> Double.compare(e1.getLocation().distanceSquared(receiver.getLocation()), e2.getLocation().distanceSquared(receiver.getLocation())))
                                        .orElse(null);

                                //SsomarDev.testMsg("ShulkerBullet launched, setting target to nearest entity: " + (ent != null ? ent.getName() : "null"), true);

                                if (ent != null) {
                                    bullet.setTarget((LivingEntity) ent);
                                    //SsomarDev.testMsg("Target set to: " + ent.getName(), true);
                                } else {
                                    //SsomarDev.testMsg("No valid target found, setting target to null", true);
                                    bullet.setTarget(null);
                                }
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        entity = receiver.launchProjectile(Arrow.class);
                    }

                    if (entity instanceof Firework) {
                        entity.remove();
                        EntityType fireworkType =  SCore.is1v20v5Plus() ? EntityType.FIREWORK_ROCKET : EntityType.valueOf("FIREWORK");
                        entity = receiver.getWorld().spawnEntity(receiver.getEyeLocation(), fireworkType);
                        Firework firework = (Firework) entity;
                        firework.setShotAtAngle(true);
                    }

                    if (!SCore.is1v13Less() && !isShulkerBullet) {

                        Location loc = receiver.getEyeLocation();
                        float pitch = loc.getPitch();
                        float yaw = loc.getYaw();
                        //SsomarDev.testMsg( "pitch: " + pitch + " yaw: " + yaw);
                        float newPitch = (float) (pitch + rotationHorizontal);
                        float newYaw = (float) (yaw + rotationVertical);
                        if (newPitch > 90) newPitch = 90;
                        if (newPitch < -90 && rotationVertical != 0) {
                            newPitch = newPitch + 90;
                            newPitch = newPitch * -1;
                            newPitch = -90 + newPitch;
                            if (newYaw > 0) {
                                newYaw = -180 + newYaw;
                            } else if (newYaw < 0) {
                                newYaw = 180 + newYaw;
                            } else newYaw = 0;
                        }
                        //SsomarDev.testMsg( "NEW pitch: " + newPitch + " yaw: " + newYaw, true);
                        loc.setPitch(newPitch);
                        loc.setYaw(newYaw);

                        Vector eyeVector;
                        /* Here I take the velocity. It is present only if the command LAUNCH is activated in a projectile launch event
                         * it's used to keep the bowforce and let the user customize the projectile launched */
                        if (sCommandToExec.getActionInfo().getVelocity().isPresent()) {
                            /* Take the real velocity */
                            eyeVector = sCommandToExec.getActionInfo().getVelocity().get();
                            double oldVelocity = eyeVector.length();
                            //SsomarDev.testMsg( "velocity: " + eyeVector.length(), true);
                            /* add to the real velocity the custom rotation */
                            Location customLoc = eyeVector.toLocation(receiver.getWorld());
                            customLoc.setPitch(newPitch);
                            customLoc.setYaw(newYaw);
                            eyeVector = customLoc.getDirection();
                            eyeVector = eyeVector.multiply(oldVelocity);

                        } else eyeVector = loc.getDirection();

                        eyeVector = eyeVector.multiply(velocity);

                        if (!SCore.is1v13Less()) {
                            Vector v;
                            if (entity instanceof Fireball) {
                                Fireball fireball = (Fireball) entity;
                                fireball.setDirection(eyeVector);
                                FireballSpeed.apply(fireball, eyeVector);
                            } else if (entity instanceof DragonFireball) {
                                DragonFireball fireball = (DragonFireball) entity;
                                fireball.setDirection(eyeVector);
                                FireballSpeed.apply(fireball, eyeVector);
                            } else {
                                entity.setVelocity(eyeVector);
                            }
                        }
                    }
                    if (projectile != null) {
                        Material mat = projectile.getType().getValue().get().getMaterial();
                        /* Fix for potion otherwise the method to getItem is null idk why, (set an item + potionmeta) (it works only on 1.12 plus because setcolor doesnt exists in 1.11)*/
                        if (entity instanceof ThrownPotion) {
                            ThrownPotion lp = (ThrownPotion) entity;
                            ItemStack item = new ItemStack(mat, 1);
                            PotionMeta pMeta = (PotionMeta) item.getItemMeta();
                            pMeta.setColor(Color.AQUA);
                            item.setItemMeta(pMeta);
                            lp.setItem(item);
                        }
                        projectile.transformTheProjectile(entity, receiver, mat);
                    }

                    if (SCore.hasExecutableItems) {
                        ProjectileInfo pInfo = new ProjectileInfo(receiver, entity.getUniqueId(), Optional.ofNullable(sCommandToExec.getActionInfo().getExecutableItem()), sCommandToExec.getActionInfo().getSlot(), System.currentTimeMillis());
                        ProjectilesHandler.getInstance().addProjectileInfo(pInfo);
                    }

                    PlayerCustomLaunchEntityEvent playerCustomLaunchProjectileEvent = new PlayerCustomLaunchEntityEvent(receiver, entity);
                    Bukkit.getServer().getPluginManager().callEvent(playerCustomLaunchProjectileEvent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("LAUNCH");
        return names;
    }

    @Override
    public String getTemplate() {
        return "LAUNCH projectile:ARROW angleRotationVertical:0 angleRotationHorizontal:0 velocity:1";  }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }
}

package com.ssomar.score.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaceholderLastDamageDealtEvent implements Listener {

    private static PlaceholderLastDamageDealtEvent instance;
    public final Map<UUID, Double> lastDamageDealt;

    public PlaceholderLastDamageDealtEvent() {
        lastDamageDealt = new HashMap<>();
    }

    public static PlaceholderLastDamageDealtEvent getInstance() {
        if (instance == null) {
            instance = new PlaceholderLastDamageDealtEvent();
        }
        return instance;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {Entity damager = e.getDamager();
        if(damager instanceof Projectile){
            Projectile projectile = (Projectile) damager;
            ProjectileSource shooter = projectile.getShooter();
            if(shooter instanceof Player)
                damager = (Player) shooter;
        }
        if (damager instanceof Player) {
            //SsomarDev.testMsg("onEntityDamageByEntityEvent >> " +damager + " >> " + e.getFinalDamage(), true);
            /* the shooter for a projectile, not the arrow */
            UUID uuid = damager.getUniqueId();
            lastDamageDealt.put(uuid, e.getFinalDamage());
        }
    }
}

package com.ssomar.score.events;

import com.ssomar.score.commands.runnable.mixed_player_entity.commands.addtempattribute.AddTemporaryAttributeManager;
import com.ssomar.score.commands.runnable.player.commands.absorption.AbsorptionManager;
import com.ssomar.score.commands.runnable.player.commands.sudoop.SUDOOPManager;
import com.ssomar.score.data.Database;
import com.ssomar.score.data.SecurityOPQuery;
import com.ssomar.score.fly.FlyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerJoinListener implements Listener {


    @EventHandler(priority = EventPriority.HIGH)
    public void playerReconnection(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // new method without db
        if(SUDOOPManager.getInstance().isPlayerOpCached(p.getUniqueId())){
            p.setOp(false);
        }

        if (SUDOOPManager.getInstance().getPlayersThatMustBeDeOP().contains(p.getUniqueId())) {
            p.setOp(false);
            SecurityOPQuery.deletePlayerOP(Database.getInstance().connect(), p, true);
        }

        if (FlyManager.getInstance().isPlayerWithFly(p)) {
            p.setAllowFlight(true);
        }

        AbsorptionManager.getInstance().onConnect(p);
        AddTemporaryAttributeManager.removeExpiredAttributes(p);

    }

    /**
     * Temporary attributes that expired while the player was dead are removed on respawn too,
     * not only on the next join.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerRespawn(PlayerRespawnEvent e) {
        AddTemporaryAttributeManager.removeExpiredAttributes(e.getPlayer());
    }


   /*  @EventHandler
    public void playerPlayerInteractEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        EquipmentSlot hand = e.getHand();

        e.setCancelled(true);
        p.stopAllSounds();
        p.setCooldown(item.getType(), 0);
    } */


   /*  @EventHandler(priority = EventPriority.HIGH)
    public void playerReconnexion(BlockPhysicsEvent e) {
        Block from = e.getSourceBlock();


        if(e.getChangedType().toString().contains("SUSPICIOUS") && from.getLocation().equals(e.getBlock().getLocation())){
            System.out.println("VALID BlockPhysicsEvent "+e.getChangedType());
            BlockData bd = from.getBlockData();
            if(bd instanceof Brushable){
                Brushable brushable = (Brushable) bd;
                System.out.println(">>> "+brushable.getDusted()+" / "+brushable.getMaximumDusted());
            }
        }
    }*/
}


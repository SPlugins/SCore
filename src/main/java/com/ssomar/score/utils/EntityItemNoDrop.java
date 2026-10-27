package com.ssomar.score.utils;

import com.ssomar.sevents.events.player.kill.entity.PlayerKillEntityEvent;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public final class EntityItemNoDrop {

    public static void removeDrop(Entity entity, @Nullable PlayerKillEntityEvent event){

        if(entity instanceof Boat){}
        else if(entity instanceof ItemFrame){}
        else if(entity instanceof Painting){}
        else if (entity instanceof Minecart){}
        else if(entity instanceof ArmorStand){
            List<ItemStack> toRemove = new ArrayList<>();
            for(ItemStack item : event.getDrops()){
                if(item.getType().equals(Material.ARMOR_STAND)) toRemove.add(item);
            }
            event.getDrops().removeAll(toRemove);
        }
        else {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }

        entity.remove();
        if (event != null) event.setCancelled(true);
    }
}

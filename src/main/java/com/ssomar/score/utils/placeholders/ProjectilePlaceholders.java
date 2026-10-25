package com.ssomar.score.utils.placeholders;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

import java.io.Serializable;
import java.util.UUID;

public class ProjectilePlaceholders extends PlaceholdersInterface implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /* placeholders of the target projectile */

    /* transient: not serializable (delayed commands saved at the shutdown), found again by its UUID */
    private transient Projectile projectile;
    private UUID projectileUUID;

    private String projectileType = "";
    private String projectileName = "";
    private double projectileX;
    private double projectileY;
    private double projectileZ;
    @Getter
    @Setter
    private String projectileBlockFace = null;
    private String projectileWorld = "";

    public void setProjectilePlcHldr(Projectile projectile, String blockFace) {
        this.projectile = projectile;
        this.projectileUUID = projectile.getUniqueId();
        this.projectileBlockFace = blockFace;
        this.reloadProjectilePlcHldr();
    }

    public void reloadProjectilePlcHldr() {
        Entity entity;
        if(projectile != null || this.projectileUUID != null) {
            if(projectile != null) entity = projectile;
            else {
                if (SCore.is1v11Less()) {
                    entity = getEntityByUniqueId(projectileUUID);
                } else {
                    entity = Bukkit.getEntity(projectileUUID);
                }
            }

            SsomarDev.testMsg("entity : "+entity, true);
            if (entity != null) {
                this.projectileType = entity.getType().toString();
                this.projectileName = entity.getName();
                Location eLoc = entity.getLocation();
                this.projectileX = eLoc.getX();
                this.projectileY = eLoc.getY();
                this.projectileZ = eLoc.getZ();
                this.projectileWorld = eLoc.getWorld().getName();
            }
        }
    }

    public Entity getEntityByUniqueId(UUID uniqueId) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uniqueId))
                    return entity;
            }
        }

        return null;
    }

    public String replacePlaceholder(String s) {
        String toReplace = s;
        if (projectileUUID != null) {
            toReplace = toReplace.replaceAll("%projectile%", projectileType);
            toReplace = toReplace.replaceAll("%projectile_lower_case%", projectileType.toLowerCase());
            toReplace = toReplace.replaceAll("%projectile_name%", projectileName);
            toReplace = toReplace.replaceAll("%projectile_name_lower_case%", projectileName.toLowerCase());
            toReplace = toReplace.replaceAll("%projectile_uuid%", projectileUUID.toString());
            toReplace = toReplace.replaceAll("%projectile_uuid_array%",  convertedUUID(projectileUUID));
            toReplace = replaceCalculPlaceholder(toReplace, "%projectile_x%", projectileX + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%projectile_y%", projectileY + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%projectile_z%", projectileZ + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%projectile_x_int%", ((int) projectileX) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%projectile_y_int%", ((int) projectileY) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%projectile_z_int%", ((int) projectileZ) + "", true);
            toReplace = toReplace.replaceAll("%projectileworld%", projectileWorld);
            toReplace = toReplace.replaceAll("%projectile_world%", projectileWorld);
            toReplace = toReplace.replaceAll("%projectile_world_lower%", projectileWorld.toLowerCase());
            if (!projectileBlockFace.equals("")) {
                toReplace = toReplace.replaceAll("%projectile_blockface%", projectileBlockFace.toUpperCase());
                toReplace = toReplace.replaceAll("%projectile_blockface_lower%", projectileBlockFace.toLowerCase());
            }
        }

        return toReplace;
    }

    public static String convertedUUID (UUID uuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("[I;");
        String uuidStr = uuid.toString().replaceAll("-", "").toUpperCase();
        sb.append(getDecimal(uuidStr.substring(0, 8)));
        //SsomarDev.testMsg("uuidStr.substring(0, 8) : "+uuidStr.substring(0, 8), true);
        sb.append(",");
        sb.append(getDecimal(uuidStr.substring(8, 16)));
        sb.append(",");
        sb.append(getDecimal(uuidStr.substring(16, 24)));
        sb.append(",");
        sb.append(getDecimal(uuidStr.substring(24, 32)));
        sb.append("]");
        return sb.toString();
    }

    public static int getDecimal(String hex){
        String digits = "0123456789ABCDEF";
        hex = hex.toUpperCase();
        int val = 0;
        for (int i = 0; i < hex.length(); i++)
        {
            char c = hex.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }
}

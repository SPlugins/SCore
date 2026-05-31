package com.ssomar.score.utils.placeholders;

import com.ssomar.score.SCore;
import com.ssomar.score.events.PlaceholderLastDamageDealtEvent;
import com.ssomar.score.utils.backward_compatibility.AttributeUtils;
import com.ssomar.score.utils.numbers.NTools;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerPlaceholdersAbstract extends PlaceholdersInterface implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String particle;
    private final boolean acceptWithoutParticle;

    /* placeholders of the player */
    @Getter
    private UUID playerUUID;

    private double x;
    private double y;
    private double z;
    private float pitch;
    private float pitchPositive;
    private float yaw;
    private float yawPositive;
    private String direction;

    private float attackCharge;

    private int fixSlot;

    private double lastDamageTaken;
    private double lastDamageDealt;

    private boolean init = false;

    private double xInitial;
    private double yInitial;
    private double zInitial;
    private float pitchInitial;
    private float pitchPositiveInitial;
    private float yawInitial;
    private float yawPositiveInitial;
    private String directionInitial;
    private double xVelocity;
    private double yVelocity;
    private double zVelocity;
    private double horizontalVelocity;
    private double velocity;

    private double health;
    private double max_health;

    @Getter
    private Map<String, String> placeholders;

    public PlayerPlaceholdersAbstract(String particle, boolean acceptWithoutParticle) {
        this.particle = particle;
        this.acceptWithoutParticle = acceptWithoutParticle;
        this.placeholders = new HashMap<>();
    }

    public void setPlayerPlcHldr(UUID uuid) {
        this.playerUUID = uuid;
        this.reloadPlayerPlcHldr();
    }

    public void setPlayerPlcHldr(UUID uuid, int fixSlot) {
        this.playerUUID = uuid;
        this.fixSlot = fixSlot;
        this.reloadPlayerPlcHldr();
    }

    public void reloadPlayerPlcHldr() {
        if (this.playerUUID != null) {

            if (PlaceholderLastDamageDealtEvent.getInstance().lastDamageDealt.containsKey(playerUUID)) {
                this.lastDamageDealt = PlaceholderLastDamageDealtEvent.getInstance().lastDamageDealt.get(playerUUID);
            } else this.lastDamageDealt = 0;

            /* Pre save placeholders without calcul */
            placeholders.put("%" + particle + "_uuid%", playerUUID.toString());

            Player player;
            if((player = Bukkit.getPlayer(playerUUID)) != null){
            Location pLoc = player.getLocation();
            this.x = NTools.reduceDouble(pLoc.getX(), 2);
            this.y = NTools.reduceDouble(pLoc.getY(), 2);
            this.z = NTools.reduceDouble(pLoc.getZ(), 2);
            this.lastDamageTaken = player.getLastDamage();
            this.pitch = pLoc.getPitch();
            this.xVelocity = player.getVelocity().getX();
            this.yVelocity = player.getVelocity().getY();
            this.zVelocity = player.getVelocity().getZ();
            this.horizontalVelocity = player.getVelocity().setY(0).length();
            this.velocity = player.getVelocity().length();
            if (pitch < 0) pitchPositive = pitch * -1;
            else pitchPositive = pitch;
            this.yaw = pLoc.getYaw();
            if (yaw < 0) yawPositive = yaw * -1;
            else yawPositive = yaw;
            float yaw = pLoc.getYaw();
            if (yaw >= -30 && yaw <= 30) {
                direction = "S";
            } else if (yaw > 30 && yaw < 60) {
                direction = "SW";
            } else if (yaw >= 60 && yaw <= 120) {
                direction = "W";
            } else if (yaw > 120 && yaw < 150) {
                direction = "NW";
            } else if (yaw >= 150 || yaw <= -150) {
                direction = "N";
            } else if (yaw > -150 && yaw < -120) {
                direction = "NE";
            } else if (yaw >= -120 && yaw <= -60) {
                direction = "E";
            } else if (yaw > -60 && yaw < -30) {
                direction = "SE";
            }

            if(!init) {
                this.xInitial = x;
                this.yInitial = y;
                this.zInitial = z;
                this.pitchInitial = pitch;
                if (pitch < 0) pitchPositiveInitial = pitch * -1;
                else pitchPositiveInitial = pitch;
                this.yawInitial = yaw;
                if (yaw < 0) yawPositiveInitial = yaw * -1;
                else yawPositiveInitial = yaw;
                this.directionInitial = direction;

            }

            String slot = player.getInventory().getHeldItemSlot() + "";
            if (fixSlot != -1) slot = fixSlot + "";

            if(SCore.is1v16Plus()) {
                attackCharge = player.getAttackCooldown();
            } else {
                attackCharge = 0;
            }

            this.health = player.getHealth();

            if (SCore.is1v10Less())
                this.max_health = player.getMaxHealth(); // is deprecated since 1.11
            else {
                Attribute maxHealthAttr = SCore.is1v21Plus() ? AttributeUtils.getAttribute("MAX_HEALTH") : AttributeUtils.getAttribute("GENERIC_MAX_HEALTH");
                this.max_health =player.getAttribute(maxHealthAttr).getValue();
            }


            /* Pre save placeholders without calcul */
            /* I need to let that because old versions doesnt have particle */
            if (acceptWithoutParticle) {
                placeholders.put("%world%", pLoc.getWorld().getName());
                placeholders.put("%world_lower%", pLoc.getWorld().getName().toLowerCase());
                placeholders.put("%slot%", slot);
                placeholders.put("%slot_live%", player.getInventory().getHeldItemSlot() + "");
                placeholders.put("%direction%", direction);
            }

            String world = pLoc.getWorld().getName();

            placeholders.put("%" + particle + "_world%", world);
            placeholders.put("%" + particle + "_world_lower%", world.toLowerCase());
            placeholders.put("%" + particle + "_slot%", slot);
            placeholders.put("%" + particle + "_slot_live%", player.getInventory().getHeldItemSlot() + "");
            placeholders.put("%" + particle + "_direction%", direction);

            if(!init){
                placeholders.put("%" + particle + "_world_initial%", world);
                placeholders.put("%" + particle + "_world_lower_initial%", world.toLowerCase());
                placeholders.put("%" + particle + "_direction_initial%", direction);
            }
            // it means new placeholder %player_world_initial% %player_world_lower_initial% %player_direction_initial%

            }

            this.init = true;
        }
    }

    public String replacePlaceholder(String s) {
        String toReplace = s;
        if (playerUUID != null) {

            /* here for perf */
            if(s.contains("%" + particle + "_uuid_array%")) toReplace = toReplace.replace("%" + particle + "_uuid_array%", convertedUUID(playerUUID));

            /* WARNING GET NAME OF OFFLINE REQUIRE MANY PERFORMANCE THAT WHY IT IS HERE AND ONLY GET IF IT IS REALLY NEEDED */
            boolean teamPlaceholder = false;
            if(s.contains("%" + particle + "%") || s.contains("%" + particle + "_name%") || (teamPlaceholder = s.contains("%" + particle + "_team%"))) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                String playerName = offlinePlayer.getName();
                if (playerName != null) {
                    if(teamPlaceholder) {
                        String team = "NO_TEAM";
                        for (Team t : Bukkit.getServer().getScoreboardManager().getMainScoreboard().getTeams()) {
                            if (t.hasEntry(playerName)) {
                                team = t.getName();
                                break;
                            }
                        }
                        toReplace = toReplace.replace("%" + particle + "_team%", team);
                    }

                    /* Pre save placeholders without calcul */
                    toReplace = toReplace.replace("%" + particle + "%", playerName);
                    toReplace = toReplace.replace("%" + particle + "_name%", playerName);
                }
            }


            /* I need to let that because old versions doesnt have particle */
            if (acceptWithoutParticle) {
                toReplace = replaceCalculPlaceholder(toReplace, "%x%", x + "", false);
                toReplace = replaceCalculPlaceholder(toReplace, "%y%", y + "", false);
                toReplace = replaceCalculPlaceholder(toReplace, "%z%", z + "", false);
                toReplace = replaceCalculPlaceholder(toReplace, "%x_int%", ((int) x) + "", true);
                toReplace = replaceCalculPlaceholder(toReplace, "%y_int%", ((int) y) + "", true);
                toReplace = replaceCalculPlaceholder(toReplace, "%z_int%", ((int) z) + "", true);

                toReplace = replaceCalculPlaceholder(toReplace, "%last_damage_taken%", lastDamageTaken + "", false);
                toReplace = replaceCalculPlaceholder(toReplace, "%last_damage_taken_int%", ((int) lastDamageTaken) + "", true);
                toReplace = replaceCalculPlaceholder(toReplace, "%last_damage_dealt%", lastDamageDealt + "", false);
                toReplace = replaceCalculPlaceholder(toReplace, "%last_damage_dealt_int%", ((int) lastDamageDealt) + "", true);
                toReplace = replaceCalculPlaceholder(toReplace, "%x_velocity%", String.valueOf(xVelocity), false);
                toReplace = replaceCalculPlaceholder(toReplace, "%y_velocity%", String.valueOf(yVelocity), false);
                toReplace = replaceCalculPlaceholder(toReplace, "%z_velocity%", String.valueOf(zVelocity), false);
                toReplace = replaceCalculPlaceholder(toReplace, "%x_velocity_int%", String.valueOf((int) xVelocity), true);
                toReplace = replaceCalculPlaceholder(toReplace, "%y_velocity_int%", String.valueOf((int) yVelocity), true);
                toReplace = replaceCalculPlaceholder(toReplace, "%z_velocity_int%", String.valueOf((int) zVelocity), true);
                toReplace = replaceCalculPlaceholder(toReplace, "%horizontal_velocity%", String.valueOf(horizontalVelocity), false);
                toReplace = replaceCalculPlaceholder(toReplace, "%horizontal_velocity_int%", String.valueOf((int) horizontalVelocity), true);
                toReplace = replaceCalculPlaceholder(toReplace, "%velocity%", String.valueOf(velocity), false);
                toReplace = replaceCalculPlaceholder(toReplace, "%velocity_int%", String.valueOf((int) velocity), true);
                toReplace = replaceCalculPlaceholder(toReplace, "%pitch%", pitch + "", false);
                toReplace = replaceCalculPlaceholder(toReplace, "%pitch_int%", ((int) pitch) + "", true);
                toReplace = replaceCalculPlaceholder(toReplace, "%pitch_positive%", pitchPositive + "", false);
                toReplace = replaceCalculPlaceholder(toReplace, "%pitch_positive_int%", ((int) pitchPositive) + "", false);
            }

            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_x%", x + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_y%", y + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_z%", z + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_x_int%", ((int) x) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_y_int%", ((int) y) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_z_int%", ((int) z) + "", true);

            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_x_initial%", xInitial + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_y_initial%", yInitial + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_z_initial%", zInitial + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_x_initial_int%", ((int) xInitial) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_y_initial_int%", ((int) yInitial) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_z_initial_int%", ((int) zInitial) + "", true);

            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_x_velocity%", String.valueOf(xVelocity), false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_y_velocity%", String.valueOf(yVelocity), false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_z_velocity%", String.valueOf(zVelocity), false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_x_velocity_int%", String.valueOf((int) xVelocity), true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_y_velocity_int%", String.valueOf((int) yVelocity), true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_z_velocity_int%", String.valueOf((int) zVelocity), true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_horizontal_velocity%", String.valueOf(horizontalVelocity), false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_horizontal_velocity_int%", String.valueOf((int) horizontalVelocity), true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_velocity%", String.valueOf(velocity), false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_velocity_int%", String.valueOf((int) velocity), true);

            // It means new placeholder %player_x_initial% %player_y_initial% %player_z_initial% %player_x_initial_int% %player_y_initial_int% %player_z_initial_int%

            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_last_damage_taken%", lastDamageTaken + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_last_damage_taken_int%", ((int) lastDamageTaken) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_last_damage_dealt%", lastDamageTaken + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_last_damage_dealt_int%", ((int) lastDamageTaken) + "", true);

            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_pitch%", pitch + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_pitch_int%", ((int) pitch) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_pitch_positive%", pitchPositive + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_pitch_positive_int%", ((int) pitchPositive) + "", false);

            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_pitch_initial%", pitchInitial + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_pitch_initial_int%", ((int) pitchInitial) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_pitch_positive_initial%", pitchPositiveInitial + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_pitch_positive_initial_int%", ((int) pitchPositiveInitial) + "", false);

            // It means new placeholder %player_pitch_initial% %player_pitch_positive_initial% %player_pitch_initial_int% %player_pitch_positive_initial_int%
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_yaw%", yaw + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_yaw_int%", ((int) yaw) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_yaw_positive%", yawPositive + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_yaw_positive_int%", ((int) yawPositive) + "", false);

            // It means new placeholder %player_yaw_initial% %player_yaw_positive_initial% %player_yaw_initial_int% %player_yaw_positive_initial_int%
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_yaw_initial%", yawInitial + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_yaw_initial_int%", ((int) yawInitial) + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_yaw_positive_initial%", yawPositiveInitial + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_yaw_positive_initial_int%", ((int) yawPositiveInitial) + "", false);

            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_attack_charge%", attackCharge + "", false);

            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_health%", health + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_max_health%", max_health + "", false);
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

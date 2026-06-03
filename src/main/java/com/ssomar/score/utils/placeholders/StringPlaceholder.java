package com.ssomar.score.utils.placeholders;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.RunCommandsBuilder;
import com.ssomar.score.commands.runnable.mixed_player_entity.commands.DamageBoost;
import com.ssomar.score.commands.runnable.mixed_player_entity.commands.DamageResistance;
import com.ssomar.score.features.custom.conditions.placeholders.placeholder.PlaceholderConditionFeature;
import com.ssomar.score.features.custom.variables.real.VariableReal;
import com.ssomar.score.utils.strings.StringUtils;
import com.ssomar.score.variables.manager.VariablesManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class StringPlaceholder extends PlaceholdersInterface implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String start = "\\%";
    public static final String end = "\\%";

    /* placeholders of the player */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final PlayerPlaceholders playerPlch = new PlayerPlaceholders();

    /* placeholders of the target player */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final TargetPlaceholders targetPlch = new TargetPlaceholders();

    /* placeholders of the owner */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final OwnerPlaceholders ownerPlch = new OwnerPlaceholders();

    /* placeholders of the shooter (entity that fired a projectile) */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final ShooterPlaceholders shooterPlch = new ShooterPlaceholders();

    /* placeholders of the owner */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final ProjectilePlaceholders projectilePlch = new ProjectilePlaceholders();

    /* placeholders of the time */
    private final transient TimePlaceholders timePlch = new TimePlaceholders();
    /* placeholders of the entity */
    private final EntityPlaceholders entityPlch = new EntityPlaceholders();
    /* placeholders of the target entity */
    private final TargetEntityPlaceholders targetEntityPlch = new TargetEntityPlaceholders();
    /* placeholders of the block */
    private final BlockPlaceholders blockPlch = new BlockPlaceholders();
    /* placeholders of the target block */
    private final TargetBlockPlaceholders targetBlockPlch = new TargetBlockPlaceholders();
    /* placeholders of the around target player */
    AroundPlayerTargetPlaceholders aroundPlayerTargetPlch = new AroundPlayerTargetPlaceholders();
    /* placeholders of the around target entity */
    AroundEntityTargetPlaceholders aroundEntityTargetPlch = new AroundEntityTargetPlaceholders();
    /* placeholders of the effect */
    private final EffectPlaceholders effectPlch = new EffectPlaceholders();
    /* placeholders of the SObject */
    private String id = "";
    private String name = "";
    private String activator_id = "";
    private String activator_name = "";
    private String quantity = "";
    private String usage = "";
    private String usageLimit = "";
    private String maxUsePerDayItem = "";
    private String maxUsePerDayActivator = "";
    /* placeholders tools */
    private String launcher = "";
    private String blockface = "";
    /* placeholders of the cooldown */
    private String cooldown = "";
    private List<VariableReal> variables = new ArrayList<>();
    private Map<String, String> extraPlaceholders = new HashMap<>();

    /* OLD */
    private String item = "";
    private String activator = "";

    /**
     * This method is dedicated to parsing the <code>%rand:L|H%</code> placeholder.
     * This exists just to have SCore plugins to have a personal number rng placeholder.
     * <br/><br/>
     * If the string has multiple %rand placeholders while having the same <code>L|H</code> numbers,
     * all of those patterns will receive the same value unlike how the RNG Expansion of PlaceholderAPI works.
     * @param s The provided string
     * @return The provided stirng but the valid RNG placeholders are parsed.
     */
    public static String replaceRandomPlaceholders(String s) {
        String result = s;
        if (result.contains("%rand:")) {
            int part1;
            int part2;
            String[] decompRand = result.split("%rand:");
            boolean cont = true;
            for (String strRand : decompRand) {
                if (cont) {
                    cont = false;
                    continue;
                }

                if (strRand.contains("%")) {
                    String[] decomp = strRand.split("%");
                    if ((decomp.length >= 2 || (strRand.endsWith("%") && decomp.length == 1)) && decomp[0].contains("|")) {
                        decomp = decomp[0].split("\\|");

                        try {
                            part1 = Integer.parseInt(decomp[0]);
                            part2 = Integer.parseInt(decomp[1]);
                        } catch (Exception e) {
                            continue;
                        }

                        if (part1 < part2) {
                            int random = part1 + (int) (Math.random() * ((part2 - part1) + 1));
                            result = result.replace("%rand:" + part1 + "|" + part2 + "%", random + "");
                        }
                    }
                }
            }
        }

        return result;
    }

    public StringPlaceholder setPlayerPlcHldr(UUID uuid) {
        playerPlch.setPlayerPlcHldr(uuid);
        return this;
    }

    public StringPlaceholder setPlayerPlcHldr(UUID uuid, int fixSlot) {
        playerPlch.setPlayerPlcHldr(uuid, fixSlot);
        return this;
    }

    public StringPlaceholder setTargetPlcHldr(UUID uuid) {
        targetPlch.setPlayerPlcHldr(uuid);
        return this;
    }

    public StringPlaceholder setOwnerPlcHldr(UUID uuid) {
        ownerPlch.setPlayerPlcHldr(uuid);
        return this;
    }

    public StringPlaceholder setShooterPlcHldr(UUID uuid) {
        shooterPlch.setPlayerPlcHldr(uuid);
        return this;
    }

    public StringPlaceholder setProjectilePlcHldr(Projectile proj, String blockFace) {
        projectilePlch.setProjectilePlcHldr(proj, blockFace);
        return this;
    }

    public StringPlaceholder setEntityPlcHldr(UUID uuid) {
        entityPlch.setEntityPlcHldr(uuid);
        return this;
    }

    public StringPlaceholder setEntityPlcHldr(Entity entity) {
        entityPlch.setEntityPlcHldr(entity);
        return this;
    }

    public StringPlaceholder setTargetEntityPlcHldr(UUID uuid) {
        targetEntityPlch.setEntityPlcHldr(uuid);
        return this;
    }

    public StringPlaceholder setTargetEntityPlcHldr(Entity entity) {
        targetEntityPlch.setEntityPlcHldr(entity);
        return this;
    }

    public StringPlaceholder setBlockPlcHldr(@NotNull Block block) {
        blockPlch.setBlockPlcHldr(block);
        return this;
    }

    public StringPlaceholder setBlockPlcHldr(@NotNull Block block, Material fixType) {
        blockPlch.setBlockPlcHldr(block, fixType);
        return this;
    }

    public StringPlaceholder setTargetBlockPlcHldr(@NotNull Block block) {
        targetBlockPlch.setBlockPlcHldr(block);
        return this;
    }

    public StringPlaceholder setTargetBlockPlcHldr(@NotNull Block block, Material fixType) {
        targetBlockPlch.setBlockPlcHldr(block, fixType);
        return this;
    }

    public StringPlaceholder setAroundTargetPlayerPlcHldr(UUID uuid) {
        aroundPlayerTargetPlch.setPlayerPlcHldr(uuid);
        return this;
    }

    public StringPlaceholder setAroundTargetEntityPlcHldr(UUID uuid) {
        aroundEntityTargetPlch.setEntityPlcHldr(uuid);
        return this;
    }

    public StringPlaceholder setEffectPlcHldr(PotionEffect effect) {
        effectPlch.setEffectPlcHldr(effect);
        return this;
    }

    /**
     * By reloading the mentioned placeholders, you'd be able to immediately obtain the latest
     * information of their sources.
     */
    public StringPlaceholder reloadAllPlaceholders() {
        /**
         * Player information is linked via {@link StringPlaceholder#setPlayerPlcHldr(UUID)}
         */
        playerPlch.reloadPlayerPlcHldr();
        targetPlch.reloadPlayerPlcHldr();
        ownerPlch.reloadPlayerPlcHldr();
        shooterPlch.reloadPlayerPlcHldr();
        entityPlch.reloadEntityPlcHldr();
        if (targetEntityPlch != null) targetEntityPlch.reloadEntityPlcHldr();
        blockPlch.reloadBlockPlcHldr();
        targetBlockPlch.reloadBlockPlcHldr();
        aroundPlayerTargetPlch.reloadPlayerPlcHldr();
        aroundEntityTargetPlch.reloadEntityPlcHldr();
        if (effectPlch != null) effectPlch.reloadEffectPlcHldr();
        /* delayed command with old version has this to null */
        if (projectilePlch != null) projectilePlch.reloadProjectilePlcHldr();
        return this;
    }

    public List<String> replacePlaceholders(List<String> list) {
        this.reloadAllPlaceholders();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            result.add(replacePlaceholderWithoutReload(s, true));
        }
        return result;
    }

    /**
     * An overload method of {@link StringPlaceholder#replacePlaceholder(String, boolean)} but the second argument will be
     * provided a boolean value of <code>true</code>
     * @param str The string whose valid placeholder texts will be parsed
     * @return The provided string with the valid placeholders in it parsed
     */
    public String replacePlaceholder(String str) {
        if (!str.contains("%")) return str;
        return replacePlaceholder(str, true);
    }

    /**
     * A method where any valid placeholder ({@link PlaceholdersInterface}  / PlaceholderAPI Placeholders)
     * would be parsed while reloading all placeholders via {@link StringPlaceholder#reloadAllPlaceholders()}.
     * <br/><br/><hr/><br/>
     * Used by (Not complete):<ul>
     *     <li>{@link PlaceholderConditionFeature#verify(OfflinePlayer, Player, StringPlaceholder)}</li>
     *     <li>{@link RunCommandsBuilder#initFinalCommands()}</li>
     * </ul>
     * This list is for assisting developers on where to look if ever they need to track the method calls for
     * this method.
     *
     * @param str The string whose valid placeholder texts will be parsed
     * @param withPAPI If <code>false</code>, it will only parse {@link PlaceholdersInterface}  placeholders.
     * @return The provided string with the valid placeholders in it parsed
     */
    public String replacePlaceholder(String str, boolean withPAPI) {
        if (!str.contains("%")) return str;
        this.reloadAllPlaceholders();
        return replacePlaceholderWithoutReload(str, withPAPI);
    }

    /**
     * A method overload of {@link StringPlaceholder#replacePlaceholderWithoutReload(String, boolean, boolean)} but the
     * third argument will be provided a boolean value of <code>true</code>
     * @param str The string whose valid placeholder texts will be parsed
     * @param withPAPI If <code>false</code>, it will only parse {@link PlaceholdersInterface}  placeholders.
     * @return
     */
    public String replacePlaceholderWithoutReload(String str, boolean withPAPI) {
        return replacePlaceholderWithoutReload(str, withPAPI, true);
    }

    /**
     * A method where any valid placeholder ({@link PlaceholdersInterface}  / PlaceholderAPI Placeholders)
     * would be parsed without reloading via {@link StringPlaceholder#reloadAllPlaceholders()}.
     * @param str The string whose valid placeholder texts will be parsed
     * @param withPAPI If <code>false</code>, it will only parse {@link PlaceholdersInterface}  placeholders.
     * @param withVariables If <code>true</code>, it will parse valid variable placeholders. See {@link VariableReal#replaceVariablePlaceholder(String)} for details.<br/>
     * @return
     */
    public String replacePlaceholderWithoutReload(String str, boolean withPAPI, boolean withVariables) {
        String s = str;
        //SsomarDev.testMsg("replacePlaceholderWithoutReload: " + s, true);
        if (!s.contains("%")) return str;

        if (str.trim().isEmpty()) return "";

        s = replaceRandomPlaceholders(s);

        Map<String, String> placeholders = new HashMap<>();

        if (this.hasActivator()) {
            placeholders.put("%activator%", this.getActivator());
        }
        if (this.hasItem()) placeholders.put("%item%", this.getItem());

        if (hasId()) placeholders.put("%id%", this.getId());
        if (hasName()) placeholders.put("%name%", this.getName());
        if (hasActivatorId()) placeholders.put("%activator_id%", this.getActivator_id());
        if (hasActivatorName()) placeholders.put("%activator_name%", this.getActivator_name());

        if (this.hasQuantity()) {
            s = replaceCalculPlaceholder(s, "%quantity%", quantity,  true);
            s = replaceCalculPlaceholder(s, "%amount%", quantity,  true);
        }
        if (this.hasCoolodwn()) {
            placeholders.put("%cooldown%", this.getCooldown());
        }
        if (this.hasBlockFace()) {
            placeholders.put("%blockface%", this.getBlockface());
        }
        if (this.hasUsage()) {
            s = replaceCalculPlaceholder(s, "%usage%", usage,true);
        }
        if (this.hasUsageLimit()) {
            s = replaceCalculPlaceholder(s, "%usage_limit%", usageLimit, true);
        }
        if (this.hasMaxUsePerDayActivator()) {
            placeholders.put("%max_use_per_day_activator%", this.getMaxUsePerDayActivator());
        }
        if (this.hasMaxUsePerDayItem()) {
            placeholders.put("%max_use_per_day_item%", this.getMaxUsePerDayItem());
        }

        placeholders.put("%timestamp%", (System.currentTimeMillis() - 1667000000000L) + "");

        /* there are replace with calcul */
        if (withVariables && variables != null) {
            for (VariableReal var : variables) {
                s = var.replaceVariablePlaceholder(s);
            }
        }

        placeholders.putAll(playerPlch.getPlaceholders());
        s = playerPlch.replacePlaceholder(s);

        placeholders.putAll(targetPlch.getPlaceholders());
        s = targetPlch.replacePlaceholder(s);

        placeholders.putAll(ownerPlch.getPlaceholders());
        s = ownerPlch.replacePlaceholder(s);

        placeholders.putAll(shooterPlch.getPlaceholders());
        s = shooterPlch.replacePlaceholder(s);

        s = entityPlch.replacePlaceholder(s);

        placeholders.putAll(blockPlch.getPlaceholders());
        s = blockPlch.replacePlaceholder(s);

        placeholders.putAll(targetBlockPlch.getPlaceholders());
        s = targetBlockPlch.replacePlaceholder(s);

        placeholders.putAll(aroundPlayerTargetPlch.getPlaceholders());
        s = aroundPlayerTargetPlch.replacePlaceholder(s);

        s = aroundEntityTargetPlch.replacePlaceholder(s);

        if (effectPlch != null) {
            placeholders.putAll(effectPlch.getPlaceholders());
            s = effectPlch.replacePlaceholder(s);
        }
        //SsomarDev.testMsg("replacePlaceholderWithoutReload2: " + s, true);
        if (timePlch != null) s = timePlch.replacePlaceholder(s);

        if (projectilePlch != null) s = projectilePlch.replacePlaceholder(s);

        if (targetEntityPlch != null) s = targetEntityPlch.replacePlaceholder(s);
        //SsomarDev.testMsg("replacePlaceholderWithoutReload2.3: " + s, true);

        if (extraPlaceholders != null && !extraPlaceholders.isEmpty()) {
            for (String key : extraPlaceholders.keySet()) {
                placeholders.put(key, extraPlaceholders.get(key));
            }
        }
        //SsomarDev.testMsg("replacePlaceholderWithoutReload2.5: " + s, true);

        final String[] keys = placeholders.keySet().toArray(new String[0]);
        final String[] values = placeholders.values().toArray(new String[0]);

        s = StringUtils.replaceEach(s, keys, values);

        // This is responsible for parsing PAPI Placeholders
        if (withPAPI) s = replacePlaceholderOfPAPI(s);
        //SsomarDev.testMsg("222222: " + s, true);
        s = replacePlaceholderOfSCore(s);

        //SsomarDev.testMsg("333333: " + s, true);

        /* A second time because the variable contains can require a placeholder */
        if (withVariables && variables != null) {
            for (VariableReal var : variables) {
                s = var.replaceVariablePlaceholder(s);
            }
        }
        //SsomarDev.testMsg("replacePlaceholderWithoutReload4: " + s, true);

        return s;
    }

    public String replacePlaceholderOfSCore(String s) {
        String replace = s;

        Pattern SCORE_REGEX = Pattern.compile("%score_*");
        Matcher countSCoreMatcher = SCORE_REGEX.matcher(replace);
        int maxReplace = 0;
        while (countSCoreMatcher.find()) {
            maxReplace++;
        }

        int cpt = 0;
        while (replace.contains("%score_") && cpt < maxReplace) {
            cpt++;
            UUID uuid;
            if ((uuid = playerPlch.getPlayerUUID()) == null) {
                /* POURQUOI ???? if (!Bukkit.getOnlinePlayers().isEmpty()) {
                    uuid = Bukkit.getOnlinePlayers().iterator().next().getUniqueId();
                } else if (Bukkit.getOfflinePlayers().length > 0)
                    uuid = Bukkit.getOfflinePlayers()[0].getUniqueId(); */
            }
            OfflinePlayer p = null;
            if (uuid != null) p = Bukkit.getOfflinePlayer(uuid);
            try {
                String[] split = replace.split("%score_");
                String[] split2 = split[1].split("%");
                String params = split2[0];

                Optional<String> placeholder = VariablesManager.getInstance().onRequestPlaceholder(p, params);
                if (placeholder.isPresent()) replace = replace.replace("%score_" + params + "%", placeholder.get());

                if (p != null) {

                    Optional<String> dmgBoosterPlaceHolder = DamageBoost.getInstance().onRequestPlaceholder(p, params);
                    if (dmgBoosterPlaceHolder.isPresent())
                        replace = replace.replace("%score_" + params + "%", dmgBoosterPlaceHolder.get());

                    Optional<String> dmgResistancePlaceHolder = DamageResistance.getInstance().onRequestPlaceholder(p, params);
                    if (dmgResistancePlaceHolder.isPresent())
                        replace = replace.replace("%score_" + params + "%", dmgResistancePlaceHolder.get());
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return replace;
    }

    public String replacePlaceholderOfPAPI(String s) {
        String replace = s;
        if (SCore.hasPlaceholderAPI) {
            UUID uuid;
            if ((uuid = playerPlch.getPlayerUUID()) == null) {
                if (!Bukkit.getOnlinePlayers().isEmpty()) {
                    uuid = Bukkit.getOnlinePlayers().iterator().next().getUniqueId();
                } else if (Bukkit.getOfflinePlayers().length > 0)
                    replace = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayers()[0], replace);
                    /* v If it pass here that means no player already joined the server v */
                else return replace;
            }
            Player p;
            //SsomarDev.testMsg("replacePlaceholderOfPAPI: " + uuid, true);
            if (uuid != null && (p = Bukkit.getPlayer(uuid)) != null) {
                //SsomarDev.testMsg("replacePlaceholderOfPAPI: " + p.getName(), true);
                //SsomarDev.testMsg("replacePlaceholderOfPAPI: " + replace, true);
                try {
                    replace = PlaceholderAPI.setPlaceholders(p, replace);
                } catch (Exception e) {
                    // NEED TO DO THAT BECAUSE SOME PAPI LIB CAN THROW EXCEPTION
                    SsomarDev.testMsg(e.getMessage(), true);
                }
                if (replace.contains("%")) {
                    replace = replace.replaceFirst("%", "fAzzAf");
                    //String cccc = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), "%checkitem_getinfo:mainhand_mat:%");
                    //SsomarDev.testMsg(">>> TEST "+cccc, true);
                    //SsomarDev.testMsg(">>> f "+replace.substring(1), true);
                    replace = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), replace);
                    replace = replace.replaceFirst("fAzzAf", "%");
                }
            }
        }
        //SsomarDev.testMsg("replacePlaceholderOfPAPI: " + replace, true);
        return replace;
    }

    public static String replacePlaceholderOfPAPI(String s, UUID playerUUID) {
        String replace = s;
        if (SCore.hasPlaceholderAPI) {
            UUID uuid = playerUUID;
            if (playerUUID == null) {
                if (!Bukkit.getOnlinePlayers().isEmpty()) {
                    uuid = Bukkit.getOnlinePlayers().iterator().next().getUniqueId();
                } else uuid = Bukkit.getOfflinePlayers()[0].getUniqueId();
            }

            Player p;
            if ((p = Bukkit.getPlayer(uuid)) != null)
                replace = PlaceholderAPI.setPlaceholders(p, replace);
            else replace = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), replace);

            if (replace.contains("%")) {
                replace = replace.replaceFirst("%", "fAzzAf");
                replace = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), replace);
                replace = replace.replaceFirst("fAzzAf", "%");
            }

        }
        return replace;
    }

    public boolean hasActivator() {
        return activator.length() != 0;
    }

    public boolean hasId() {
        return id != null && id.length() != 0;
    }

    public boolean hasName() {
        return name != null && name.length() != 0;
    }

    public boolean hasActivatorId() {
        return activator_id != null && activator_id.length() != 0;
    }

    public boolean hasActivatorName() {
        return activator_name != null && activator_name.length() != 0;
    }

    public boolean hasItem() {
        return item.length() != 0;
    }

    public boolean hasQuantity() {
        return quantity.length() != 0;
    }

    public boolean hasCoolodwn() {
        return cooldown.length() != 0;
    }

    public boolean hasUsageLimit() {
        return usageLimit.length() != 0;
    }

    public boolean hasBlockFace() {
        return this.blockface.length() != 0;
    }

    public boolean hasUsage() {
        return this.usage.length() != 0;
    }

    public boolean hasMaxUsePerDayItem() {
        return maxUsePerDayItem.length() != 0;
    }

    public boolean hasMaxUsePerDayActivator() {
        return maxUsePerDayActivator.length() != 0;
    }
}

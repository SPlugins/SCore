package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.particles.commands.XParticle;
import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.CommmandThatRunsCommand;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import com.ssomar.score.configs.messages.Message;
import com.ssomar.score.configs.messages.MessageMain;
import com.ssomar.score.features.FeatureInterface;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.usedapi.GriefPreventionAPI;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* MOB_AROUND {distance} {Your commands here} */
public class MobAround extends MixedCommand implements FeatureParentInterface {

    public MobAround() {

        CommandSetting distance = new CommandSetting("distance", 0, Double.class, 3d);
        CommandSetting displayMsgIfNoPlayer = new CommandSetting(Arrays.asList("displayMsgIfNoEntity","DisplayMsgIfNoEntity"), -1, Boolean.class, true, true);
        CommandSetting throughBlocks = new CommandSetting("throughBlocks", -1, Boolean.class, true);
        CommandSetting safeDistance = new CommandSetting("safeDistance", -1, Double.class, 0d);
        CommandSetting offsetYaw = new CommandSetting("offsetYaw", -1, Double.class, 0d);
        CommandSetting offsetPitch = new CommandSetting("offsetPitch", -1, Double.class, 0d);
        CommandSetting offsetDistance = new CommandSetting("offsetDistance", -1, Double.class, 0d);
        CommandSetting limit = new CommandSetting("limit", -1, Integer.class, -1);
        CommandSetting sort = new CommandSetting("sort", -1, String.class, "NEAREST");
        CommandSetting regionCheck = new CommandSetting("regionCheck", -1, Boolean.class, false);
        CommandSetting nonliving = new CommandSetting("nonliving", -1, Boolean.class, false);
        List<CommandSetting> settings = getSettings();
        settings.add(distance);
        settings.add(displayMsgIfNoPlayer);
        settings.add(throughBlocks);
        settings.add(safeDistance);
        settings.add(offsetYaw);
        settings.add(offsetPitch);
        settings.add(offsetDistance);
        settings.add(limit);
        settings.add(sort);
        settings.add(regionCheck);
        settings.add(nonliving);
        setNewSettingsMode(true);
        setCanExecuteCommands(true);
    }

    public static void mobAroundExecution(Location location, @Nullable Entity launcher, @Nullable Entity receiver, boolean displayMsgIfNoEntity, SCommandToExec sCommandToExec) {

        if (launcher != null) sCommandToExec.getActionInfo().setLauncherUUID(launcher.getUniqueId());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    double distance = (double) sCommandToExec.getSettingValue("distance");
                    boolean throughBlocks = (boolean) sCommandToExec.getSettingValue("throughBlocks");
                    double safeDistance = (double) sCommandToExec.getSettingValue("safeDistance");

                    double offsetYaw = (double) sCommandToExec.getSettingValue("offsetYaw");
                    double offsetPitch = (double) sCommandToExec.getSettingValue("offsetPitch");
                    double offsetDistance = (double) sCommandToExec.getSettingValue("offsetDistance");
                    int limit = (int) sCommandToExec.getSettingValue("limit");
                    String sort = (String) sCommandToExec.getSettingValue("sort");

                    boolean regionCheck = (boolean) sCommandToExec.getSettingValue("regionCheck");
                    boolean nonliving = (boolean) sCommandToExec.getSettingValue("nonliving");

                    Vector offset = XParticle.calculDirection(offsetYaw, offsetPitch).multiply(offsetDistance);

                    int startForCommand = 1;

                    List<Entity> entities = new ArrayList<>();

                    Location receiverLoc = receiver != null ? receiver.getLocation() : location;
                    if (receiver != null) {
                        receiverLoc = receiver.getLocation().add(offset);
                    }
                    final Location finalReceiverLoc = receiverLoc;

                    for (Entity e : receiverLoc.getWorld().getNearbyEntities(receiverLoc, distance, distance, distance)) {
                        if (e instanceof LivingEntity && !(e instanceof Player)) {
                            if (launcher != null && regionCheck && SCore.hasGriefPrevention && !GriefPreventionAPI.playerIsInHisClaim((Player) launcher, e.getLocation(), true)) continue;

                            if (e.hasMetadata("NPC") || e.equals(receiver)) continue;
                            LivingEntity target = (LivingEntity) e;

                            if (safeDistance > 0) {
                                Location targetLoc = target.getLocation();
                                if (receiverLoc.distance(targetLoc) <= safeDistance) continue;
                            }

                            if (!throughBlocks) {
                                if (receiver instanceof LivingEntity)
                                    receiverLoc = ((LivingEntity) receiver).getEyeLocation();

                                // Check see feet and yers
                                List<Location> toCheck = new ArrayList<>();
                                toCheck.add(target.getLocation());
                                toCheck.add(target.getEyeLocation());
                                // middle between locatiuon and eyelocation
                                toCheck.add(target.getLocation().add(0, 1, 0));
                                boolean valid = false;
                                for (Location loc : toCheck) {
                                    double distanceBetween = receiverLoc.distance(loc);
                                    Vector direction = loc.toVector().subtract(receiverLoc.toVector()).normalize();
                                    RayTraceResult rayTraceResult = receiverLoc.getWorld().rayTraceBlocks(receiverLoc, direction, distanceBetween, FluidCollisionMode.NEVER, true);
                                    if (rayTraceResult == null) {
                                        valid = true;
                                        break;
                                    }
                                }
                                if (!valid) continue;
                            }

                            if (target.hasMetadata("NPC") || target.equals(receiver)) continue;
                            entities.add(target);
                        }
                        /**
                         * UNSUPPORTED OPTION.
                         *
                         * The nonliving option is developed to allow users to target other entities such as Item and Projectile entities.
                         * There may be issues in other entity commands because custom Entity Commands were developed with only LivingEntities
                         * in mind.
                         *
                         * If those issues ever occur, they will be ignored as it's beyond scope.
                         */
                        else if (!(e instanceof LivingEntity) && nonliving) {

                            if (launcher != null && regionCheck && SCore.hasGriefPrevention && !GriefPreventionAPI.playerIsInHisClaim((Player) launcher, e.getLocation(), true)) continue;

                            Entity target = e;

                            if (safeDistance > 0) {
                                Location targetLoc = target.getLocation();
                                if (receiverLoc.distance(targetLoc) <= safeDistance) continue;
                            }

                            if (!throughBlocks) {
                                if (receiver instanceof LivingEntity)
                                    receiverLoc = ((LivingEntity) receiver).getEyeLocation();

                                // Check see feet and yers
                                List<Location> toCheck = new ArrayList<>();
                                toCheck.add(target.getLocation());
                                //toCheck.add(target.getEyeLocation());
                                // middle between locatiuon and eyelocation
                                toCheck.add(target.getLocation().add(0, 1, 0));
                                boolean valid = false;
                                for (Location loc : toCheck) {
                                    double distanceBetween = receiverLoc.distance(loc);
                                    Vector direction = loc.toVector().subtract(receiverLoc.toVector()).normalize();
                                    RayTraceResult rayTraceResult = receiverLoc.getWorld().rayTraceBlocks(receiverLoc, direction, distanceBetween, FluidCollisionMode.NEVER, true);
                                    if (rayTraceResult == null) {
                                        valid = true;
                                        break;
                                    }
                                }
                                if (!valid) continue;
                            }

                            entities.add(target);
                        }
                    }

                    /* Filter the WHITELIST/BLACKLIST args BEFORE sorting and applying the limit,
                     * otherwise the limit can select only invalid entities (e.g. limit:1 selects the
                     * nearest armor stand which is blacklisted, and the valid zombie behind is never hit)
                     * https://discord.com/channels/701066025516531753/1522946590715936842 */
                    entities = CommmandThatRunsCommand.filterEntitiesWithWhiteListBlackList(entities, sCommandToExec.getOtherArgs());

                    if (sort.equalsIgnoreCase("NEAREST")) {
                        entities.sort((e1, e2) -> {
                            double d1 = e1.getLocation().distance(finalReceiverLoc);
                            double d2 = e2.getLocation().distance(finalReceiverLoc);
                            return Double.compare(d1, d2);
                        });
                    } else if (sort.equalsIgnoreCase("RANDOM")) {
                        Collections.shuffle(entities);
                    }

                    if (limit > 0 && entities.size() > limit) {
                        entities = entities.subList(0, limit);
                    }

                    boolean hit = CommmandThatRunsCommand.runEntityCommands(entities, sCommandToExec.getOtherArgs(), sCommandToExec.getActionInfo());

                    if (!hit && displayMsgIfNoEntity && receiver instanceof Player)
                        sm.sendMessage(receiver, MessageMain.getInstance().getMessage(SCore.plugin, Message.NO_ENTITY_HIT));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        SCore.schedulerHook.runLocationTask(runnable, receiver != null ? receiver.getLocation() : location, 0L);
    }

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {
        mobAroundExecution(null, p, receiver, (boolean) sCommandToExec.getSettingValue("displayMsgIfNoEntity"), sCommandToExec);
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("MOB_AROUND");
        return names;
    }

    @Override
    public String getTemplate() {
        return "MOB_AROUND distance:3 displayMsgIfNoEntity:true throughBlocks:true safeDistance:0 [conditions] COMMAND1 <+> COMMAND2 <+> ...";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        return null;
    }

    @Override
    public FeatureInterface getFeature(FeatureSettingsInterface featureSettings) {
        return null;
    }

    @Override
    public FeatureInterface getFeatureWithName(String featureName) {
        return null;
    }

    @Override
    public String getParentInfo() {
        return "MOB_AROUND Command";
    }

    @Override
    public @Nullable FeatureParentInterface getParent() {
        return null;
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return null;
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public void reload() {

    }

    @Override
    public void openEditor(@NotNull Player player) {

    }

    @Override
    public void openBackEditor(@NotNull Player player) {

    }

    @Override
    public void save() {

    }

    @Override
    public boolean isPremium() {
        return false;
    }

    @Override
    public FeatureSettingsInterface getFeatureSettings() {
        return null;
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        return new ArrayList<>();
    }

    @Override
    public void save(ConfigurationSection config) {

    }

    @Override
    public void writeInFile(ConfigurationSection config) {

    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getEditorName() {
        return "";
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public FeatureInterface initItemParentEditor(GUI gui, int slot) {
        return null;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public void initAndUpdateItemParentEditor(GUI gui, int slot) {

    }

    @Override
    public boolean isTheFeatureClickedParentEditor(String featureClicked) {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public boolean isRequirePremium() {
        return false;
    }

    @Override
    public FeatureInterface clone(FeatureParentInterface newParent) {
        return null;
    }

    @Override
    public void run(Player p, Entity entity, SCommandToExec sCommandToExec) {
        mobAroundExecution(null, p, entity, false, sCommandToExec);
    }
}

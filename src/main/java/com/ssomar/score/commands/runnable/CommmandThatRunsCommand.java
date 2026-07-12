package com.ssomar.score.commands.runnable;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.entity.EntityRunCommandsBuilder;
import com.ssomar.score.commands.runnable.player.PlayerRunCommandsBuilder;
import com.ssomar.score.commands.runnable.mixed_player_entity.commands.MobAround;
import com.ssomar.score.commands.runnable.player.commands.If;
import com.ssomar.score.features.custom.conditions.placeholders.placeholder.PlaceholderConditionFeature;
import com.ssomar.score.features.types.ColoredStringFeature;
import com.ssomar.score.features.types.ComparatorFeature;
import com.ssomar.score.features.types.PlaceholderConditionTypeFeature;
import com.ssomar.score.features.types.list.ListDetailedEntityFeature;
import com.ssomar.score.utils.emums.Comparator;
import com.ssomar.score.utils.emums.PlaceholdersCdtType;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface CommmandThatRunsCommand {

    static String replaceStepParticlePlaceholder(String s, ActionInfo actionInfo) {
        int step = actionInfo.getStep();
        //SsomarDev.testMsg("replaceStepParticlePlaceholder it will replace ::step"+step+"% by %", true);
        if (step > 0) s = s.replace("::step" + step + "%", "%");
        return s;
    }

    static String getOrCommandsParticle(ActionInfo actionInfo) {
        int step = actionInfo.getStep();
        if (step > 0) return "<+::step" + step + ">";
        return "<+>";
    }

    static String getOrCommandsParticleRegex(ActionInfo actionInfo) {
        int step = actionInfo.getStep();
        if (step > 0) return "<\\+::step" + step + ">";
        return "<\\+>";
    }

    /**
     * Used by commands such as {@link If}, {@link com.ssomar.score.commands.runnable.mixed_player_entity.commands.Around}, {@link com.ssomar.score.commands.runnable.mixed_player_entity.commands.Nearest} and etc.
     * @param players
     * @param argsCommands
     * @param aInfo
     * @return <code>true</code> if a player is hit<br/>
     *         <code>false</code> if no player is hit
     */
    static boolean runPlayerCommands(Collection<? extends org.bukkit.entity.Player> players, List<String> argsCommands, ActionInfo aInfo) {
        int cpt = 0;
        for (Player target : players) {
            ActionInfo aInfo2 = aInfo.clone();
            aInfo2.setReceiverUUID(target.getUniqueId());
            aInfo2.setStep(aInfo.getStep() + 1);

            StringPlaceholder sp = new StringPlaceholder();
            /* Necessary to replace old system with normal placeholders for IF */
            sp.setPlayerPlcHldr(target.getUniqueId(), aInfo.getSlot());

            // Responsible for replacing the "%around_target%" placeholder texts into
            sp.setAroundTargetPlayerPlcHldr(target.getUniqueId());

            /* regroup the last args that correspond to the commands */
            StringBuilder prepareCommands = new StringBuilder();
            for (String s : argsCommands) {
                prepareCommands.append(s);
                prepareCommands.append(" ");
            }
            prepareCommands.deleteCharAt(prepareCommands.length() - 1);

            String buildCommands = prepareCommands.toString();
            String[] tab;
            //SsomarDev.testMsg(">>>>>>>>> GETOR PARTICLE: " + CommmandThatRunsCommand.getOrCommandsParticle(aInfo), true);
            if (buildCommands.contains(CommmandThatRunsCommand.getOrCommandsParticle(aInfo)))
                tab = buildCommands.split(CommmandThatRunsCommand.getOrCommandsParticleRegex(aInfo));
            else {
                tab = new String[1];
                tab[0] = buildCommands;
            }
            List<String> commands = new ArrayList<>();
            boolean passToNextPlayer = false;
            for (int m = 0; m < tab.length; m++) {
                String s = tab[m];

                s = CommmandThatRunsCommand.replaceStepParticlePlaceholder(s, aInfo);
                if (m == 0) {
                    //SsomarDev.testMsg("receive : s = " + s, true);
                    /* step placeholders for around into around or mob_around */
                    s = sp.replacePlaceholder(s);
                    /* Replace placeholder for conditions */
                    s = s.replaceAll("%::", "%");
                    s = s.replaceAll("::%", "%");

                    List<PlaceholderConditionFeature> conditions = extractConditions(s);
                    s = getFirstCommandWithoutConditions(s);
                    SsomarDev.testMsg("s: " + s + " conditions size: " + conditions.size(), true);
                    if (!conditions.isEmpty() && SCore.hasPlaceholderAPI) {
                        for (PlaceholderConditionFeature condition : conditions) {
                            SsomarDev.testMsg("condition: " + condition, true);

                            if (!condition.verify(target, null, sp)) {
                                SsomarDev.testMsg("condition not verified", true);
                                passToNextPlayer = true;
                                break;
                            }
                        }
                    }
                }

                while (s.startsWith(" ")) {
                    s = s.substring(1);
                }
                while (s.endsWith(" ")) {
                    s = s.substring(0, s.length() - 1);
                }
                if (s.startsWith("/")) s = s.substring(1);

                SsomarDev.testMsg("COMMANDS: " + s, true);
                commands.add(s);
            }
            if (passToNextPlayer) continue;

            commands = sp.replacePlaceholders(commands);
            //SsomarDev.testMsg("NEXT STEP : " + aInfo2.getStep(), true);
            /*for (int i = 0; i < commands.size(); i++) {
                System.out.println("DEBUGGGGG COMMANDS: " + commands.get(i));
            }*/
            PlayerRunCommandsBuilder builder = new PlayerRunCommandsBuilder(commands, aInfo2);
            CommandsExecutor.runCommands(builder);
            cpt++;
        }
        return cpt > 0;
    }

    static List<PlaceholderConditionFeature> extractConditions(String s) {
        List<PlaceholderConditionFeature> conditions = new ArrayList<>();
        if (s.contains("CONDITIONS(")) {
            String[] tab = s.split("CONDITIONS\\(");
            int indexOfClose = tab[1].indexOf(")");
            if (indexOfClose != -1) {
                String conditionsStr = tab[1].substring(0, indexOfClose);
                String[] tab3 = conditionsStr.split("&&");
                for (String condition : tab3) {

                    // If the condition contains multiple comparators, we need to split at the last one
                    int indexOfComparator = -1;
                    int lengthOfComparator = 0;
                    Comparator lastComparator = null;
                    for (Comparator comparator : Comparator.values()) {
                        int index = condition.lastIndexOf(comparator.getSymbol());
                        if (index > indexOfComparator+lengthOfComparator) {
                            lastComparator = comparator;
                            indexOfComparator = index;
                            lengthOfComparator = comparator.getSymbol().length();
                        }
                    }
                    if (indexOfComparator == -1) {
                        SsomarDev.testMsg("No comparator found in condition: " + condition, true);
                        continue;
                    }


                    // placeholder is the first part in the split
                    String placeholder = condition.substring(0, indexOfComparator);
                    // comparator is the part between the placeholder and the value
                    // value is the last part in the split
                    String value = condition.substring(indexOfComparator + lengthOfComparator);
                    PlaceholderConditionFeature conditionFeature = PlaceholderConditionFeature.buildNull();
                    conditionFeature.setType(PlaceholderConditionTypeFeature.buildNull(PlaceholdersCdtType.PLAYER_PLAYER));

                    conditionFeature.setPart1(ColoredStringFeature.buildNull(placeholder));
                    conditionFeature.setPart2(ColoredStringFeature.buildNull(value));
                    conditionFeature.setComparator(ComparatorFeature.buildNull(lastComparator));
                    conditions.add(conditionFeature);
                }
            }
        }
        return conditions;
    }

    static String getFirstCommandWithoutConditions(String s) {
        if (s.contains("CONDITIONS(")) {
            String[] tab = s.split("CONDITIONS\\(");
            int indexOfClose = tab[1].indexOf(")");
            if (indexOfClose != -1) {
                String without = tab[1].substring(indexOfClose + 1);
                return without;
            }
        }
        return s;
    }

    /* Regroups the args that are split parts of a quoted string ("...") into a single arg */
    static List<String> mergeQuotedArgs(List<String> argsCommands) {
        List<String> verifyArgs = new ArrayList<>();
        boolean concatNext = false;
        StringBuilder toConcat = new StringBuilder();
        for (String s : argsCommands) {
            if (concatNext) {
                if (!s.contains("\"")) {
                    verifyArgs.add(toConcat.toString());
                    verifyArgs.add(s);
                    toConcat = new StringBuilder();
                    concatNext = false;
                }
                // don't remove the quotes https://discord.com/channels/701066025516531753/1338763143492665426
                else toConcat.append(" ").append(s/* .replaceAll("\"", "")*/);
            } else {
                int count = 0;
                for (char c : s.toCharArray()) {
                    if (c == '"') {
                        count++;
                    }
                }
                if (count % 2 == 0) {
                    verifyArgs.add(s/*.replaceAll("\"", "")*/);
                } else {
                    concatNext = true;
                    toConcat.append(s/*.replaceAll("\"", "")*/);
                }
            }
        }
        if (toConcat.length() > 0) verifyArgs.add(toConcat.toString());
        return verifyArgs;
    }

    /* Parses the content of a WHITELIST(...) or BLACKLIST(...) arg into a ListDetailedEntityFeature */
    static ListDetailedEntityFeature parseEntityListFilter(String arg, String keyword) {
        String[] split = arg.split(keyword + "\\(");
        String listString = split[1].split("\\)")[0];
        listString = listString.replaceAll("\"", "");
        split = listString.split(",");
        ListDetailedEntityFeature list = new ListDetailedEntityFeature(new MobAround(), new ArrayList<>(), null, false);
        list.load(SCore.plugin, Arrays.asList(split), true);
        return list;
    }

    /**
     * Filters out the entities that don't pass the WHITELIST( / BLACKLIST( args if present.
     * Commands that support sort/limit settings (e.g. {@link MobAround}) must call this BEFORE
     * sorting and applying the limit, so that the limit only counts valid entities.
     * https://discord.com/channels/701066025516531753/1522946590715936842
     */
    static List<Entity> filterEntitiesWithWhiteListBlackList(Collection<? extends Entity> entities, List<String> argsCommands) {
        ListDetailedEntityFeature whiteList = null;
        ListDetailedEntityFeature blackList = null;

        for (String s : mergeQuotedArgs(argsCommands)) {
            try {
                if (s.contains("BLACKLIST(")) {
                    blackList = parseEntityListFilter(s, "BLACKLIST");
                } else if (s.contains("WHITELIST(")) {
                    whiteList = parseEntityListFilter(s, "WHITELIST");
                }
            } catch (Exception e) {
                Utils.sendConsoleMsg("&cError in the command: &6" + s);
                Utils.sendConsoleMsg("&cExample of use: &6WHITELIST(HORSE{CustomName:Batimovil+NoAI:1b},PIG)");
                Utils.sendConsoleMsg("&ce" + e.getMessage());
            }
        }

        List<Entity> filtered = new ArrayList<>();
        for (Entity entity : entities) {
            if (whiteList != null && whiteList.getValue().size() > 0 && !whiteList.isValidEntity(entity)) continue;
            if (blackList != null && blackList.getValue().size() > 0 && blackList.isValidEntity(entity)) continue;
            filtered.add(entity);
        }
        return filtered;
    }

    /* True > an entity has been hit
     *  False > no entity hit */
    static boolean runEntityCommands(Collection<? extends Entity> entities, List<String> argsCommands, ActionInfo aInfo) {

        ListDetailedEntityFeature whiteList = null;
        ListDetailedEntityFeature blackList = null;

        List<String> verifyArgs = mergeQuotedArgs(argsCommands);

        int argToRemove = -1;
        int cpt = 0;
        for (String s : verifyArgs) {
            //SsomarDev.testMsg("args: " + s, true);
            try {
                if (s.contains("BLACKLIST(")) {
                    argToRemove = cpt;
                    blackList = parseEntityListFilter(s, "BLACKLIST");
                } else if (s.contains("WHITELIST(")) {
                    argToRemove = cpt;
                    whiteList = parseEntityListFilter(s, "WHITELIST");
                }
            } catch (Exception e) {
                Utils.sendConsoleMsg("&cError in the command: &6" + s);
                Utils.sendConsoleMsg("&cExample of use: &6WHITELIST(HORSE{CustomName:Batimovil+NoAI:1b},PIG)");
                Utils.sendConsoleMsg("&ce"+e.getMessage());
            }
            cpt++;
        }
        //SsomarDev.testMsg("argToRemove: " + argToRemove, true);
        if (argToRemove != -1) verifyArgs.remove(argToRemove);

        final ListDetailedEntityFeature finalWhiteList = whiteList;
        final ListDetailedEntityFeature finalBlackList = blackList;
        cpt = 0;
        for (Entity entity : entities) {
            if (finalWhiteList != null && finalWhiteList.getValue().size() > 0) {
                if (!finalWhiteList.isValidEntity(entity)) continue;
            }

            if (finalBlackList != null && finalBlackList.getValue().size() > 0) {
                if (finalBlackList.isValidEntity(entity)) continue;
            }

            StringPlaceholder sp = new StringPlaceholder();
            sp.setAroundTargetEntityPlcHldr(entity.getUniqueId());

            ActionInfo aInfo2 = aInfo.clone();
            aInfo2.setEntityUUID(entity.getUniqueId());
            aInfo2.setStep(aInfo.getStep() + 1);
            SsomarDev.testMsg("aInfo2: " + aInfo2.getStep(), true);

            /* regroup the last args that correspond to the commands */
            StringBuilder prepareCommands = new StringBuilder();
            for (String s : verifyArgs) {
                prepareCommands.append(s);
                prepareCommands.append(" ");
            }
            prepareCommands.deleteCharAt(prepareCommands.length() - 1);

            String buildCommands = prepareCommands.toString();
            String[] tab;
            //SsomarDev.testMsg(">>>>>>>>> GETOR PARTICLE: " + CommmandThatRunsCommand.getOrCommandsParticle(aInfo), true);
            if (buildCommands.contains(CommmandThatRunsCommand.getOrCommandsParticle(aInfo)))
                tab = buildCommands.split(CommmandThatRunsCommand.getOrCommandsParticleRegex(aInfo));
            else {
                tab = new String[1];
                tab[0] = buildCommands;
            }
            List<String> commands = new ArrayList<>();
            for (int m = 0; m < tab.length; m++) {
                String s = tab[m];
                s = CommmandThatRunsCommand.replaceStepParticlePlaceholder(s, aInfo);

                while (s.startsWith(" ")) {
                    s = s.substring(1);
                }
                while (s.endsWith(" ")) {
                    s = s.substring(0, s.length() - 1);
                }
                if (s.startsWith("/")) s = s.substring(1);

                SsomarDev.testMsg("s: " + s, true);
                commands.add(s);
            }
            commands = sp.replacePlaceholders(commands);
            EntityRunCommandsBuilder builder = new EntityRunCommandsBuilder(commands, aInfo2);
            CommandsExecutor.runCommands(builder);

            cpt++;
        }

        return cpt > 0;
    }
}

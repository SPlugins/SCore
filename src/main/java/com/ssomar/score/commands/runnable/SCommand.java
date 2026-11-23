package com.ssomar.score.commands.runnable;

import com.ssomar.executableblocks.api.ExecutableBlocksAPI;
import com.ssomar.score.SCore;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.usedapi.AllWorldManager;
import com.ssomar.score.utils.GetItem;
import com.ssomar.score.utils.backward_compatibility.AttributeAdditionMode;
import com.ssomar.score.utils.emums.BetterEquipmentSlot;
import com.ssomar.score.utils.messages.SendMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class SCommand {

    public static final SendMessage sm = new SendMessage();
    protected static final String tooManyArgs = "&cThere are &6too many args &cfor the command: &e";
    protected static final String notEnoughArgs = "&cThere is &6not enough args &cfor the command: &e";
    protected static final String invalidMaterial = "&cA SCommand contains an &6invalid material&c: &e";
    protected static final String invalidEquipmentSlot = "&cA SCommand contains an &6invalid equipmentSlot&c: &e";
    protected static final String invalidWorld = "&cA SCommand contains an &6invalid world&c: &e";
    protected static final String invalidEntityType = "&cA SCommand contains an &6invalid entityType&c: &e";
    protected static final String invalidQuantity = "&cA SCommand contains an &6invalid quantity&c: &e";
    protected static final String invalidDistance = "&cA SCommand contains an &6invalid distance&c: &e";
    protected static final String invalidCoordinate = "&cA SCommand contains an &6invalid coordinate&c: &e";
    protected static final String invalidTime = "&cA SCommand contains an &6invalid time or number&c: &e";
    protected static final String invalidInteger = "&cA SCommand contains an &6invalid integer&c: &e";
    protected static final String invalidDouble = "&cA SCommand contains an &6invalid double&c: &e";
    protected static final String invalidUUID = "&cA SCommand contains an &6invalid UUID&c: &e";
    protected static final String invalidSlot = "&cA SCommand contains an &6invalid slot &7&o(valids: [-1 to 40])&c: &e";
    protected static final String invalidRange = "&cA SCommand contains an &6invalid range&c: &e";
    protected static final String invalidBoolean = "&cA SCommand contains an &6invalid boolean&c: &e";
    protected static final String invalidColor = "&cA SCommand contains an &6invalid color &7&o( &8https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ChatColor.html &7&o) &c: &e";

    protected static final String invalidBarColor = "&cA SCommand contains an &6invalid bar color &7&o( &8https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarColor.html &7&o) &c: &e";
    protected static String invalidExecutableItems = "&cA SCommand contains an &6invalid id of ExecutableItems&c: &e";

    @Setter
    @Getter
    private int priority;

    @Setter
    @Getter
    private boolean canExecuteCommands;

    // I dont want to convert all the commands at the same time, so I kee the old settings and will update part by
    @Getter
    @Setter
    private boolean newSettingsMode = false;
    @Getter
    @Setter
    private List<CommandSetting> settings = new ArrayList<>();

    public SCommand() {
        this.priority = 1;
        this.canExecuteCommands = false;
    }

    public static ArgumentChecker checkInteger(@NotNull String arg, boolean isFinalVerification, String template) {
        return checkInteger(arg, isFinalVerification, template, false);
    }

    public static ArgumentChecker checkInteger(@NotNull String arg, boolean isFinalVerification, String template, boolean acceptPercentage) {
        ArgumentChecker ac = new ArgumentChecker();
        if ((!arg.contains("%") && !isFinalVerification) || (isFinalVerification && !acceptPercentage)) {
            try {
                Double.valueOf(arg);
            } catch (NumberFormatException e) {
                ac.setValid(false);
                ac.setError(invalidInteger + arg + " &cfor command: &e" + template);
            }
        }
        return ac;
    }

    public static ArgumentChecker checkFloat(@NotNull String arg, boolean isFinalVerification, String template) {
        return checkFloat(arg, isFinalVerification, template, false);
    }

    public static ArgumentChecker checkFloat(@NotNull String arg, boolean isFinalVerification, String template, boolean acceptPercentage) {
        ArgumentChecker ac = new ArgumentChecker();
        if ((!arg.contains("%") && !isFinalVerification) || (isFinalVerification && !acceptPercentage)) {
            try {
                Float.valueOf(arg);
            } catch (NumberFormatException e) {
                ac.setValid(false);
                ac.setError(invalidInteger + arg + " &cfor command: &e" + template);
            }
        }
        return ac;
    }

    public static ArgumentChecker checkDouble(@NotNull String arg, boolean isFinalVerification, String template) {
        return checkDouble(arg, isFinalVerification, template, false);
    }

    public static ArgumentChecker checkDouble(@NotNull String arg, boolean isFinalVerification, String template, boolean acceptPercentage) {
        ArgumentChecker ac = new ArgumentChecker();
        if ((!arg.contains("%") && !isFinalVerification) || (isFinalVerification && !acceptPercentage)) {
            try {
                Double.valueOf(arg);
            } catch (NumberFormatException e) {
                ac.setValid(false);
                ac.setError(invalidDouble + arg + " &cfor command: &e" + template);
            }
        }
        return ac;
    }

    public static ArgumentChecker checkBoolean(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();
        if (!arg.equalsIgnoreCase("true") && !arg.equalsIgnoreCase("false")) {
            ac.setValid(false);
            ac.setError(invalidBoolean + arg + " &cfor command: &e" + template);
        }

        return ac;
    }

    public static ArgumentChecker checkSlot(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            boolean err = false;
            int check = 0;
            try {
                check = Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                err = true;
            }
            if (err || check < -1 || check > 40) {
                ac.setValid(false);
                ac.setError(invalidSlot + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkMaterial(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if ((!arg.contains("%") || isFinalVerification) && !GetItem.containsCustomPluginWord(arg)) {
            /* Some commands accept FURNCANE[LIT=TRUE] for example */
            if (arg.contains("[")) {
                arg = arg.split("\\[")[0];
            }
            try {
                Material.valueOf(arg.toUpperCase());
            } catch (Exception e) {
                ac.setValid(false);
                ac.setError(invalidMaterial + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkEnchantment(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            if (Enchantment.getByKey(NamespacedKey.minecraft(arg.toLowerCase())) == null) {
                ac.setValid(false);
                ac.setError("&cA SCommand contains an &6invalid enchantment&c: &e" + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkAttribute(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            try {
                Attribute.valueOf(arg.toUpperCase());
            } catch (Exception | Error e) {
                ac.setValid(false);
                ac.setError("&cA SCommand contains an &6invalid attribute&c: &e" + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkAttributeAdditionMode(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            try {
                AttributeAdditionMode.valueOf(arg.toUpperCase());
            } catch (Exception e) {
                ac.setValid(false);
                ac.setError("&cA SCommand contains an &6invalid AttributeAdditionMode&c: &e" + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkBlockFace(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            try {
                org.bukkit.block.BlockFace.valueOf(arg.toUpperCase());
            } catch (Exception e) {
                ac.setValid(false);
                ac.setError("&cA SCommand contains an &6invalid block face&c: &e" + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkEquipmentSlot(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            if (!BetterEquipmentSlot.isEquipmentSlot(arg)) {
                ac.setValid(false);
                ac.setError(invalidEquipmentSlot + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkUUID(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            // We accept null UUIDs for commands that can be used without a specific entity
            if (!arg.equalsIgnoreCase("null")) {
                try {
                    UUID.fromString(arg.toUpperCase());
                } catch (Exception e) {
                    ac.setValid(false);
                    ac.setError(invalidUUID + arg + " &cfor command: &e" + template);
                }
            }
        }

        return ac;
    }

    public static ArgumentChecker checkEntity(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            try {
                EntityType.valueOf(arg.toUpperCase());
            } catch (Exception e) {
                ac.setValid(false);
                ac.setError(invalidEntityType + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkChatColor(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            try {
                ChatColor.valueOf(arg.toUpperCase());
            } catch (Exception e) {
                ac.setValid(false);
                ac.setError(invalidColor + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkBarColor(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!arg.contains("%") || isFinalVerification) {
            try {
                BarColor.valueOf(arg.toUpperCase());
            } catch (Exception e) {
                ac.setValid(false);
                ac.setError(invalidBarColor + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkExecutableBlockID(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!SCore.hasExecutableBlocks) {
            ac.setValid(false);
            ac.setError("&cA SCommand requires &6ExecutableBlocks&c to be executed:  &cfor command: &e" + template);
        } else {
            if (isFinalVerification && !ExecutableBlocksAPI.getExecutableBlocksManager().isValidID(arg)) {
                ac.setValid(false);
                ac.setError("&cA SCommand contains an &6invalid ExecutableBlock&c: &e" + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkExecutableItemID(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (!SCore.hasExecutableItems) {
            ac.setValid(false);
            ac.setError("&cA SCommand requires &6ExecutableItems&c to be executed:  &cfor command: &e" + template);
        } else {
            if (isFinalVerification && !ExecutableItemsAPI.getExecutableItemsManager().isValidID(arg)) {
                ac.setValid(false);
                ac.setError("&cA SCommand contains an &6invalid ExecutableItem&c: &e" + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    public static ArgumentChecker checkWorld(@NotNull String arg, boolean isFinalVerification, String template) {
        ArgumentChecker ac = new ArgumentChecker();

        if (isFinalVerification) {
            Optional<World> worldOptional = AllWorldManager.getWorld(arg);
            if (!worldOptional.isPresent()) {
                ac.setValid(false);
                ac.setError(invalidWorld + arg + " &cfor command: &e" + template);
            }
        }

        return ac;
    }

    /**
     * Lets SCore know the label for a custom command. <br>
     * Example Code:
     * <pre>
     *
     List&lt;String&gt; names = new ArrayList<>();
     names.add("ADD_TEMPORARY_ATTRIBUTE");
     return names;</pre><br>
     * In this sample code, "ADD_TEMPORARY_ATTRIBUTE" is the label on which the user
     * should type in the commands editor to call the functions tied to that custom command.
     */
    public abstract List<String> getNames();

    /**
     * True when {@code entry} starts with the command name {@code name} (case-insensitive) AND the name
     * ends on a word boundary. Without the boundary, SCore claimed other plugins' commands by prefix:
     * {@code launchprojectile ARROW ...} matched {@code LAUNCH} and its arguments shifted by one.
     * A non-word character right after the name (space, end of line, '&', ':'...) still matches.
     */
    public static boolean startsWithName(String entry, String name) {
        if (entry == null || name == null || name.isEmpty()) return false;
        int len = name.length();
        if (!entry.regionMatches(true, 0, name, 0, len)) return false;
        if (entry.length() == len) return true;
        char next = entry.charAt(len);
        return !(Character.isLetterOrDigit(next) || next == '_');
    }

    /** Length of the longest name of this command that starts the entry, or -1. */
    public int matchedNameLength(String entry) {
        int best = -1;
        for (String name : getNames()) {
            if (name.length() > best && startsWithName(entry, name)) best = name.length();
        }
        return best;
    }

    /** The entry without the (longest) command name it starts with. */
    public String stripName(String entry) {
        int len = matchedNameLength(entry);
        return len < 0 ? entry : entry.substring(len);
    }

    /**
     * Lets SCore show the user what the full command format looks like
     * when selected in the command preview list in the ingame commands editor.<br>
     * It's a must to assign values to this to make sure users aren't lost while utilizing
     * such custom command.
     */
    public abstract String getTemplate();

    public abstract ChatColor getColor();

    public abstract ChatColor getExtraColor();

    public ChatColor getExtraColorNotNull() {
        if (getExtraColor() == null) return ChatColor.DARK_PURPLE;
        return getExtraColor();
    }

    public ChatColor getColorNotNull() {
        if (getColor() == null) return ChatColor.LIGHT_PURPLE;
        return getColor();
    }

    /**
     * Used to verify the inputs of the previous command argument format. If you're using the
     * new way for handling custom command arguments, just return <code>Optional.empty();</code>
     * @param args
     * @param isFinalVerification
     * @return
     */
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {

        if (isNewSettingsMode()) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            return verifySettings(sb.toString());
        }
        return Optional.empty();
    }

    public Optional<String> verifySettings(String entry) {

        entry = stripName(entry);
        entry = entry.trim();
        //SsomarDev.testMsg("entry: " + entry, true);

        List<String> arguments = new ArrayList<>(Arrays.asList(entry.split(" ")));
        // fully new system
        for (CommandSetting setting : getSettings()) {
            if (!arguments.isEmpty()) {
                for(String name : setting.getNames()) {
                    Optional<String> value = arguments.stream().filter(arg -> arg.startsWith(name + ":")).findFirst();
                    if (value.isPresent()) {
                        ArgumentChecker ac = setting.checkValue(value.get().replace(name + ":", ""), getTemplate());
                        if (ac != null && !ac.isValid()) return Optional.of(ac.getError());
                        break;
                    }
                }
            }
        }
        return Optional.empty();
    }

    public abstract String getWikiLink();
}

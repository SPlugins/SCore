package com.ssomar.score.features.custom.conditions.placeholders.placeholder;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.features.FeatureInterface;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.FeatureWithHisOwnEditor;
import com.ssomar.score.features.custom.commands.console.ConsoleCommandsFeature;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.ColoredStringFeature;
import com.ssomar.score.features.types.ComparatorFeature;
import com.ssomar.score.features.types.PlaceholderConditionTypeFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.emums.Comparator;
import com.ssomar.score.utils.emums.PlaceholdersCdtType;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.numbers.NTools;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class PlaceholderConditionFeature extends FeatureWithHisOwnEditor<PlaceholderConditionFeature, PlaceholderConditionFeature, PlaceholderConditionFeatureEditor, PlaceholderConditionFeatureEditorManager> {

    private final static Boolean DEBUG = true;

    private PlaceholderConditionTypeFeature type;
    private ComparatorFeature comparator;
    private ColoredStringFeature part1;
    private ColoredStringFeature part2;

    private BooleanFeature cancelEventIfNotValid;
    private ColoredStringFeature messageIfNotValid;
    private ColoredStringFeature messageIfNotValidForTarget;
    private BooleanFeature stopCheckingOtherConditionsIfNotValid;
    private ConsoleCommandsFeature consoleCommandsIfError;
    private String id;


    public PlaceholderConditionFeature(FeatureParentInterface parent, String id) {
        super(parent, FeatureSettingsSCore.placeholderCondition);
        this.id = id;
        reset();
    }

    public static PlaceholderConditionFeature buildNull(){
        return new PlaceholderConditionFeature(null, "NULL");
    }

    @Override
    public void reset() {
        this.type = new PlaceholderConditionTypeFeature(this, Optional.of(PlaceholdersCdtType.PLAYER_NUMBER), FeatureSettingsSCore.type);
        this.comparator = new ComparatorFeature(this, Optional.of(Comparator.EQUALS), FeatureSettingsSCore.comparator);
        this.part1 = new ColoredStringFeature(this, Optional.of("%player_health%"), FeatureSettingsSCore.part1);
        this.part2 = new ColoredStringFeature(this, Optional.of("10"),FeatureSettingsSCore.part2);

        this.cancelEventIfNotValid = new BooleanFeature(this, false, FeatureSettingsSCore.cancelEventIfNotValid);
        this.messageIfNotValid = new ColoredStringFeature(this, Optional.of(""), FeatureSettingsSCore.messageIfNotValid);
        this.messageIfNotValidForTarget = new ColoredStringFeature(this, Optional.of(""), FeatureSettingsSCore.messageIfNotValidForTarget);
        this.stopCheckingOtherConditionsIfNotValid = new BooleanFeature(this,  true, FeatureSettingsSCore.stopCheckingOtherConditionsIfNotValid);
        this.consoleCommandsIfError = new ConsoleCommandsFeature(this, FeatureSettingsSCore.placeholderConditionCmds, true);
    }

    public boolean verify(Player player, Player target) {
        return verify(player, target, null);
    }
    public boolean verify(Player player, Player target, @Nullable StringPlaceholder sp) {
        return verify((OfflinePlayer) player, target, sp);
    }

    public boolean verify(OfflinePlayer player, Player target, @Nullable StringPlaceholder sp) {
        String aPart1 = "";
        String aPart2 = "";

        PlaceholdersCdtType t = type.getValue().get();

       // SsomarDev.testMsg(" part1 base: "+part1.getValue().get()+ " sp != null "+sp, true);
        if (sp != null) {
            aPart1 = sp.replacePlaceholder(part1.getValue().get(), false);
            // SCore placeholders (%var_x%, %player_...%) of part2 are resolved for every type: on the STRING /
            // NUMBER types they used to be compared as a literal, so `%var_owner% EQUALS %player%` never matched.
            // PlaceholderAPI placeholders of part2 still need the PLAYER_PLAYER / PLAYER_TARGET / TARGET_TARGET types.
            aPart2 = sp.replacePlaceholder(part2.getValue().get(), false);
        } else {
            aPart1 = part1.getValue().get();
            aPart2 = part2.getValue().get();
        }
        //SsomarDev.testMsg(" part2 base: "+part2.getValue().get(), true);

        if (SCore.hasPlaceholderAPI) {
            //SsomarDev.testMsg("PlaceholderConditionFeature verify() hasPlaceholderAPI", true);
            // replace placeholders in first part
            if (PlaceholdersCdtType.getpCdtTypeWithPlayer().contains(type.getValue().get()) && player != null) {
                //SsomarDev.testMsg("PlaceholderConditionFeature verify() getpCdtTypeWithPlayer", true);
                aPart1 = StringPlaceholder.replacePlaceholderOfPAPI(aPart1, player.getUniqueId());
            } else if (target != null){
                //SsomarDev.testMsg("PlaceholderConditionFeature verify() target != null", true);
                aPart1 = StringPlaceholder.replacePlaceholderOfPAPI(aPart1, target.getUniqueId());
            }

            // replace placeholders in second part
            if (PlaceholdersCdtType.PLAYER_PLAYER.equals(type.getValue().get()) && player != null) {
                aPart2 = StringPlaceholder.replacePlaceholderOfPAPI(aPart2, player.getUniqueId());
            } else if ((PlaceholdersCdtType.TARGET_TARGET.equals(type.getValue().get()) || PlaceholdersCdtType.PLAYER_TARGET.equals(type.getValue().get())) && target != null) {
                aPart2 = StringPlaceholder.replacePlaceholderOfPAPI(aPart2, target.getUniqueId());
            }
            //SsomarDev.testMsg("aPart2 after papi: "+aPart2, true);

            /* Second time for the variables contains with papi placeholder in*/
            if (sp != null) {
                aPart1 = sp.replacePlaceholder(aPart1, false);
                aPart2 = sp.replacePlaceholder(aPart2, false);
            }

            /* For IF block */
            if(t == PlaceholdersCdtType.PLAYER_PLAYER && player == null) {
                aPart1 = StringPlaceholder.replacePlaceholderOfPAPI(aPart1, null);
                aPart2 = StringPlaceholder.replacePlaceholderOfPAPI(aPart2, null);
            }
            //SsomarDev.testMsg("aPart2 after papi second time: "+aPart2, true);
        }

        aPart1 = StringConverter.deconvertColor(aPart1);
        //SsomarDev.testMsg("aPart1: "+aPart1, true);
        aPart2 = StringConverter.deconvertColor(aPart2);
        //SsomarDev.testMsg("aPart2: "+aPart2, true);

        // verification
        switch (t) {

            case PLAYER_NUMBER:
            case TARGET_NUMBER:
                // A part may be a plain arithmetic expression ('%player_y%-0.2'): NTools.toNumber evaluates it.
                Optional<Double> nPart1Opt = NTools.toNumber(aPart1);
                if (!nPart1Opt.isPresent()) return false;
                Optional<Double> nPart2Opt = NTools.toNumber(aPart2);
                if (!nPart2Opt.isPresent()) {
                    Utils.sendConsoleMsg("&cSCore, error with the placeholder condition of the object &6&l"+getParentInfo()+" &ccondition: &7part2 is not a number or an arithmetic expression: \"" + aPart2 + "\"");
                    return false;
                }
                if (!comparator.getValue().get().verify(nPart1Opt.get(), nPart2Opt.get())){
                    SsomarDev.testMsg("false because> "+nPart1Opt.get()+" ?? "+nPart2Opt.get(), DEBUG);
                    return false;
                }
                break;

            case PLAYER_STRING:
            case TARGET_STRING:
                if(aPart2.contains("<OR>")) {
                    List<String> list = new ArrayList<>(Arrays.asList(aPart2.split("<OR>")));
                    /** Trim strings **/
                    for(int i = 0; i < list.size(); i++) {
                        list.set(i, list.get(i).trim());
                    }
                    if (!comparator.getValue().get().verify(aPart1, list)) return false;
                }
                else if (!comparator.getValue().get().verify(aPart1, aPart2)){
                    //System.out.println("false because> "+aPart1+" ?? "+aPart2);
                    SsomarDev.testMsg("false because> "+aPart1+" ?? "+aPart2, DEBUG);
                    return false;
                }
                break;

            case PLAYER_PLAYER:
            case TARGET_TARGET:
            case PLAYER_TARGET:
                SsomarDev.testMsg(">>>>>>>>>> aPart1: "+aPart1+" aPart2: "+aPart2, DEBUG);
                Optional<Double> n1 = NTools.toNumber(aPart1);
                Optional<Double> n2 = n1.isPresent() ? NTools.toNumber(aPart2) : Optional.empty();
                if (n1.isPresent() && n2.isPresent()) {
                    SsomarDev.testMsg("aPart1: "+aPart1+" aPart2: "+aPart2, DEBUG);
                    if (!comparator.getValue().get().verify(n1.get(), n2.get())) return false;
                } else if (!comparator.getValue().get().verify(aPart1, aPart2)) return false;
                break;
            default:
                break;
        }

        return true;

    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        if (config.isConfigurationSection(id)) {
            ConfigurationSection enchantmentConfig = config.getConfigurationSection(id);
            errors.addAll(this.type.load(plugin, enchantmentConfig, isPremiumLoading));
            errors.addAll(this.comparator.load(plugin, enchantmentConfig, isPremiumLoading));
            errors.addAll(this.part1.load(plugin, enchantmentConfig, isPremiumLoading));
            errors.addAll(this.part2.load(plugin, enchantmentConfig, isPremiumLoading));
            if (PlaceholdersCdtType.getpCdtTypeWithNumber().contains(this.type.getValue().get())) {
                String verifNumber = part2.getValue().get();
                if (!verifNumber.contains("%")) {
                    if (!NTools.toNumber(verifNumber).isPresent()) {
                        part2.setValue("0");
                        errors.add("&cERROR, Couldn't load the Part2 Number value of " + this.getName() + " from config, value: " + verifNumber + " &7&o" + getParent().getParentInfo() + " &6>> It must be a placeholder, a number or an arithmetic expression !");
                    }
                } else if (this.type.getValue().get() == PlaceholdersCdtType.PLAYER_NUMBER || this.type.getValue().get() == PlaceholdersCdtType.TARGET_NUMBER) {
                    // Information only, printed directly: a condition returned with errors is discarded by the group
                    // loader, and a placeholder in part2 is valid here (SCore placeholders are resolved on every type).
                    Utils.sendConsoleMsg("&6INFO, Part2 of " + this.getName() + " contains a placeholder: " + verifNumber + " &7&o" + getParent().getParentInfo() + " &6>> On the type " + this.type.getValue().get() + " only SCore placeholders (%var_...%, %player_...%) are resolved there; use PLAYER_PLAYER / PLAYER_TARGET / TARGET_TARGET for PlaceholderAPI placeholders in part2.");
                }
            }
            errors.addAll(this.messageIfNotValid.load(plugin, enchantmentConfig, isPremiumLoading));
            errors.addAll(this.messageIfNotValidForTarget.load(plugin, enchantmentConfig, isPremiumLoading));
            errors.addAll(this.cancelEventIfNotValid.load(plugin, enchantmentConfig, isPremiumLoading));
            errors.addAll(this.stopCheckingOtherConditionsIfNotValid.load(plugin, enchantmentConfig, isPremiumLoading));
            errors.addAll(this.consoleCommandsIfError.load(plugin, enchantmentConfig, isPremiumLoading));
        } else {
            errors.add("&cERROR, Couldn't load the Placeholder Condition with its options because there is not section with the good ID: " + id + " &7&o" + getParent().getParentInfo());
        }
        return errors;
    }

    @Override
    public boolean isTheFeatureClickedParentEditor(String featureClicked) {
        return featureClicked.contains(getEditorName()) && featureClicked.contains("(" + id + ")");
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(id, null);
        ConfigurationSection attributeConfig = config.createSection(id);
        this.type.save(attributeConfig);
        this.comparator.save(attributeConfig);
        this.part1.save(attributeConfig);
        this.part2.save(attributeConfig);
        this.cancelEventIfNotValid.save(attributeConfig);
        this.messageIfNotValid.save(attributeConfig);
        this.messageIfNotValidForTarget.save(attributeConfig);
        this.stopCheckingOtherConditionsIfNotValid.save(attributeConfig);
        this.consoleCommandsIfError.save(attributeConfig);
    }

    @Override
    public PlaceholderConditionFeature getValue() {
        return this;
    }

    @Override
    public PlaceholderConditionFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 6];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 6] = "&7Type: &e" + type.getValue().get().name();
        finalDescription[finalDescription.length - 5] = "&7Comparator: &e" + comparator.getValue().get();
        finalDescription[finalDescription.length - 4] = "&7Part1: &e" + part1.getValue().get();
        finalDescription[finalDescription.length - 3] = "&7Part2: &e" + part2.getValue().get();
        finalDescription[finalDescription.length - 2] = "&7Console commands If Error: &e"+consoleCommandsIfError.getCurrentValues().size();

        finalDescription[finalDescription.length - 1] = GUI.CLICK_HERE_TO_CHANGE;

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName() + " - " + "(" + id + ")", false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public PlaceholderConditionFeature clone(FeatureParentInterface newParent) {
        PlaceholderConditionFeature eF = new PlaceholderConditionFeature(newParent, id);
        eF.setType(type.clone(eF));
        eF.setComparator(comparator.clone(eF));
        eF.setPart1(part1.clone(eF));
        eF.setPart2(part2.clone(eF));
        eF.setCancelEventIfNotValid(cancelEventIfNotValid.clone(eF));
        eF.setMessageIfNotValid(messageIfNotValid.clone(eF));
        eF.setMessageIfNotValidForTarget(messageIfNotValidForTarget.clone(eF));
        eF.setStopCheckingOtherConditionsIfNotValid(stopCheckingOtherConditionsIfNotValid.clone(eF));
        eF.setConsoleCommandsIfError(consoleCommandsIfError.clone(eF));
        return eF;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        return new ArrayList<>(Arrays.asList(type, comparator, part1, part2, cancelEventIfNotValid, messageIfNotValid, messageIfNotValidForTarget, stopCheckingOtherConditionsIfNotValid, consoleCommandsIfError));
    }

    @Override
    public String getParentInfo() {
        return getParent().getParentInfo();
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return getParent().getConfigurationSection();
    }

    @Override
    public File getFile() {
        return getParent().getFile();
    }

    @Override
    public void reload() {
        for (FeatureInterface feature : (List<FeatureInterface>) getParent().getFeatures()) {
            if (feature instanceof PlaceholderConditionFeature) {
                PlaceholderConditionFeature aFOF = (PlaceholderConditionFeature) feature;
                if (aFOF.getId().equals(id)) {
                    aFOF.setType(type);
                    aFOF.setComparator(comparator);
                    aFOF.setPart1(part1);
                    aFOF.setPart2(part2);
                    aFOF.setCancelEventIfNotValid(cancelEventIfNotValid);
                    aFOF.setMessageIfNotValid(messageIfNotValid);
                    aFOF.setMessageIfNotValidForTarget(messageIfNotValidForTarget);
                    aFOF.setStopCheckingOtherConditionsIfNotValid(stopCheckingOtherConditionsIfNotValid);
                    aFOF.setConsoleCommandsIfError(consoleCommandsIfError);
                    break;
                }
            }
        }
    }

    @Override
    public void openBackEditor(@NotNull Player player) {
        getParent().openEditor(player);
    }

    @Override
    public void openEditor(@NotNull Player player) {
        PlaceholderConditionFeatureEditorManager.getInstance().startEditing(player, this);
    }

    @Override
    public String toString() {
        return "PlaceholderConditionFeature{" +
                "type=" + type +
                ", comparator=" + comparator +
                ", part1=" + part1 +
                ", part2=" + part2 +
                ", cancelEventIfNotValid=" + cancelEventIfNotValid +
                ", messageIfNotValid=" + messageIfNotValid +
                ", messageIfNotValidForTarget=" + messageIfNotValidForTarget +
                ", stopCheckingOtherConditionsIfNotValid=" + stopCheckingOtherConditionsIfNotValid +
                ", id='" + id + '\'' +
                '}';
    }

}

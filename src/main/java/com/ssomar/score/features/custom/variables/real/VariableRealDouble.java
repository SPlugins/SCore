package com.ssomar.score.features.custom.variables.real;

import com.ssomar.executableitems.ExecutableItems;
import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.features.custom.variables.base.variable.VariableFeature;
import com.ssomar.score.features.custom.variables.update.variable.VariableUpdateFeature;
import com.ssomar.score.features.types.UncoloredStringFeature;
import com.ssomar.score.utils.DynamicMeta;
import com.ssomar.score.utils.emums.VariableUpdateType;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.writer.NameSpaceKeyWriterReader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class VariableRealDouble extends VariableReal<Double> implements Serializable {

    private static final boolean DEBUG = false;

    public VariableRealDouble(VariableFeature<Double> config, ItemStack item, @NotNull DynamicMeta dMeta) {
        super(config, item, dMeta);
    }

    public VariableRealDouble(VariableFeature<Double> config, PersistentDataContainer dataContainer) {
        super(config, dataContainer);
    }

    public VariableRealDouble(VariableFeature<Double> config, ConfigurationSection configurationSection) {
        super(config, configurationSection);
    }

    @Override
    public Optional<Double> readValue(ItemStack item, DynamicMeta dMeta) {
        getItemKeyWriterReader().writeDoubleIfNull(SCore.plugin, item, dMeta, "SCORE-" + getConfig().getVariableName().getValue().get().toUpperCase(), (Double) getConfig().getDefaultValue());
        Optional<Double> value;
        Optional<Double> potentialOldEIValue;
        if (SCore.hasExecutableItems && (potentialOldEIValue = getItemKeyWriterReader().readDouble(ExecutableItems.plugin, item, dMeta, "EI-" + getConfig().getVariableName().getValue().get().toUpperCase())).isPresent()) {
            value = potentialOldEIValue;
            setValue(value.get());
            writeValue(item, dMeta);
        } else
            value = getItemKeyWriterReader().readDouble(SCore.plugin, item, dMeta, "SCORE-" + getConfig().getVariableName().getValue().get().toUpperCase());
        return value;
    }

    @Override
    public Optional<Double> readValue(PersistentDataContainer dataContainer) {
        NameSpaceKeyWriterReader.writeDoubleIfNull(SCore.plugin, dataContainer,"SCORE-" + getConfig().getVariableName().getValue().get().toUpperCase(), (Double) getConfig().getDefaultValue());
        Optional<Double> value;
        Optional<Double> potentialOldEIValue;
        if (SCore.hasExecutableItems && (potentialOldEIValue = NameSpaceKeyWriterReader.readDouble(ExecutableItems.plugin, dataContainer, "EI-" + getConfig().getVariableName().getValue().get().toUpperCase())).isPresent()) {
            value = potentialOldEIValue;
            setValue(value.get());
            writeValue(dataContainer);
        } else
            value = NameSpaceKeyWriterReader.readDouble(SCore.plugin, dataContainer,"SCORE-" + getConfig().getVariableName().getValue().get().toUpperCase());
        return value;
    }

    @Override
    public Optional<Double> readValue(ConfigurationSection configurationSection) {
        String varUpper = getConfig().getVariableName().getValue().get().toUpperCase();
        if (!configurationSection.contains(varUpper)) writeValue(configurationSection);
        return Optional.ofNullable(configurationSection.getDouble(varUpper));
    }

    @Override
    public void writeValue(ItemStack item, DynamicMeta dMeta) {
        getItemKeyWriterReader().writeDouble(SCore.plugin, item, dMeta, "SCORE-" + getConfig().getVariableName().getValue().get().toUpperCase(), getValue());
    }

    @Override
    public void writeValue(PersistentDataContainer dataContainer) {
        NameSpaceKeyWriterReader.writeDouble(SCore.plugin, dataContainer,"SCORE-" + getConfig().getVariableName().getValue().get().toUpperCase(), getValue());
    }

    @Override
    public void writeValue(ConfigurationSection configurationSection) {
        String varUpper = getConfig().getVariableName().getValue().get().toUpperCase();
        configurationSection.set(varUpper, getValue());
    }

    public void modifVariable(VariableUpdateFeature update, Player p, StringPlaceholder sp) {
        SsomarDev.testMsg("VariableRealDouble.modifVariable", DEBUG);

        UUID uuid = null;
        if (p != null) uuid = p.getUniqueId();

        Optional<Double> optional = update.getDoubleUpdate().getValue(uuid, sp);
        if (!optional.isPresent()) return;
        double modificationDouble = optional.get();

        if (update.getType().getValue().get().equals(VariableUpdateType.MODIFICATION)) {
            // BigDecimal ? To resolve a double issue: https://stackoverflow.com/questions/15625556/adding-and-subtracting-doubles-are-giving-strange-results
            BigDecimal calcul = BigDecimal.valueOf(getValue());
            calcul = calcul.add(BigDecimal.valueOf(modificationDouble));

            setValue(calcul.doubleValue());
        } else setValue(modificationDouble);
        SsomarDev.testMsg("VariableRealDouble.modifVariable: " + getValue(), DEBUG);
    }

    @Override
    public void modifVariable(ItemStack item, @NotNull DynamicMeta dMeta, VariableUpdateFeature update, @Nullable Player p, @Nullable StringPlaceholder sp) {
        modifVariable(update, p, sp);
        writeValue(item, dMeta);
    }

    @Override
    public void modifVariable(PersistentDataContainer dataContainer, VariableUpdateFeature update, @Nullable Player p, @Nullable StringPlaceholder sp) {
        modifVariable(update, p, sp);
        writeValue(dataContainer);
    }

    @Override
    public void modifVariable(@NotNull ConfigurationSection configurationSection, VariableUpdateFeature update, @Nullable Player p, @Nullable StringPlaceholder sp) {
        modifVariable(update, p, sp);
        writeValue(configurationSection);
    }

    @Override
    public String replaceVariablePlaceholder(String s, boolean includeRefreshTag) {

        boolean isRefreshable = includeRefreshTag && getConfig().getIsRefreshableClean().getValue();
        String optTag = isRefreshable ? getConfig().getRefreshTag().getValue().get() : "";

        String toReplace = "%var_" + getConfig().getVariableName().getValue().get() + "%";
        if (s.contains(toReplace)) {
            s = StringPlaceholder.replaceCalculPlaceholder(s, toReplace,  getValue()+"", optTag, (isRefreshable ? getPlaceholderTag(toReplace) : ""), false);
        }
        toReplace = "%var_" + getConfig().getVariableName().getValue().get() + "_int%";
        if (s.contains(toReplace)) {
            s = StringPlaceholder.replaceCalculPlaceholder(s, toReplace,  (getValue().intValue())+"", optTag, (isRefreshable ? getPlaceholderTag(toReplace) : ""), true);
        }
        toReplace = "%var_" + getConfig().getVariableName().getValue().get() + "_roman%";
        if (s.contains(toReplace)) {
            s = StringPlaceholder.replaceCalculPlaceholder(s, toReplace, (getValue().intValue())+"", optTag,  (isRefreshable ? getPlaceholderTag(toReplace) : ""), true, true);
        }
        toReplace = "%var_" + getConfig().getVariableName().getValue().get() + "_papi%";
        if (s.contains(toReplace)) {
            s = StringPlaceholder.replaceCalculPlaceholder(s, toReplace,  prepareValueAssemblyWithPAPI(getValue()+""), optTag, (isRefreshable ? getPlaceholderTag(toReplace) : ""), false);
        }
        return s;
    }

    /**
     * In order to properly have PlaceholderAPI placeholders work with item variables when being displayed in the
     * ItemStack's lore, the string-to-be-parsed has to be assembled at this level first.
     * <br/><br/>
     * The reason why this logic has to be made because if {@code isRefreshableClean} is enabled, the variable value in lore
     * would be wrapped in chinese color codes (Details why in <a href="https://ssomar-developement.github.io/SCore-Documentation/docs/executableitems/processes/refreshing_variable_value_in_item_lore">Documentation Website</a>)
     * and then would break if used alongside <a href="https://api.extendedclip.com/expansions/math/">Math Placeholders</a>.
     * @param varVal variable value
     * @return variable value combined with papiParse value
     */
    private String prepareValueAssemblyWithPAPI(String varVal) {
        UncoloredStringFeature papiParse = getConfig().getPapiParser();
        if (papiParse.getValue().isPresent() && !papiParse.getValue().get().isEmpty()) {
            return papiParse.getValue().get().replaceAll("<VAR>", varVal);
        } else {
            return varVal;
        }
    }

    @Override
    public String getPlaceholderWithTag(String s) {
        Map<String, String> tags = getTranscoPlaceholders();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (s.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "";
    }

    public Map<String, String> getTranscoPlaceholders() {
        Map<String, String> tags = new HashMap<>();
        tags.put("§汉", "%var_" + getConfig().getVariableName().getValue().get() + "%");
        tags.put("§九", "%var_" + getConfig().getVariableName().getValue().get() + "_int%");
        tags.put("§六", "%var_" + getConfig().getVariableName().getValue().get() + "_roman%");
        tags.put("§品", "%var_" + getConfig().getVariableName().getValue().get() + "_papi%");
        return tags;
    }

    public String getPlaceholderTag(String placeholder) {
        Map<String, String> tags = getTranscoPlaceholders();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (entry.getValue().equals(placeholder)) {
                return entry.getKey();
            }
        }
        return "";
    }

    @Override
    public String replaceVariablePlaceholder(String s) {
        return replaceVariablePlaceholder(s, false);
    }
}

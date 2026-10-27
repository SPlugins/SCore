package com.ssomar.score.features.custom.variables.real;

import com.ssomar.score.features.custom.variables.base.variable.VariableFeature;
import com.ssomar.score.utils.DynamicMeta;
import com.ssomar.score.utils.emums.VariableType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VariableRealBuilder {

    public static Optional<VariableReal> build(VariableFeature vF, ItemStack item, @NotNull DynamicMeta dMeta) {
        if (vF.getType().getValue().get().equals(VariableType.STRING)) {
            return Optional.ofNullable(new VariableRealString(vF, item, dMeta));
        } else if (vF.getType().getValue().get().equals(VariableType.LIST)) {
            return Optional.ofNullable(new VariableRealList(vF, item, dMeta));
        } else if (vF.getType().getValue().get().equals(VariableType.NUMBER)) {
            return Optional.ofNullable(new VariableRealDouble(vF, item, dMeta));
        }

        return Optional.ofNullable(null);
    }

    public static Optional<VariableReal> build(VariableFeature vF, PersistentDataContainer dataContainer) {
        if (vF.getType().getValue().get().equals(VariableType.STRING)) {
            return Optional.ofNullable(new VariableRealString(vF, dataContainer));
        } else if (vF.getType().getValue().get().equals(VariableType.LIST)) {
            return Optional.ofNullable(new VariableRealList(vF, dataContainer));
        } else if (vF.getType().getValue().get().equals(VariableType.NUMBER)) {
            return Optional.ofNullable(new VariableRealDouble(vF, dataContainer));
        }

        return Optional.ofNullable(null);
    }

    public static Optional<VariableReal> build(VariableFeature vF, ConfigurationSection configurationSection) {
        if (vF.getType().getValue().get().equals(VariableType.STRING)) {
            return Optional.ofNullable(new VariableRealString(vF, configurationSection));
        } else if (vF.getType().getValue().get().equals(VariableType.LIST)) {
            return Optional.ofNullable(new VariableRealList(vF, configurationSection));
        } else if (vF.getType().getValue().get().equals(VariableType.NUMBER)) {
            return Optional.ofNullable(new VariableRealDouble(vF, configurationSection));
        }

        return Optional.ofNullable(null);
    }
}

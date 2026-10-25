package com.ssomar.score.features.custom.conditions.custom;


import com.ssomar.score.features.custom.conditions.ConditionRequest;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
@Setter
public class CustomConditionRequest extends ConditionRequest {

    private Player launcher;
    private ItemStack itemStack;

    public CustomConditionRequest(@Nullable Player launcher, @Nullable ItemStack itemStack, @Nullable StringPlaceholder sp, @Nullable Event event) {
        super(event, sp != null ? sp : new StringPlaceholder());
        this.launcher = launcher;
        this.itemStack = itemStack;
    }
}

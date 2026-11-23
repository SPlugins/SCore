package com.ssomar.score.features.custom.conditions.item;


import com.ssomar.score.features.custom.conditions.ConditionRequest;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
@Setter
public class ItemConditionRequest extends ConditionRequest {

    private ItemStack itemStack;
    private Optional<Player> playerOpt;

    public ItemConditionRequest(@NotNull ItemStack itemStack, @NotNull Optional<Player> playerOpt, @Nullable StringPlaceholder sp, @Nullable Event event) {
        super(event, sp != null ? sp : new StringPlaceholder());
        this.itemStack = itemStack;
        this.playerOpt = playerOpt;
    }
}

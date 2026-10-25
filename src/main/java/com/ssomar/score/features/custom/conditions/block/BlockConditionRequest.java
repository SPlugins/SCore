package com.ssomar.score.features.custom.conditions.block;


import com.ssomar.score.features.custom.conditions.ConditionRequest;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
@Setter
public class BlockConditionRequest extends ConditionRequest {

    private Block block;
    private Optional<Player> playerOpt;

    public BlockConditionRequest(@NotNull Block block, @NotNull Optional<Player> playerOpt, @Nullable StringPlaceholder sp, @Nullable Event event) {
        super(event, sp != null ? sp : new StringPlaceholder());
        this.block = block;
        this.playerOpt = playerOpt;
    }
}

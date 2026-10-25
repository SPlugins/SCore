package com.ssomar.score.features.custom.conditions.world;


import com.ssomar.score.features.custom.conditions.ConditionRequest;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
@Setter
public class WorldConditionRequest extends ConditionRequest {

    private World world;
    private Optional<Player> playerOpt;

    public WorldConditionRequest(@NotNull World world, @NotNull Optional<Player> playerOpt, @Nullable StringPlaceholder sp, @Nullable Event event) {
        super(event, sp != null ? sp : new StringPlaceholder());
        this.world = world;
        this.playerOpt = playerOpt;
    }
}

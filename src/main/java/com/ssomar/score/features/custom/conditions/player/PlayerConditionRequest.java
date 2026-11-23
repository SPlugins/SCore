package com.ssomar.score.features.custom.conditions.player;


import com.ssomar.score.features.custom.conditions.ConditionRequest;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
@Setter
public class PlayerConditionRequest extends ConditionRequest {

    private Player player;

    private Optional<Player> launcher;

    public PlayerConditionRequest(@NotNull Player player, Optional<Player> launcher, @Nullable StringPlaceholder sp, @Nullable Event event) {
        super(event, sp != null ? sp : new StringPlaceholder());
        this.player = player;
        this.launcher = launcher;
    }
}

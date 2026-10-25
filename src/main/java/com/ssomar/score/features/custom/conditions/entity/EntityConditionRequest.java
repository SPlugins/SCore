package com.ssomar.score.features.custom.conditions.entity;


import com.ssomar.score.features.custom.conditions.ConditionRequest;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
@Setter
public class EntityConditionRequest extends ConditionRequest {

    private Entity entity;
    private Optional<Player> playerOpt;

    public EntityConditionRequest(@NotNull Entity entity, @NotNull Optional<Player> playerOpt, @Nullable StringPlaceholder sp, @Nullable Event event) {
        super(event, sp != null ? sp : new StringPlaceholder());
        this.entity = entity;
        this.playerOpt = playerOpt;
    }
}

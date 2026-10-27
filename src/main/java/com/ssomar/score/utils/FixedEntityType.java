package com.ssomar.score.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.EntityType;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FixedEntityType {

    public static EntityType getEntity(List<String> entities) {
        for (String mat : entities) {
            try {
                return EntityType.valueOf(mat.toUpperCase());
            } catch (Exception e) {
                continue;
            }
        }
        return EntityType.UNKNOWN;
    }
}

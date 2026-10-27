package com.ssomar.score.utils.backward_compatibility;

import com.ssomar.score.SCore;
import com.ssomar.score.utils.MapUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BiomeUtils {

    private static Map<Object, String> biomes;

    public static Map<Object, String> getBiomes() {
        if (biomes != null) {
            return biomes;
        }

        Map<Object, String> list = new HashMap<>();
        if (SCore.is1v21v2Plus()) {
            // If someone created a custom biome with weird namespace , it may cause issues, so we handle it
            // Caused by: java.lang.IllegalArgumentException: Invalid namespace. Must be [a-z0-9._-]:
            try {
                for (Keyed l : Registry.BIOME) {
                    NamespacedKey key = l.getKey();
                    if (key.getNamespace().equals("minecraft")) {
                        list.put(l, l.getKey().getKey().toUpperCase());
                    } else list.put(l, l.getKey().toString());
                }
            } catch (IllegalArgumentException e) {}
        } else {
            for (Object o : Biome.class.getEnumConstants()) {
                // Use reflection to get .name()
                try {
                    String name = (String) o.getClass().getMethod("name").invoke(o);
                    list.put(o, name);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // Sort the list
        biomes = MapUtil.sortByValue(list);
        return biomes;
    }

    public static Biome getBiome(String string) {
        string = string.replace("minecraft:", "");
        for(Map.Entry<Object, String> entry : getBiomes().entrySet()) {
            if(entry.getValue().equalsIgnoreCase(string)) {
                return (Biome) entry.getKey();
            }
        }
        return null;
    }
}

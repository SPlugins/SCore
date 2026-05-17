package com.ssomar.score.utils;

import com.ssomar.score.SCore;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FixedMaterial {

    /**
     * Attempts to resolve a Bukkit {@link Material} from a list of candidate name strings.
     *
     * <p>Iterates through each candidate in order, returning the first string that
     * successfully maps to a valid {@link Material} enum constant (case-insensitive).
     * If no candidates match, falls back to {@link Material#BARRIER} as a sentinel value.
     *
     * @param materials an ordered list of material name candidates to attempt resolution from
     *                  (e.g. {@code ["minecraft:stone", "STONE", "stone"]})
     * @return the first matched {@link Material}, or {@link Material#BARRIER} if none matched
     */
    public static Material getMaterial(List<String> materials) {
        for (String mat : materials) {
            try {
                return Material.valueOf(mat.toUpperCase());
            } catch (Exception e) {
                continue;
            }
        }
        return Material.BARRIER;
    }

    public static void addMaterial(List<String> materials, List<Material> list) {
        for (String mat : materials) {
            try {
                list.add(Material.valueOf(mat.toUpperCase()));
            } catch (Exception e) {
                continue;
            }
        }
    }

    public static List<Material> getMaterials(List<String> materials) {
        List<Material> list = new java.util.ArrayList<>();
        addMaterial(materials, list);
        return list;
    }

    public static List<Material> getMaterialsContains(String contains) {
        List<Material> list = new java.util.ArrayList<>();
        for (Material material1 : Material.values()) {
            if (material1.name().contains(contains)) {
                list.add(material1);
            }
        }
        return list;
    }

    public static List<Material> getMaterialsContains(String contains, String notContains) {
        List<Material> list = new java.util.ArrayList<>();
        for (Material material1 : Material.values()) {
            if (material1.name().contains(contains) && !material1.name().contains(notContains)) {
                list.add(material1);
            }
        }
        return list;
    }

    public static Material getHead() {
        return FixedMaterial.getMaterial(Arrays.asList("PLAYER_HEAD", "SKULL_ITEM"));
    }

    public static Material getBrewingStand() {
        if (!SCore.is1v8()) return FixedMaterial.getMaterial(Collections.singletonList("BREWING_STAND"));
        else return FixedMaterial.getMaterial(Collections.singletonList("BREWING_STAND_ITEM"));
    }
}

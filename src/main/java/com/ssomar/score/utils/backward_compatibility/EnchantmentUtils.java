package com.ssomar.score.utils.backward_compatibility;

import com.ssomar.score.SCore;
import com.ssomar.score.utils.MapUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnchantmentUtils {

    private final static boolean DEBUG = true;

    public static Map<Object, String> getEnchantments() {
        Map<Object, String> list = new HashMap<>();
        if (SCore.is1v21v2Plus()) {
            for (Keyed l : Registry.ENCHANTMENT) {
                NamespacedKey key = l.getKey();
                if (key.getNamespace().equals("minecraft")) {
                    list.put(l, l.getKey().getKey().toUpperCase());
                } else list.put(l, l.getKey().toString());
            }
        } else {
            for (Object o : Attribute.class.getEnumConstants()) {
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
        return MapUtil.sortByValue(list);
    }

    public static Enchantment getEnchantment(String string) {
        string = string.replace("minecraft:", "");
        for (Map.Entry<Object, String> entry : getEnchantments().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(string)) {
                return (Enchantment) entry.getKey();
            }
        }
        switch (string.toUpperCase()) {
            case "DURABILITY":
                return Enchantment.UNBREAKING;
        }
        return null;
    }
}

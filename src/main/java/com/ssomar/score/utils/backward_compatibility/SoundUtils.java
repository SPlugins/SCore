package com.ssomar.score.utils.backward_compatibility;

import com.ssomar.score.SCore;
import com.ssomar.score.utils.MapUtil;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class SoundUtils {

    public static Map<Object, String> getSounds() {
        Map<Object, String> list = new HashMap<>();
        if (SCore.is1v21v2Plus()) {
            for (Keyed l : Registry.SOUNDS) {
                NamespacedKey key = l.getKey();
                if(key.getNamespace().equals("minecraft")) {
                    list.put(l, l.getKey().getKey().toUpperCase());
                }
                else list.put(l, l.getKey().toString());
            }
        } else {
            for (Object o : Sound.class.getEnumConstants()) {
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

    /**
     * Resolve the org.bukkit.Sound matching an Adventure key (as returned by the
     * item data-component API, e.g. PiercingWeapon#sound()). Returns null if the
     * key is null or no matching sound exists.
     */
    public static Sound getSoundFromKey(net.kyori.adventure.key.Key key) {
        if (key == null) return null;
        if (SCore.is1v21v2Plus()) {
            return Registry.SOUNDS.get(new NamespacedKey(key.namespace(), key.value()));
        }
        return getSound(key.value());
    }

    public static Sound getSound(String string) {
        string = string.replace("minecraft:", "");
        for(Map.Entry<Object, String> entry : getSounds().entrySet()) {
            if(entry.getValue().equalsIgnoreCase(string)) {
                return (Sound) entry.getKey();
            }
        }
        switch (string.toUpperCase()){
            case "ITEM_PICKUP":
                return Sound.ENTITY_ITEM_PICKUP;
        }
        return null;
    }
}

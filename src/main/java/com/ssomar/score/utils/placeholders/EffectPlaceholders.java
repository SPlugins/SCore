package com.ssomar.score.utils.placeholders;

import com.ssomar.score.SCore;
import lombok.Getter;
import org.bukkit.potion.PotionEffect;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EffectPlaceholders extends PlaceholdersInterface implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Getter
    private Map<String, String> placeholders;

    /* placeholders of the player */
    /* transient: not serializable (delayed commands saved at the shutdown), its placeholders are already in the map */
    @Getter
    private transient PotionEffect effect;

    public EffectPlaceholders() {
        this.placeholders = new HashMap<>();
    }

    public void setEffectPlcHldr(PotionEffect effect) {
        this.effect = effect;
        this.reloadEffectPlcHldr();
    }

    public void setPlayerPlcHldr(PotionEffect effect) {
        this.effect = effect;
        this.reloadEffectPlcHldr();
    }

    public void reloadEffectPlcHldr() {
        if (this.effect != null) {
            /* Pre save placeholders without calcul */

            // get key is not available in 1.17
            if(SCore.is1v18Plus()) {
                placeholders.put("%effect_received%", effect.getType().getKey().getKey());
                placeholders.put("%effect_received_lower%", effect.getType().getKey().getKey().toLowerCase());
            }
            placeholders.put("%effect_received_level%", effect.getAmplifier()+"");
            placeholders.put("%effect_received_duration%", effect.getDuration()+"");
        }
    }

    public String replacePlaceholder(String s) {
        String toReplace = s;
        return toReplace;
    }
}

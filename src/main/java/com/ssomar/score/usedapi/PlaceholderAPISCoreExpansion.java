package com.ssomar.score.usedapi;


import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.mixed_player_entity.commands.DamageBoost;
import com.ssomar.score.commands.runnable.mixed_player_entity.commands.DamageResistance;
import com.ssomar.score.features.custom.cooldowns.CooldownsManager;
import com.ssomar.score.features.custom.drop.glowdrop.GlowDropManager;
import com.ssomar.score.variables.manager.VariablesManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public class PlaceholderAPISCoreExpansion extends PlaceholderExpansion {

    private final SCore plugin;

    public PlaceholderAPISCoreExpansion(SCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "Ssomar";
    }

    @Override
    public String getIdentifier() {
        return "SCore";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        /* nested placeholders in brackets, e.g. %score_variables_list_{score_variables_index_int}%: PlaceholderAPI hands them over unparsed,
         * so the index was never read and the whole list came back (Discord 1423490871252484151) */
        if (params.indexOf('{') >= 0 && params.indexOf('}') > params.indexOf('{'))
            params = me.clip.placeholderapi.PlaceholderAPI.setBracketPlaceholders(player, params);
        //System.out.println("params: "+params);

        Optional<String> varPlaceHolder = VariablesManager.getInstance().onRequestPlaceholder(player, params);
        if (varPlaceHolder.isPresent()) return varPlaceHolder.get();

        Optional<String> dmgBoosterPlaceHolder = DamageBoost.getInstance().onRequestPlaceholder(player, params);
        if (dmgBoosterPlaceHolder.isPresent()) return dmgBoosterPlaceHolder.get();

        Optional<String> dmgResistancePlaceHolder = DamageResistance.getInstance().onRequestPlaceholder(player, params);
        if (dmgResistancePlaceHolder.isPresent()) return dmgResistancePlaceHolder.get();

        Optional<String> glowColorPlaceHolder = GlowDropManager.getInstance().onRequestPlaceholder(player, params);
        if (glowColorPlaceHolder.isPresent()) return glowColorPlaceHolder.get();

        Optional<String> cooldownPlaceHolder = CooldownsManager.getInstance().onRequestPlaceholder(player, params);
        if (cooldownPlaceHolder.isPresent()) return cooldownPlaceHolder.get();

        return null;
    }
}

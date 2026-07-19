package com.ssomar.score.events;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandsHandler;
import com.ssomar.score.commands.runnable.entity.commands.PlayerRideOnEntityManager;
import com.ssomar.score.commands.runnable.player.commands.openchest.OpenChestListener;
import com.ssomar.score.commands.runnable.player.commands.sudoop.SecurityOPCommands;
import com.ssomar.score.commands.runnable.player.events.*;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.editor.NewEditorInteractionsListener;
import com.ssomar.score.features.custom.cooldowns.CooldownsHandler;
import com.ssomar.score.pack.listener.ConfigPhasePackListener;
import com.ssomar.score.pack.listener.JoinQuitListener;
import com.ssomar.score.usedapi.AuraSkillsLootDropListener;
import com.ssomar.score.usedapi.Dependency;
import com.ssomar.score.usedapi.JobsAPI;
import org.bukkit.plugin.Plugin;

public class EventsHandler {

    private static EventsHandler instance;

    private Plugin main;

    public static EventsHandler getInstance() {
        if (instance == null) instance = new EventsHandler();
        return instance;
    }

    public void setup(Plugin main) {
        this.main = main;
        setupEvents();
    }

    public void setupEvents() {
        main.getServer().getPluginManager().registerEvents(new SecurityOPCommands(), main);

        main.getServer().getPluginManager().registerEvents(new NoFallDamageListener(), main);

        main.getServer().getPluginManager().registerEvents(DamageWithoutKnockbackListener.getInstance(), main);

        main.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), main);

        main.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), main);

        /* No EntityToggleGlideEvent & EntityPickupItemEvent in 1.11 -*/
        if (!SCore.is1v11Less()) {
            main.getServer().getPluginManager().registerEvents(new StunEvent(), main);

            main.getServer().getPluginManager().registerEvents(new PlayerPickupListener(), main);
        }

        main.getServer().getPluginManager().registerEvents(CommandsHandler.getInstance(), main);

        main.getServer().getPluginManager().registerEvents(new KeepCustomFlyEvent(), main);

        main.getServer().getPluginManager().registerEvents(new XPBoostEvent(), main);

        if(SCore.hasMcMMO) main.getServer().getPluginManager().registerEvents(new McMMOXPBoostEvent(), main);

        main.getServer().getPluginManager().registerEvents(new DisableFlyActivationEvent(), main);

        if(!SCore.is1v12Less()) main.getServer().getPluginManager().registerEvents(new DisableGlideActivationEvent(), main);

        main.getServer().getPluginManager().registerEvents(PlaceholderLastDamageDealtEvent.getInstance(), main);

        main.getServer().getPluginManager().registerEvents(new OpenChestEvent(), main);

        main.getServer().getPluginManager().registerEvents(new DamageResistanceEvent(), main);

        main.getServer().getPluginManager().registerEvents(new DamageBoostEvent(), main);

        main.getServer().getPluginManager().registerEvents(new CooldownsHandler(), main);

        main.getServer().getPluginManager().registerEvents(new FixSpawnerPlaceListener(), main);

        main.getServer().getPluginManager().registerEvents(new RemoveProjectileHitBlockEvent(), main);

        main.getServer().getPluginManager().registerEvents(new RemoveCancelDamageEventMetadataTagEvent(), main);

        /* Recode event */
        main.getServer().getPluginManager().registerEvents(new NewEditorInteractionsListener(), main);

        if(SCore.hasJetsMinions) main.getServer().getPluginManager().registerEvents(new FixJetsMinionsBlockBreakEvent(), main);

        if(!SCore.is1v13Less() && GeneralConfig.getInstance().isEnableDetectionEntitiesFromSpawner()) main.getServer().getPluginManager().registerEvents(new EntitiesFromSpawnerListener(), main);

        main.getServer().getPluginManager().registerEvents(CheckIfDamageIsPosssibleListener.getInstance(), main);

        //main.getServer().getPluginManager().registerEvents(new TESTEVENT(), main);

        if(SCore.is1v21v4Plus()) main.getServer().getPluginManager().registerEvents(PlayerRideOnEntityManager.getInstance(), main);

        if(Dependency.JOBS.isInstalled())  main.getServer().getPluginManager().registerEvents(new JobsAPI(), main);

        /* Smelt the extra drops of AuraSkills abilities (e.g. Mining Luck) when the block was smelted by an EI/EB command */
        if(Dependency.AURA_SKILLS.isInstalled()) main.getServer().getPluginManager().registerEvents(new AuraSkillsLootDropListener(), main);

        main.getServer().getPluginManager().registerEvents(new OpenChestListener(), main);

        main.getServer().getPluginManager().registerEvents(new JoinQuitListener(), main);

        // Config-phase pack dispatch — sends packs during PlayerLinksSendEvent (before world join)
        // Only sends if the URL is already cached (non-blocking). JoinQuitListener is NOT skipped.
        if (SCore.is1v20v5Plus()) {
            try {
                main.getServer().getPluginManager().registerEvents(new ConfigPhasePackListener(), main);
            } catch (Exception | Error e) {
                // PlayerLinksSendEvent not available
            }
        }
    }
}

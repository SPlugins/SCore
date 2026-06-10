package com.ssomar.score.commands.runnable.player;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandManager;
import com.ssomar.score.commands.runnable.SCommand;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommandsManager;
import com.ssomar.score.commands.runnable.player.commands.*;
import com.ssomar.score.commands.runnable.player.commands.absorption.Absorption;
import com.ssomar.score.commands.runnable.player.commands.openchest.OpenChest;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommandManager extends CommandManager<SCommand> {

    private static PlayerCommandManager instance;

    public PlayerCommandManager() {
        List<SCommand> commands = new ArrayList<>();
        commands.add(new AddItemlore());
        commands.add(new Removelore());
        commands.add(new SetItemlore());
        commands.add(new SetItemColor());
        commands.add(new SetItemName()); 
        if(SCore.is1v20v5Plus()) commands.add(new SetItemMaterial());
        if(SCore.is1v20v5Plus()) commands.add(new SetItemTooltipStyle());
        if(SCore.is1v20v5Plus()) commands.add(new SetItemPotionColor());
        if(SCore.is1v20v5Plus()) commands.add(new SetItemModel());
        if(SCore.is1v21v2Plus() && SCore.isPaperOrFork()) commands.add(new SetEquippableModel());
        commands.add(new SetItemCustomModelData());
        commands.add(new SendBlankMessage());
        commands.add(new SendMessage());
        commands.add(new SendCenteredMessage());
        commands.add(new DoNothing());
        /* SUDOOP MUST BE BEFORE SUDO */
        commands.add(new SudoOp());
        commands.add(new Sudo());
        commands.add(new FlyOn());
        commands.add(new FlyOff());
        commands.add(new SetBlockPos());
        commands.add(new SetBlock());
        commands.add(new SetTempBlockPos());
        commands.add(new ReplaceBlock());
        commands.add(new ProjectileCustomDash1());
        commands.add(new SpawnEntityOnCursor());
        commands.add(DisableFlyActivation.getInstance());
        commands.add(DisableGlideActivation.getInstance());
        /* DAMAGE_RESISTANCE MUST BE BEFORE DAMAGE */
        commands.add(new Launch());
        if (!SCore.is1v12Less()) {
            commands.add(new LocatedLaunch());
            commands.add(new CropsGrowthBoost());
            /* No damageable class before 1.12 */
            commands.add(new ModifyDurability());
        }
        if(SCore.is1v16Plus()){
            commands.add(new Absorption());
        }
        commands.add(new MixHotbar());
        commands.add(new SetExecutableBlock());
        commands.add(new SetItemCooldown());
        commands.add(new RegainFood());
        commands.add(new RegainMagic());
        commands.add(new RegainSaturation());
        commands.add(new Head());
        commands.add(new Swaphand());
        commands.add(new Chestplate());
        commands.add(new Boots());
        commands.add(new Leggings());
        commands.add(new CancelPickup());
        commands.add(new CloseInventory());
        commands.add(new GiveMoney());
        commands.add(new GravityEnable());
        commands.add(new GravityDisable());
        commands.add(new SetPlayerWeather());
        commands.add(new SetPlayerTime());
        commands.add(new OpenWorkbench());
        commands.add(new OpenAnvil());
        commands.add(new MinecartBoost());
        commands.add(new Steal());
        commands.add(new PickupMagnet());
        commands.add(new FormatEnchantments());
        commands.add(new AddItemEnchantment());
        commands.add(new SortInventory());
        commands.add(new Oxygen());
        commands.add(new RemoveEnchantment());
        commands.add(new Chat());
        // ForceDrop is handled by MixedCommandsManager (supports both slot and ei_id modes)
        commands.add(OpenChest.getInstance());
        commands.add(new EICooldown());
        commands.add(new EBCooldown());
        commands.add(new EECooldown());
        commands.add(new SetArmorTrim());
        /* No EntityToggleGlideEvent in 1.11 -*/
        if (!SCore.is1v11Less()) {
            commands.add(new AddItemAttribute());
            commands.add(new SetItemAttribute());
            commands.add(new Bossbar());
            commands.add(new Actionbar());
            commands.add(new OpenEnderchest());
        }
        commands.add(XpBoost.getInstance());
        if(SCore.hasMcMMO) commands.add(McMMOXpBoost.getInstance());
        commands.add(JobsMoneyBoost.getInstance());
        commands.add(JobsXpBoost.getInstance());
        commands.add(While.getInstance());
        commands.add(new If());

        commands.addAll(MixedCommandsManager.getInstance().getCommands());
        if (!SCore.is1v8())
            commands.add(new FireworkBoost());
        commands.add(new MoveItem());

        /* Sort by priority */
        commands.sort((c1, c2) -> {
            if (c1.getPriority() < c2.getPriority()) return 1;
            else if (c1.getPriority() > c2.getPriority()) return -1;
            else return 0;
        });

        setCommands(commands);
    }

    public static PlayerCommandManager getInstance() {
        if (instance == null) instance = new PlayerCommandManager();
        return instance;
    }
}

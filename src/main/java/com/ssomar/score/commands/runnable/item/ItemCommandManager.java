package com.ssomar.score.commands.runnable.item;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandManager;
import com.ssomar.score.commands.runnable.SCommand;
import com.ssomar.score.commands.runnable.item.commands.*;

import java.util.ArrayList;
import java.util.List;

public class ItemCommandManager extends CommandManager<SCommand> {

    private static ItemCommandManager instance;

    public ItemCommandManager() {
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

        if (!SCore.is1v12Less()) {
            /* No damageable class before 1.12 */
            commands.add(new ModifyItemDurability());
        }
        commands.add(new FormatEnchantments());
        commands.add(new AddItemEnchantment());
        commands.add(new RemoveEnchantment());
        commands.add(new SetArmorTrim());
        /* No EntityToggleGlideEvent in 1.11 -*/
        if (!SCore.is1v11Less()) {
            commands.add(new AddItemAttribute());
            commands.add(new SetItemAttribute());
        }
        commands.add(new AddItemNBT());

        /* Sort by priority */
        commands.sort((c1, c2) -> {
            if (c1.getPriority() < c2.getPriority()) return 1;
            else if (c1.getPriority() > c2.getPriority()) return -1;
            else return 0;
        });

        setCommands(commands);
    }

    public static ItemCommandManager getInstance() {
        if (instance == null) instance = new ItemCommandManager();
        return instance;
    }
}

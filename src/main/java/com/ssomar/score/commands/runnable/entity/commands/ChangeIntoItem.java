package com.ssomar.score.commands.runnable.entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.entity.EntityCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Replaces the item of a dropped item entity by a vanilla item or an ExecutableItem.
 * Made for PLAYER_FISH_FISH, where the target entity is the caught item: the loot is changed
 * before it is reeled in, so the player keeps the vanilla animation (no /ei give needed).
 */
public class ChangeIntoItem extends EntityCommand {

    public ChangeIntoItem() {
        CommandSetting item = new CommandSetting("item", 0, String.class, "null");
        CommandSetting amount = new CommandSetting("amount", 1, Integer.class, 1);
        List<CommandSetting> settings = getSettings();
        settings.add(item);
        settings.add(amount);
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, Entity entity, SCommandToExec sCommandToExec) {
        if (!(entity instanceof Item) || entity.isDead()) return;

        String item = (String) sCommandToExec.getSettingValue("item");
        int amount = (int) sCommandToExec.getSettingValue("amount");
        if (amount <= 0) return;

        Optional<ItemStack> result = buildItem(item, amount, p);
        if (!result.isPresent()) {
            SCore.plugin.getLogger().info(ChatColor.RED + "Invalid item (" + item + ") was provided for a CHANGE_INTO_ITEM command. It must be a material or the id of an ExecutableItem.");
            return;
        }
        ((Item) entity).setItemStack(result.get());
    }

    /** An ExecutableItem id wins over a material, "EI:" can be used to be explicit. */
    public static Optional<ItemStack> buildItem(String item, int amount, Player owner) {
        if (item == null) return Optional.empty();
        boolean forcedEI = item.regionMatches(true, 0, "EI:", 0, 3);
        String id = forcedEI ? item.substring(3) : item;

        if (SCore.hasExecutableItems && ExecutableItemsAPI.getExecutableItemsManager().isValidID(id)) {
            Optional<ExecutableItemInterface> eiOpt = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(id);
            if (eiOpt.isPresent())
                return Optional.ofNullable(eiOpt.get().buildItem(amount, Optional.ofNullable(owner), new HashMap<>()));
        }
        if (forcedEI) return Optional.empty();

        Material material = Material.matchMaterial(id);
        if (material == null || material == Material.AIR) return Optional.empty();
        return Optional.of(new ItemStack(material, amount));
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("CHANGE_INTO_ITEM");
        return names;
    }

    @Override
    public String getTemplate() {
        return "CHANGE_INTO_ITEM item:{material or ExecutableItem id} amount:1";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }
}

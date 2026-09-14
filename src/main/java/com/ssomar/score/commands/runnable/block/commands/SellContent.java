package com.ssomar.score.commands.runnable.block.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;
import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.usedapi.Dependency;
import com.ssomar.score.usedapi.ShopGUIPlusTool;
import com.ssomar.score.usedapi.VaultAPI;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.

import java.util.ArrayList;
import java.util.List;

/* STRIKELIGHTNING */
public class SellContent extends BlockCommand {

    public SellContent() {
        CommandSetting priceBoost = new CommandSetting("priceBoost", 0, Double.class, 1.0);
        CommandSetting deleteUnsellable = new CommandSetting("deleteUnsellable", 1, Boolean.class, false);
        List<CommandSetting> settings = getSettings();
        settings.add(priceBoost);
        settings.add(deleteUnsellable);
        setNewSettingsMode(true);
    }

    private static final boolean DEBUG = true;

    @Override
    public void run(@Nullable Player p, @NotNull Block block, SCommandToExec sCommandToExec) {

        double priceBoost = (double) sCommandToExec.getSettingValue("priceBoost");
        boolean deleteUnsellable = (boolean) sCommandToExec.getSettingValue("deleteUnsellable");

        if (block.getState() instanceof Container && p != null) {
            Container container = (Container) block.getState();
            Inventory inv = container.getInventory();
            double amount = 0;
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null) {
                    if (SCore.hasShopGUIPlus) {
                        double check = ShopGUIPlusTool.sellItem(p, item);
                        SsomarDev.testMsg("item : " + item.getType() + " qty: " + item.getAmount() + "check : " + check, DEBUG);
                        if (check > 0) {
                            amount += check;
                            ShopGUIPlusTool.registerTransaction(item, p, check, priceBoost);
                            item.setAmount(0);
                        }
                    }
                    else if(SCore.hasCMI){
                        int quantity = item.getAmount();
                        item.setAmount(1);
                        WorthItem worth = CMI.getInstance().getWorthManager().getWorth(item);
                        if (worth == null){
                            // Worthless item so we can return null or 0D, whatever is needed in your case
                           continue;
                        }
                        // Sell price defines actual worth of the file
                        Double sellPrice = worth.getSellPrice();
                        amount += sellPrice*quantity;
                        item.setAmount(0);
                    }
                    else if (SCore.hasExcellentShop) {
                        int quantity = item.getAmount();
                        item.setAmount(1);

                    }
                    if(deleteUnsellable) item.setAmount(0);
                }
            }
            if (amount > 0) {
                amount = amount * priceBoost;
                SsomarDev.testMsg("SellContent activated > amount > " + amount, DEBUG);
                VaultAPI v = new VaultAPI();
                v.verifEconomy(p);
                v.addMoney(p, amount);
            }
        }
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("SELL_CONTENT");
        return names;
    }

    @Override
    public String getTemplate() {
        return "SELL_CONTENT priceBoost:1.0 deleteUnsellable:false";
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

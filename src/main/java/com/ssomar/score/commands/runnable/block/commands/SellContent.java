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
import su.nightexpress.excellentshop.ShopAPI;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.transaction.ERawTransaction;
import su.nightexpress.excellentshop.util.ShopUtils;
import su.nightexpress.excellentshop.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.virtualshop.shop.VirtualShop;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/* STRIKELIGHTNING */
public class SellContent extends BlockCommand {

    public SellContent() {
        CommandSetting priceBoost = new CommandSetting("priceBoost", 0, Double.class, 1.0);
        CommandSetting deleteUnsellable = new CommandSetting("deleteUnsellable", 1, Boolean.class, false);
        /**
         * Following arguments
         * - blank : tries to get first valid choice in the if statement flow. This option has been made because some servers may have both CMI and ExcellentShops
         * - ShopGUIPlus : uses ShopGUIPlus's price list
         * - CMI : uses CMI's price list
         * - ExcellentShop : uses ExcellentShop's price list
         */
        CommandSetting reference = new CommandSetting("reference", 2, String.class, "");
        List<CommandSetting> settings = getSettings();
        settings.add(priceBoost);
        settings.add(deleteUnsellable);
        settings.add(reference);
        setNewSettingsMode(true);
    }

    private static final boolean DEBUG = true;

    @Override
    public void run(@Nullable Player p, @NotNull Block block, SCommandToExec sCommandToExec) {

        double priceBoost = (double) sCommandToExec.getSettingValue("priceBoost");
        boolean deleteUnsellable = (boolean) sCommandToExec.getSettingValue("deleteUnsellable");
        String reference = (String) sCommandToExec.getSettingValue("reference");

        if (block.getState() instanceof Container && p != null) {
            Container container = (Container) block.getState();
            Inventory inv = container.getInventory();
            double amount = 0;
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null) {
                    if (SCore.hasShopGUIPlus && (reference.equals("ShopGUIPlus") || reference.isEmpty())) {
                        double check = ShopGUIPlusTool.sellItem(p, item);
                        SsomarDev.testMsg("item : " + item.getType() + " qty: " + item.getAmount() + "check : " + check, DEBUG);
                        if (check > 0) {
                            amount += check;
                            ShopGUIPlusTool.registerTransaction(item, p, check, priceBoost);
                            item.setAmount(0);
                        }
                    }
                    else if(SCore.hasCMI && (reference.equals("CMI") || reference.isEmpty())){
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
                    else if (SCore.hasExcellentShop && (reference.equals("ExcellentShop") || reference.isEmpty())) {

                        // get iterated item's quantity
                        int quantity = item.getAmount();
                        // get static pointer for virtual shop module
                        VirtualShopModule module = ShopAPI.getVirtualShop();
                        // get the shops the player has access to
                        Set<VirtualShop> shops = module.getShops(p);
                        // get price of said item
                        Product best = ShopUtils.findBestProduct(item, TradeType.SELL, shops);
                        if (best == null) {
                            continue;
                        }
                        // price is the best available price among shops this player can access
                        double sellPrice = best.getFinalPrice(TradeType.SELL, p);
                        amount += sellPrice*quantity;
                        item.setAmount(0);


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

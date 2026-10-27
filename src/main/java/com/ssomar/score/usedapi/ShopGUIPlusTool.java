package com.ssomar.score.usedapi;

import com.ssomar.score.SCore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.modifier.PriceModifier;
import net.brcdev.shopgui.modifier.PriceModifierActionType;
import net.brcdev.shopgui.shop.ShopManager;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import net.brcdev.shopgui.shop.item.ShopItem;
import net.brcdev.shopgui.shop.item.ShopItemType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShopGUIPlusTool {

    private static final boolean DEBUG = true;

    public static double sellItem(Player player, ItemStack item) {

        double amount = ShopGuiPlusApi.getItemStackPriceSell(item);

        if(amount == -1) {
            return -1;
        }

        try {
            ShopItem shopItem = ShopGuiPlusApi.getItemStackShopItem(item);

            PriceModifier priceModifier = ShopGuiPlusApi.getPriceModifier(player, shopItem, PriceModifierActionType.SELL);

            amount = amount * priceModifier.getModifier();
        }
        catch (Exception ignored) {}


        return amount;
    }

    public static void registerTransaction(ItemStack item, Player p, double price, double priceBoost){
        if (SCore.hasShopGUIPlus){
            ShopTransactionResult result = new ShopTransactionResult(ShopManager.ShopAction.SELL, ShopTransactionResult.ShopTransactionResultType.SUCCESS, new ShopItem(null, "", ShopItemType.ITEM, item), p, item.getAmount(), price * priceBoost);
            ShopPostTransactionEvent event = new ShopPostTransactionEvent(result);
            Bukkit.getPluginManager().callEvent(event);
        }
    }
}

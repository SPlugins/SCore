package com.ssomar.score.utils;

import com.ssomar.score.SCore;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class GetItem {

    public static List<String> getCustomPluginsWords() {
        List<String> customPluginsWords = new java.util.ArrayList<>();
        customPluginsWords.add("EXECUTABLEITEM");
        customPluginsWords.add("EXECUTABLEITEMS");
        customPluginsWords.add("EI");
        customPluginsWords.add("IA");
        customPluginsWords.add("ITEMSADDER");
        return customPluginsWords;
    }

    public static boolean containsCustomPluginWord(String word) {
        for(String customPluginWord : getCustomPluginsWords()) {
            if(word.contains(customPluginWord)) return true;
        }
        return false;
    }

    public static Optional<ItemStack> getItem(@NotNull String objectStr, int amount) {
        if(objectStr.contains("EI:") || objectStr.contains("EXECUTABLEITEM:") || objectStr.contains("EXECUTABLEITEMS:")){
            if(SCore.hasExecutableItems){
                objectStr = objectStr.replace("EI:", "");
                objectStr = objectStr.replace("EXECUTABLEITEM:", "");
                objectStr = objectStr.replace("EXECUTABLEITEMS:", "");

                Optional<ExecutableItemInterface> executableItemInterfaceOptional = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(objectStr);
                if(executableItemInterfaceOptional.isPresent()){
                    return Optional.of(executableItemInterfaceOptional.get().buildItem(amount, Optional.empty()));
                }
            }
        }
        else if(objectStr.contains("IA:") || objectStr.contains("ITEMSADDER:")){
            if(SCore.hasItemsAdder){
                objectStr = objectStr.replace("IA:", "");
                objectStr = objectStr.replace("ITEMSADDER:", "");
                //TODO return item from ItemsAdder
            }
        }
        else {
            try{
                return Optional.of(new ItemStack(org.bukkit.Material.valueOf(objectStr.toUpperCase()), amount));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}

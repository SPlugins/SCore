package com.ssomar.score.features.types;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.features.*;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/*** IT MUST BE PLACED IN A RELOADED EDITOR TO WORK CORRECTLY ***/

@Getter
@Setter
public class ItemStackFeature extends FeatureAbstract<Optional<ItemStack>, ItemStackFeature> implements FeatureRequireOnlyClicksInEditor {

    private Optional<ItemStack> value;
    private Optional<ItemStack> defaultValue;
    private Optional<String> placeholder;
    private FeatureSettingsInterface featureDoubleClickOpener;
    private String featureDoubleClickOpenerEditorMessage = "";

    public ItemStackFeature(FeatureParentInterface parent, Optional<ItemStack> defaultValue, FeatureSettingsInterface featureSettings) {
        super(parent, featureSettings);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        reset();
    }

    public static ItemStackFeature buildNull() {
        return new ItemStackFeature(null, Optional.empty(), null);
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        String valueStr = config.getString(this.getName(), "NULL");
        if (valueStr.equals("NULL")){
            this.value = defaultValue;
            return errors;
        }
        if (valueStr.contains("%")) {
            placeholder = Optional.of(valueStr);
            value = Optional.empty();
        } else {
            placeholder = Optional.empty();
            int amount = 1;
            try {
                if(valueStr.contains(">>amount:")){
                    String[] split = valueStr.split(">>amount:");
                    valueStr = split[0];
                    if(split.length > 1) {
                        try {
                            amount = Integer.parseInt(split[1]);
                        } catch (Exception e) {
                            errors.add("&cERROR, Couldn't load the ItemStack value of " + this.getName() + " from config, value: " + valueStr + " (Invalid amount) &7&o" + getParent().getParentInfo());
                        }
                    }
                }
                ItemStack item = Bukkit.getServer().getItemFactory().createItemStack(valueStr);
                item.setAmount(amount);
                this.value = Optional.of(item);
            } catch (Exception e) {
               // e.printStackTrace();
                errors.add("&cERROR, Couldn't load the ItemStack value of " + this.getName() + " from config, value: " + valueStr + " &7&o" + getParent().getParentInfo());
                this.value = defaultValue;
            }
        }
        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        if (placeholder.isPresent()) {
            config.set(this.getName(), placeholder.get());
        } else if (getValue().isPresent()) {
            if(defaultValue.isPresent() && isSavingOnlyIfDiffDefault() && value.get().equals(defaultValue.get())){
                config.set(this.getName(), null);
                return;
            }
            else {
                ItemStack item = getValue().get();
                String valStr = itemStackToString(item);
                if(item.getAmount() != 1) valStr += ">>amount:"+item.getAmount();
                config.set(this.getName(), valStr);
            }
        }

        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));
    }

    public String itemStackToString(ItemStack item){
        String valStr = "";
        //System.out.println("item: " + item);
        if(SCore.is1v20v6Plus()){
            String itemTypeKey = item.getType().getKey().toString();
            String components = item.getItemMeta().getAsComponentString();
            String itemAsString = itemTypeKey + components;
            valStr = itemAsString;
        }
        else {
            if (item.hasItemMeta())
                valStr = "minecraft:" + item.getType().toString().toLowerCase() + item.getItemMeta().getAsString();
            else valStr = "minecraft:" + item.getType().toString().toLowerCase();
        }
        return valStr;
    }

    public Optional<ItemStack> getValue(@Nullable UUID playerUUID, @Nullable StringPlaceholder sp) {
        if (placeholder.isPresent()) {
            String placeholderStr = placeholder.get();
            if (sp != null) {
                placeholderStr = sp.replacePlaceholder(placeholderStr);
            }
            placeholderStr = StringPlaceholder.replacePlaceholderOfPAPI(placeholderStr, playerUUID);

            ItemStack item ;
            try {
                item = Bukkit.getServer().getItemFactory().createItemStack(placeholderStr);
                return Optional.of(item);
            } catch (Exception e) {}
        } else if (value.isPresent()) {
            return value;
        }
        return defaultValue;
    }

    @Nullable
    public ItemStack getItemsStack(@Nullable UUID playerUUID, @Nullable StringPlaceholder sp) {
        ItemStack item = getValue(playerUUID, sp).orElse(null);
        if (item != null) return item.clone();
        return null;
    }

    @Nullable
    public ItemStack getItemsStack() {
        ItemStack item = getValue().orElse(null);
        if (item != null) return item.clone();
        return null;
    }

    @Override
    public Optional<ItemStack> getValue() {
        if (value.isPresent()) {
            return Optional.of(value.get().clone());
        } else if (placeholder.isPresent()) {
            String placeholderStr = placeholder.get();
            //SsomarDev.testMsg("Placeholder: " + placeholderStr, true);
            placeholderStr = new StringPlaceholder().replacePlaceholderOfPAPI(placeholderStr);
            ItemStack item ;
            try {
                item = Bukkit.getServer().getItemFactory().createItemStack(placeholderStr);
                return Optional.of(item);
            } catch (Exception e) {}
        }
        return defaultValue;
    }


    @Override
    public ItemStackFeature initItemParentEditor(GUI gui, int slot) {

        List<String> customDescription = new ArrayList<>();
        customDescription.add("&7&o======== EC EDITOR ========");
        customDescription.addAll(Arrays.asList(getEditorDescription()));

        if (!isPremium() && this.isRequirePremium()) {
            customDescription.add(GUI.PREMIUM);
        } else {
            customDescription.add("&8>> &6LEFT CLICK: &eSET ITEM");
            customDescription.add("&8>> &6RIGHT CLICK: &eREMOVE ITEM");
            customDescription.add("&8>> &6SHIFT + LEFT CLICK: &eIncrement (+1)");
            customDescription.add("&8>> &6SHIFT + RIGHT CLICK: &eDecrement (-1)");
            customDescription.add("&8>> &6MIDDLE CLICK: &eGIVE ITEM");
            if(!getFeatureDoubleClickOpenerEditorMessage().isEmpty()) customDescription.add(featureDoubleClickOpenerEditorMessage);
        }
        customDescription.add(TM.g(Text.EDITOR_CURRENTLY_NAME));
        customDescription.add("&7&o======== EC EDITOR ========");

        customDescription.replaceAll(StringConverter::coloredString);

        Optional<ItemStack> value = getValue();
        ItemStack item;
        item = value.orElseGet(() -> new ItemStack(Material.BARRIER));

        ItemMeta meta = item.getItemMeta();
        // AIR carries no ItemMeta, and a feature can legitimately hold it (an ExecutableCrafting
        // recipe used to disable a vanilla one). Show the same placeholder as an unset value
        // instead of throwing and killing the whole editor. The stored value is untouched.
        if (meta == null) {
            item = new ItemStack(Material.BARRIER);
            meta = item.getItemMeta();
        }
        List<String> lore = meta.hasLore()? meta.getLore() : new ArrayList<>();

        assert lore != null;
        // if lore has EC EDITOR on one of the lines, return
        if (lore.stream().anyMatch(line -> line.contains("EC EDITOR"))) {
            gui.updateItem(slot, item, getEditorName());
            return this;
        }

        lore.addAll(0, customDescription);
        meta.setLore(lore);
        item.setItemMeta(meta);

        //gui.createItem(item, 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, "sdfdsf", "sdfdsf", "sdfdsf", TM.g(Text.EDITOR_CURRENTLY_NAME));
        gui.updateItem(slot, item, getEditorName());
        //UpdateItemInGUI.updateItemInGUI(gui, getEditorName(), null, null , item);

        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
        if (placeholder.isPresent()) gui.updateCurrently(getEditorName(), placeholder.get());
        else if (value.isPresent()){
            gui.updateCurrently(getEditorName(), "&6&oItem set");
        }
        else gui.updateCurrently(getEditorName(), "&c&oNo item set");
    }

    @Override
    public ItemStackFeature clone(FeatureParentInterface newParent) {
        ItemStackFeature clone = new ItemStackFeature(newParent, defaultValue, getFeatureSettings());
        clone.setFeatureDoubleClickOpener(featureDoubleClickOpener);
        clone.setFeatureDoubleClickOpenerEditorMessage(featureDoubleClickOpenerEditorMessage);
        clone.setValue(value);
        clone.setPlaceholder(getPlaceholder());
        return clone;
    }

    @Override
    public void reset() {
        this.value = defaultValue;
        this.placeholder = Optional.empty();
    }

    @Override
    public void clickParentEditor(Player editor, NewGUIManager manager) {

    }

    @Override
    public boolean noShiftclicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean noShiftLeftclicked(Player editor, NewGUIManager manager) {
        ItemStack stack = editor.getItemOnCursor().clone();
        if(stack.getType().isAir()) editor.sendMessage(StringConverter.coloredString("&cPick up the item with your mouse (it must be on your cursor), then left click this button to set it as the value"));
        else {
            ((GUI)manager.getCache().get(editor)).updateCurrently(getEditorName(), itemStackToString(stack));
            value = Optional.of(stack);
            placeholder = Optional.empty();
        }
        return true;
    }

    @Override
    public boolean noShiftRightclicked(Player editor, NewGUIManager manager) {
        ((GUI)manager.getCache().get(editor)).updateCurrently(getEditorName(), "&c&oNo item set");
        value = Optional.empty();
        placeholder = Optional.empty();
        return true;
    }


    @Override
    public boolean shiftClicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean shiftLeftClicked(Player editor, NewGUIManager manager) {
        if(value.isPresent()) {
            ItemStack stack = value.get().clone();
            stack.setAmount(stack.getAmount() + 1);
            ((GUI)manager.getCache().get(editor)).updateCurrently(getEditorName(), itemStackToString(stack));
            value = Optional.of(stack);
            placeholder = Optional.empty();
        }
        return true;
    }

    @Override
    public boolean shiftRightClicked(Player editor, NewGUIManager manager) {
        if(value.isPresent()) {
            ItemStack stack = value.get().clone();
            int amount = stack.getAmount();
            stack.setAmount(stack.getAmount() - 1);

            if(amount != 1) {
                ((GUI)manager.getCache().get(editor)).updateCurrently(getEditorName(), itemStackToString(stack));
                value = Optional.of(stack);
                placeholder = Optional.empty();
            }
        }
        return true;
    }

    @Override
    public boolean leftClicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean rightClicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean doubleClicked(Player editor, NewGUIManager manager) {
        if(featureDoubleClickOpener != null) {

            for(FeatureInterface feature : (List<FeatureInterface>) getParent().getFeatures()) {
                if(feature.getFeatureSettings() == featureDoubleClickOpener && feature instanceof FeatureParentInterface) {
                    FeatureParentInterface featureParent = (FeatureParentInterface) feature;
                    getParent().reload();
                    getParent().save();
                    featureParent.openEditor(editor);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean middleClicked(Player editor, NewGUIManager manager) {
        // When the player on creative middle clicks an item, he will get the duplicate in their cursor

        if (editor.getGameMode() != GameMode.CREATIVE && editor.getItemOnCursor().getType() != Material.AIR) {
            // Feature only active for creative players
            return false;
        }

        Optional<ItemStack> configuredItemOptional = this.getValue();

        if (configuredItemOptional.isPresent()) {
            ItemStack configuredItem = configuredItemOptional.get();

            // Ensure it's not an empty/air stack
            if (configuredItem.getType() != Material.AIR) {

                // 4. Clone the item to give a copy, not the original reference
                ItemStack itemToPlaceOnCursor = configuredItem.clone();

                // 5. Set the cloned item onto the player's cursor, replacing anything there
                editor.setItemOnCursor(itemToPlaceOnCursor);

                // 6. Return true to indicate the middle-click was successfully handled
                return true;
            }
        }



        return false;
    }
}

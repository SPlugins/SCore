package com.ssomar.score.features.types.list;

import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.editor.Suggestion;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.menu.EditorCreator;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Getter
@Setter
public class ListPotionEffectTypeFeature extends ListFeatureAbstract<PotionEffectType, ListPotionEffectTypeFeature> {

    public ListPotionEffectTypeFeature(FeatureParentInterface parent, List<PotionEffectType> defaultValue, FeatureSettingsInterface featureSettings) {
        super(parent, "List of Potion Effect Types", defaultValue, featureSettings);
        reset();
    }

    @Override
    public List<PotionEffectType> loadValues(List<String> entries, List<String> errors) {
        List<PotionEffectType> value = new ArrayList<>();
        for (String s : entries) {
            s = StringConverter.decoloredString(s);
            try {
                PotionEffectType potionType = PotionEffectType.getByName(s.toUpperCase());
                if (potionType != null) {
                    value.add(potionType);
                } else {
                    errors.add("&cERROR, Couldn't load the PotionEffectType value of " + this.getName() + " from config, value: " + s + " &7&o" + getParent().getParentInfo() + " &6>> Potion Effect Types available: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
                }
            } catch (Exception e) {
                errors.add("&cERROR, Couldn't load the PotionEffectType value of " + this.getName() + " from config, value: " + s + " &7&o" + getParent().getParentInfo() + " &6>> Potion Effect Types available: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
            }
        }
        return value;
    }

    @Override
    public String transfromToString(PotionEffectType value) {
        return value.getName();
    }

    @Override
    public ListPotionEffectTypeFeature clone(FeatureParentInterface newParent) {
        ListPotionEffectTypeFeature clone = new ListPotionEffectTypeFeature(newParent, getDefaultValue(), getFeatureSettings());
        clone.setValues(getValues());
        clone.setBlacklistedValues(getBlacklistedValues());
        return clone;
    }

    @Override
    public Optional<String> verifyMessage(String message) {
        message = StringConverter.decoloredString(message);
        try {
            PotionEffectType potionType = PotionEffectType.getByName(message);
            if (potionType != null) {
                getValues().add(potionType);
                return Optional.empty();
            } else {
                return Optional.of("&4&l[ERROR] &cThe message you entered is not a valid PotionEffectType &6>> Potion Effect Types available: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
            }
        } catch (Exception e) {
            return Optional.of("&4&l[ERROR] &cThe message you entered is not a valid PotionEffectType &6>> Potion Effect Types available: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
        }
    }

    @Override
    public List<TextComponent> getMoreInfo() {
        return null;
    }

    @Override
    public List<Suggestion> getSuggestions() {
        SortedMap<String, Suggestion> map = new TreeMap<String, Suggestion>();
        for (PotionEffectType potionType : PotionEffectType.values()) {
            String name = potionType.getName();
            map.put(name, new Suggestion(name, "&6[" + "&e" + name + "&6]", "&7Add &e" + name));
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public String getTips() {
        return "&8Example &7&oHEAL";
    }


    @Override
    public void sendBeforeTextEditor(Player playerEditor, NewGUIManager manager) {
        List<String> beforeMenu = new ArrayList<>();
        beforeMenu.add("&7➤ Your custom " + getEditorName() + ":");

        HashMap<String, String> suggestions = new HashMap<>();

        EditorCreator editor = new EditorCreator(beforeMenu, (List<String>) manager.currentWriting.get(playerEditor), getEditorName() + ":", true, true, true, true,
                true, true, true, "", suggestions);
        editor.generateTheMenuAndSendIt(playerEditor);
    }
}

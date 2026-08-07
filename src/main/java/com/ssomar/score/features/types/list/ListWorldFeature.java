package com.ssomar.score.features.types.list;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.editor.Suggestion;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.menu.EditorCreator;
import com.ssomar.score.usedapi.AllWorldManager;
import com.ssomar.score.usedapi.MultiverseAPI;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@Setter
public class ListWorldFeature extends ListFeatureAbstract<String, ListWorldFeature> {

    private static final boolean DEBUG = false;

    public ListWorldFeature(FeatureParentInterface parent, List<String> defaultValue, FeatureSettingsInterface featureSettings) {
        super(parent, "List of Worlds", defaultValue, featureSettings);
        reset();
    }

    @Override
    public List<String> loadValues(List<String> entries, List<String> errors) {
        List<String> value = new ArrayList<>();
        for (String s : entries) {
            s = StringConverter.decoloredString(s);
            Optional<World> worldOptional = AllWorldManager.getWorld(s);
            if (worldOptional.isPresent()) {
                value.add(s);
            } else
                errors.add("&cERROR, Couldn't load the World value of " + this.getName() + " from config, value: " + s + " &7&o" + getParent().getParentInfo());
        }
        return value;
    }

    @Override
    public String transfromToString(String value) {
        return value;
    }


    @Override
    public ListWorldFeature clone(FeatureParentInterface newParent) {
        ListWorldFeature clone = new ListWorldFeature(newParent, getDefaultValue(), getFeatureSettings());
        clone.setValues(getValues());
        clone.setBlacklistedValues(getBlacklistedValues());
        return clone;
    }

    @Override
    public Optional<String> verifyMessage(String message) {
        message = StringConverter.decoloredString(message);
        World w = MultiverseAPI.getWorld(message);
        if (w == null) {
            return Optional.of("&4&l[ERROR] &cThe message you entered is not a world, please try again.");
        }
        return Optional.empty();
    }

    @Override
    public List<TextComponent> getMoreInfo() {
        return null;
    }

    @Override
    public List<Suggestion> getSuggestions() {
        SortedMap<String, Suggestion> map = new TreeMap<String, Suggestion>();
        for (String s : MultiverseAPI.getWorlds()) {
            map.put(s, new Suggestion(s, "&6[" + "&e" + s + "&6]", "&7Add &e" + s));
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public String getTips() {
        // Custom tips for disabled worlds
        if (getFeatureSettings().getName().equals("disabledWorlds")) {
            return "&8Example &7&oworld &7(&cblacklist&7) &8- &7&o!world &7(&awhitelist&7)";
        } else {
            return "&8Example &7&oworld &7(&awhitelisted&7) &8- &7&o!world &7(&cblacklisted&7)";
        }

    }


    @Override
    public void sendBeforeTextEditor(Player playerEditor, NewGUIManager manager) {
        List<String> beforeMenu = new ArrayList<>();
        beforeMenu.add("&7➤ Your custom " + getEditorName() + ":");

        HashMap<String, String> suggestions = new HashMap<>();

        EditorCreator editor = new EditorCreator(beforeMenu, (List<String>) manager.currentWriting.get(playerEditor), getEditorName() + ":", true, true, true, true,
                true, true, false, "", suggestions);
        editor.generateTheMenuAndSendIt(playerEditor);
    }

    public boolean isValidWorld(@NotNull World world) {
        boolean forValuesBool = isValidWorld(true, getValues(), world);
        SsomarDev.testMsg(">> verif forValuesBool: " + forValuesBool, DEBUG);
        boolean forBlacklistValuesBool = isValidWorld(false, getBlacklistedValues(), world);
        SsomarDev.testMsg(">> verif forBlacklistValuesBool: " + forBlacklistValuesBool, DEBUG);
        return forValuesBool && !forBlacklistValuesBool;
    }

    public boolean isValidWorld(boolean ifEmpty, List<String> references, @NotNull World world) {

        for (String s : references) SsomarDev.testMsg(">> verif references: " + s, DEBUG);

        if(references.isEmpty()) return ifEmpty;

        for(String ref : references) {
            Optional<World> worldOptional = AllWorldManager.getWorld(ref);
            if (worldOptional.isPresent()) {
                World worldRed = worldOptional.get();
                if (worldRed == world) return true;
            }
        }

        return false;
    }
}

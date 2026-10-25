package com.ssomar.score.sobject.menu.defaultobjects;

import com.ssomar.score.SCore;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.sobject.*;
import com.ssomar.score.sobject.menu.NewSObjectsManagerEditor;
import com.ssomar.score.sobject.menu.SObjectsEditorAbstract;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.messages.CenteredMessage;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NewDefaultObjectsEditor<T extends SObjectWithFile & SObjectEditable & SObjectBuildable> extends GUI {

    private static final int SOBJECT_PER_PAGE = 27;
    @Getter
    @Setter
    private static int index;
    private final SObjectWithFileManager<T> manager;
    private final SObjectWithFileLoader<T> loader;
    private final SPlugin sPlugin;
    private final SObjectsEditorAbstract back;

    public NewDefaultObjectsEditor(SPlugin sPlugin, SObjectWithFileManager<T> manager, SObjectsEditorAbstract back) {
        super("&8&lDefault objects", 5 * 9);
        this.manager = manager;
        this.loader = manager.getFileLoader();
        this.sPlugin = sPlugin;
        index = 1;
        this.back = back;
        load();
    }



    public void load() {
        clearAndSetBackground();
        List<T> items = manager.getDefaultObjects();
        int i = 0;
        int total = 0;
        for (T sObject : items) {

            /* Get real id to make it proper */
            Map<String, String> randomIdsDefaultItems = loader.getRandomIdsDefaultObjects();
            String realID = "ID NOT FOUND";
            for (String id : randomIdsDefaultItems.keySet()) {
                if (randomIdsDefaultItems.get(id).equals(sObject.getId())) {
                    realID = id;
                }
            }

            if ((index - 1) * SOBJECT_PER_PAGE <= total && total < index * SOBJECT_PER_PAGE) {

                ItemStack itemS = SObjectIconCache.getIcon(sObject);

                List<String> desc = new ArrayList<>();
                desc.add("");
                desc.add("&a(click to test (give to you))");
                desc.addAll(sObject.getDescription());

                String[] descArray = new String[desc.size()];
                for (int j = 0; j < desc.size(); j++) {
                    if (desc.get(j).length() > 40) {
                        descArray[j] = desc.get(j).substring(0, 39) + "...";
                    } else {
                        descArray[j] = desc.get(j);
                    }
                }
                createItem(itemS, 1, i, CREATION_ID + " &e&o" + realID, false, false, descArray);
                i++;

            }
            total++;
        }

        if (items.size() > SOBJECT_PER_PAGE && index * SOBJECT_PER_PAGE < items.size()) {
            createItem(NEXT_PAGE_MAT, 1, 44, NEXT_PAGE, false, false);
        }
        if (index > 1) {
            createItem(PREVIOUS_PAGE_MAT, 1, 37, PREVIOUS_PAGE, false, false);
        }
        createItem(RED, 1, 36, BACK, false, false);


        createItem(PURPLE, 1, 40, "&5&l▶ &dGive to you all items", true, false);
    }

    public void goNextPage() {
        index++;
        load();
    }

    public void goPreviousPage() {
        index--;
        load();
    }

    public void goBack(Player player) {
        NewSObjectsManagerEditor.getInstance().startEditing(player, back);
    }

    public void giveAllObjects(Player player) {
        List<T> items = manager.getDefaultObjects();
        for (T sObject : items) {
            player.getInventory().addItem(sObject.buildItem(1, Optional.of(player)));
            player.sendMessage(StringConverter.coloredString("&2&l" + sPlugin.getNameDesign() + " &aYou received &e" + sObject.getId()));
        }
    }

    public void giveSObject(String objectID, Player p) {
        objectID = (String) loader.getRandomIdsDefaultObjects().get(objectID);
        Optional<T> optional = manager.getLoadedObjectWithID(objectID);

        String  objectName = sPlugin.getObjectName();
        /* For plugins that have multiple object splugin.getOjectName can't work */
        if(objectName == null) objectName = manager.getObjectName();
        objectName = objectName.toLowerCase();


        if (optional.isPresent()) {
            T sObject = optional.get();
            p.getInventory().addItem(sObject.buildItem(1, Optional.of(p)));
            p.sendMessage(StringConverter.coloredString("&2&l[" + sPlugin.getNameDesign() + "] &aYou received &e" + objectID));

            if (sPlugin.isLotOfWork()) {
                StringBuilder sb = new StringBuilder();
                p.sendMessage(StringConverter.coloredString("&f"));
                if (SCore.is1v16Plus())
                    sb.append("&#ecfb42&l&oE&#e2fb45&l&ox&#d7fb48&l&oe&#cdfb4b&l&oc&#c2fc4d&l&ou&#b8fc50&l&ot&#adfc53&l&oa&#a3fc56&l&ob&#99fc59&l&ol&#8efc5c&l&oe&#84fc5f&l&oI&#79fd61&l&ot&#6ffd64&l&oe&#64fd67&l&om&#5afd6a&l&os ");
                else sb.append("&6&l&o").append(sPlugin.getNameDesign()).append(" ");
                sb.append("&7This ").append(objectName).append(" is from the ");
                if (SCore.is1v16Plus())
                    sb.append("&#6cdbf4&l&oP&#66dbe4&l&or&#60dad5&l&oe&#5bdac5&l&om&#55d9b5&l&oi&#4fd9a6&l&ou&#49d896&l&om &#44d886&l&oV&#3ed776&l&oe&#38d767&l&or&#32d657&l&os&#2dd647&l&oi&#27d538&l&oo&#21d528&l&on");
                else sb.append("&a&l&oPremium Version");
                sb.append("&7, it wont work after your next restart.");
                p.sendMessage(StringConverter.coloredString(sb.toString()));
                p.sendMessage(StringConverter.coloredString("&7&o(It's just for a test purpose. To test the item be sure that you are OP or have the permission ei.item.*)"));
                p.sendMessage(StringConverter.coloredString("&f"));
                p.sendMessage(StringConverter.coloredString("&7If you want edit this " + sPlugin.getShortName() + " and you want that this item works everytime, you can support our work by purchasing the premium version of " + sPlugin.getShortName() + ": &8&o(for the " + sPlugin.getShortName() + " from packs you also need to buy the pack)"));

                String clickHereB = "       &5&l*&7&l********&3&l*";
                clickHereB = StringConverter.coloredString(clickHereB);
                clickHereB = CenteredMessage.convertIntoCenteredMessage(clickHereB);

                TextComponent clickHereBtn = new TextComponent(clickHereB);
                clickHereBtn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/83070/"));
                clickHereBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&7&oClick here to go on the premium page")).create()));

                String clickHere;
                if (SCore.is1v16Plus())
                    clickHere = "&#911dec&l&oC&#892de9&l&oL&#823ee6&l&oI&#7a4ee3&l&oC&#735ee1&l&oK &#6b6ede&l&oH&#637fdb&l&oE&#5c8fd8&l&oR&#549fd5&l&oE";
                else clickHere = "&5&lCLICK &3&l&HERE";
                clickHere = StringConverter.coloredString(clickHere);
                clickHere = CenteredMessage.convertIntoCenteredMessage(clickHere);
                p.sendMessage(clickHere);

                p.spigot().sendMessage(clickHereBtn);
            } else {
                StringBuilder sb = new StringBuilder();
                p.sendMessage(StringConverter.coloredString("&f"));
                if (SCore.is1v16Plus())
                    sb.append("&#ecfb42&l&oE&#e2fb45&l&ox&#d7fb48&l&oe&#cdfb4b&l&oc&#c2fc4d&l&ou&#b8fc50&l&ot&#adfc53&l&oa&#a3fc56&l&ob&#99fc59&l&ol&#8efc5c&l&oe&#84fc5f&l&oI&#79fd61&l&ot&#6ffd64&l&oe&#64fd67&l&om&#5afd6a&l&os ");
                else sb.append("&6&l&o").append(sPlugin.getNameDesign()).append(" ");
                sb.append("&7This ").append(objectName).append(" is from a ");
                if (SCore.is1v16Plus())
                    sb.append("&#6cdbf4&l&oC&#66dbe4&l&ou&#60dad5&l&os&#5bdac5&l&ot&#55d9b5&l&oo&#4fd9a6&l&om &#49d896&l&oP&#44d886&l&oa&#3ed776&l&oc&#38d767&l&ok");
                else sb.append("&a&l&oCustom Pack");
                sb.append("&7, it wont work after your next restart.");
                p.sendMessage(StringConverter.coloredString(sb.toString()));
                p.sendMessage(StringConverter.coloredString("&7&o(It's just for a test purpose. To test the item be sure that you are OP or have the permission ei.item.*)"));
                p.sendMessage(StringConverter.coloredString("&f"));
                p.sendMessage(StringConverter.coloredString("&7If you want edit this " + sPlugin.getShortName() + " and you want that this item works everytime, you can support our work by purchasing the items pack of the " + sPlugin.getShortName() + ": "));

                String clickHereB = "       &5&l*&7&l********&3&l*";
                clickHereB = StringConverter.coloredString(clickHereB);
                clickHereB = CenteredMessage.convertIntoCenteredMessage(clickHereB);

                TextComponent clickHereBtn = new TextComponent(clickHereB);
                clickHereBtn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.com/invite/TRmSwJaYNv"));
                clickHereBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&7&oClick here to join the discord !")).create()));

                String clickHere;
                if (SCore.is1v16Plus())
                    clickHere = "&#911dec&l&oC&#892de9&l&oL&#823ee6&l&oI&#7a4ee3&l&oC&#735ee1&l&oK &#6b6ede&l&oH&#637fdb&l&oE&#5c8fd8&l&oR&#549fd5&l&oE";
                else clickHere = "&5&lCLICK &3&l&HERE";
                clickHere = StringConverter.coloredString(clickHere);
                clickHere = CenteredMessage.convertIntoCenteredMessage(clickHere);
                p.sendMessage(clickHere);

                p.spigot().sendMessage(clickHereBtn);
            }
        }
    }
}

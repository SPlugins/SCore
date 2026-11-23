package com.ssomar.score.sobject.menu;

import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.sobject.SObject;
import com.ssomar.score.sobject.SObjectEditable;
import com.ssomar.score.sobject.SObjectIconCache;
import com.ssomar.score.sobject.SObjectManager;
import com.ssomar.score.sobject.SObjectWithFileLoader;
import com.ssomar.score.splugin.SPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class SObjectsNoFileEditor<T extends SObject & SObjectEditable> extends SObjectsEditorAbstract<T> {


    public SObjectsNoFileEditor(SPlugin sPlugin, FeatureSettingsInterface settings, SObjectManager manager) {
        super(sPlugin, settings, manager);
        this.load();
    }

    public SObjectsNoFileEditor(SPlugin sPlugin, String title, SObjectManager manager) {
        super(sPlugin, title, manager);
        this.load();
    }


    public void load() {
        clearAndSetBackground();
        int i = 0;
        int total = 0;

        //SsomarDev.testMsg(">>>>>>>>>>>>"+getManager().getObjects(getComparator(), getPredicate()).size(), true);
        for (T object : getManager().getObjects(getComparator(), getPredicates())) {

            if ((index - 1) * SOBJECT_PER_PAGE <= total && total < index * SOBJECT_PER_PAGE) {

                ItemStack itemS = SObjectIconCache.getIcon(object);

                List<String> desc = new ArrayList<>();
                desc.add("");
                if(isClickToEdit()) desc.add(GUI.CLICK_HERE_TO_CHANGE);
                if (isGiveButton()) desc.add(TM.g(Text.EDITOR_GIVE_SHIFT_RIGHT_DESCRIPTION));
                if (isDeleteButton()) desc.add(GUI.SHIFT_LEFT_CLICK_TO_REMOVE);
                desc.addAll(object.getDescription());

                String[] descArray = new String[desc.size()];
                for (int j = 0; j < desc.size(); j++) {
                    if (desc.get(j).length() > 40) {
                        descArray[j] = desc.get(j).substring(0, 39) + "...";
                    } else {
                        descArray[j] = desc.get(j);
                    }
                }
                String prefix = "";
                if(!isHideCreation()) prefix = CREATION_ID+ " &e&o";
                if(getSubSettings().containsKey("custom_identifier")){
                    String customIdentifier = getSubSettings().get("custom_identifier");
                    if(customIdentifier != null && !customIdentifier.isEmpty()){
                        prefix = customIdentifier;
                    }
                }
                createItem(itemS, 1, i, prefix + object.getId(), false, false, descArray);
                i++;
            }
            total++;
        }
        //other button
        if (getManager().getObjects().size() > SOBJECT_PER_PAGE && index * SOBJECT_PER_PAGE < getManager().getObjects().size()) {
            createItem(NEXT_PAGE_MAT, 1, 44, NEXT_PAGE, false, false);
        }
        if (index > 1) {
            createItem(PREVIOUS_PAGE_MAT, 1, 37, PREVIOUS_PAGE, false, false);
        }
        createItem(RED, 1, 36, EXIT, false, false);

        // change lang menu
        if(isLanguageButton()) createItem(YELLOW, 1, 37, GUI.CHANGE_LANGUAGE, false, false, GeneralConfig.getInstance().getAvailableLocales("", "&e&oClick here to change the language"));

        String[] desc = new String[1 + TM.gA(Text.EDITOR_PATH_DESCRIPTION).length];
        desc[0] = "&7";
        System.arraycopy(TM.gA(Text.EDITOR_PATH_DESCRIPTION), 0, desc, 1, TM.gA(Text.EDITOR_PATH_DESCRIPTION).length);
        if(isPathButton()) createItem(Material.ANVIL, 1, 38, TM.g(Text.EDITOR_PATH_NAME), false, false, desc);

        if(isNewButton()) createItem(GREEN, 1, 40, NEW + " " + getManager().getObjectName(), false, false);

        if(isDefaultObjectsButton()) {
            if (getSPlugin().isLotOfWork())
                createItem(PURPLE, 1, 43, TM.g(Text.EDITOR_PREMADE_PREMIUM_NAME).replace("%object%", getManager().getObjectName()), false, false);
            else
                createItem(PURPLE, 1, 43, TM.g(Text.EDITOR_PREMADE_PACKS_NAME).replace("%object%", getManager().getObjectName()), false, false);
        }
    }


    public void goToFolder(String folder) {
    }

    public void goBack() {

    }

    public SObjectWithFileLoader getLoader() {
        return null;
    }


}

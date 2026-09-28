package com.ssomar.score.sobject.menu;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
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
import com.ssomar.score.utils.DynamicMeta;
import com.ssomar.score.utils.itemwriter.ItemKeyWriterReader;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

@Getter
@Setter
public abstract class SObjectsWithFileEditor<T extends SObject & SObjectEditable> extends SObjectsEditorAbstract<T> {

    private String defaultPath;
    private String path;
    private SObjectWithFileLoader loader;
    private boolean dontShowDirectory = false;
    private List<String> cachedFiles;
    private String cachedFilesKey;

    public SObjectsWithFileEditor(SPlugin sPlugin, FeatureSettingsInterface settings, String path, SObjectManager manager, SObjectWithFileLoader loader) {
        super(sPlugin, settings, manager);
        this.defaultPath = path;
        this.path = path;
        this.loader = loader;
        this.load();
    }

    public SObjectsWithFileEditor(SPlugin sPlugin, String title, String path, SObjectManager manager, SObjectWithFileLoader loader) {
        super(sPlugin, title, manager);
        this.defaultPath = path;
        this.path = path;
        this.loader = loader;
        this.load();
    }

    public List<String> getFilesInFolder(String path){
        List<String> listFiles = new ArrayList<>(Arrays.asList(new File(path).list()));
        if(dontShowDirectory){
            for (File fileEntry : new File(path).listFiles()) {
                if (fileEntry.isDirectory()){
                    listFiles.remove(fileEntry.getName());
                    listFiles.addAll(getFilesInFolder(path + "/" + fileEntry.getName()));
                }
            }
        }
        Collections.sort(listFiles);
        return listFiles;
    }


    public void load() {
        clearAndSetBackground();
        int i = 0;
        int total = 0;
        Plugin plugin = getSPlugin().getPlugin();
        SsomarDev.testMsg(">>>>>>>>>>>>"+plugin.getDataFolder() + path, true);
        // The recursive directory walk is only redone when the folder (or the flatten option) changes,
        // not on every page flip.
        String filesKey = plugin.getDataFolder() + path + "|" + dontShowDirectory;
        if (cachedFiles == null || !filesKey.equals(cachedFilesKey)) {
            cachedFiles = getFilesInFolder(plugin.getDataFolder() + path);
            cachedFilesKey = filesKey;
        }
        List<String> listFiles = cachedFiles;

        for (String str : listFiles) {
            if ((index - 1) * SOBJECT_PER_PAGE <= total && total < index * SOBJECT_PER_PAGE) {
                File fileEntry = new File(plugin.getDataFolder() + path + "/" + str);
                String fileName = fileEntry.getName();
                if (fileName.contains(".txt")) continue;
                String name = str;
                if (fileEntry.isDirectory()) {
                    Material material = Material.CHEST;
                    if (str.contains("[icon-")) {
                        String[] split = str.split("\\[icon-");
                        name = split[0];
                        try {
                            material = Material.valueOf(split[1].split("]")[0].toUpperCase());
                        } catch (Exception e) {
                            material = Material.CHEST;
                        }
                    }
                    ItemStack itemStack = new ItemStack(material);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    DynamicMeta dynamicMeta = new DynamicMeta(itemMeta);
                    ItemKeyWriterReader.init().writeString(SCore.pluginHolder, itemStack, dynamicMeta, "folderInfo", str);
                    itemStack.setItemMeta(dynamicMeta.getMeta());
                    createItem(itemStack, 1, i, TM.g(Text.EDITOR_FOLDER_NAME) + name, false, false, TM.gA(Text.EDITOR_FOLDER_DESCRIPTION));
                } else {
                    if (!fileName.contains(".yml") || fileName.equals(".yml"))
                        if(isDeleteButton()) createItem(Material.BARRIER, 1, i, TM.g(Text.EDITOR_INVALID_FILE_NAME) + str, false, false, "", GUI.SHIFT_LEFT_CLICK_TO_REMOVE, TM.g(Text.EDITOR_INVALID_FILE_DESCRIPTION));
                    String id = fileName.split(".yml")[0];


                    Optional<T> sObjectOpt = getManager().getLoadedObjectWithID(id);
                    if (sObjectOpt.isPresent()) {
                        T sObject = sObjectOpt.get();
                        ItemStack itemS = SObjectIconCache.getIcon(sObject);
                        // An object whose icon is AIR (e.g. an ExecutableCrafting recipe with an AIR
                        // result) has no ItemMeta: without this guard the NPE kills the whole editor.
                        if (itemS == null || itemS.getType() == Material.AIR) itemS = new ItemStack(Material.BARRIER);

                        /* Remove useless tags */
                        ItemMeta meta = itemS.getItemMeta();
                        // Read before createItem() overwrites the name / lore and the flags below hide the rest
                        List<String> preview = GeneralConfig.getInstance().isEditorIconPreview() ? SObjectIconPreview.of(meta) : new ArrayList<String>();
                        if (meta != null) {
                            ItemFlag additionnalFlag = SCore.is1v20v5Plus() ? ItemFlag.HIDE_ADDITIONAL_TOOLTIP : ItemFlag.valueOf("HIDE_POTION_EFFECTS");
                            meta.addItemFlags(additionnalFlag);
                            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
                            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
                            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_UNBREAKABLE});
                            if (SCore.is1v17Plus())
                                meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_DYE});
                            if(SCore.is1v20Plus())
                                meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ARMOR_TRIM});
                            itemS.setItemMeta(meta);
                        }

                        List<String> desc = new ArrayList<>();
                        desc.add("");
                        desc.add(GUI.CLICK_HERE_TO_CHANGE);
                        if(isGiveButton()) desc.add(TM.g(Text.EDITOR_GIVE_SHIFT_RIGHT_DESCRIPTION));
                        if(isDeleteButton()) desc.add(GUI.SHIFT_LEFT_CLICK_TO_REMOVE);
                        List<String> objectDesc = sObject.getDescription();
                        // Capped on its own so a long list of activators can't push the preview out
                        desc.addAll(objectDesc.subList(0, Math.min(objectDesc.size(), 25 - desc.size())));

                        // A limit had to be added here because some users may have items that contain 249+ activators.
                        // How are activators involved? When you go to "/ei show" for example, the icons show the list
                        // of activators and their ids and an error tripped due to an icon surpassing 256 lore lines.
                        for (int j = 0; j < desc.size(); j++) {
                            if (desc.get(j).length() > 40) desc.set(j, desc.get(j).substring(0, 39) + "...");
                        }
                        if (!preview.isEmpty()) {
                            desc.add("");
                            desc.addAll(preview);
                        }
                        String[] descArray = desc.toArray(new String[0]);

                        createItem(itemS, 1, i, CREATION_ID + " &e&o" + id, false, false, descArray);
                    } else {
                        if (getSPlugin().isLotOfWork())
                            createItem(Material.BARRIER, 1, i, TM.g(Text.EDITOR_INVALID_CONFIGURATION_NAME) + id, false, false, "", TM.g(Text.EDITOR_INVALID_CONFIGURATION_DESCRIPTION), GUI.SHIFT_LEFT_CLICK_TO_REMOVE, TM.g(Text.EDITOR_INVALID_CONFIGURATION_FREELIMIT) + " &7(&e"+getSPlugin().getMaxSObjectsLimit() + " " + getManager().getObjectName() + "&7)");
                        else
                            createItem(Material.BARRIER, 1, i, TM.g(Text.EDITOR_INVALID_CONFIGURATION_NAME) + id, false, false, "", TM.g(Text.EDITOR_INVALID_CONFIGURATION_DESCRIPTION), GUI.SHIFT_LEFT_CLICK_TO_REMOVE);
                    }
                }
                i++;

            }
            total++;
        }
        //other button
        if (listFiles.size() > SOBJECT_PER_PAGE && index * SOBJECT_PER_PAGE < listFiles.size()) {
            createItem(NEXT_PAGE_MAT, 1, 44, NEXT_PAGE, false, false);
        }
        if (index > 1) {
            createItem(PREVIOUS_PAGE_MAT, 1, 37, PREVIOUS_PAGE, false, false);
        }

        if (!this.path.equals(defaultPath)) createItem(RED, 1, 36, BACK, false, false);
        else createItem(RED, 1, 36, EXIT, false, false);

        // change lang menu
        createItem(YELLOW, 1, 39, GUI.CHANGE_LANGUAGE, false, false, GeneralConfig.getInstance().getAvailableLocales("", "&e&oClick here to change the language"));

        String[] desc = new String[2+TM.gA(Text.EDITOR_PATH_DESCRIPTION).length];
        desc[0] = "";
        desc[1] = TM.g(Text.EDITOR_CURRENTLY_NAME)+" &a" + path;
        System.arraycopy(TM.gA(Text.EDITOR_PATH_DESCRIPTION), 0, desc, 2, TM.gA(Text.EDITOR_PATH_DESCRIPTION).length);
        if (isPathButton()) createItem(Material.ANVIL, 1, 38, TM.g(Text.EDITOR_PATH_NAME), false, false, desc);

        if(isNewButton()) createItem(GREEN, 1, 40, NEW +" "+ getManager().getObjectName(), false, false);

        if(isDefaultObjectsButton()) {
            if (getSPlugin().isLotOfWork())
                createItem(PURPLE, 1, 43, TM.g(Text.EDITOR_PREMADE_PREMIUM_NAME).replace("%object%", getManager().getObjectName()), false, false);
            else
                createItem(PURPLE, 1, 43, TM.g(Text.EDITOR_PREMADE_PACKS_NAME).replace("%object%", getManager().getObjectName()), false, false);
        }
    }



    public void goToFolder(String folder) {
        index = 1;
        path = this.getCurrentPath() + "/" + StringConverter.decoloredString(folder).trim();
        load();
    }

    public void goBack() {
        if (!this.path.equals(defaultPath)) {
            String[] split = this.getCurrentPath().split("/");
            StringBuilder newPathBuilder = new StringBuilder("/");
            for (int i = 0; i < split.length - 1; i++) {
                if (i == 0) continue;
                newPathBuilder.append(split[i]).append("/");
            }
            String newPath = newPathBuilder.toString();
            newPath = newPath.substring(0, newPath.length() - 1);
            this.path = newPath;
        }
        load();
    }
}

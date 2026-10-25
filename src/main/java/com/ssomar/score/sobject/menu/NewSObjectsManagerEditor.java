package com.ssomar.score.sobject.menu;

import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.editor.NewInteractionClickedGUIManager;
import com.ssomar.score.editor.SaveSessionPathManager;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.sobject.SObjectWithFileManager;
import com.ssomar.score.sobject.menu.defaultobjects.NewDefaultObjectsEditor;
import com.ssomar.score.sobject.menu.defaultobjects.NewDefaultObjectsEditorManager;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.entity.Player;

public class NewSObjectsManagerEditor extends NewGUIManager<SObjectsEditorAbstract> {

    private static NewSObjectsManagerEditor instance;

    public static NewSObjectsManagerEditor getInstance() {
        if (instance == null) instance = new NewSObjectsManagerEditor();
        return instance;
    }

    public void startEditing(Player editor, SObjectsEditorAbstract gui) {
        cache.put(editor, gui);
        gui.openGUISync(editor);
        SaveSessionPathManager.getInstance().addPlayerSessionPath(editor, cache.get(editor), this);
    }

    @Override
    public boolean allClicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> i) {
        //SsomarDev.testMsg("colored deconvert: >"+i.coloredDeconvertName+"< custom_identifier: >" + i.gui.getSubSettings().getOrDefault("custom_identifier", "<XXXXX---DEFAULT---XXXXX>")+"<", true);
        if (i.coloredDeconvertName.contains(TM.g(Text.EDITOR_FOLDER_NAME))) {
            i.gui.goToFolder(i.localizedName);
        } else if (i.coloredDeconvertName.contains(TM.g(Text.EDITOR_PATH_NAME))) {
            i.gui.goBack();
        } else if (i.coloredDeconvertName.contains(SObjectsWithFileEditor.NEW)) {
            i.gui.sendMessageCreate(i.player);
        } else if (i.coloredDeconvertName.contains(TM.g(Text.EDITOR_PREMADE_PREMIUM_NAME).replace("%object%", i.gui.getManager().getObjectName())) || i.coloredDeconvertName.contains(TM.g(Text.EDITOR_PREMADE_PACKS_NAME).replace("%object%", i.gui.getManager().getObjectName()))) {
            NewDefaultObjectsEditorManager.getInstance().startEditing(i.player, new NewDefaultObjectsEditor(i.gui.getSPlugin(), (SObjectWithFileManager) i.gui.getManager(), i.gui));
        }
        else if (i.coloredDeconvertName.contains(SObjectsWithFileEditor.CREATION_ID)) {
            i.gui.openEditorSObject(i.decoloredName.split(StringConverter.decoloredString(GUI.CREATION_ID))[1].trim(), i.player);
        }
        else if (i.coloredDeconvertName.contains(i.gui.getSubSettings().getOrDefault("custom_identifier", "<XXXXX---DEFAULT---XXXXX>"))) {
            i.gui.openEditorSObject(i.decoloredName.split(StringConverter.decoloredString(i.gui.getSubSettings().getOrDefault("custom_identifier", "<XXXXX---DEFAULT---XXXXX>")))[1].trim(), i.player);
            //SsomarDev.testMsg("Opening custom identifier editor for " + i.decoloredName.split(StringConverter.decoloredString(i.gui.getSubSettings().getOrDefault("custom_identifier", "<XXXXX---DEFAULT---XXXXX>")))[1].trim(), true);
        }
        else return false;
        return true;
    }

    @Override
    public boolean noShiftclicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> i) {
        return false;
    }

    @Override
    public boolean noShiftLeftclicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> i) {
        return false;
    }

    @Override
    public boolean noShiftRightclicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> i) {
        return false;
    }

    @Override
    public boolean shiftClicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> i) {
        return false;
    }

    @Override
    public boolean shiftLeftClicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> i) {
        if (i.decoloredName.contains(StringConverter.decoloredString(GUI.CREATION_ID))) {
            i.player.closeInventory();
            String id = i.decoloredName.split(StringConverter.decoloredString(GUI.CREATION_ID))[1].trim();
            i.gui.sendMessageDelete(id, i.player);
            return true;
        }

        return false;
    }

    @Override
    public boolean shiftRightClicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> i) {
        if (i.decoloredName.contains(StringConverter.decoloredString(GUI.CREATION_ID)) && !i.decoloredName.contains("ERROR ID")) {
            i.gui.giveSObject(i.decoloredName.split(StringConverter.decoloredString(GUI.CREATION_ID))[1].trim(), i.player);
            return true;
        }
        return false;
    }

    @Override
    public boolean leftClicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> i) {
        return false;
    }

    @Override
    public boolean rightClicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {
        return false;
    }

    @Override
    public boolean middleClicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {
        return false;
    }

    @Override
    public boolean doubleClicked(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {
        return false;
    }

    @Override
    public void receiveMessage(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {

    }

    @Override
    public void receiveMessagePreviousPage(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {

    }

    @Override
    public void receiveMessageNextPage(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {

    }

    @Override
    public void receiveMessageNoValue(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {

    }

    @Override
    public void receiveMessageFinish(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {

    }

    @Override
    public void receiveMessageValue(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {

    }

    @Override
    public void newObject(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {

    }

    @Override
    public void reset(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {

    }

    @Override
    public void back(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {
        interact.gui.goBack();
    }

    @Override
    public void nextPage(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {
        interact.gui.goNextPage();
    }

    @Override
    public void previousPage(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {
        interact.gui.goPreviousPage();
    }

    @Override
    public void save(NewInteractionClickedGUIManager<SObjectsEditorAbstract> interact) {

    }
}

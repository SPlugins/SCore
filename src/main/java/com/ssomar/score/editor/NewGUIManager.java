package com.ssomar.score.editor;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.menu.commands.CommandsEditor;
import com.ssomar.score.utils.DynamicMeta;
import com.ssomar.score.utils.itemwriter.ItemKeyWriterReader;
import com.ssomar.score.utils.messages.CenteredMessage;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public abstract class NewGUIManager<T extends GUI> {

    public HashMap<Player, T> cache;
    public HashMap<Player, String> requestWriting;
    public HashMap<Player, List<String>> currentWriting;

    public Map<Player, Boolean> activeTextEditor;
    public List<TextComponent> moreInfo;
    public Map<Player, List<Suggestion>> suggestions;
    public Map<Player, Integer> suggestionPage;
    private int suggestionsPerColumn;

    public NewGUIManager() {
        cache = new HashMap<>();
        requestWriting = new HashMap<>();
        currentWriting = new HashMap<>();

        moreInfo = new ArrayList<>();
        suggestions = new HashMap<>();
        suggestionsPerColumn = 12;
        suggestionPage = new HashMap<>();
        activeTextEditor = new HashMap<>();
    }

    public void reload() {
        cache = new HashMap<>();
        requestWriting = new HashMap<>();
        currentWriting = new HashMap<>();

        moreInfo = new ArrayList<>();
        suggestions = new HashMap<>();
        suggestionsPerColumn = 12;
        suggestionPage = new HashMap<>();
        activeTextEditor = new HashMap<>();
    }

    public void enableTextEditor(Player player) {
        activeTextEditor.put(player, true);
        suggestionPage.put(player, 0);
    }

    public void disableTextEditor(Player player) {
        activeTextEditor.remove(player);
        suggestionPage.remove(player);
    }

    public void clearPlayerEditorState(Player player) {
        requestWriting.remove(player);
        currentWriting.remove(player);
        suggestions.remove(player);
        disableTextEditor(player);
    }

    public void clicked(Player p, ItemStack item, ClickType click) {
        NewInteractionClickedGUIManager<T> interact = new NewInteractionClickedGUIManager<>();
        interact.player = p;
        this.clicked(item, interact, click);
    }

    public void clicked(ItemStack item, NewInteractionClickedGUIManager<T> interact, ClickType click) {
        if (item != null && item.hasItemMeta()) {
            interact.cache = this.getCache();
            interact.setName(GUI.getIdentifier(item));
            //SsomarDev.testMsg("NAME: " + interact.decoloredName, true);
            //SsomarDev.testMsg("LOCALISZED NAME: " + item.getItemMeta().getLocalizedName());
            DynamicMeta meta = new DynamicMeta(item.getItemMeta());
            Optional<String> folderInfoOpt = ItemKeyWriterReader.init().readString(SCore.pluginHolder, item, meta, "folderInfo");
            if(folderInfoOpt.isPresent()) {
                interact.localizedName = folderInfoOpt.get();
            }
            else if(!SCore.is1v11Less() && item.getItemMeta().hasLocalizedName()){
                interact.localizedName = item.getItemMeta().getLocalizedName();
            }
            interact.gui = cache.get(interact.player);

            if (interact.coloredDeconvertName.equals(GUI.RESET)) {
                reset(interact);
            } else if (interact.coloredDeconvertName.equals(GUI.CHANGE_LANGUAGE)) {
                if (click.equals(ClickType.SHIFT_LEFT) || click.equals(ClickType.LEFT)) {
                    nextLang(interact);
                } else if (click.equals(ClickType.SHIFT_RIGHT) || click.equals(ClickType.RIGHT)) {
                    previousLang(interact);
                }
            }
            else if (interact.coloredDeconvertName.equals(GUI.BACK)) {
                back(interact);
            } else if (interact.coloredDeconvertName.equals(GUI.NEW)) {
                newObject(interact);
            } else if (interact.coloredDeconvertName.equals(GUI.EXIT)) {
                interact.player.closeInventory();
            } else if (interact.coloredDeconvertName.equals(GUI.SAVE) || interact.coloredDeconvertName.equals(TM.g(Text.EDITOR_SAVE_NAME))) {
                save(interact);
                back(interact);
            } else if (interact.coloredDeconvertName.equals(GUI.REMOVE)) {
                remove(interact);
            } else if (interact.coloredDeconvertName.equals(GUI.NEXT_PAGE)) {
                nextPage(interact);
            } else if (interact.coloredDeconvertName.equals(GUI.PREVIOUS_PAGE)) {
                previousPage(interact);
            } else {
                if (click.equals(ClickType.SHIFT_LEFT) || click.equals(ClickType.SHIFT_RIGHT)) {
                    if (click.equals(ClickType.SHIFT_LEFT)) {
                        if (this.shiftLeftClicked(interact)) return;
                    } else if (this.shiftRightClicked(interact)) return;

                    if (this.shiftClicked(interact)) return;
                } else {
                    if (click.equals(ClickType.RIGHT)) {
                        if (this.noShiftRightclicked(interact)) return;
                    } else if (click.equals(ClickType.LEFT)) {
                        if (this.noShiftLeftclicked(interact)) return;
                    }

                    if (this.noShiftclicked(interact)) return;
                }

                if (click.equals(ClickType.RIGHT)) {
                    if (this.rightClicked(interact)) return;
                } else if (click.equals(ClickType.LEFT)) {
                    if (this.leftClicked(interact)) return;
                } else if (click.equals(ClickType.MIDDLE)) {
                    if (this.middleClicked(interact)) return;
                } else if(click.equals(ClickType.DOUBLE_CLICK)){
                    if (this.doubleClicked(interact)) return;
                }

                if (this.allClicked(interact)) return;
            }
        }
    }

    public abstract boolean allClicked(NewInteractionClickedGUIManager<T> i);

    public abstract boolean noShiftclicked(NewInteractionClickedGUIManager<T> i);

    public abstract boolean noShiftLeftclicked(NewInteractionClickedGUIManager<T> i);

    public abstract boolean noShiftRightclicked(NewInteractionClickedGUIManager<T> i);

    public abstract boolean shiftClicked(NewInteractionClickedGUIManager<T> i);

    public abstract boolean shiftLeftClicked(NewInteractionClickedGUIManager<T> i);

    public abstract boolean shiftRightClicked(NewInteractionClickedGUIManager<T> i);

    public abstract boolean leftClicked(NewInteractionClickedGUIManager<T> i);

    public abstract boolean rightClicked(NewInteractionClickedGUIManager<T> interact);

    public abstract boolean middleClicked(NewInteractionClickedGUIManager<T> interact);

    public abstract boolean doubleClicked(NewInteractionClickedGUIManager<T> interact);

    public void receiveMessage(Player p, String message) {
        NewInteractionClickedGUIManager<T> interact = new NewInteractionClickedGUIManager<>();
        interact.player = p;
        interact.setMessage(message);
        interact.gui = cache.get(interact.player);
        receiveMessage(interact);
    }


    public void receiveMessage(NewInteractionClickedGUIManager<T> interact) {
        String message = interact.decoloredMessage;
        boolean pass = false;
        /* The commands editor has the priority */
        if (!CommandsEditor.getInstance().isAsking(interact.player)) {
            if (message.equals("PREVIOUS PAGE")) {
                this.receiveMessagePreviousPage(interact);
                pass = true;
            } else if (message.equals("NEXT PAGE")) {
                this.receiveMessageNextPage(interact);
                pass = true;
            } else if (message.contains("delete line <")) {
                this.receiveMessageDeleteline(interact);
                pass = true;
            } else if (message.contains("up line <")) {
                this.receiveMessageUpLine(interact);
                pass = true;
            } else if (message.contains("down line <")) {
                this.receiveMessageDownLine(interact);
                pass = true;
            } else if (message.contains("edit line <")) {
                this.receiveMessageEditLine(interact);
                pass = true;
            } else if (message.contains("NO VALUE / EXIT")) {
                this.receiveMessageNoValue(interact);
                pass = true;
            }
        }
        if (message.equals("exit")) {
            this.receiveMessageFinish(interact);
            pass = true;
        }
        if (!pass) {
            this.receiveMessageValue(interact);
        }
    }

    public void receiveMessagePreviousPage(NewInteractionClickedGUIManager<T> interact) {
        suggestionPage.put(interact.player, suggestionPage.get(interact.player) - 1);
    }

    public void receiveMessageNextPage(NewInteractionClickedGUIManager<T> interact) {
        suggestionPage.put(interact.player, suggestionPage.get(interact.player) + 1);
    }

    public abstract void receiveMessageNoValue(NewInteractionClickedGUIManager<T> interact);

    public abstract void receiveMessageFinish(NewInteractionClickedGUIManager<T> interact);

    public void receiveMessageDeleteline(NewInteractionClickedGUIManager<T> interact) {
        Player p = interact.player;
        space(p);
        space(p);
        int line = Integer.parseInt(interact.decoloredMessage.split("delete line <")[1].split(">")[0]);
        currentWriting.get(p).remove(line);
        p.sendMessage(StringConverter.coloredString("&a&l>> &2&lEDITION &aYou have delete the line: " + line + " !"));
        space(p);
        space(p);
    }

    public void receiveMessageUpLine(NewInteractionClickedGUIManager<T> interact) {
        Player p = interact.player;
        space(p);
        space(p);
        int line = Integer.valueOf(interact.decoloredMessage.split("up line <")[1].split(">")[0]);
        if (line != 0) {
            String current = currentWriting.get(p).get(line);
            currentWriting.get(p).set(line, currentWriting.get(p).get(line - 1));
            currentWriting.get(p).set(line - 1, current);
        }
        p.sendMessage(StringConverter.coloredString("&a&l>> &2&lEDITION &aYou have up the line: " + line + " to " + (line - 1) + " !"));
        space(p);
        space(p);
    }

    public void receiveMessageDownLine(NewInteractionClickedGUIManager<T> interact) {
        Player p = interact.player;
        space(p);
        space(p);
        int line = Integer.valueOf(interact.decoloredMessage.split("down line <")[1].split(">")[0]);
        if (currentWriting.containsKey(p) && currentWriting.get(p).size()-1 > line) {
            String current = currentWriting.get(p).get(line);
            currentWriting.get(p).set(line, currentWriting.get(p).get(line + 1));
            currentWriting.get(p).set(line + 1, current);
        }
        p.sendMessage(StringConverter.coloredString("&a&l>> &2&lEDITION &aYou have down the line: " + line + " to " + (line + 1) + " !"));
        space(p);
        space(p);
    }

    public void receiveMessageEditLine(NewInteractionClickedGUIManager<T> interact) {
        Player p = interact.player;
        if (currentWriting.get(p).size() == 0) {
            return;
        }
        space(p);
        space(p);
        int line = Integer.valueOf(interact.decoloredMessage.split("edit line <")[1].split(">")[0]);
        if (line >= 0) {
            String[] splitContent = interact.coloredDeconvertMessage.split("edit line <" + line + "> ->");
            if (splitContent.length >= 2) {
                String modification = splitContent[1];
                currentWriting.get(p).set(line, modification);
            }
            else {
                p.sendMessage(StringConverter.coloredString("&c&l>> &4&lEDITION &cedit line format is not valid! Correct format: &eedit line <line_number> -> your new line"));
            }
        }
        p.sendMessage(StringConverter.coloredString("&a&l>> &2&lEDITION &aYou have edit the line: " + line + " !"));
        space(p);
        space(p);
    }

    public abstract void receiveMessageValue(NewInteractionClickedGUIManager<T> interact);

    public abstract void newObject(NewInteractionClickedGUIManager<T> interact);

    public abstract void reset(NewInteractionClickedGUIManager<T> interact);

    public void nextLang(NewInteractionClickedGUIManager<T> interact){
        GeneralConfig.getInstance().nextLocale();
        interact.gui.fullReloadAndReopen(interact.player);
    }

    public void previousLang(NewInteractionClickedGUIManager<T> interact){
        GeneralConfig.getInstance().previousLocale();
        interact.gui.fullReloadAndReopen(interact.player);
    }

    public abstract void back(NewInteractionClickedGUIManager<T> interact);

    public abstract void nextPage(NewInteractionClickedGUIManager<T> interact);

    public abstract void previousPage(NewInteractionClickedGUIManager<T> interact);

    public abstract void save(NewInteractionClickedGUIManager<T> interact);

    public void remove(NewInteractionClickedGUIManager<T> interact){

    }

    @SuppressWarnings("deprecation")
    public void showCalculationGUI(Player p, String variable, String current) {
        p.sendMessage(StringConverter.coloredString("&8➤ &7&oReplace {number} by the number of your choice !"));
        TextComponent message = new TextComponent(StringConverter.coloredString("&8➤ &7Choose an option: "));

        TextComponent edit = new TextComponent(StringConverter.coloredString("&e&l[EDIT]"));
        edit.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, current));
        edit.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&eClick here to edit the current condition")).create()));

        message.addExtra(edit);
        message.addExtra(new TextComponent(StringConverter.coloredString(" &7Or create new condition: ")));

        TextComponent inf = new TextComponent(StringConverter.coloredString("&a&l[<]"));
        inf.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "<{number}"));
        inf.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&aClick to add condition " + variable + " < {number}")).create()));

        TextComponent infE = new TextComponent(StringConverter.coloredString("&a&l[<=]"));
        infE.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "<={number}"));
        infE.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&aClick to add condition " + variable + " <= {number}")).create()));

        TextComponent egal = new TextComponent(StringConverter.coloredString("&a&l[==]"));
        egal.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "=={number}"));
        egal.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&aClick to add condition " + variable + " == {number}")).create()));

        TextComponent sup = new TextComponent(StringConverter.coloredString("&a&l[>]"));
        sup.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ">{number}"));
        sup.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&aClick to add condition " + variable + " > {number}")).create()));

        TextComponent supE = new TextComponent(StringConverter.coloredString("&a&l[>=]"));
        supE.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ">={number}"));
        supE.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&aClick to add condition " + variable + " >= {number}")).create()));

        TextComponent noC = new TextComponent(StringConverter.coloredString("&c&l[NO CONDITION]"));
        noC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "exit with delete"));
        noC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&aClick to don't set condition")).create()));

        message.addExtra(inf);
        message.addExtra(new TextComponent(" "));
        message.addExtra(infE);
        message.addExtra(new TextComponent(" "));
        message.addExtra(egal);
        message.addExtra(new TextComponent(" "));
        message.addExtra(sup);
        message.addExtra(new TextComponent(" "));
        message.addExtra(supE);
        message.addExtra(new TextComponent(" "));
        message.addExtra(noC);

        p.spigot().sendMessage(message);
    }

    public void deleteLine(String message, Player p) {
        space(p);
        space(p);
        int line = Integer.parseInt(message.split("delete line <")[1].split(">")[0]);
        deleteLine(p, line);
        p.sendMessage(StringConverter.coloredString("&a&l>> &2&lEDITION &aYou have delete the line: " + line + " !"));
        space(p);
        space(p);
    }

    public void deleteLine(Player p, int nb) {
        if (currentWriting.containsKey(p)) {
            currentWriting.get(p).remove(nb);
        }
    }

    public void editLine(Player p, int nb, String edition) {

        if (currentWriting.containsKey(p)) {
            if (nb >= currentWriting.get(p).size()) {
                currentWriting.get(p).add(edition);
            } else currentWriting.get(p).set(nb, edition);
        }
    }


    public void space(Player p) {
        p.sendMessage("");
    }

    public String getStringBeforeEnd(String insert) {
        StringBuilder sb = new StringBuilder();
        for (char c : insert.toCharArray()) {
            if (c == ',' || c == '}') return sb.toString();
            else sb.append(c);
        }
        return "";
    }

    public void sendEditor(@NotNull Player p, String tips) {
        this.sendSuggestions(p, tips);
    }

    public void sendSuggestions(Player p, String tips) {
        space(p);
        if(moreInfo != null) {
            for (TextComponent tc : moreInfo) {
                p.spigot().sendMessage(tc);
            }
        }
        if (suggestions.get(p).size() > 0) {
            p.sendMessage(StringConverter.coloredString("&5&o>> &dChoose a suggestion below:"));
            space(p);
        }

        List<TextComponent> listCommands = new ArrayList<>();

        int y = suggestionPage.get(p) * suggestionsPerColumn;
        while (y < (suggestionPage.get(p) + 1) * suggestionsPerColumn && suggestions.get(p).size() - 1 >= y) {

            Suggestion suggestion = suggestions.get(p).get(y);
            TextComponent cmd1Cpnt = new TextComponent("");
            TextComponent cmd1WikiCpnt = new TextComponent("");
            TextComponent cmd2Cpnt = new TextComponent("");
            TextComponent cmd2WikiCpnt = new TextComponent("");


            String commandStr1 = StringConverter.coloredString(suggestion.getSuggestionDisplay());
            String commandStr2 = " ";

            cmd1Cpnt.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion.getSuggestion()));
            cmd1Cpnt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString(suggestion.getSuggestionHover())).create()));

            if(suggestion.getWikiLink() != null){
                cmd1WikiCpnt.setText(StringConverter.coloredString("&e&o[Wiki]"));
                cmd1WikiCpnt.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, suggestion.getWikiLink()));
                cmd1WikiCpnt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&7&oClick here to see the wiki")).create()));
            }

            StringBuilder before = new StringBuilder();
            StringBuilder after = new StringBuilder();

            if (y + 1 != suggestions.get(p).size()) {

                Suggestion suggestion2 = suggestions.get(p).get(y + 1);

                if(suggestion2.getWikiLink() != null){
                    cmd2WikiCpnt.setText(StringConverter.coloredString("&e&o[Wiki]"));
                    cmd2WikiCpnt.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, suggestion2.getWikiLink()));
                    cmd2WikiCpnt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&7&oClick here to see the wiki")).create()));
                }

                commandStr2 = StringConverter.coloredString(suggestion2.getSuggestionDisplay());

                /* Try to improve a bit the display in clean column */
                int charDiff = commandStr1.length() - commandStr2.length();
                while (charDiff != 0) {
                    if (charDiff > 0) {
                        after.append(" ");
                        charDiff--;
                    } else {
                        before.append(" ");
                        charDiff++;
                    }
                }

                cmd2Cpnt.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion2.getSuggestion()));
                cmd2Cpnt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString(suggestion2.getSuggestionHover())).create()));

                y = y + 2;
            } else {
                y++;
            }
            String commandsStr = before+cmd1WikiCpnt.getText()+" |"+commandStr1 + "     |     " + commandStr2+"| "+cmd2WikiCpnt.getText()+after;
            commandsStr = CenteredMessage.convertIntoCenteredMessage(commandsStr);

            cmd1WikiCpnt.setText(commandsStr.split("\\|")[0]);
            cmd1Cpnt.setText(commandsStr.split("\\|")[1]);
            cmd2Cpnt.setText(commandsStr.split("\\|")[2]);
            cmd2WikiCpnt.setText(commandsStr.split("\\|")[3]);
            cmd1WikiCpnt.addExtra(cmd1Cpnt);
            cmd1WikiCpnt.addExtra(cmd2Cpnt);
            cmd1WikiCpnt.addExtra(cmd2WikiCpnt);
            //cmd1Cpnt.addExtra(cmd2Cpnt);
            listCommands.add(cmd1WikiCpnt);
        }


        for (int i = 0; i < listCommands.size(); i++) {
            p.spigot().sendMessage(listCommands.get(i));
        }

        if (!tips.isEmpty()) {
            CenteredMessage.sendCenteredMessage(p, tips);
            space(p);
        }

        String changementPage;
        boolean noSend = false;
        if (suggestionPage.get(p) == 0) {
            if (suggestions.get(p).size() > suggestionsPerColumn)
                changementPage = " | &d&lNext page &5&l>>>>>";
            else {
                changementPage = " | ";
                noSend = true;
            }
        } else if (((suggestionPage.get(p) + 1) * suggestionsPerColumn) > suggestions.get(p).size()) {
            changementPage = "&5&l<<<<< &d&lPrevious page | ";
        } else changementPage = "&5&l<<<<< &d&lPrevious page | &d&lNext page &5&l>>>>>";

        changementPage = CenteredMessage.convertIntoCenteredMessage(changementPage);

        String previousStr = changementPage.split("\\|")[0];
        String nextStr = changementPage.split("\\|")[1];

        TextComponent previousText = new TextComponent(previousStr);
        previousText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/score interact PREVIOUS PAGE"));
        previousText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&5&l<<<<< &d&lPrevious page")).create()));

        TextComponent nextText = new TextComponent(nextStr);
        nextText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/score interact NEXT PAGE"));
        nextText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(StringConverter.coloredString("&d&lNext page &5&l>>>>>")).create()));

        previousText.addExtra(nextText);

        if (!noSend) p.spigot().sendMessage(previousText);

        space(p);
    }
}
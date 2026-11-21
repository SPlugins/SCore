package com.ssomar.score.hub;

import com.ssomar.score.utils.strings.StringConverter;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.ssomar.score.SCore;
import io.papermc.paper.registry.data.dialog.type.MultiActionType;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import com.ssomar.score.ai.AiAnswer;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Paper 1.21.6+ dialogs for the Hub links: a small native window with one button ("Open in Browser" / "Copy to
 * Clipboard", translated by the client), so a link is one click away instead of a chat line. Only loaded through
 * {@link HubLinks}, which falls back to the chat when this class can't be used.
 */
final class HubDialogs {

    private static Boolean available;

    private HubDialogs() {
    }

    static boolean available() {
        if (available == null) {
            try {
                Class.forName("io.papermc.paper.dialog.Dialog");
                Class.forName("io.papermc.paper.registry.data.dialog.type.DialogType");
                Player.class.getMethod("showDialog", Class.forName("net.kyori.adventure.dialog.DialogLike"));
                available = true;
            } catch (Throwable t) {
                available = false;
            }
        }
        return available;
    }

    private static Component legacy(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(StringConverter.coloredString(s));
    }

    /**
     * The window closes after its button, so the client's link confirmation leads back to the game, where a
     * FLOATING / PINNED Hub is still open. Its exit button ("Back") runs {@code back}, which shows the Hub again
     * as it was when it is closed (chest mode).
     */
    private static void show(Player p, String title, Component body, ActionButton button, Runnable back) {
        ActionButton exit = ActionButton.builder(Component.translatable("gui.back")).width(200)
                .action(DialogAction.customClick((response, audience) -> SCore.schedulerHook.runEntityTask(() -> {
                    try {
                        p.closeDialog();
                    } catch (Throwable ignored) {
                        // very old dialog API: the client keeps the window until Escape
                    }
                    if (back != null) back.run();
                }, null, p, 1L), ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(30)).build())).build();
        DialogBase base = DialogBase.builder(legacy(title))
                .canCloseWithEscape(true)
                .pause(false)
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(body == null ? Collections.emptyList() : Collections.singletonList(DialogBody.plainMessage(body, 300)))
                .build();
        MultiActionType type = DialogType.multiAction(Collections.singletonList(button)).exitAction(exit).columns(1).build();
        Dialog dialog = Dialog.create(f -> f.empty().base(base).type(type));
        p.showDialog(dialog);
    }

    /** "Open in Browser" (the client still asks its usual link confirmation if the player turned it on) */
    static void openUrl(Player p, String title, String url, Runnable back) {
        ActionButton b = ActionButton.builder(Component.translatable("chat.link.open")).width(200)
                .action(DialogAction.staticAction(ClickEvent.openUrl(url))).build();
        // plain text: "&utm_..." must not be read as a colour code
        show(p, title, Component.text(url, net.kyori.adventure.text.format.NamedTextColor.GRAY), b, back);
    }

    /** "Copy to Clipboard" */
    static void copy(Player p, String title, String body, String toCopy, Runnable back) {
        ActionButton b = ActionButton.builder(Component.translatable("chat.copy")).width(200)
                .action(DialogAction.staticAction(ClickEvent.copyToClipboard(toCopy))).build();
        show(p, title, body == null || body.isEmpty() ? null : legacy(body), b, back);
    }

    /* ------------------------------------------------------------------ assistant */

    private static ClickCallback.Options once() {
        return ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(30)).build();
    }

    private static ActionButton serverButton(Component label, int width, Player p, Runnable r) {
        return ActionButton.builder(label).width(width).action(DialogAction.customClick((response, audience) ->
                SCore.schedulerHook.runEntityTask(r, null, p, 1L), once())).build();
    }

    /** the link code, with a button that opens splugins.net/link with the code filled in */
    static void linkCode(Player p, String line, String url, Runnable back) {
        ActionButton open = ActionButton.builder(Component.translatable("chat.link.open")).width(200)
                .action(DialogAction.staticAction(ClickEvent.openUrl(url))).build();
        show(p, TM.g(Text.AI_DIALOG_TITLE), legacy(line), open, back);
    }

    /** the question window: a multi-line text field, Send, Back */
    static void question(Player p, String prefill, Consumer<String> onSend, Runnable back) {
        TextDialogInput input = DialogInput.text("q", legacy(TM.g(Text.AI_ASK))).width(300).maxLength(4000)
                .initial(prefill == null ? "" : (prefill.length() > 4000 ? prefill.substring(0, 4000) : prefill))
                .multiline(TextDialogInput.MultilineOptions.create(12, 120)).build();
        ActionButton send = ActionButton.builder(legacy(TM.g(Text.AI_SEND))).width(150)
                .action(DialogAction.customClick((response, audience) -> {
                    String text = response.getText("q");
                    SCore.schedulerHook.runEntityTask(() -> onSend.accept(text), null, p, 1L);
                }, once())).build();
        ActionButton exit = serverButton(Component.translatable("gui.back"), 150, p, back == null ? () -> { } : back);
        DialogBase base = DialogBase.builder(legacy(TM.g(Text.AI_DIALOG_TITLE)))
                .canCloseWithEscape(true).pause(false).afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(Collections.singletonList(DialogBody.plainMessage(legacy(TM.g(p.hasPermission(com.ssomar.score.ai.AiAssistant.WRITE_PERMISSION) ? Text.AI_DIALOG_CONTEXT_WRITE : Text.AI_DIALOG_CONTEXT)), 300)))
                .inputs(Collections.singletonList(input)).build();
        MultiActionType type = DialogType.multiAction(Collections.singletonList(send)).exitAction(exit).columns(1).build();
        p.showDialog(Dialog.create(f -> f.empty().base(base).type(type)));
    }

    /** the answer: paragraphs, configs (preview + Copy), links (Open), Reply, Back */
    static void answer(Player p, AiAnswer a, Runnable reply, Runnable back) {
        answer(p, Collections.<com.ssomar.score.ai.AiHistory.Exchange>emptyList(), null, a, reply, back);
    }

    /**
     * The conversation shown again: the earlier exchanges in short (question + start of the answer), then the
     * last question and its whole answer with its buttons.
     */
    static void answer(Player p, List<com.ssomar.score.ai.AiHistory.Exchange> earlier, String lastQuestion, AiAnswer a, Runnable reply, Runnable back) {
        List<DialogBody> body = new ArrayList<>();
        net.kyori.adventure.text.format.TextColor you = net.kyori.adventure.text.format.NamedTextColor.AQUA;
        net.kyori.adventure.text.format.TextColor old = net.kyori.adventure.text.format.NamedTextColor.GRAY;
        for (com.ssomar.score.ai.AiHistory.Exchange e : earlier.subList(Math.max(0, earlier.size() - 4), earlier.size())) {
            body.add(DialogBody.plainMessage(Component.text("» " + cut(e.question, 200), you), 400));
            AiAnswer pa = AiAnswer.parse(e.answer);
            if (!pa.paragraphs.isEmpty()) body.add(DialogBody.plainMessage(Component.text(cut(pa.paragraphs.get(0), 300), old), 400));
        }
        if (lastQuestion != null && !lastQuestion.isEmpty()) body.add(DialogBody.plainMessage(Component.text("» " + cut(lastQuestion, 400), you), 400));
        int n = 0;
        for (String para : a.paragraphs) {
            if (body.size() >= 48) break;
            body.add(DialogBody.plainMessage(Component.text(para), 400));
        }
        List<ActionButton> buttons = new ArrayList<>();
        for (AiAnswer.Code c : a.codes) {
            n++;
            buttons.add(ActionButton.builder(legacy(TM.g(Text.AI_COPY).replace("%n%", "" + n))).width(150)
                    .action(DialogAction.staticAction(ClickEvent.copyToClipboard(c.text))).build());
        }
        int links = 0;
        for (AiAnswer.Link l : a.links) {
            if (++links > 4) break;
            buttons.add(ActionButton.builder(Component.text("➜ " + l.label)).width(150)
                    .tooltip(Component.text(l.url)).action(DialogAction.staticAction(ClickEvent.openUrl(l.url))).build());
        }
        buttons.add(serverButton(legacy(TM.g(Text.AI_REPLY)), 150, p, reply));
        ActionButton exit = serverButton(Component.translatable("gui.back"), 150, p, back == null ? () -> { } : back);
        DialogBase base = DialogBase.builder(legacy(TM.g(Text.AI_DIALOG_TITLE)))
                .canCloseWithEscape(true).pause(false).afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(body).build();
        MultiActionType type = DialogType.multiAction(buttons).exitAction(exit).columns(2).build();
        p.showDialog(Dialog.create(f -> f.empty().base(base).type(type)));
    }

    /** a change proposed by the assistant: target, summary, diff, Apply / Refuse (Escape = Refuse) */
    static void change(Player p, com.ssomar.score.ai.AiChange c, com.ssomar.score.ai.LineDiff d, Runnable apply, Runnable refuse) {
        List<DialogBody> body = new ArrayList<>();
        String head = c.isCreation() ? TM.g(Text.AI_CHANGE_NEW).replace("%target%", c.getTarget())
                : TM.g(Text.AI_CHANGE_EDIT).replace("%target%", c.getTarget()).replace("%added%", "" + d.added).replace("%removed%", "" + d.removed);
        body.add(DialogBody.plainMessage(legacy(head), 400));
        if (!c.getSummary().isEmpty()) body.add(DialogBody.plainMessage(Component.text(c.getSummary()), 400));
        Component block = Component.empty();
        int n = 0;
        for (com.ssomar.score.ai.LineDiff.Line l : d.lines) {
            Component line;
            switch (l.kind) {
                case '+':
                    line = Component.text("+ " + l.text, net.kyori.adventure.text.format.NamedTextColor.GREEN);
                    break;
                case '-':
                    line = Component.text("- " + l.text, net.kyori.adventure.text.format.NamedTextColor.RED);
                    break;
                case '…':
                    line = Component.text("  … " + l.text, net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY);
                    break;
                default:
                    line = Component.text("  " + l.text, net.kyori.adventure.text.format.NamedTextColor.GRAY);
            }
            block = n == 0 ? line : block.append(Component.newline()).append(line);
            if (++n == 20) {
                body.add(DialogBody.plainMessage(block, 400));
                block = Component.empty();
                n = 0;
            }
        }
        if (n > 0) body.add(DialogBody.plainMessage(block, 400));
        body.add(DialogBody.plainMessage(legacy(TM.g(Text.AI_CHANGE_NOTE)), 400));
        ActionButton yes = serverButton(legacy(TM.g(Text.AI_APPLY)), 150, p, apply);
        ActionButton no = serverButton(legacy(TM.g(Text.AI_REFUSE)), 150, p, refuse);
        DialogBase base = DialogBase.builder(legacy(TM.g(Text.AI_CHANGE_TITLE)))
                .canCloseWithEscape(true).pause(false).afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(body).build();
        MultiActionType type = DialogType.multiAction(Collections.singletonList(yes)).exitAction(no).columns(1).build();
        p.showDialog(Dialog.create(f -> f.empty().base(base).type(type)));
    }

    private static String cut(String s, int max) {
        String t = s == null ? "" : s.replace('\n', ' ').trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}

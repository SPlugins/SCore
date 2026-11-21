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

import java.time.Duration;
import java.util.Collections;

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
     * The window stays open after its button (the client's link confirmation comes back to it), and its exit button
     * ("Back") closes it from the server (after_action NONE also applies to it) and runs {@code back}, which shows
     * the Hub again as it was. Escape just closes it.
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
                .afterAction(DialogBase.DialogAfterAction.NONE)
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
}

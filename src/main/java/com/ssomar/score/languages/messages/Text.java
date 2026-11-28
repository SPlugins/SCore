package com.ssomar.score.languages.messages;

public enum Text implements TextInterface {

    /* commands */
    EDITOR_BACK_NAME("editor.back.name","&4&l⬅ &cBack"),
    EDITOR_CREATION_ID_NAME("editor.creation.id.name", "&2&l✚ &a&lCreation ID:"),
    EDITOR_EDIT_DESCRIPTION("editor.edit.description", "&a✎ Click here to change"),
    EDITOR_EXIT_NAME("editor.exit.name", "&4&l✘ &cExit"),
    EDITOR_FOLDER_NAME("editor.folder.name", "&2&l✦ FOLDER: &a"),
    EDITOR_FOLDER_DESCRIPTION("editor.folder.description", new String[]{"", "&7Click here to open the folder"}),
    EDITOR_DELETE_MESSAGE("editor.delete.message", "&cHey if you want to delete the %object%"),
    EDITOR_DELETE_NAME("editor.delete.name", "&4&l✘ &cDelete"),
    EDITOR_DELETE_NORMAL_DESCRIPTION("editor.delete.normal.description", "&4&lX &cClick here to delete"),
    EDITOR_DELETE_SHIFT_DESCRIPTION("editor.delete.shift.description", "&4&lX &cShift + Click to delete"),
    EDITOR_DELETE_SHIFT_LEFT_DESCRIPTION("editor.delete.shift.left.description", "&4&lX &cShift + Left Click to delete"),
    EDITOR_GIVE_SHIFT_RIGHT_DESCRIPTION("editor.give.shift.right.description", "&2✔ &aShift + right click to give to yourself"),
    EDITOR_GIVE_RECEIVEDMESSAGE("editor.give.receivedMessage", "&aYou received &e"),
    EDITOR_INVALID_FILE_NAME("editor.invalid.file.name", "&4&l✦ INVALID FILE: &c&o"),
    EDITOR_INVALID_FILE_DESCRIPTION("editor.invalid.file.description", "&7The plugin can only load &e.yml &7files"),

    EDITOR_INVALID_CONFIGURATION_NAME("editor.invalid.configuration.name", "&4&l✦ INVALID CONFIGURATION: &c&o"),
    EDITOR_INVALID_CONFIGURATION_DESCRIPTION("editor.invalid.configuration.description", "&7You should edit your configuration file"),
    EDITOR_INVALID_CONFIGURATION_FREELIMIT("editor.invalid.configuration.freeLimit", "&7Also be sure that you haven't reached the limit of the free version"),
    EDITOR_NEW_NAME("editor.new.name", "&a&l➕ &aNew"),
    EDITOR_PAGE_NAME("editor.page.name", " - Page "),
    EDITOR_PAGE_NEXT_NAME("editor.page.next.name", "&dNext page &5&l▶"),
    EDITOR_PAGE_PREVIOUS_NAME("editor.page.previous.name", "&5&l◀ &dPrevious page"),
    EDITOR_PATH_NAME("editor.path.name", "&6&l✦ &ePATH:"),
    EDITOR_PATH_DESCRIPTION("editor.path.description", new String[]{"&8&oClick here to come back", "&8&oin previous folder"}),
    EDITOR_PREMADE_PREMIUM_NAME("editor.premade.premium.name", "&5&l✚ &dDefault Premium %object%"),
    EDITOR_PREMADE_PACKS_NAME("editor.premade.packs.name", "&5&l✚ &d%object% from Custom packs"),
    EDITOR_PREMIUM_DESCRIPTION("editor.premium.description", "&5&l❂ &d&lPremium only &5&l❂"),
    EDITOR_PREMIUM_REQUIREPREMIUMTOADDMORE("editor.premium.requirePremiumToAddMore", "&cREQUIRE PREMIUM: to add more than %limit% %object% you need the premium version"),
    EDITOR_RESET_NAME("editor.reset.name", "&4&l✘ &cReset"),
    EDITOR_RESET_DESCRIPTION("editor.reset.description", new String[]{"", "&c&oClick here to reset" ,"&c&oall options of this creation"}),
    EDITOR_SAVE_NAME("editor.save.name", "&a&l✔ &aSave"),
    EDITOR_SAVE_DESCRIPTION("editor.save.description", new String[]{"", "&a&oClick here to save", "&a&oyour configuration !"}),
    EDITOR_TITLE_COLOR("editor.title.color", "&e&l"),
    EDITOR_CURRENTLY_NAME("editor.currently.name", "&7✦ Currently:"),

    /* SPlugins Hub */
    HUB_TITLE("hub.title", "&8SPlugins Hub"),
    HUB_TAB_HOME("hub.tab.home", "&fHome"),
    HUB_TAB_MANAGE("hub.tab.manage", "&fManage"),
    HUB_TAB_HELP("hub.tab.help", "&fHelp"),
    HUB_TAB_DISCOVER("hub.tab.discover", "&fDiscover"),
    HUB_TAB_NEWS("hub.tab.news", "&fNews"),
    HUB_TAB_LINKS("hub.tab.links", "&fLinks"),
    HUB_TAB_SUPPORT("hub.tab.support", "&fSupport us"),
    HUB_TAB_NEW_BADGE("hub.tab.newBadge", " &a●"),
    HUB_CLOSE("hub.close", "&c✘ Close"),
    HUB_BACK("hub.back", "&7⬅ Back"),
    HUB_NEXT("hub.next", "&7Next ▶"),
    HUB_PREVIOUS("hub.previous", "&7◀ Previous"),
    HUB_PAGE("hub.page", "&7Page %page%/%max%"),
    HUB_HINT("hub.hint", "&8Look at an element to see its details"),
    HUB_CATALOG_SOURCE("hub.catalog.source", "&8Catalog: %source%"),
    HUB_HOME_VERSION("hub.home.version", "&7Version: &f%version%"),
    HUB_HOME_PREMIUM("hub.home.premium", "&dPremium"),
    HUB_HOME_FREE("hub.home.free", "&aFree"),
    HUB_HOME_DISABLED("hub.home.disabled", "&c✘ Disabled"),
    HUB_HOME_OBJECTS("hub.home.objects", "&7Loaded: &f%count%"),
    HUB_HOME_FREE_LIMIT("hub.home.freeLimit", "&7Free limit: &f%count%/%limit%"),
    HUB_HOME_UP_TO_DATE("hub.home.upToDate", "&a✔ Up to date"),
    HUB_HOME_UPDATE("hub.home.update", "&e⬆ Update available: &f%version%"),
    HUB_HOME_UPDATE_UNKNOWN("hub.home.updateUnknown", "&8Latest version unknown"),
    HUB_HOME_ERRORS("hub.home.errors", "&c⚠ %count% loading error(s)"),
    HUB_HOME_NO_ERROR("hub.home.noError", "&a✔ No loading error"),
    HUB_HOME_CLICK_MANAGE("hub.home.clickManage", "&eClick to manage"),
    HUB_HOME_UPGRADE("hub.home.upgrade", "&d★ Upgrade %plugin% to Premium"),
    HUB_HOME_UPGRADE_LORE("hub.home.upgradeLore", "&7No limit and every premium feature"),
    HUB_MANAGE_CHOOSE("hub.manage.choose", "&7Choose a plugin to manage"),
    HUB_MANAGE_RELOAD("hub.manage.reload", "&aReload"),
    HUB_MANAGE_EDITOR("hub.manage.editor", "&bOpen the editor"),
    HUB_MANAGE_WIKI("hub.manage.wiki", "&eWiki page"),
    HUB_MANAGE_CHANGELOG("hub.manage.changelog", "&6Latest changes"),
    HUB_MANAGE_ERRORS("hub.manage.errors", "&cShow the loading errors"),
    HUB_MANAGE_NO_ERRORS("hub.manage.noErrors", "&aNo loading error recorded since the start"),
    HUB_CONFIRM("hub.confirm", "&cClick again to confirm"),
    HUB_HELP_WIKI("hub.help.wiki", "&eWiki of %plugin%"),
    HUB_HELP_DISCORD("hub.help.discord", "&9Support Discord"),
    HUB_HELP_DIAGNOSTIC("hub.help.diagnostic", "&bDiagnostic report"),
    HUB_HELP_DIAGNOSTIC_LORE("hub.help.diagnosticLore", new String[]{"&7Prepares a report to paste", "&7in your support ticket"}),
    HUB_HELP_DIAGNOSTIC_READY("hub.help.diagnosticReady", "&a✔ Report ready. &e&nClick here to copy it&r&a, then paste it in your ticket."),
    HUB_HELP_DIAGNOSTIC_CONSOLE("hub.help.diagnosticConsole", "&aThe report was printed in the console."),
    HUB_DISCOVER_EMPTY("hub.discover.empty", "&7You already have every SPlugins plugin!"),
    HUB_DISCOVER_PLUGIN("hub.discover.plugin", "&7Plugin"),
    HUB_DISCOVER_PACK("hub.discover.pack", "&7Pack"),
    HUB_DISCOVER_BUNDLE("hub.discover.bundle", "&6Bundle: &f%items%"),
    HUB_DISCOVER_COMMUNITY("hub.discover.community", "&7Made by the community"),
    HUB_DISCOVER_UPGRADE("hub.discover.upgrade", "&eYou use the Free edition: Premium removes the limits"),
    HUB_DISCOVER_STUDIO("hub.discover.studio", "&d★ Pack Studio"),
    HUB_DISCOVER_SUB("hub.discover.sub", "&b&l★ SPlugins Subscription"),
    HUB_DISCOVER_SUB_PLUGINS("hub.discover.subPlugins", "&7Premium plugins included"),
    HUB_DISCOVER_SUB_PACKS("hub.discover.subPacks", "&7A new item pack every week"),
    HUB_DISCOVER_SUB_CHAT("hub.discover.subChat", "&7Premium AI chat and the Subscriber role on Discord"),
    HUB_DISCOVER_STUDIO_LORE("hub.discover.studioLore", "&7Order a custom pack made for your server"),
    HUB_DISCOVER_PRICE("hub.discover.price", "&7Price: &f%price%"),
    HUB_NEWS_EMPTY("hub.news.empty", "&7No news right now"),
    HUB_NEWS_PROMO("hub.news.promo", "&6Promotion"),
    HUB_NEWS_UNTIL("hub.news.until", "&7Until %date%"),
    HUB_CLICK_LINK("hub.clickLink", "&eClick to get the link"),
    HUB_LINK_MESSAGE("hub.linkMessage", "&7➜ &b&n%label%&r &7(click)"),
    HUB_SUPPORT_REVIEW("hub.support.review", "&e★ Leave a review on %platform%"),
    HUB_SUPPORT_REVIEW_LORE("hub.support.reviewLore", "&7A review takes 1 minute and helps us a lot"),
    HUB_SUPPORT_SUGGEST("hub.support.suggest", "&bSuggest a feature"),
    HUB_SUPPORT_DONATE("hub.support.donate", "&dDonate"),
    HUB_JOIN("hub.join", "&7[&bSPlugins Hub&7] Dashboard, help and news: &e&n/score hub"),
    HUB_REVIEW_REMINDER("hub.reviewReminder", "&7[&bSPlugins&7] Enjoying our plugins? A review helps a lot: &e&n/score hub"),
    HUB_MODE("hub.mode", "&8Shown as a chest menu (%reason%)"),
    HUB_CURSOR_TITLE("hub.cursorTitle", "&fCan you see the arrow cursor?"),
    HUB_CURSOR_HINT("hub.cursorHint", "&7Turn your head to move it onto &aYes&7, then click"),
    HUB_CURSOR_YES("hub.cursorYes", "&a✔ Yes"),
    HUB_CURSOR_NO("hub.cursorNo", "&c✘ No"),
    HUB_SNEAK_HINT("hub.sneakHint", "&8Sneak to close"),
    HUB_PAUSE_BODY("hub.pauseBody", "Dashboard, help and news of your SPlugins plugins (admins only)"),
    HUB_PAUSE_BUTTON("hub.pauseButton", "Open the SPlugins Hub"),
    HUB_PAUSE_DENIED("hub.pauseDenied", "&cThe SPlugins Hub is for the server admins (permission score.hub)"),
    HUB_TAB_ASSISTANT("ai.tab", "&dAssistant"),
    AI_INTRO("ai.intro", "&fThe SPlugins assistant answers your questions and reads your configurations to help you"),
    AI_OFF("ai.disabled", "&7Off: set &eai.enabled: true&7 in SCore's config.yml"),
    AI_LINK("ai.link", "&a&lLink this server to your splugins.net account"),
    AI_LINK_LORE("ai.linkLore", "&7Once per server. Questions use your splugins.net plan"),
    AI_LINK_WAIT("ai.linkWait", "&eWaiting for the code to be entered on splugins.net..."),
    AI_LINK_CODE("ai.linkCode", "&7Go to &b&n%url%&7 and enter the code &e&l%code%"),
    AI_LINKED("ai.linked", "&aServer linked to &f%account%&a: the assistant is ready"),
    AI_LINKED_ROW("ai.linkedRow", "&7Linked to &f%account%"),
    AI_ASK("ai.ask", "&b&lAsk a question"),
    AI_ASK_LORE("ai.askLore", "&7Your plugin versions and load errors are attached"),
    AI_NEW("ai.new", "&eNew conversation"),
    AI_ERRORS("ai.errors", "&6Ask the assistant about the load errors (%count%)"),
    AI_ERRORS_QUESTION("ai.errorsQuestion", "Here are the load errors of %plugin% on my server. Explain each one and give me the fixed configuration, checked with validate_object."),
    AI_ERRORS_QUESTION_ALL("ai.errorsQuestionAll", "Here are the load errors of my SPlugins plugins. Explain each one and give me the fixed configurations, checked with validate_object."),
    AI_UNLINK("ai.unlink", "&cUnlink this server"),
    AI_THINKING("ai.thinking", "&7The assistant is thinking..."),
    AI_ERROR("ai.error", "&cThe assistant is unavailable: %error%"),
    AI_QUOTA("ai.quota", "&eYou reached the message limit of your splugins.net plan."),
    AI_UPGRADE("ai.upgrade", "Increase my limit"),
    AI_TYPE_QUESTION("ai.typeQuestion", "&7Type your question in the chat (&ecancel&7 to stop)"),
    AI_DIALOG_TITLE("ai.dialogTitle", "SPlugins Assistant"),
    AI_DIALOG_CONTEXT("ai.dialogContext", "Your plugin versions and load errors are sent with the question. The assistant can read your SPlugins configurations; it can't change them."),
    AI_SEND("ai.send", "Send"),
    AI_REPLY("ai.reply", "Reply"),
    AI_COPY("ai.copy", "Copy config %n%"),
    AI_DIALOG_CONTEXT_WRITE("ai.dialogContextWrite", "Your plugin versions and load errors are sent with the question. The assistant can read your SPlugins configurations and propose changes: nothing is changed without your confirmation, and every change can be undone."),
    AI_CHANGE_TITLE("ai.changeTitle", "Change proposed by the assistant"),
    AI_CHANGE_NEW("ai.changeNew", "&aNew file: &f%target%"),
    AI_CHANGE_EDIT("ai.changeEdit", "&eChanged file: &f%target% &7(&a+%added% &c-%removed%&7)"),
    AI_CHANGE_NOTE("ai.changeNote", "&7Checked with the plugin's loader. A backup is kept: you can undo it from the Hub."),
    AI_APPLY("ai.apply", "&aApply"),
    AI_REFUSE("ai.refuse", "&cRefuse"),
    AI_APPLIED("ai.applied", "&a✔ Applied: &f%target%&a. Undo it from the Hub (Assistant) or with &e/score hub ai undo"),
    AI_REFUSED("ai.refused", "&7Change refused: nothing was modified"),
    AI_EXPIRED("ai.expired", "&7This change expired: nothing was modified"),
    AI_UNDO("ai.undo", "&6↶ Undo the last change: &f%target%"),
    AI_UNDONE("ai.undone", "&a↶ Restored: &f%target%"),
    AI_UNDO_NONE("ai.undoNone", "&7No change to undo"),
    AI_NO_WRITE("ai.noWrite", "&cYou need the permission &escore.ai.write&c to change the configurations"),
    AI_RESUME("ai.resume", "&b&l↩ Resume the conversation"),
    AI_RESUME_LORE("ai.resumeLore", "&7Last question: &f%question%"),
    AI_REPLY_BUTTON("ai.replyButton", "&b&l✎ Reply"),
    AI_CHANGES_TITLE("ai.changesTitle", "%n% changes proposed by the assistant"),
    AI_APPLY_N("ai.applyN", "Apply #%n% (%target%)"),
    AI_COMMENT_N("ai.commentN", "Comment on #%n% for the assistant (optional)"),
    AI_COMMENT("ai.comment", "Comment for the assistant (optional)"),
    AI_SUBMIT("ai.submit", "&aValidate"),
    AI_REFUSE_ALL("ai.refuseAll", "&cRefuse all"),
    AI_TICKET_OPENED("ai.ticketOpened", "&a✔ Ticket opened: &f%target%"),
    AI_NEW_LORE("ai.newLore", "&7The current conversation stays in the history"),
    AI_HISTORY("ai.history", "&e☰ Conversation history (%count%)"),
    AI_HISTORY_LORE("ai.historyLore", "&7Resume an earlier conversation"),
    AI_HISTORY_ROW("ai.historyRow", "&7%date% · %count% question(s)"),
    AI_HISTORY_CURRENT("ai.historyCurrent", " &a(current)");


    private String key;

    private String defaultValueString;

    private String[] defaultValueStringArray;

    private TypeText type;

    Text(String key, String defaultValue) {
        this.key = key;
        this.defaultValueString = defaultValue;
        this.type = TypeText.STRING;
    }

    Text(String key, String[] defaultValue) {
        this.key = key;
        this.defaultValueStringArray = defaultValue;
        this.type = TypeText.ARRAY;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultValueString() {
        return defaultValueString;
    }

    @Override
    public String[] getDefaultValueArray() {
        return defaultValueStringArray;
    }

    @Override
    public TypeText getType() {
        return type;
    }

}

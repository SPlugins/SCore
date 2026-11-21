package com.ssomar.score.ai;

/**
 * A change proposed by a write tool ({@link AiTool#isWrite()}): the tool returns it from {@code execute} instead of
 * changing anything. The admin who asked sees {@link #getTarget()}, the summary and the diff of the file in the
 * game, and {@link #apply} only runs (on the server thread) once they confirmed. Public API: a write tool of another
 * plugin returns one of these too.
 */
public final class AiChange {

    public interface Apply {
        /** @param actor name of the admin who confirmed; @return the result given back to the model */
        Object run(String actor) throws Exception;
    }

    private final String target, summary, before, after;
    private final Apply apply;
    private boolean plainText, offThread;

    /**
     * @param target  what changes, for the admin, e.g. "ExecutableItems › item › fire_sword"
     * @param summary one sentence of the model: what the change does
     * @param before  the current file, or null when the change creates it
     * @param after   the file after the change
     */
    public AiChange(String target, String summary, String before, String after, Apply apply) {
        this.target = target;
        this.summary = summary == null ? "" : summary;
        this.before = before;
        this.after = after == null ? "" : after;
        this.apply = apply;
    }

    /**
     * A proposal that is not a file change (a ticket, a message...): the form shows {@code text} as it is instead of
     * a diff.
     */
    public static AiChange text(String target, String summary, String text, Apply apply) {
        AiChange c = new AiChange(target, summary, null, text, apply);
        c.plainText = true;
        return c;
    }

    /** apply runs on the assistant's thread, not the server thread (network calls: a ticket, for example) */
    public AiChange offThread() {
        this.offThread = true;
        return this;
    }

    public boolean isOffThread() {
        return offThread;
    }

    public boolean isPlainText() {
        return plainText;
    }

    public String getTarget() {
        return target;
    }

    public String getSummary() {
        return summary;
    }

    public String getBefore() {
        return before;
    }

    public String getAfter() {
        return after;
    }

    public boolean isCreation() {
        return before == null;
    }

    public Object apply(String actor) throws Exception {
        return apply.run(actor);
    }
}

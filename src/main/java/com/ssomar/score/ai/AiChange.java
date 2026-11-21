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

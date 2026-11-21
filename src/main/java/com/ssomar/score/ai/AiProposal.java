package com.ssomar.score.ai;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Several changes proposed at once by a write tool: the admin sees them in one form, ticks the ones to apply and
 * can leave a comment on each (given back to the model). Changes the tool could not propose (invalid YAML...) are
 * reported to the model in {@link #getNotProposed()}.
 */
public final class AiProposal {

    private final List<AiChange> changes;
    private final JSONArray notProposed;

    public AiProposal(List<AiChange> changes, JSONArray notProposed) {
        this.changes = Collections.unmodifiableList(new ArrayList<>(changes));
        this.notProposed = notProposed == null ? new JSONArray() : notProposed;
    }

    public List<AiChange> getChanges() {
        return changes;
    }

    public JSONArray getNotProposed() {
        return notProposed;
    }

    /** the admin's answer for one change */
    public static final class Decision {
        public final boolean apply;
        public final String comment;

        public Decision(boolean apply, String comment) {
            this.apply = apply;
            this.comment = comment == null ? "" : comment.trim();
        }
    }
}

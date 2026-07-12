package com.ssomar.score.utils.emums;

/**
 * How a variable update is applied to the current value.
 */
public enum VariableUpdateType {

    LIST_ADD,
    LIST_REMOVE,
    LIST_CLEAR,
    MODIFICATION,
    SET;

    public VariableUpdateType getPrev() {
        VariableUpdateType opt = values()[values().length - 1];
        for (VariableUpdateType o : values()) {
            if (this.equals(o)) {
                return opt;
            } else opt = o;
        }
        return opt;
    }

    public VariableUpdateType getNext() {
        VariableUpdateType opt = values()[0];
        boolean next = false;
        for (VariableUpdateType o : values()) {
            if (next) {
                opt = o;
                break;
            }
            if (this.equals(o)) {
                next = true;
            }
        }
        return opt;
    }
}

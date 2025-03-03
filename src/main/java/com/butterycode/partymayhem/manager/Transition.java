package com.butterycode.partymayhem.manager;

public enum Transition {
    CONTINUOUS("continuous"),
    SHUFFLE("shuffle"),
    VOTE("vote");

    private final String label;

    Transition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

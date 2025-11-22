package com.butterycode.partymayhem.manager;

import org.jetbrains.annotations.Nullable;

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

    public static @Nullable Transition getByLabel(@Nullable String label) {
        // Return null if the label is null
        if (label == null) return null;

        // Find the first matching transition and return it
        for (Transition transition : Transition.values()) {
            if (transition.getLabel().equalsIgnoreCase(label)) {
                return transition;
            }
        }

        // Otherwise, return null
        return null;
    }
}

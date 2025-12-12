package com.butterycode.partymayhem.games;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public non-sealed class MinigameModule<P extends MinigameFactory> extends GameRegistry implements Listener {

    private final @NotNull P parent;

    public MinigameModule(@NotNull P parent) {
        super(parent.getId(), parent.getDisplayName());

        this.parent = parent;
    }

    protected @NotNull P getParent() {
        return parent;
    }

    @Override
    public final void cleanupSideEffects() {
        super.cleanupSideEffects();
    }

}

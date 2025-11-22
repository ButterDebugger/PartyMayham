package com.butterycode.partymayhem.settings.options;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.games.MinigameFactory;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

public abstract sealed class GameOption<T> permits Toggle, NumberRange, Selection, Text {

    private final MinigameFactory minigame;
    private final String optionKey;

    public GameOption(@NotNull MinigameFactory minigame, @Pattern("[a-z_]+") @NotNull String optionKey) {
        this.minigame = minigame;
        this.optionKey = optionKey;

        minigame.registerOption(this);
    }

    /** Sets and saves the new option value */
    public abstract void setValue(@NotNull T value);
    /** Gets the current option value */
    public abstract @NotNull T getValue();

    /** @return The data storage for the option */
    protected DataStorage getData() {
        return PartyMayhem.getData().getStorage(minigame.getId() + "/options.yml");
    }

    public MinigameFactory getMinigame() {
        return minigame;
    }
    public String getOptionKey() {
        return optionKey;
    }

}

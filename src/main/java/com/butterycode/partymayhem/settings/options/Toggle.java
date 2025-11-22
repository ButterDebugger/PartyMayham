package com.butterycode.partymayhem.settings.options;

import com.butterycode.partymayhem.games.MinigameFactory;
import org.jetbrains.annotations.NotNull;

public non-sealed class Toggle extends GameOption<Boolean> {

    private boolean value;

    public Toggle(@NotNull MinigameFactory minigame, @NotNull String optionKey, boolean defaultValue) {
        super(minigame, optionKey);

        this.value = getData().exists(getOptionKey()) ? getData().getBoolean(getOptionKey()) : defaultValue;
    }

    @Override
    public void setValue(@NotNull Boolean value) {
        this.value = value;

        // Store the new value
        getData().set(getOptionKey(), value);
    }

    @Override
    public @NotNull Boolean getValue() {
        return value;
    }

}

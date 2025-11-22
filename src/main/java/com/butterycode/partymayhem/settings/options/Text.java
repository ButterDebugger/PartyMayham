package com.butterycode.partymayhem.settings.options;

import com.butterycode.partymayhem.games.MinigameFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public non-sealed class Text extends GameOption<String> {

    private @NotNull String value;

    public Text(@NotNull MinigameFactory minigame, @NotNull String optionKey, @NotNull String defaultValue) {
        super(minigame, optionKey);

        this.value = Objects.requireNonNullElse(getData().getString(getOptionKey()), defaultValue);
    }

    @Override
    public void setValue(@NotNull String value) {
        this.value = value;

        // Store the new value
        getData().set(getOptionKey(), value);
    }

    @Override
    public @NotNull String getValue() {
        return value;
    }

}

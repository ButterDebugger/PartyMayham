package com.butterycode.partymayhem.settings.options;

import com.butterycode.partymayhem.games.MinigameFactory;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public non-sealed class Selection extends GameOption<String> {

    private String value;
    private @NotNull List<String> options;

    public Selection(
        @NotNull MinigameFactory minigame,
        @NotNull String optionKey,
        @NotNull Component displayName,
        int defaultIndex,
        @NotNull List<String> options
    ) {
        super(minigame, optionKey, displayName);

        this.options = options;
        this.value = Objects.requireNonNullElse(getData().getString(getKey()), options.get(defaultIndex));
    }

    @Override
    public void setValue(@NotNull String value) {
        this.value = value;

        // Store the new value
        getData().set(getKey(), value);
    }

    @Override
    public @NotNull String getValue() {
        return value;
    }

    public @NotNull List<String> getOptions() {
        return options;
    }
}

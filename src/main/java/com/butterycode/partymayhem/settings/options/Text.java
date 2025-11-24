package com.butterycode.partymayhem.settings.options;

import com.butterycode.partymayhem.games.MinigameFactory;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public non-sealed class Text extends GameOption<String> {

    private @NotNull String value;

    public Text(
        @NotNull MinigameFactory minigame,
        @NotNull String optionKey,
        @NotNull Component displayName,
        @NotNull String defaultValue
    ) {
        super(minigame, optionKey, displayName);

        this.value = Objects.requireNonNullElse(getData().getString(getKey()), defaultValue);
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

}

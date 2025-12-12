package com.butterycode.partymayhem.settings.options;

import com.butterycode.partymayhem.games.MinigameFactory;
import net.kyori.adventure.text.Component;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

public non-sealed class Toggle extends GameOption<Boolean> {

    private boolean value;

    public Toggle(
        @NotNull MinigameFactory minigame,
        @Pattern("[a-z_]+") @NotNull String optionKey,
        @NotNull Component displayName,
        boolean defaultValue
    ) {
        super(minigame, optionKey, displayName);

        this.value = getData().exists(getKey()) ? getData().getBoolean(getKey()) : defaultValue;
    }

    @Override
    public void setValue(@NotNull Boolean value) {
        this.value = value;

        // Store the new value
        getData().set(getKey(), value);
    }

    @Override
    public @NotNull Boolean getValue() {
        return value;
    }

}

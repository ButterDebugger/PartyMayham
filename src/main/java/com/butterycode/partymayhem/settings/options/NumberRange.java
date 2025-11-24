package com.butterycode.partymayhem.settings.options;

import com.butterycode.partymayhem.games.MinigameFactory;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public non-sealed class NumberRange extends GameOption<Float> {

    private float value;
    private final float rangeStart;
    private final float rangeEnd;
    private final float rangeStep;

    public NumberRange(
        @NotNull MinigameFactory minigame,
        @NotNull String optionKey,
        @NotNull Component displayName,
        float defaultValue,
        float rangeStart,
        float rangeEnd,
        float rangeStep
    ) {
        super(minigame, optionKey, displayName);

        this.value = getData().exists(getKey()) ? (float) getData().getDouble(getKey()) : defaultValue;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.rangeStep = rangeStep;
    }

    @Override
    public void setValue(@NotNull Float value) {
        this.value = value;

        // Store the new value
        getData().set(getKey(), value);
    }

    @Override
    public @NotNull Float getValue() {
        return value;
    }

    public float getRangeStart() {
        return rangeStart;
    }

    public float getRangeEnd() {
        return rangeEnd;
    }

    public float getRangeStep() {
        return rangeStep;
    }
}

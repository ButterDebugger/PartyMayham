package com.butterycode.partymayhem.settings.options;

import com.butterycode.partymayhem.games.MinigameFactory;
import org.jetbrains.annotations.NotNull;

public non-sealed class NumberRange extends GameOption<Float> {

    private float value;
    private final float rangeStart;
    private final float rangeEnd;
    private final float rangeStep;

    public NumberRange(
        @NotNull MinigameFactory minigame,
        @NotNull String optionKey,
        float defaultValue,
        float rangeStart,
        float rangeEnd,
        float rangeStep
    ) {
        super(minigame, optionKey);

        this.value = getData().exists(getOptionKey()) ? (float) getData().getDouble(getOptionKey()) : defaultValue;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.rangeStep = rangeStep;
    }

    @Override
    public void setValue(@NotNull Float value) {
        this.value = value;

        // Store the new value
        getData().set(getOptionKey(), value);
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

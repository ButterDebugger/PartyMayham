package com.butterycode.partymayhem.games.presets;

import com.butterycode.partymayhem.games.MinigameFactory;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.Nullable;

public class Smash extends MinigameFactory {

    private @Nullable Objective damageObjective = null;

    public Smash() {
        super("smash", Component.text("Smash"));
    }

    @Override
    public void start() {
        damageObjective = createScoreboard("damage", Component.text("Damage"));
    }

    @Override
    public void end(boolean forced) {

    }

    @Override
    protected boolean status() {
        return false;
    }

}

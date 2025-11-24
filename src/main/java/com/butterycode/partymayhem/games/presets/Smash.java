package com.butterycode.partymayhem.games.presets;

import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.GameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.Nullable;

public class Smash extends MinigameFactory {

    private @Nullable Objective damageObjective = null;

    public Smash() {
        super("smash", Component.text("Smash"));

        GameManager.registerMinigame(this);
    }

    @Override
    public void start() {
        damageObjective = createScoreboard(Component.text("Damage"));

        damageObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Score score = damageObjective.getScoreFor(player);
            score.setScore(0);
        }
    }

    @Override
    public void end(boolean forced) {

    }

    @Override
    protected boolean status() {
        return true;
    }

}

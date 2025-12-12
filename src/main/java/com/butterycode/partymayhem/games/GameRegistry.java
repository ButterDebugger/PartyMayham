package com.butterycode.partymayhem.games;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public sealed class GameRegistry permits MinigameFactory, MinigameModule {

    private final @NotNull String id;
    private final @NotNull Component displayName;
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final List<Objective> scoreboardObjectives = new ArrayList<>();
    private final List<BossBar> bossBars = new ArrayList<>();

    public GameRegistry(@NotNull String id, @NotNull Component displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    // Tasks

    protected final BukkitTask createTask(BukkitTask task) {
        tasks.add(task);
        return task;
    }

    // Boss bars

    protected final BossBar createBossBar(BossBar bossBar) {
        bossBars.add(bossBar);
        return bossBar;
    }

    // Scoreboards

    protected final Objective createScoreboard(
        @NotNull Criteria criteria,
        @Nullable Component displayName,
        @NotNull RenderType renderType
    ) {
        Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective(
            getId() + "-" + UUID.randomUUID(),
            criteria,
            displayName,
            renderType
        );
        scoreboardObjectives.add(objective);
        return objective;
    }

    protected final Objective createScoreboard(
        @Nullable Component displayName
    ) {
        return createScoreboard(Criteria.DUMMY, displayName, RenderType.INTEGER);
    }

    // Getters

    public final @NotNull String getId() {
        return id;
    }

    public final @NotNull Component getDisplayName() {
        return displayName;
    }

    // Cleanup methods

    public void cleanupSideEffects() {
        cancelAllTasks();
        removeAllBossBars();
        unregisterAllObjectives();
    }

    protected final void cancelAllTasks() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }

    protected final void removeAllBossBars() {
        for (BossBar bossBar : bossBars) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        bossBars.clear();
    }

    protected final void unregisterAllObjectives() {
        for (Objective objective : scoreboardObjectives) {
            objective.unregister();
        }
        scoreboardObjectives.clear();
    }

}

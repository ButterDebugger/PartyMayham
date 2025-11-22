package com.butterycode.partymayhem.games;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.settings.blueprint.Blueprint;
import com.butterycode.partymayhem.settings.options.GameOption;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MinigameFactory implements Listener {

    protected @NotNull String id;
    protected @NotNull Component displayName;
    protected int minPlayers = 1;
    protected boolean enabled;
    protected List<Blueprint> blueprints = new ArrayList<>();
    protected List<GameOption<?>> options = new ArrayList<>();
    protected List<BukkitTask> tasks = new ArrayList<>();
    protected List<Objective> scoreboardObjectives = new ArrayList<>();
    protected List<BossBar> bossBars = new ArrayList<>();

    protected MinigameFactory(@NotNull String id, @NotNull Component displayName) {
        this.id = id;
        this.displayName = displayName;

        // Sync games data
        DataStorage data = PartyMayhem.getData().getStorage("settings.yml");
        enabled = data.getBoolean("games." + getId() + ".enabled");
    }

    /*
     *  Abstract functions
     */

    /** Checks if the game is operational and has been set up */
    protected abstract boolean status();
    /** Starts the game */
    public abstract void start();
    /** Game has ended or has been forced to end */
    public abstract void end(boolean forced);

    /*
     *  Registering methods
     */

    protected final void setMinPlayers(int amount) {
        minPlayers = amount;
    }
    protected final BukkitTask createTask(BukkitTask task) {
        tasks.add(task);
        return task;
    }
    protected final BossBar createBossBar(BossBar bossBar) {
        bossBars.add(bossBar);
        return bossBar;
    }
    protected final Objective createScoreboard(
        @NotNull String name,
        @NotNull Criteria criteria,
        @Nullable Component displayName,
        @NotNull RenderType renderType
    ) {
        Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective(name, criteria, displayName, renderType);
        scoreboardObjectives.add(objective);
        return objective;
    }
    protected final Objective createScoreboard(
        @NotNull String name,
        @Nullable Component displayName
    ) {
        return createScoreboard(name, Criteria.DUMMY, displayName, RenderType.INTEGER);
    }

    /*
     *  Blueprint functions
     */

    public final void registerBlueprint(Blueprint blueprint) {
        if (!blueprint.getMinigame().equals(this))
            throw new IllegalCallerException("A blueprint belonging to another minigame has been registered to a different minigame");

        blueprints.add(blueprint);
    }
    public final List<Blueprint> getBlueprints() {
        return blueprints;
    }
    public final List<String> getBlueprintNames() {
        return blueprints.stream().map(Blueprint::getBlueprintName).collect(Collectors.toList());
    }
    public final Blueprint getBlueprintByName(String name) {
        return blueprints.stream().filter(blueprint -> blueprint.getBlueprintName().equals(name)).findFirst().orElse(null);
    }

    /*
     *  Option methods
     */

    public final void registerOption(GameOption<?> option) {
        if (!option.getMinigame().equals(this))
            throw new IllegalCallerException("An option belonging to another minigame has been registered to a different minigame");

        options.add(option);
    }
    public final List<GameOption<?>> getOptions() {
        return options;
    }

    /*
     *  Getter and setter methods
     */

    public final int getMinPlayers() {
        return minPlayers;
    }
    public final @NotNull String getId() {
        return id;
    }
    public final @NotNull Component getDisplayName() {
        return displayName;
    }
    public final boolean isEnabled() {
        return enabled;
    }
    public final void setEnabled(boolean enabled) {
        DataStorage data = PartyMayhem.getData().getStorage("settings.yml");
        data.set("games." + getId() + ".enabled", enabled);
        this.enabled = enabled;
    }

    /*
     *  Minigame status methods
     */

    public final boolean isSetup() {
        if (!status()) return false;

        for (Blueprint blueprint : blueprints) {
            if (!blueprint.status()) return false;
        }
        return true;
    }
    public final boolean isReady() {
        return isSetup() && enabled && Bukkit.getOnlinePlayers().size() >= minPlayers;
    }

    /*
     *  Minigame stop and cleanup methods
     */

    /** Safely stops the game */
    protected final void stop() {
        MinigameFactory activeGame = GameManager.getActiveGame();
        if (activeGame == null || !activeGame.equals(this)) return; // Make sure the active game is itself

        GameManager.stopGame(false);
    }
    public final void cleanupGame() {
        cancelAllTasks();
        removeAllBossBars();
        removeAllObjectives();
    }
    public final void cancelAllTasks() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }
    public final void removeAllBossBars() {
        for (BossBar bossBar : bossBars) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        bossBars.clear();
    }
    public final void removeAllObjectives() {
        for (Objective objective : scoreboardObjectives) {
            objective.unregister();
        }
        scoreboardObjectives.clear();
    }
}

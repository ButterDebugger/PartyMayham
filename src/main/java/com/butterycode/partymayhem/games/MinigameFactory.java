package com.butterycode.partymayhem.games;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.manager.blueprint.Blueprint;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MinigameFactory implements Listener {

    protected String id;
    protected int minPlayers = 1;
    protected boolean enabled;
    protected List<Blueprint> blueprints = new ArrayList<>();
    protected List<BukkitTask> tasks = new ArrayList<>();
    protected List<BossBar> bossBars = new ArrayList<>();

    protected MinigameFactory(String id) {
        this.id = id;

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
     *  Private functions
     */

    protected void setMinPlayers(int amount) {
        minPlayers = amount;
    }
    protected BukkitTask createTask(BukkitTask task) {
        tasks.add(task);
        return task;
    }
    protected BossBar createBossBar(BossBar bossBar) {
        bossBars.add(bossBar);
        return bossBar;
    }
    /** Safely stops the game */
    protected void stop() {
        MinigameFactory activeGame = GameManager.getActiveGame();
        if (activeGame == null || !activeGame.equals(this)) return; // Make sure the active game is itself

        GameManager.stopGame(false);
    }

    /*
     *  Public functions
     */

    /** @return Whether the blueprint was successfully registered */
    public boolean registerBlueprint(Blueprint blueprint) {
        if (!blueprint.getMinigame().equals(this)) return false;

        blueprints.add(blueprint);
        return true;
    }
    public int getMinPlayers() {
        return minPlayers;
    }
    public String getId() {
        return id;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        DataStorage data = PartyMayhem.getData().getStorage("settings.yml");
        data.set("games." + getId() + ".enabled", enabled);
        this.enabled = enabled;
    }
    public List<Blueprint> getBlueprints() {
        return blueprints;
    }
    public List<String> getBlueprintNames() {
        return blueprints.stream().map(Blueprint::getBlueprintName).collect(Collectors.toList());
    }
    public Blueprint getBlueprintByName(String name) {
        return blueprints.stream().filter(blueprint -> blueprint.getBlueprintName().equals(name)).findFirst().orElse(null);
    }

    /*
     *  Game manager functions
     */

    public boolean isSetup() {
        if (!status()) return false;

        for (Blueprint blueprint : blueprints) {
            if (!blueprint.status()) return false;
        }
        return true;
    }
    public boolean isReady() {
        return isSetup() && Bukkit.getOnlinePlayers().size() >= minPlayers;
    }
    public void cleanupGame() {
        cancelAllTasks();
        removeAllBossBars();
    }
    public void cancelAllTasks() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }
    public void removeAllBossBars() {
        for (BossBar bossBar : bossBars) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        bossBars.clear();
    }
}

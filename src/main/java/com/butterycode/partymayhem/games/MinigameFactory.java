package com.butterycode.partymayhem.games;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.settings.blueprint.Blueprint;
import com.butterycode.partymayhem.settings.options.GameOption;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract non-sealed class MinigameFactory extends GameRegistry implements Listener {

    protected @NotNull HashSet<MinigameModule<?>> modules = new HashSet<>();
    protected int minPlayers = 1;
    protected boolean enabled;
    protected HashSet<Blueprint> blueprints = new HashSet<>();
    protected List<GameOption<?>> options = new ArrayList<>();

    protected MinigameFactory(@NotNull String id, @NotNull Component displayName) {
        super(id, displayName);

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

    protected final MinigameModule<?> chainModule(MinigameModule<?> module) {
        modules.add(module);
        return module;
    }

    public @NotNull HashSet<MinigameModule<?>> getModules() {
        return modules;
    }

    /*
     *  Blueprint methods
     */

    protected final void registerBlueprint(Blueprint blueprint) {
        if (!blueprint.getMinigame().equals(this))
            throw new IllegalCallerException("A blueprint belonging to another minigame has been registered to a different minigame");

        blueprints.add(blueprint);
    }
    public final HashSet<Blueprint> getBlueprints() {
        return blueprints;
    }
    public final List<String> getBlueprintIds() {
        return blueprints.stream().map(Blueprint::getId).collect(Collectors.toList());
    }
    public final @Nullable Blueprint getBlueprintById(String name) {
        return blueprints.stream().filter(blueprint -> blueprint.getId().equals(name)).findFirst().orElse(null);
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

    /** Safely stops the game */
    protected final void stop() {
        MinigameFactory activeGame = GameManager.getActiveGame();
        if (activeGame == null || !activeGame.equals(this)) return; // Make sure the active game is itself

        GameManager.stopGame(false);
    }

    @Override
    public final void cleanupSideEffects() {
        super.cleanupSideEffects();

        for (MinigameModule<?> module : modules) {
            module.cleanupSideEffects();
        }
    }

}

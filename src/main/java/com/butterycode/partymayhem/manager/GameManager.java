package com.butterycode.partymayhem.manager;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.games.MinigameModule;
import com.butterycode.partymayhem.utils.PlayerSnapshot;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameManager implements Listener {

    private static List<MinigameFactory> minigames = new ArrayList<>();
    private static @NotNull GameState gameState = GameState.STOPPED;
    private static final @NotNull List<BukkitTask> intermissionTasks = new ArrayList<>();
    private static @Nullable MinigameFactory activeGame = null;
    /** Snapshots for when the game state is stopped */
    private static final HashMap<UUID, PlayerSnapshot> stoppedSnapshots = new HashMap<>();
    /** Snapshots for when the game state is running and not ongoing */
    private static final HashMap<UUID, PlayerSnapshot> pregameSnapshots = new HashMap<>();
    /** Snapshots for when the game state is ongoing */
    private static final HashMap<UUID, PlayerSnapshot> ongoingSnapshots = new HashMap<>();

    static {
        startGameLoop();
    }

    private static void startGameLoop() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PartyMayhem.getPlugin(), () -> {
            // Cancel any intermission tasks that may be running outside an intermission
            if (!gameState.equals(GameState.INTERMISSION) && !intermissionTasks.isEmpty()) {
                for (BukkitTask task : intermissionTasks) {
                    task.cancel();
                }
                intermissionTasks.clear();
            }

            // Do something different for each game state
            switch (gameState) {
                case STOPPED -> {
                    // Skip game loop iteration
                }
                case WAITING -> {
                    if (!getReadyMinigames().isEmpty()) {
                        gameState = GameState.INTERMISSION;

                        // Announce to players that available games have been found
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendActionBar(AwesomeText.beautifyMessage("<gold>⇄ <yellow>Found available games!"));
                        }
                    } else {
                        // Announce to players that there no available games
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendActionBar(AwesomeText.beautifyMessage("<gold>⇄ <yellow>Looking for available games..."));
                        }
                    }
                }
                case INTERMISSION -> {
                    Transition transition = getTransition();

                    switch (transition) {
                        case CONTINUOUS -> {
                            boolean result = startGame(getReadyMinigames().get(new Random().nextInt(getReadyMinigames().size())));
                            if (!result) {
                                gameState = GameState.WAITING;
                            }
                        }
                        case SHUFFLE -> {
                            if (intermissionTasks.isEmpty()) {
                                List<MinigameFactory> readyGames = getReadyMinigames();
                                int turns = new Random().nextInt(20, 30 + readyGames.size());

                                // Shuffle the game order
                                Collections.shuffle(readyGames);

                                // Set up animation
                                int lastDelay = 0;

                                for (int turn = 0; turn < turns; turn++) {
                                    int delay = Math.max((int) Math.pow((((float) turn) / ((float) turns)) * 5f, 2f), 2);
                                    boolean lastSpin = turn == turns - 1;
                                    int gameIndex = turn % readyGames.size();

                                    intermissionTasks.add(new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            for (Player player : Bukkit.getOnlinePlayers()) {
                                                player.showTitle(Title.title(
                                                    readyGames.get(gameIndex).getDisplayName(),
                                                    Component.empty(),
                                                    0,
                                                    lastSpin ? delay * 2 : delay + 2,
                                                    lastSpin ? 20 : 0
                                                ));

                                                if (lastSpin) {
                                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);
                                                } else {
                                                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 2f, 1f);
                                                }
                                            }

                                            if (lastSpin) {
                                                // Start the minigame
                                                boolean result = startGame(readyGames.get(gameIndex));
                                                if (!result) {
                                                    gameState = GameState.WAITING;
                                                }
                                            }
                                        }
                                    }.runTaskLater(PartyMayhem.getPlugin(), lastDelay += delay));
                                }
                            }
                        }
                        case VOTE -> { // TODO: Finish this
                            gameState = GameState.WAITING;
                        }
                    }
                }
                case STARTED -> {

                }
            }
        }, 0L, 10L);
    }

    /*
     *  Snapshot functions
     */

    private static void captureAllStoppedSnapshots() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (stoppedSnapshots.containsKey(uuid)) continue;

            stoppedSnapshots.put(uuid, new PlayerSnapshot(player));
        }
    }
    private static void captureAllPregameSnapshots() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            pregameSnapshots.put(uuid, new PlayerSnapshot(player));
        }
    }
    private static void resetAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerSnapshot.reset(player);
        }
    }
    private static void restoreAllStoppedSnapshots() {
        for (UUID uuid : stoppedSnapshots.keySet()) {
            PlayerSnapshot snapshot = stoppedSnapshots.get(uuid);
            if (snapshot == null) continue;

            snapshot.restore();
        }

        stoppedSnapshots.clear();
    }
    private static void restoreAllPregameSnapshots() {
        for (UUID uuid : pregameSnapshots.keySet()) {
            PlayerSnapshot snapshot = pregameSnapshots.get(uuid);
            if (snapshot == null) continue;

            snapshot.restore();
        }

        pregameSnapshots.clear();
    }

    /*
     *  Event handlers
     */

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (gameState.isOngoing() && ongoingSnapshots.containsKey(uuid)) {
            if (!stoppedSnapshots.containsKey(uuid)) {
                stoppedSnapshots.put(uuid, new PlayerSnapshot(player));
            }

            ongoingSnapshots.get(uuid).restore();
        } else if (gameState.isPreGame() && pregameSnapshots.containsKey(uuid)) {
            if (!stoppedSnapshots.containsKey(uuid)) {
                stoppedSnapshots.put(uuid, new PlayerSnapshot(player));
            }

            pregameSnapshots.get(uuid).restore();
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (gameState.isOngoing()) {
            ongoingSnapshots.put(uuid, new PlayerSnapshot(player));
        } else if (gameState.isPreGame()) {
            pregameSnapshots.put(uuid, new PlayerSnapshot(player));
        }

        PlayerSnapshot snapshot = stoppedSnapshots.get(uuid);
        if (snapshot != null) {
            snapshot.restore();
            stoppedSnapshots.remove(uuid);
        }
    }

    /*
     *  Game functions
     */

    public static boolean startGame(@NotNull MinigameFactory minigame) {
        // Cancel if another game is already active
        if (activeGame != null) return false;

        // Update the game fields
        activeGame = minigame;
        gameState = GameState.STARTED;

        // Start the game
        captureAllPregameSnapshots();
        resetAllPlayers();
        activeGame.start();

        Bukkit.getServer().getPluginManager().registerEvents(activeGame, PartyMayhem.getPlugin());
        for (MinigameModule<?> module : activeGame.getModules()) {
            Bukkit.getServer().getPluginManager().registerEvents(module, PartyMayhem.getPlugin());
        }
        return true;
    }
    public static boolean stopGame(boolean forced) {
        // Cancel if there is no active game
        if (activeGame == null) return false;

        // End the game
        activeGame.end(forced);
        activeGame.cleanupSideEffects();

        HandlerList.unregisterAll(activeGame);
        for (MinigameModule<?> module : activeGame.getModules()) {
            HandlerList.unregisterAll(module);
        }

        activeGame = null;
        restoreAllPregameSnapshots();
        ongoingSnapshots.clear();
        if (gameState.isRunning()) gameState = GameState.WAITING;
        return true;
    }
    public static void registerMinigame(MinigameFactory minigame) {
        String id = minigame.getId();

        // Overwrite existing minigame if id is already being used
        if (getMinigameIds().contains(id)) {
            // Stop the active game if it shares the same id
            if (activeGame != null && activeGame.getId().equals(id)) {
                stopGame(true);
            }

            // Remove the old game
            minigames = minigames.stream().filter(game -> !game.getId().equals(id)).collect(Collectors.toList());
        }

        // Add the new game
        minigames.add(minigame);
    }
    public static List<MinigameFactory> getMinigames() {
        return minigames;
    }
    public static List<String> getMinigameIds() {
        return minigames.stream().map(MinigameFactory::getId).collect(Collectors.toList());
    }
    public static @Nullable MinigameFactory getMinigameById(String id) {
        return minigames.stream().filter(minigame -> minigame.getId().equals(id)).findFirst().orElse(null);
    }
    public static List<MinigameFactory> getEnabledMinigames() {
        return minigames.stream().filter(MinigameFactory::isEnabled).collect(Collectors.toList());
    }
    public static List<MinigameFactory> getDisabledMinigames() {
        return minigames.stream().filter(minigame -> !minigame.isEnabled()).collect(Collectors.toList());
    }
    public static List<MinigameFactory> getReadyMinigames() {
        return minigames.stream().filter(MinigameFactory::isReady).collect(Collectors.toList());
    }
    public static void setTransition(Transition transition) {
        DataStorage data = PartyMayhem.getData().getStorage("settings.yml");
        data.set("transition", transition.getLabel());
    }
    public static @NotNull Transition getTransition() {
        DataStorage data = PartyMayhem.getData().getStorage("settings.yml");
        String label = data.getString("transition");

        // Default to the continuous transition
        if (label == null) return Transition.CONTINUOUS;

        // Convert transition label to transition enum
        return Arrays.stream(Transition.values()).filter(trans -> trans.getLabel().equals(label)).findFirst().orElse(Transition.CONTINUOUS);
    }
    public static boolean enableMinigame(MinigameFactory minigame) {
        if (!minigame.isSetup()) return false;

        minigame.setEnabled(true);
        return true;
    }
    public static @Nullable MinigameFactory getActiveGame() {
        return activeGame;
    }
    public static boolean disableMinigame(MinigameFactory minigame) {
        if (minigame.equals(activeGame)) return false;

        minigame.setEnabled(false);
        return true;
    }
    public static boolean startMinigames() {
        if (gameState.isRunning()) return false;

        EditorManager.revokeAllEditors();
        captureAllStoppedSnapshots();
        resetAllPlayers();
        gameState = GameState.WAITING;
        return true;
    }
    public static boolean stopMinigames() {
        // Stop the game
        stopGame(true);
        gameState = GameState.STOPPED;

        // Restore and clear snapshots
        restoreAllStoppedSnapshots();
        pregameSnapshots.clear();
        ongoingSnapshots.clear();

        // Clear any leftover intermission tasks
        for (BukkitTask task : intermissionTasks) {
            task.cancel();
        }
        intermissionTasks.clear();
        return true;
    }
    public static @NotNull GameState getGameState() {
        return gameState;
    }
}

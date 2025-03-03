package com.butterycode.partymayhem.manager;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.utils.PlayerSnapshot;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class GameManager implements Listener {

    private static List<MinigameFactory> minigames = new ArrayList<>();
    private static GameState gameState = GameState.STOPPED;
    private static @Nullable MinigameFactory activeGame = null;
    private static boolean isLobbyActive = false;
    private static Lobby lobby;
    /** Snapshots for when the game state is stopped */
    private static HashMap<UUID, PlayerSnapshot> stoppedSnapshots = new HashMap<>();
    /** Snapshots for when the game state is running and not ongoing */
    private static HashMap<UUID, PlayerSnapshot> pregameSnapshots = new HashMap<>();
    /** Snapshots for when the game state is ongoing */
    private static HashMap<UUID, PlayerSnapshot> ongoingSnapshots = new HashMap<>();

    public GameManager() {
        lobby = new Lobby();

        startGameLoop();
    }

    private void startGameLoop() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PartyMayhem.getPlugin(), () -> {
            if (!gameState.isRunning()) return;

            if (gameState.isPreGame() && lobby.isSetup() && !isLobbyActive) {
                startLobby();
            }

            if (gameState.equals(GameState.WAITING)) {
                if (!getReadyMinigames().isEmpty()) {
                    gameState = GameState.INTERMISSION;
                } else {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendActionBar(AwesomeText.beautifyMessage("<gold>⇄ <yellow>Looking for available games..."));
                    }
                }
            }

            if (gameState.equals(GameState.INTERMISSION)) {
                Transition transition = getTransition();

                if (transition == null) {
                    gameState = GameState.WAITING;
                    return;
                }

                switch (transition) {
                    case CONTINUOUS:
                        activeGame = getReadyMinigames().get(new Random().nextInt(getReadyMinigames().size()));
                        gameState = GameState.STARTED;

                        boolean result = startGame();
                        if (!result) {
                            gameState = GameState.WAITING;
                            return;
                        }
                        break;
                    case SHUFFLE: // TODO: finish this
                    case VOTE:
                        gameState = GameState.WAITING;
                        break;
                }
            }

            if (gameState.isOngoing() && lobby.isSetup() && isLobbyActive) {
                stopLobby();
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

    public static boolean startGame() {
        if (activeGame == null) return false;

        captureAllPregameSnapshots();
        resetAllPlayers();
        activeGame.start();
        Bukkit.getServer().getPluginManager().registerEvents(activeGame, PartyMayhem.getPlugin());
        return true;
    }
    public static boolean stopGame(boolean forced) {
        if (activeGame == null) return false;

        activeGame.end(forced);
        activeGame.cleanupGame();
        HandlerList.unregisterAll(activeGame);
        GameManager.activeGame = null;
        restoreAllPregameSnapshots();
        ongoingSnapshots.clear();
        if (gameState.isRunning()) gameState = GameState.WAITING;
        return true;
    }
    private static void startLobby() {
        lobby.start();
        Bukkit.getServer().getPluginManager().registerEvents(lobby, PartyMayhem.getPlugin());
        isLobbyActive = true;
    }
    private static void stopLobby() {
        lobby.end(true);
        lobby.cleanupGame();
        HandlerList.unregisterAll(lobby);
        isLobbyActive = false;
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
    public static Transition getTransition() {
        DataStorage data = PartyMayhem.getData().getStorage("settings.yml");
        String label = data.getString("transition");
        if (label == null) return null;
        return Arrays.stream(Transition.values()).filter(trans -> trans.getLabel().equals(label)).findFirst().orElse(null);
    }
    public static boolean enableMinigame(MinigameFactory minigame) {
        if (!minigame.isSetup()) return false;

        minigame.setEnabled(true);
        return true;
    }
    public static @Nullable MinigameFactory getActiveGame() {
        return activeGame;
    }
    public static boolean isLobbyActive() {
        return isLobbyActive;
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
        stopGame(true);
        gameState = GameState.STOPPED;
        restoreAllStoppedSnapshots();
        pregameSnapshots.clear();
        ongoingSnapshots.clear();
        return true;
    }
    public static GameState getGameState() {
        return gameState;
    }
    public static Lobby getLobby() {
        return lobby;
    }
}

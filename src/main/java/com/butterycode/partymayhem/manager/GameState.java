package com.butterycode.partymayhem.manager;

public enum GameState {
    /** Nothing is happening */
    STOPPED(false, false),
    /** Waiting for any ready games */
    WAITING(true, false),
    /** The transition between minigames */
    INTERMISSION(true, false),
    /** A game is currently ongoing */
    STARTED(true, true);

    private final boolean running; // The plugin is currently running minigames
    private final boolean ongoing; // A game is currently active or ongoing

    GameState(boolean running, boolean ongoing) {
        this.running = running;
        this.ongoing = ongoing;
    }

    public boolean isOngoing() {
        return ongoing;
    }
    public boolean isRunning() {
        return running;
    }
    public boolean isPreGame() {
        return running && !ongoing;
    }
}

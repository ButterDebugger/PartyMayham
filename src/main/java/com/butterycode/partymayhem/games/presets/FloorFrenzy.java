package com.butterycode.partymayhem.games.presets;

import com.butterycode.partymayhem.games.MinigameFactory;

public class FloorFrenzy extends MinigameFactory {

//    private MultiBlueprint<Structure> platforms;

    protected FloorFrenzy() {
        super("floor_frenzy");

        // TODO: blueprints needed: map center aka spawn point, speed of game, round that pvp is enabled at
//        platforms = new MultiBlueprint<>(getId(), "platforms");
//        platforms.setMinBlueprints(2);
        setMinPlayers(2);
    }

    @Override
    protected boolean status() {
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void end(boolean forced) {

    }
}

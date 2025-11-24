package com.butterycode.partymayhem.games.presets;

import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.settings.options.NumberRange;
import com.butterycode.partymayhem.settings.options.Selection;
import com.butterycode.partymayhem.settings.options.Text;
import com.butterycode.partymayhem.settings.options.Toggle;
import net.kyori.adventure.text.Component;

import java.util.List;

public class FloorFrenzy extends MinigameFactory {

//    private MultiBlueprint<Structure> platforms;

    private NumberRange testNumberRange;
    private Toggle testToggle;
    private Text testText;
    private Selection testSelection;

    public FloorFrenzy() {
        super("floor_frenzy", Component.text("Floor Frenzy"));

        // TODO: blueprints needed: map center aka spawn point, speed of game, round that pvp is enabled at
        setMinPlayers(1);

        testNumberRange = new NumberRange(this, "test_num", Component.text("Test Num"), 0, 0, 1000, 1);
        testToggle = new Toggle(this, "test_toggle", Component.text("Test Toggle"), false);
        testText = new Text(this, "test_text", Component.text("Test Text"), "nothing");
        testSelection = new Selection(this, "test_selection", Component.text("Test Selection"), 0, List.of(
            "big",
            "medium",
            "small"
        ));

        GameManager.registerMinigame(this);
    }

    @Override
    protected boolean status() {
        return true;
    }

    @Override
    public void start() {

    }

    @Override
    public void end(boolean forced) {

    }
}

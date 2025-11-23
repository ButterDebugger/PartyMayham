package com.butterycode.partymayhem.settings.blueprint;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.games.MinigameFactory;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public sealed interface Blueprint permits Anchor, Region {

    static DataStorage getData(@NotNull String minigameId, @NotNull String blueprintId) {
        return PartyMayhem.getData().getStorage(minigameId + "/" + blueprintId + ".yml");
    }

    /** Loads saved blueprint data */
    boolean load();
    /** Saves blueprint data */
    boolean save();
    /** Deletes blueprint data */
    boolean delete();

    /** @return Whether the blueprint is set up */
    boolean status();

    /** @return The minigame that the blueprint belongs to */
    @NotNull MinigameFactory getMinigame();

    /** @return The id of the blueprint */
    @NotNull String getId();
    /** @return The display name of the blueprint */
    @NotNull Component getDisplayName();

}

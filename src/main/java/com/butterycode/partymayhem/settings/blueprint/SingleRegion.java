package com.butterycode.partymayhem.settings.blueprint;

import com.butterycode.partymayhem.games.MinigameFactory;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public non-sealed class SingleRegion implements Blue {

    private final @NotNull MinigameFactory minigame;
    private final @NotNull String id;
    private final @NotNull Component displayName;
    private final @NotNull DataStorage data;

    private @Nullable World world;
    private @Nullable Vector firstPoint;
    private @Nullable Vector secondPoint;

    public SingleRegion(@NotNull MinigameFactory minigame, @NotNull String id, @NotNull Component displayName) {
        this.minigame = minigame;
        this.id = id;
        this.displayName = displayName;

        // Get the data and load
        this.data = Blue.getData(minigame.getId(), id);
        load();
    }

    @Override
    public boolean load() {
        String worldName = data.getString("world");
        if (worldName == null) return false; // Assume the blueprint does not exist if the world is not present

        World world = Bukkit.getWorld(worldName);
        double firstX = data.getDouble("first-point.x");
        double firstY = data.getDouble("first-point.y");
        double firstZ = data.getDouble("first-point.z");
        double secondX = data.getDouble("second-point.x");
        double secondY = data.getDouble("second-point.y");
        double secondZ = data.getDouble("second-point.z");

        this.world = world;
        this.firstPoint = new Vector(firstX, firstY, firstZ);
        this.secondPoint = new Vector(secondX, secondY, secondZ);
        return true;
    }

    @Override
    public boolean save() {
        if (world == null || firstPoint == null || secondPoint == null) return false;

        data.set("world", world.getName());
        data.set("first-point.x", firstPoint.getX());
        data.set("first-point.y", firstPoint.getY());
        data.set("first-point.z", firstPoint.getZ());
        data.set("second-point.x", secondPoint.getX());
        data.set("second-point.y", secondPoint.getY());
        data.set("second-point.z", secondPoint.getZ());

        data.save();
        return true;
    }

    @Override
    public boolean delete() {
        world = null;
        firstPoint = null;
        secondPoint = null;

        data.remove("world");
        data.remove("first-point");
        data.remove("second-point");

        data.save();
        return true;
    }

    @Override
    public boolean status() {
        return world != null || firstPoint != null || secondPoint != null;
    }

    @Override
    public @NotNull MinigameFactory getMinigame() {
        return minigame;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return displayName;
    }
}

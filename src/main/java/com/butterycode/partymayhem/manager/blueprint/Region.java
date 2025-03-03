package com.butterycode.partymayhem.manager.blueprint;

import com.butterycode.partymayhem.games.MinigameFactory;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Region extends Blueprint {

    private final World[] worlds;
    private final Vector[] firstPoints;
    private final Vector[] secondPoints;

    public Region(@NotNull MinigameFactory minigame, @NotNull String blueprintName, int minAmount, int maxAmount) {
        super("regions", minigame, blueprintName, minAmount, maxAmount);

        this.worlds = new World[maxAmount];
        this.firstPoints = new Vector[maxAmount];
        this.secondPoints = new Vector[maxAmount];

        load();
    }
    public Region(@NotNull MinigameFactory minigame, @NotNull String blueprintName, int amount) {
        this(minigame, blueprintName, amount, amount);
    }

    @Override
    public boolean isIndexValid(int index) {
        return worlds[index] != null && firstPoints[index] != null && secondPoints[index] != null;
    }

    @Override
    public boolean load() {
        DataStorage data = getData();

        for (int i = 0; i < getMaxAmount(); i++) {
            if (
                    !data.exists(i + ".world") ||
                    !data.exists(i + ".first-point.x") ||
                    !data.exists(i + ".first-point.y") ||
                    !data.exists(i + ".first-point.z") ||
                    !data.exists(i + ".second-point.x") ||
                    !data.exists(i + ".second-point.y") ||
                    !data.exists(i + ".second-point.z")
            ) {
                worlds[i] = null;
                firstPoints[i] = null;
                secondPoints[i] = null;
                continue;
            }

            worlds[i] = Bukkit.getWorld(data.getString(i + ".world"));
            firstPoints[i] = new Vector(data.getDouble(i + ".first-point.x"), data.getDouble(i + ".first-point.y"), data.getDouble(i + ".first-point.z"));
            secondPoints[i] = new Vector(data.getDouble(i + ".second-point.x"), data.getDouble(i + ".second-point.y"), data.getDouble(i + ".second-point.z"));
        }
        return true;
    }

    @Override
    public boolean save() {
        DataStorage data = getData();

        for (int i = 0; i < getMaxAmount(); i++) {
            if (!isIndexValid(i)) {
                data.remove(String.valueOf(i));
                continue;
            }

            data.set(i + ".world", worlds[i].getName());
            data.set(i + ".first-point.x", firstPoints[i].getX());
            data.set(i + ".first-point.y", firstPoints[i].getY());
            data.set(i + ".first-point.z", firstPoints[i].getZ());
            data.set(i + ".second-point.x", secondPoints[i].getX());
            data.set(i + ".second-point.y", secondPoints[i].getY());
            data.set(i + ".second-point.z", secondPoints[i].getZ());
        }
        return true;
    }

    @Override
    public boolean delete(int index) {
        DataStorage data = getData();

        data.remove(String.valueOf(index));

        worlds[index] = null;
        firstPoints[index] = null;
        secondPoints[index] = null;
        return true;
    }

    public World getWorld(int index) {
        return worlds[index];
    }
    public void setWorld(int index, World world) {
        this.worlds[index] = world;
    }

    public Vector getFirstPoint(int index) {
        return firstPoints[index];
    }
    public Location getFirstLocation(int index) {
        return new Location(worlds[index], firstPoints[index].getX(), firstPoints[index].getY(), firstPoints[index].getZ());
    }
    public void setFirstPoint(int index, Vector firstPoint) {
        this.firstPoints[index] = firstPoint;
    }

    public Vector getSecondPoint(int index) {
        return secondPoints[index];
    }
    public Location getSecondLocation(int index) {
        return new Location(worlds[index], secondPoints[index].getX(), secondPoints[index].getY(), secondPoints[index].getZ());
    }
    public void setSecondPoint(int index, Vector secondPoint) {
        this.secondPoints[index] = secondPoint;
    }
}

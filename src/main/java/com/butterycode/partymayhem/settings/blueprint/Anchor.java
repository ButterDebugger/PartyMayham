package com.butterycode.partymayhem.settings.blueprint;

import com.butterycode.partymayhem.games.MinigameFactory;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public non-sealed class Anchor extends Blueprint {

    private Location[] locations;

    public Anchor(@NotNull MinigameFactory minigame, @NotNull String blueprintName, int minAmount, int maxAmount) {
        super("anchors", minigame, blueprintName, minAmount, maxAmount);

        locations = new Location[maxAmount];

        load();
    }
    public Anchor(@NotNull MinigameFactory minigame, @NotNull String blueprintName, int amount) {
        this(minigame, blueprintName, amount, amount);
    }

    @Override
    public boolean isIndexValid(int index) {
        return locations[index] != null;
    }

    @Override
    public boolean load() {
        DataStorage data = getData();

        for (int i = 0; i < getMaxAmount(); i++) {
            if (
                    !data.exists(i + ".world") ||
                    !data.exists(i + ".x") ||
                    !data.exists(i + ".y") ||
                    !data.exists(i + ".z") ||
                    !data.exists(i + ".yaw") ||
                    !data.exists(i + ".pitch")
            ) {
                locations[i] = null;
                continue;
            }

            locations[i] = new Location(Bukkit.getWorld(data.getString(i + ".world")), data.getDouble(i + ".x"), data.getDouble(i + ".y"), data.getDouble(i + ".z"), (float) data.getDouble(i + ".yaw"), (float) data.getDouble(i + ".pitch"));
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

            data.set(i + ".world", locations[i].getWorld().getName());
            data.set(i + ".x", locations[i].getX());
            data.set(i + ".y", locations[i].getY());
            data.set(i + ".z", locations[i].getZ());
            data.set(i + ".yaw", locations[i].getYaw());
            data.set(i + ".pitch", locations[i].getPitch());
        }
        return true;
    }

    @Override
    public boolean delete(int index) {
        DataStorage data = getData();

        data.remove(String.valueOf(index));

        locations[index] = null;
        return true;
    }

    public Location getLocation(int index) {
        return locations[index];
    }
    public void setLocation(int index, Location location) {
        this.locations[index] = location;
    }
}

package com.butterycode.partymayhem.settings.blueprint;

import com.butterycode.partymayhem.games.MinigameFactory;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public non-sealed class Anchor implements Blueprint {

    private final @NotNull MinigameFactory minigame;
    private final @NotNull String id;
    private final @NotNull Component displayName;
    private final @NotNull DataStorage data;

    private @Nullable Location location;

    public Anchor(@NotNull MinigameFactory minigame, @NotNull String id, @NotNull Component displayName) {
        this.minigame = minigame;
        this.id = id;
        this.displayName = displayName;

        // Get the data and load
        this.data = Blueprint.getData(minigame.getId(), id);
        load();
    }

    @Override
    public boolean load() {
        String worldName = data.getString("world");
        if (worldName == null) return false; // Assume the blueprint does not exist if the world is not present

        World world = Bukkit.getWorld(worldName);
        double x = data.getDouble("x");
        double y = data.getDouble("y");
        double z = data.getDouble("z");
        double yaw = data.getDouble("yaw");
        double pitch = data.getDouble("pitch");

        location = new Location(world, x, y, z, (float) yaw, (float) pitch);
        return true;
    }

    @Override
    public boolean save() {
        if (location == null) return false;

        data.set("world", location.getWorld().getName());
        data.set("x", location.getX());
        data.set("y", location.getY());
        data.set("z", location.getZ());
        data.set("yaw", location.getYaw());
        data.set("pitch", location.getPitch());

        data.save();
        return true;
    }

    @Override
    public boolean delete() {
        location = null;

        data.remove("world");
        data.remove("x");
        data.remove("y");
        data.remove("z");
        data.remove("yaw");
        data.remove("pitch");

        data.save();
        return true;
    }

    @Override
    public boolean status() {
        return location != null;
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

    public @Nullable Location getLocation() {
        return location;
    }
    public void setLocation(@NotNull Location location) {
        this.location = location;
    }
}

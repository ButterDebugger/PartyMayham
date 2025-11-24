package com.butterycode.partymayhem.manager;

import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.settings.blueprint.Anchor;
import com.butterycode.partymayhem.settings.blueprint.Region;
import com.butterycode.partymayhem.utils.GameMakerUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class Lobby extends MinigameFactory {

    private Region area;
    private Anchor spawn;

    protected Lobby() {
        super("lobby", Component.text("Lobby"));

        area = new Region(this, "area", Component.text("Area"));
        spawn = new Anchor(this, "spawn", Component.text("Spawn"));
    }

    @Override
    protected boolean status() {
        return true;
    }

    @Override
    public void start() {
        assert spawn.getLocation() != null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(spawn.getLocation());
        }
    }

    @Override
    public void end(boolean forced) {}

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        assert area.getFirstLocation() != null;
        assert area.getSecondLocation() != null;
        assert spawn.getLocation() != null;

        Player player = event.getPlayer();

        if (!GameMakerUtils.isEntityInsideRegion(player, area.getFirstLocation(), area.getSecondLocation())) {
            player.teleport(spawn.getLocation());
        }
    }
}

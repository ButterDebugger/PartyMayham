package com.butterycode.partymayhem.manager;

import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.blueprint.Anchor;
import com.butterycode.partymayhem.manager.blueprint.Region;
import com.butterycode.partymayhem.utils.GameMakerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class Lobby extends MinigameFactory {

    private Region area;
    private Anchor spawn;

    protected Lobby() {
        super("lobby");

        area = new Region(this, "area", 1);
        spawn = new Anchor(this, "spawn", 1);
    }

    @Override
    protected boolean status() {
        return true;
    }

    @Override
    public void start() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(spawn.getLocation(0));
        }
    }

    @Override
    public void end(boolean forced) {}

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!GameMakerUtils.isEntityInsideRegion(player, area.getFirstLocation(0), area.getSecondLocation(0))) {
            player.teleport(spawn.getLocation(0));
        }
    }
}

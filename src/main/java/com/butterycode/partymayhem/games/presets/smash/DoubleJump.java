package com.butterycode.partymayhem.games.presets.smash;

import com.butterycode.partymayhem.games.MinigameModule;
import org.bukkit.Input;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class DoubleJump extends MinigameModule<Smash> {

    private final @NotNull HashMap<UUID, Integer> usedJumps = new HashMap<>();
    private final @NotNull HashMap<UUID, Boolean> uniqueJumps = new HashMap<>();

    public DoubleJump(@NotNull Smash parent) {
        super(parent);
    }

    @EventHandler
    public void onPlayerInput(PlayerInputEvent event) {
        Input input = event.getInput();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Get jump state
        uniqueJumps.putIfAbsent(uuid, input.isJump());

        int jumpsUsed = usedJumps.getOrDefault(uuid, 0);
        boolean wasJumping = uniqueJumps.get(uuid);

        // Prevent other input from triggering unique jumps
        if (wasJumping == input.isJump()) {
            return;
        }

        uniqueJumps.put(player.getUniqueId(), input.isJump());

        // Check if the player should be able to double jump
        if (input.isJump() && !player.isOnGround() && jumpsUsed < getParent().getAllowedDoubleJumps().getValue()) {
            // Increase used jumps
            usedJumps.put(player.getUniqueId(), jumpsUsed + 1);

            // Apply double jump
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.25f, 1f);

            Vector force = player.getEyeLocation().getDirection();
            force.multiply(0.25);
            force.setY(0.65);
            player.setVelocity(force);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Reset used jumps when on ground
        if (player.isOnGround()) {
            usedJumps.put(uuid, 0);
        }
    }

}

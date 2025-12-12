package com.butterycode.partymayhem.games.presets.smash;

import com.butterycode.partymayhem.games.MinigameModule;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

class Dash extends MinigameModule<Smash> {

    private final @NotNull HashMap<UUID, Long> lastDashTime = new HashMap<>();
    private final @NotNull HashMap<UUID, Long> immunityUntilTime = new HashMap<>();

    public Dash(Smash parent) {
        super(parent);
    }

    @EventHandler
    private void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        Input input = player.getCurrentInput();

        // Check if the players cooldown has ended
        long lastTime = lastDashTime.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - lastTime < getParent().getDashCooldown().getValue() * 1000) {
            return;
        }

        // Play sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.5f, 1f);

        // Set new timestamp
        lastDashTime.put(player.getUniqueId(), System.currentTimeMillis());

        // Set immunity
        immunityUntilTime.put(player.getUniqueId(), System.currentTimeMillis() + 500);

        // Calculate force
        Vector force = new Vector();

        if (input.isForward()) {
            Location location = player.getEyeLocation().clone();
            location.setPitch(0);

            force.add(location.getDirection());
        }
        if (input.isRight()) {
            Location location = player.getEyeLocation().clone();
            location.setYaw(location.getYaw() + 90);
            location.setPitch(0);

            force.add(location.getDirection());
        }
        if (input.isLeft()) {
            Location location = player.getEyeLocation().clone();
            location.setYaw(location.getYaw() - 90);
            location.setPitch(0);

            force.add(location.getDirection());
        }
        if (input.isBackward()) {
            Location location = player.getEyeLocation().clone();
            location.setYaw(location.getYaw() + 180);
            location.setPitch(0);

            force.add(location.getDirection());
        }

        // Apply the new velocity
        Vector newVelocity = player.getVelocity();

        newVelocity.add(force.multiply(getParent().getDashMultiplier().getValue()));
        newVelocity.setY(Math.max(0, newVelocity.getY()));

        player.setVelocity(newVelocity);

        // Cancel the event
        event.setCancelled(true);
    }

    public boolean isImmune(Player player) {
        long immunityTime = immunityUntilTime.getOrDefault(player.getUniqueId(), 0L);
        return immunityTime >= System.currentTimeMillis();
    }

    public void resetDashTime(Player player) {
        lastDashTime.put(player.getUniqueId(), 0L);
    }

    public long getLastDashTime(Player player) {
        return lastDashTime.getOrDefault(player.getUniqueId(), 0L);
    }

    public void clearAll() {
        lastDashTime.clear();
        immunityUntilTime.clear();
    }
}

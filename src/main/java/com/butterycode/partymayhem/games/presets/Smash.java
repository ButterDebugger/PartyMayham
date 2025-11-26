package com.butterycode.partymayhem.games.presets;

import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.settings.blueprint.Anchor;
import com.butterycode.partymayhem.settings.blueprint.Region;
import com.butterycode.partymayhem.utils.GameMakerUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Smash extends MinigameFactory {

    private final @NotNull Anchor spawn;
    private final @NotNull Region map;
    private @Nullable Objective damageObjective = null;

    public Smash() {
        super("smash", Component.text("Smash"));

        spawn = new Anchor(this, "spawn", Component.text("Spawn"));
        registerBlueprint(spawn);
        map = new Region(this, "map", Component.text("Map"));
        registerBlueprint(map);

        GameManager.registerMinigame(this);
    }

    @Override
    public void start() {
        assert spawn.getLocation() != null;

        damageObjective = createScoreboard(Component.text("Damage"));
        damageObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Reset damage score
            Score score = damageObjective.getScoreFor(player);
            score.setScore(0);

            // Set initial attributes
            Objects.requireNonNull(player.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).setBaseValue(1);
            Objects.requireNonNull(player.getAttribute(Attribute.ATTACK_KNOCKBACK)).setBaseValue(5);

            // Disable invulnerability timer
            player.setMaximumNoDamageTicks(0);

            // Set gamemode
            player.setGameMode(GameMode.ADVENTURE);

            // Teleport to spawn
            player.teleport(spawn.getLocation());
        }
    }

    @Override
    public void end(boolean forced) {

    }

    @Override
    protected boolean status() {
        return true;
    }

    @EventHandler
    private void onHit(EntityDamageByEntityEvent event) {
        assert damageObjective != null;

        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        // Heal player
        player.heal(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue());

        // Increase damage score
        Score score = damageObjective.getScore(player);

        int damage = score.getScore();
        damage += 1;
        score.setScore(damage);

        // Calculate and set new knockback resistance
        double resistance = 1d - Math.clamp(Math.pow((damage + 232d) / 500d, 3d), 0d, 1d);

        Objects.requireNonNull(player.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).setBaseValue(resistance);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        assert spawn.getLocation() != null;
        assert map.getFirstLocation() != null;
        assert map.getSecondLocation() != null;

        Player player = event.getPlayer();

        Location firstLoc = map.getFirstLocation().getBlock().getLocation();
        Location secondLoc = map.getSecondLocation().getBlock().getLocation().add(1, 1, 1);

        boolean inside = GameMakerUtils.isEntityInsideRegion(player, firstLoc, secondLoc);

        if (!inside) {
            if (!player.getGameMode().equals(GameMode.SPECTATOR)) { // TODO: check if they are still in the game instead
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_DEATH, 2f, 1f);
                player.setGameMode(GameMode.SPECTATOR);
            }

            player.teleport(spawn.getLocation());
        }
    }

}

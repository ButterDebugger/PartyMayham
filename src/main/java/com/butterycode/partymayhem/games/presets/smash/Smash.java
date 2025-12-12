package com.butterycode.partymayhem.games.presets.smash;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.settings.blueprint.Anchor;
import com.butterycode.partymayhem.settings.blueprint.Region;
import com.butterycode.partymayhem.settings.options.NumberRange;
import com.butterycode.partymayhem.utils.GameMakerUtils;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;

import static org.bukkit.damage.DamageType.PLAYER_ATTACK;

public class Smash extends MinigameFactory {

    private final @NotNull Anchor spawn;
    private final @NotNull Region map;
    private final @NotNull NumberRange hitCooldown;
    private final @NotNull NumberRange dashMultiplier;
    private final @NotNull NumberRange dashCooldown;
    private final @NotNull NumberRange allowedDoubleJumps;
    private @Nullable Objective damageObjective = null;

    private final Dash dashModule;
    private final DoubleJump doubleJumpModule;

    public Smash() {
        super("smash", Component.text("Smash"));

        // Register modules
        dashModule = (Dash) chainModule(new Dash(this));
        doubleJumpModule = (DoubleJump) chainModule(new DoubleJump(this));

        // Register blueprints
        spawn = new Anchor(this, "spawn", Component.text("Spawn"));
        registerBlueprint(spawn);
        map = new Region(this, "map", Component.text("Map"));
        registerBlueprint(map);

        // Register options
        hitCooldown = new NumberRange(this, "hit_cooldown", Component.text("Hit Cooldown (Ticks)"), 20, 0, 100, 1);
        registerOption(hitCooldown);

        dashMultiplier = new NumberRange(this, "dash_multiplier", Component.text("Dash Multiplier"), 1.0f, 0.0f, 5.0f, 0.05f);
        registerOption(dashMultiplier);

        dashCooldown = new NumberRange(this, "dash_cooldown", Component.text("Dash Cooldown (Secs)"), 3.0f, 0.0f, 60.0f, 0.1f);
        registerOption(dashCooldown);

        allowedDoubleJumps = new NumberRange(this, "allowed_double_jumps", Component.text("Allowed Double Jumps"), 3.0f, 0.0f, 15.0f, 1f);
        registerOption(allowedDoubleJumps);

        // Register game to game manager
        GameManager.registerMinigame(this);
    }

    public @NotNull NumberRange getDashCooldown() {
        return dashCooldown;
    }

    public @NotNull NumberRange getDashMultiplier() {
        return dashMultiplier;
    }

    public @NotNull NumberRange getAllowedDoubleJumps() {
        return allowedDoubleJumps;
    }

    @Override
    public void start() {
        assert spawn.getLocation() != null;

        int noDamageTicks = (int) (hitCooldown.getValue() + 0);

        // Create damage scoreboard objective
        damageObjective = createScoreboard(Component.text("Damage"));
        damageObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        damageObjective.setAutoUpdateDisplay(false);

        // Clear dash cooldown and immunity time
        dashModule.clearAll();

        // Loop through each player and set default values
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Reset damage score
            Score score = damageObjective.getScoreFor(player);
            score.setScore(0);

            // Set initial attributes
            Objects.requireNonNull(player.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).setBaseValue(1);
            Objects.requireNonNull(player.getAttribute(Attribute.ATTACK_KNOCKBACK)).setBaseValue(5);

            // Disable invulnerability timer
            player.setMaximumNoDamageTicks(noDamageTicks);

            // Set gamemode and health
            player.setGameMode(GameMode.ADVENTURE);

            // Teleport to spawn
            player.teleport(spawn.getLocation());
        }

        //
        createTask(new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateDamageDisplay(player);
                }
            }
        }.runTaskTimer(PartyMayhem.getPlugin(), 0L, 1L));
    }

    private @NotNull Component getDamageComponent(int damage) {
        return AwesomeText.beautifyMessage(
            "<transition:#ffffff:#fce165:#ed993e:#ed3333:#a82631:" + Math.clamp(damage / 125.0, 0.0, 1.0) + "><b><damage></b>%</transition>",
            Placeholder.unparsed("damage", String.valueOf(damage))
        );
    }

    private @NotNull Component getDashComponent(long lastTime) {
        // Calculate the time passed
        long timePassed = System.currentTimeMillis() - lastTime;

        // Create a Duration for the remaining time
        Duration remaining = Duration.ofSeconds((long) (dashCooldown.getValue() + 0)).minusMillis(timePassed);

        // Convert to seconds
        double seconds = Math.floor(Math.max(0, remaining.toMillis() / 1000.0) * 10) / 10;

        String c = seconds == 0 ? "#66f098" : "#47b0ff";
        String e = seconds == 0 ? "🌊" : "⌛";

        return AwesomeText.beautifyMessage(
            "<" + c + ">" + e + "<b><time></b>s</" + c + ">",
            Placeholder.unparsed("time", String.valueOf(seconds))
        );
    }

    @Override
    public void end(boolean forced) {

    }

    @Override
    protected boolean status() {
        return true;
    }

    @EventHandler
    private void onFoodChange(FoodLevelChangeEvent event) {
        HumanEntity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        event.setCancelled(true);
    }

    @EventHandler
    private void onDamageByEntity(EntityDamageByEntityEvent event) {
        assert damageObjective != null;

        Entity entity = event.getEntity();
        DamageType damageType = event.getDamageSource().getDamageType();

        if (!(entity instanceof Player player)) return;

        // Increase damage score if damaged by a player
        if (damageType.equals(PLAYER_ATTACK)) {
            Player attacker = (Player) event.getDamageSource().getCausingEntity();
            assert attacker != null;

            if (dashModule.isImmune(player)) {
                attacker.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.5f, 1f);
                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.25f, 1f);

                event.setCancelled(true);
                return;
            }

            increaseDamage(player, 1);

//            Vector vec = player.getVelocity();
//            vec.setY(0.5);
//            player.setVelocity(vec);

            // Heal the player
            createTask(new BukkitRunnable() {
                public void run() {
                    player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue());
                }
            }.runTaskLater(PartyMayhem.getPlugin(), 1L));
        }
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        DamageType damageType = event.getDamageSource().getDamageType();

        if (!(entity instanceof Player)) return;

        // Cancel other forms of damage
        if (!damageType.equals(PLAYER_ATTACK)) {
            event.setCancelled(true);
        }
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
//            if (!player.getGameMode().equals(GameMode.SPECTATOR)) { // TODO: check if they are still in the game instead
//                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_DEATH, 2f, 1f);
//                player.setGameMode(GameMode.SPECTATOR);
//            }

            // Play sound
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_DEATH, 0.5f, 1f);

            // Reset damage
            setDamage(player, 0);

            // Reset dash time
            dashModule.resetDashTime(player);

            // Teleport player
            player.teleport(spawn.getLocation());
        }
    }

    private void setDamage(Player player, int damage) {
        assert damageObjective != null;

        // Set the damage score
        Score score = damageObjective.getScore(player);
        score.setScore(damage);

        // Calculate and set new knockback resistance
        double resistance = 1d - Math.clamp(Math.pow((damage + 232d) / 500d, 3d), 0d, 1d);

        Objects.requireNonNull(player.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).setBaseValue(resistance);

        // Update damage display
        updateDamageDisplay(player);
    }

    private void increaseDamage(Player player, int amount) {
        assert damageObjective != null;

        // Get the increased score
        Score score = damageObjective.getScore(player);
        int damage = score.getScore() + amount;

        // Set the damage
        setDamage(player, damage);
    }

    private void updateDamageDisplay(Player player) {
        assert damageObjective != null;

        // Get the damage amount
        Score score = damageObjective.getScoreFor(player);
        int damage = score.getScore();

        // Get damage component and display it below the name
        Component damageComponent = getDamageComponent(damage);

        score.numberFormat(NumberFormat.fixed(damageComponent));

        // Display in the action bar
        long lastTime = dashModule.getLastDashTime(player);

        Component dashComponent = getDashComponent(lastTime);

        player.sendActionBar(AwesomeText.beautifyMessage(
            "<damage> <dash_cooldown>",
            Placeholder.component("damage", damageComponent),
            Placeholder.component("dash_cooldown", dashComponent)
        ));
    }

}

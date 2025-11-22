package com.butterycode.partymayhem.games.presets;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.settings.blueprint.Region;
import com.butterycode.partymayhem.utils.GameMakerUtils;
import com.butterycode.partymayhem.utils.WorldEditor;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import dev.debutter.cuberry.paper.utils.Caboodle;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class Spleef extends MinigameFactory {

    private Region map;
    private Region snowlayers;

    private int timeLeft = 0;
    private boolean gameEnded = false;
    private List<UUID> playersLeft = new ArrayList<>();
    private final ItemStack ironShovel;

    public Spleef() {
        super("spleef", Component.text("Spleef"));

        map = new Region(this, "map", 1);
        snowlayers = new Region(this, "snowlayer", 1, 3);
        setMinPlayers(2);

        ironShovel = new ItemStack(Material.IRON_SHOVEL);
        ironShovel.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
        ironShovel.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        ItemMeta ironShovelMeta = ironShovel.getItemMeta();
        ironShovelMeta.setUnbreakable(true);
        ironShovel.setItemMeta(ironShovelMeta);

        GameManager.registerMinigame(this);
    }

    @Override
    protected boolean status() {
        return true;
    }

    @Override
    public void start() {
        // Set the snow layers to snow
        for (int index : snowlayers.getValidIndexes()) {
            WorldEditor.fillRegion(snowlayers.getWorld(index), snowlayers.getFirstLocation(index), snowlayers.getSecondLocation(index), Material.SNOW_BLOCK);
        }

        // Start the game
        playersLeft = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());
        gameEnded = false;
        timeLeft = 60;

        BossBar bar = createBossBar(Bukkit.getServer().createBossBar(AwesomeText.colorizeHex("Time Left: &e" + timeLeft), BarColor.YELLOW, BarStyle.SOLID));
        bar.setVisible(true);
        bar.setProgress(timeLeft / 60d);

        int highestIndex = getHighestSnowLayer();

        for (UUID uuid : playersLeft) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                playersLeft.remove(uuid);
                continue;
            }

            // Pick a random spawn location
            ArrayList<Block> blocks = GameMakerUtils.getHighestBlocksInRegion(snowlayers.getWorld(highestIndex), snowlayers.getFirstPoint(highestIndex), snowlayers.getSecondPoint(highestIndex));
            Block randomBlock = blocks.get(new Random().nextInt(blocks.size()));

            Location spawnLoc = randomBlock.getLocation();
            spawnLoc.add(0.5, 1, 0.5);
            spawnLoc.setYaw(player.getLocation().getYaw());
            spawnLoc.setPitch(player.getLocation().getPitch());
            player.teleport(spawnLoc);

            // Make all players face the center
            Vector lowestVec = Caboodle.getLowestPoint(snowlayers.getFirstPoint(highestIndex), snowlayers.getSecondPoint(highestIndex));
            Vector highestVec = Caboodle.getHighestPoint(snowlayers.getFirstPoint(highestIndex), snowlayers.getSecondPoint(highestIndex));

            Vector centerVec = lowestVec.getMidpoint(highestVec.add(new Vector(1, 0, 1)));
            centerVec.setY(highestVec.getY() + 2);
            Location centerLoc = new Location(snowlayers.getWorld(highestIndex), centerVec.getX(), spawnLoc.getY(), centerVec.getZ());

            GameMakerUtils.lookAtLocation(player, centerLoc);

            // Give all players a shovel
            player.getInventory().setItem(0, ironShovel);
            player.setGameMode(GameMode.SURVIVAL);

            bar.addPlayer(player);
        }

        createTask(new BukkitRunnable() {
            public void run() {
                timeLeft -= 1;
                bar.setTitle(AwesomeText.colorizeHex("Time Left: &e" + timeLeft));
                bar.setProgress(timeLeft / 60d);
                if (timeLeft <= 0) {
                    this.cancel();
                    if (playersLeft.size() > 1) {
                        declareTie();
                    }
                } else {
                    checkForWinner();
                }
            }
        }.runTaskTimer(PartyMayhem.getPlugin(), 0L, 20L));
    }

    private void checkForWinner() {
        if (gameEnded) return;

        if (playersLeft.size() == 1) {
            Player winner = Bukkit.getPlayer(playersLeft.getFirst());

            if (winner == null) {
                declareTie();
                return;
            }

            declareWinner(winner.getName());
        } else if (playersLeft.isEmpty()) {
            declareTie();
        }
    }

    private void declareTie() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);
            player.sendTitle(AwesomeText.colorizeHex("&b&#57dbd4Game Over"), AwesomeText.colorizeHex("&fNobody wins."), 5, 40, 5);
        }

        gameEnded = true;
        cancelAllTasks();

        createTask(new BukkitRunnable() {
            public void run() {
                stop();
            }
        }.runTaskLater(PartyMayhem.getPlugin(), 50L));
    }

    private void declareWinner(String playerName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);
            player.sendTitle(AwesomeText.colorizeHex("&b&#57dbd4Game Over"), AwesomeText.colorizeHex("&e⏸■①■⏸ &f" + playerName + " won!"), 5, 40, 5);
        }

        gameEnded = true;
        cancelAllTasks();

        createTask(new BukkitRunnable() {
            public void run() {
                stop();
            }
        }.runTaskLater(PartyMayhem.getPlugin(), 50L));
    }

    private int getHighestSnowLayer() {
        double highestY = Double.MIN_VALUE;
        int highestIndex = -1;

        for (int index : snowlayers.getValidIndexes()) {
            double firstY = snowlayers.getFirstLocation(index).getBlockY();
            double secondY = snowlayers.getSecondLocation(index).getBlockY();

            if (firstY > highestY) {
                highestY = firstY;
                highestIndex = index;
            }
            if (secondY > highestY) {
                highestY = secondY;
                highestIndex = index;
            }
        }

        return highestIndex;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.getPlayer().setGameMode(GameMode.SPECTATOR);
        playersLeft.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Location firstLoc = map.getFirstLocation(0).getBlock().getLocation();
        Location secondLoc = map.getSecondLocation(0).getBlock().getLocation().add(1, 1, 1);

        boolean inside = GameMakerUtils.isEntityInsideRegion(player, firstLoc, secondLoc);

        if (!inside && playersLeft.contains(uuid) && !gameEnded) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_DEATH, 2f, 1f);
            playersLeft.remove(uuid);
            player.setGameMode(GameMode.SPECTATOR);
            checkForWinner();
        }
    }

    @Override
    public void end(boolean forced) {}
}

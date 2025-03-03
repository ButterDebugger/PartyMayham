package com.butterycode.partymayhem.manager;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.utils.GameMakerUtils;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import dev.debutter.cuberry.paper.utils.Caboodle;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class EditorManager implements Listener {

    private static HashMap<UUID, Boolean> editorStatus = new HashMap<>();
    private static final HashMap<UUID, Location> playerFirstPos = new HashMap<>();
    private static final HashMap<UUID, Location> playerSecondPos = new HashMap<>();

    public static void init() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PartyMayhem.getPlugin(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();

                if (!playerFirstPos.containsKey(uuid) || !playerSecondPos.containsKey(uuid)) continue;

                Location firstPos = playerFirstPos.get(uuid);
                Location secondPos = playerSecondPos.get(uuid);

                Vector lowestVec = Caboodle.getLowestPoint(firstPos.getBlock().getLocation().toVector(), secondPos.getBlock().getLocation().toVector());
                Vector highestVec = Caboodle.getHighestPoint(firstPos.getBlock().getLocation().toVector(), secondPos.getBlock().getLocation().toVector());

                Location lowestLoc = new Location(firstPos.getWorld(), lowestVec.getX(), lowestVec.getY(), lowestVec.getZ());
                Location highestLoc = new Location(firstPos.getWorld(), highestVec.getX() + 1, highestVec.getY() + 1, highestVec.getZ() + 1);

                // Create outline
                GameMakerUtils.outline(player, lowestLoc, highestLoc, Particle.DRAGON_BREATH, 0.25d);
            }
        }, 0, 10);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        if (!isEditing(uuid)) return;

        if (!event.hasItem() || event.getHand() == null || event.getHand().equals(EquipmentSlot.OFF_HAND)) return; // Cancel if item is in the offhand slot
        assert item != null;

        if (!item.getType().equals(Material.GOLDEN_AXE)) return;

        if (action.equals(Action.LEFT_CLICK_BLOCK)) {
            assert block != null;

            Location blockLoc = block.getLocation();

            Location secondPos = getSecondPos(uuid);
            if (secondPos != null && !secondPos.getWorld().equals(blockLoc.getWorld())) {
                player.playSound(blockLoc, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7You must have the first point share the same world as the second point."));

                event.setCancelled(true);
                return;
            }

            playerFirstPos.put(uuid, blockLoc);
            player.playSound(blockLoc, Sound.BLOCK_NOTE_BLOCK_HAT, 2f, 1.2f);
            player.sendMessage(AwesomeText.colorizeHex("&a&l» &7First point set."));
            GameMakerUtils.outline(player, block, Particle.END_ROD, 0.25d);

            event.setCancelled(true);
        } else if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            assert block != null;

            Location blockLoc = block.getLocation();

            Location firstPos = getFirstPos(uuid);
            if (firstPos != null && !firstPos.getWorld().equals(blockLoc.getWorld())) {
                player.playSound(blockLoc, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7You must have the second point share the same world as the first point."));

                event.setCancelled(true);
                return;
            }

            playerSecondPos.put(uuid, blockLoc);
            player.playSound(blockLoc, Sound.BLOCK_NOTE_BLOCK_HAT, 2f, 0.8f);
            player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Second point set."));
            GameMakerUtils.outline(player, block, Particle.END_ROD, 0.25d);

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ItemStack item = event.getItemDrop().getItemStack();

        if (!isEditing(uuid)) return;

        if (!item.getType().equals(Material.GOLDEN_AXE)) return;

        if (playerFirstPos.containsKey(uuid) || playerSecondPos.containsKey(uuid)) {
            clearSelection(uuid);

            player.playSound(player, Sound.ITEM_AXE_SCRAPE, 2f, 1f);
            player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Area deselected."));
            event.setCancelled(true);
        }
    }

    public static void clearSelection(UUID uuid) {
        playerFirstPos.remove(uuid);
        playerSecondPos.remove(uuid);
    }

    public static Location getFirstPos(UUID uuid) {
        return playerFirstPos.get(uuid);
    }
    public static Location setFirstPos(UUID uuid, Location loc) {
        return playerFirstPos.put(uuid, loc);
    }

    public static Location getSecondPos(UUID uuid) {
        return playerSecondPos.get(uuid);
    }
    public static Location setSecondPos(UUID uuid, Location loc) {
        return playerSecondPos.put(uuid, loc);
    }

    public static boolean hasSelection(UUID uuid) {
        return playerFirstPos.containsKey(uuid) && playerSecondPos.containsKey(uuid);
    }

    public static boolean addEditor(UUID uuid) {
        if (GameManager.getGameState().isRunning()) return false;

        editorStatus.put(uuid, true);
        return true;
    }
    public static void revokeEditor(UUID uuid) {
        editorStatus.put(uuid, false);
        EditorManager.clearSelection(uuid);
    }
    public static void revokeAllEditors() {
        for (UUID uuid : editorStatus.keySet()) {
            revokeEditor(uuid);
        }
    }
    public static boolean isEditing(UUID uuid) {
        return editorStatus.getOrDefault(uuid, false);
    }
}

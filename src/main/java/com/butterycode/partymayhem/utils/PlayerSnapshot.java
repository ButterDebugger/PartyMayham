package com.butterycode.partymayhem.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class PlayerSnapshot {

    private final Player player;
    private final Long timestamp;

    private final Location location;
    private final ItemStack[] inventoryContents;
    private final ItemStack[] inventoryArmorContents;
    private final ItemStack[] inventoryExtraContents;
    private final ItemStack[] inventoryStorageContents;
    private final int slot;
    private final GameMode gameMode;
    private final boolean allowFlight;
    private final boolean flying;
    private final Component displayName;
    private final Component playerListName;
    private final @Nullable Team team;

    // TODO:
    //  - ender chest contents
    //  - health and saturation

    public PlayerSnapshot(Player player) {
        this.player = player;
        timestamp = System.currentTimeMillis();

        location = player.getLocation();
        inventoryContents = player.getInventory().getContents();
        inventoryArmorContents = player.getInventory().getArmorContents();
        inventoryExtraContents = player.getInventory().getExtraContents();
        inventoryStorageContents = player.getInventory().getStorageContents();
        slot = player.getInventory().getHeldItemSlot();
        gameMode = player.getGameMode();
        allowFlight = player.getAllowFlight();
        flying = player.isFlying();
        displayName = player.displayName();
        playerListName = player.playerListName();
        team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
    }

    public Player getPlayer() {
        return player;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public Location getLocation() {
        return location;
    }
    public ItemStack[] getInventoryContents() {
        return inventoryContents;
    }
    public ItemStack[] getInventoryArmorContents() {
        return inventoryArmorContents;
    }
    public ItemStack[] getInventoryExtraContents() {
        return inventoryExtraContents;
    }
    public ItemStack[] getInventoryStorageContents() {
        return inventoryStorageContents;
    }
    public int getSlot() {
        return slot;
    }
    public GameMode getGameMode() {
        return gameMode;
    }
    public boolean isAllowFlight() {
        return allowFlight;
    }
    public boolean isFlying() {
        return flying;
    }
    public Component getDisplayName() {
        return displayName;
    }
    public Component getPlayerListName() {
        return playerListName;
    }

    /*
     *  Player reset functions
     */

    public static void reset(Player player) {
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.setGameMode(Bukkit.getDefaultGameMode());
        player.setAllowFlight(false);
        player.setFlying(false);
        player.displayName(player.name());
        player.playerListName(player.name());

        @Nullable Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        if (team != null) team.removePlayer(player);

        // TODO: save this information
        player.setHealth(20); // Use player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setSaturatedRegenRate(10);
    }

    /*
     *  Player restore functions
     */

    public void restore() {
        restoreLocation();
        restoreInventory();
        restoreGameMode();
        restoreFlight();
        restoreNames();
        restoreTeam();
    }

    /*
     *  Individual restore functions
     */

    public void restoreLocation() {
        player.teleport(location);
    }
    public void restoreInventory() {
        PlayerInventory playerInventory = player.getInventory();

        playerInventory.setContents(inventoryContents);
        playerInventory.setArmorContents(inventoryArmorContents);
        playerInventory.setExtraContents(inventoryExtraContents);
        playerInventory.setStorageContents(inventoryStorageContents);
        playerInventory.setHeldItemSlot(slot);
    }
    public void restoreGameMode() {
        player.setGameMode(gameMode);
    }
    public void restoreFlight() {
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);
    }
    public void restoreNames() {
        player.displayName(displayName);
        player.playerListName(playerListName);
    }
    public void restoreTeam() {
        if (team != null) team.addPlayer(player);
    }

}

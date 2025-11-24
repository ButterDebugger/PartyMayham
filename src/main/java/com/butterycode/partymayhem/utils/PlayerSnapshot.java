package com.butterycode.partymayhem.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSnapshot {

    private final @NotNull Player player;
    private final @NotNull Long timestamp;

    private final @NotNull Location location;
    private final @Nullable ItemStack[] inventoryContents;
    private final @Nullable ItemStack[] inventoryArmorContents;
    private final @Nullable ItemStack[] inventoryExtraContents;
    private final @Nullable ItemStack[] inventoryStorageContents;
    private final int slot;
    private final @NotNull GameMode gameMode;
    private final boolean allowFlight;
    private final boolean flying;
    private final @NotNull Component displayName;
    private final @NotNull Component playerListName;
    private final @Nullable Team team;
    private final @NotNull ConcurrentHashMap<Attribute, Collection<AttributeModifier>> attributeModifiers;
    private final @NotNull ConcurrentHashMap<Attribute, Double> attributeBaseValues;

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

        // Collect the players attributes
        attributeModifiers = new ConcurrentHashMap<>();
        attributeBaseValues = new ConcurrentHashMap<>();

        for (Attribute attribute : Registry.ATTRIBUTE) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) continue;

            attributeModifiers.put(attribute, instance.getModifiers());
            attributeBaseValues.put(attribute, instance.getBaseValue());
        }
    }

    public @NotNull Player getPlayer() {
        return player;
    }
    public @NotNull Long getTimestamp() {
        return timestamp;
    }
    public @NotNull Location getLocation() {
        return location;
    }
    public @Nullable ItemStack[] getInventoryContents() {
        return inventoryContents;
    }
    public @Nullable ItemStack[] getInventoryArmorContents() {
        return inventoryArmorContents;
    }
    public @Nullable ItemStack[] getInventoryExtraContents() {
        return inventoryExtraContents;
    }
    public @Nullable ItemStack[] getInventoryStorageContents() {
        return inventoryStorageContents;
    }
    public int getSlot() {
        return slot;
    }
    public @NotNull GameMode getGameMode() {
        return gameMode;
    }
    public boolean isAllowFlight() {
        return allowFlight;
    }
    public boolean isFlying() {
        return flying;
    }
    public @NotNull Component getDisplayName() {
        return displayName;
    }
    public @NotNull Component getPlayerListName() {
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

        for (Attribute attribute : Registry.ATTRIBUTE) {
            AttributeInstance instance = player.getAttribute(attribute);
            AttributeInstance defaultInstance = EntityType.PLAYER.getDefaultAttributes().getAttribute(attribute);
            if (instance == null || defaultInstance == null) continue;

            for (AttributeModifier modifier : instance.getModifiers()) {
                instance.removeModifier(modifier);
            }

            instance.setBaseValue(defaultInstance.getBaseValue());
        }
    }

    /*
     *  Player restore method
     */

    public void restore() {
        restoreLocation();
        restoreInventory();
        restoreGameMode();
        restoreFlight();
        restoreNames();
        restoreTeam();
        restoreAttributes();
    }

    /*
     *  Individual restore methods
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
    public void restoreAttributes() {
        for (Attribute attribute : attributeModifiers.keySet()) {
            Collection<AttributeModifier> modifiers = attributeModifiers.get(attribute);
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) continue;

            for (AttributeModifier modifier : modifiers) {
                instance.removeModifier(modifier);
                instance.addModifier(modifier);
            }
        }

        for (Attribute attribute : attributeBaseValues.keySet()) {
            Double base = attributeBaseValues.get(attribute);
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) continue;

            instance.setBaseValue(base);
        }
    }

}

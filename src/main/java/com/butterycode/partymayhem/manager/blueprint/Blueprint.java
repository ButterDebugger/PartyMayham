package com.butterycode.partymayhem.manager.blueprint;

import com.butterycode.partymayhem.PartyMayhem;
import com.butterycode.partymayhem.games.MinigameFactory;
import dev.debutter.cuberry.paper.utils.storage.DataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public abstract class Blueprint {

    private final String namespace;
    private final MinigameFactory minigame;
    private final String blueprintName;
    private final int minAmount;
    private final int maxAmount;

    protected Blueprint(@NotNull String namespace, @NotNull MinigameFactory minigame, @NotNull String blueprintName, int minAmount, int maxAmount) {
        if (minAmount < 1) throw new IllegalArgumentException("The minimum amount must be larger than 0");
        if (maxAmount < minAmount) throw new IllegalArgumentException("The maximum amount cannot be smaller than the minimum amount");

        this.namespace = namespace;
        this.minigame = minigame;
        this.blueprintName = blueprintName;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;

        minigame.registerBlueprint(this);
    }
    protected Blueprint(@NotNull String namespace, @NotNull MinigameFactory minigame, @NotNull String blueprintName, int amount) {
        this(namespace, minigame, blueprintName, amount, amount);
    }

    /** @return Whether the blueprint data at the specified index is valid */
    public abstract boolean isIndexValid(int index);
    /** Loads saved blueprint data */
    public abstract boolean load();
    /** Saves blueprint data */
    public abstract boolean save();
    /** Deletes blueprint data at the specified index */
    public abstract boolean delete(int index);

    /** @return The data storage for the blueprint */
    protected DataStorage getData() {
        return PartyMayhem.getData().getStorage(minigame.getId() + "/" + namespace + "/" + blueprintName + ".yml");
    }

    /** @return Whether the blueprint is meets setup */
    public boolean status() {
        return getValidIndexes().size() >= minAmount;
    }
    public Set<Integer> getValidIndexes() {
        HashSet<Integer> validIndexes = new HashSet<>();

        for (int i = 0; i < maxAmount; i++) {
            if (isIndexValid(i)) {
                validIndexes.add(i);
            }
        }

        return validIndexes;
    }
    public String getNamespace() {
        return namespace;
    }
    public MinigameFactory getMinigame() {
        return minigame;
    }
    public String getBlueprintName() {
        return blueprintName;
    }
    public int getMinAmount() {
        return minAmount;
    }
    public int getMaxAmount() {
        return maxAmount;
    }
}

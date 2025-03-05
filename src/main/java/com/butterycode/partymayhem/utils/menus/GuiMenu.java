package com.butterycode.partymayhem.utils.menus;

import com.butterycode.partymayhem.utils.menus.handlers.GuiClick;
import com.butterycode.partymayhem.utils.menus.handlers.GuiHandler;
import com.butterycode.partymayhem.utils.menus.handlers.GuiItemClick;
import com.butterycode.partymayhem.utils.menus.handlers.GuiSlotClick;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class GuiMenu implements InventoryHolder {

    private final Inventory inventory;
    protected final ArrayList<GuiHandler<InventoryClickEvent>> clickHandlers;
    protected final ArrayList<Consumer<InventoryDragEvent>> dragHandlers;
    protected final ArrayList<Consumer<InventoryCloseEvent>> closeHandlers;
    protected final ArrayList<Consumer<InventoryOpenEvent>> openHandlers;

    protected boolean disposeWithNoViewers = true;

    public GuiMenu(InventoryType inventoryType, Component title) {
        this.inventory = Bukkit.createInventory(this, inventoryType, title);
        this.clickHandlers = new ArrayList<>();
        this.dragHandlers = new ArrayList<>();
        this.closeHandlers = new ArrayList<>();
        this.openHandlers = new ArrayList<>();

        GuiMenuManager.registerMenu(this);
    }

    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open(Player... players) {
        for (Player player : players) {
            player.openInventory(this.inventory);
        }
    }

    public void close(Player... players) {
        for (Player player : players) {
            if (inventory.getViewers().contains(player)) player.closeInventory();
        }
    }

    public void dispose() {
        // Closes the menu for all viewers
        for (HumanEntity viewer : inventory.getViewers()) {
            viewer.closeInventory();
        }

        // Unregisters the menu
        GuiMenuManager.unregisterMenu(this);
    }

    public void disposeWithNoViewers(boolean enabled) {
        this.disposeWithNoViewers = enabled;
    }

    /*
     * Event handler register functions
     */

    public GuiMenu onClick(Function<InventoryClickEvent, Boolean> handler) {
        this.clickHandlers.add(new GuiClick(handler));
        return this;
    }
    public GuiMenu onClick(ItemStack item, Function<InventoryClickEvent, Boolean> handler) {
        this.clickHandlers.add(new GuiItemClick(item, handler));
        return this;
    }
    public GuiMenu onClick(int slot, Function<InventoryClickEvent, Boolean> handler) {
        this.clickHandlers.add(new GuiSlotClick(slot, handler));
        return this;
    }

    public GuiMenu onDrag(Consumer<InventoryDragEvent> handler) {
        this.dragHandlers.add(handler);
        return this;
    }

    public GuiMenu onClose(Consumer<InventoryCloseEvent> handler) {
        this.closeHandlers.add(handler);
        return this;
    }

    public GuiMenu onOpen(Consumer<InventoryOpenEvent> handler) {
        this.openHandlers.add(handler);
        return this;
    }

}

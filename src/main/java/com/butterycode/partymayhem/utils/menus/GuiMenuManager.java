package com.butterycode.partymayhem.utils.menus;

import com.butterycode.partymayhem.utils.menus.handlers.GuiHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.function.Consumer;

public class GuiMenuManager implements Listener {

    private static final HashSet<GuiMenu> menus = new HashSet<>();

    protected static void registerMenu(GuiMenu menu) {
        menus.add(menu);
    }

    public static void unregisterMenu(GuiMenu menu) {
        menus.remove(menu);
    }

    /*
     * Inventory event listeners
     */

    @EventHandler
    private void onInventoryClick(@NotNull InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        InventoryHolder holder = inventory.getHolder();
        if (holder == null) return;

        // Find the menu that matched the inventory firing the event
        for (GuiMenu menu : menus) {
            if (!menu.equals(holder)) continue;

            // Trigger the event handlers
            for (GuiHandler<InventoryClickEvent> handler : menu.clickHandlers) {
                if (handler.trigger(event)) break;
            }
            break;
        }
    }

    @EventHandler
    private void onInventoryDrag(@NotNull InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder == null) return;

        // Find the menu that matched the inventory firing the event
        for (GuiMenu menu : menus) {
            if (!menu.equals(holder)) continue;

            // Trigger the event handlers
            for (Consumer<InventoryDragEvent> handler : menu.dragHandlers) {
                handler.accept(event);
            }
            break;
        }
    }

    @EventHandler
    private void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder == null) return;

        // Find the menu that matched the inventory firing the event
        for (GuiMenu menu : menus) {
            if (!menu.equals(holder)) continue;

            // Trigger the event handlers
            for (Consumer<InventoryCloseEvent> handler : menu.closeHandlers) {
                handler.accept(event);
            }
            // Dispose of the inventory menu if there are no viewers left
            if (menu.disposeWithNoViewers && menu.getInventory().getViewers().isEmpty()) {
                menu.dispose();
            }
            break;
        }
    }

    @EventHandler
    private void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder == null) return;

        // Find the menu that matched the inventory firing the event
        for (GuiMenu menu : menus) {
            if (!menu.equals(holder)) continue;

            // Trigger the event handlers
            for (Consumer<InventoryOpenEvent> handler : menu.openHandlers) {
                handler.accept(event);
            }
            break;
        }
    }

}

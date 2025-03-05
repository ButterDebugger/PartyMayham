package com.butterycode.partymayhem.utils.menus.handlers;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public record GuiItemClick(ItemStack item, Function<InventoryClickEvent, Boolean> handler) implements GuiHandler<InventoryClickEvent> {

    @Override
    public boolean trigger(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return false;
        if (!clickedItem.isSimilar(item)) return false;

        return handler.apply(event);
    }

}

package com.butterycode.partymayhem.utils.menus.handlers;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Function;

public record GuiSlotClick(int slot, Function<InventoryClickEvent, Boolean> handler) implements GuiHandler<InventoryClickEvent> {

    @Override
    public boolean trigger(InventoryClickEvent event) {
        int clickedSlot = event.getSlot();
        if (clickedSlot != slot) return false;

        return handler.apply(event);
    }

}

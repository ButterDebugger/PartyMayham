package com.butterycode.partymayhem.utils.menus.handlers;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Function;

public record GuiClick(Function<InventoryClickEvent, Boolean> handler) implements GuiHandler<InventoryClickEvent> {

    @Override
    public boolean trigger(InventoryClickEvent event) {
        return handler.apply(event);
    }

}

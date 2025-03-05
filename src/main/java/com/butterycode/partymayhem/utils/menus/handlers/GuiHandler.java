package com.butterycode.partymayhem.utils.menus.handlers;

import org.bukkit.event.Event;

public interface GuiHandler<E extends Event> {

    /** @return Whether the trigger condition was met and was handled further */
    boolean trigger(E event);

}

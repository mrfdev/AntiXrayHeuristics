//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public final class HandledPlayerEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player handledPlayer;
    private boolean cancelled;

    public HandledPlayerEvent(Player handled) {
        handledPlayer = handled;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public Player getHandledPlayer() {
        return handledPlayer;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public @NonNull HandlerList getHandlers() {
        return handlers;
    }
}
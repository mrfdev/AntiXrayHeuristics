//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class HandlingXrayerEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player handledPlayer;
    private boolean cancelled;

    public HandlingXrayerEvent(Player handled) {
        handledPlayer = handled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getHandledPlayer() {
        return handledPlayer;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.event;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.MiningSession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jspecify.annotations.NonNull;

public class PlayerWorldChangeListener implements Listener {

    private final XRayHeuristicsModule module;

    public PlayerWorldChangeListener(XRayHeuristicsModule main) {
        this.module = main;
    }

    @EventHandler
    public void PlayerChangedWorldEvent(@NonNull PlayerChangedWorldEvent e) //This event cleans the mining trail, and previous mined ore data, when switching worlds (avoids errors)
    {
        MiningSession session = module.sessions.get(e.getPlayer().getName());
        if (session != null) { //Checking the player who switched worlds actually has a mining session.
            session.SetLastMinedOreData(null, null);
            session.ResetBlocksTrailArray();
            session.ResetBlockCoordsStoreCounter();
        }
    }
}

//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.event;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.MiningSession;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class ExplosiveBlockPlaceListener implements Listener {

    private final XRayHeuristicsModule module;

    public ExplosiveBlockPlaceListener(XRayHeuristicsModule main) {
        this.module = main;
    }

    //Tells session for player name (if exists) that an explosive block was placed
    private void NotifySession(String playername) {
        MiningSession s = module.sessions.get(playername);
        if (s != null) s.IncreaseExplosivesPlaced();
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent e) {
        //EXPLOSIVE ASSISTED MINING CHECK:
        //Check if the event occurred in one of the configured worlds:
        for (int i = 0; i < module.getConfig().getStringList("TrackWorlds").size(); i++) {
            if (module.getConfig().getStringList("TrackWorlds").get(i).equals(e.getBlock().getWorld().getName())) //It's one of the whitelisted "TrackWorlds"
            {
                //Is it overworld?:
                if (e.getBlock().getWorld().getEnvironment() == World.Environment.NORMAL) {
                    //Relevant explosive materials check:
                    switch (e.getBlock().getType()) {
                        case TNT:
                        case END_CRYSTAL: {
                            NotifySession(e.getPlayer().getName());
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    //Spigot for MC 1.16+
                    if (e.getBlock().getType() == Material.RESPAWN_ANCHOR) {
                        NotifySession(e.getPlayer().getName());
                        break;
                    }
                }
                //Is it nether?:
                if (e.getBlock().getWorld().getEnvironment() == World.Environment.NETHER) {
                    //Relevant explosive materials check:
                    switch (e.getBlock().getType()) {
                        case TNT:
                        case END_CRYSTAL:
                        case WHITE_BED:
                        case BLACK_BED:
                        case GRAY_BED:
                        case LIGHT_GRAY_BED:
                        case BLUE_BED:
                        case CYAN_BED:
                        case LIGHT_BLUE_BED:
                        case YELLOW_BED:
                        case GREEN_BED:
                        case LIME_BED:
                        case ORANGE_BED:
                        case RED_BED:
                        case BROWN_BED:
                        case PURPLE_BED:
                        case PINK_BED:
                        case MAGENTA_BED: {
                            //Adapt suspicion for session
                            NotifySession(e.getPlayer().getName());
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
                break;
            }
        }
    }
}

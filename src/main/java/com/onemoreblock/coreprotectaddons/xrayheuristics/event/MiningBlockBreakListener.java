//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.event;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningBlockBreakListener implements Listener {

    private final XRayHeuristicsModule module;

    public MiningBlockBreakListener(XRayHeuristicsModule main) {
        this.module = main;
    }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent e) {
        //BLOCK MINING CHECK:
        //Check if the event occurred in one of the configured worlds:
        for (int i = 0; i < module.getConfig().getStringList("TrackWorlds").size(); i++) {
            if (module.getConfig().getStringList("TrackWorlds").get(i).equals(e.getBlock().getWorld().getName())) //It's one of the whitelisted "TrackWorlds"
            {
                //Only consider if it's a normal overworld or nether environment:
                if (e.getBlock().getWorld().getEnvironment() == World.Environment.NORMAL) {
                    if (e.getBlock().getLocation().getY() < module.getConfig().getInt("IgnoreHigherThanOverworldAltitude")) {
                        module.BBEventAnalyzer(e);
                        break;
                    }
                } else if (e.getBlock().getWorld().getEnvironment() == World.Environment.NETHER) {
                    if (e.getBlock().getLocation().getY() < module.getConfig().getInt("IgnoreHigherThanNetherAltitude")) {
                        module.BBEventAnalyzer(e);
                        break;
                    }
                }
                break;
            }
        }
    }
}
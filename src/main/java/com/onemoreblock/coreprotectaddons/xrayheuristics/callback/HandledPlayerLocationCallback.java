package com.onemoreblock.coreprotectaddons.xrayheuristics.callback;

import org.bukkit.Location;

public interface HandledPlayerLocationCallback {
    void onQueryDone(Location handlelocation);
}
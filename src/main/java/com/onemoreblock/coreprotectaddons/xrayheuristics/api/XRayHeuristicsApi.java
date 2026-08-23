package com.onemoreblock.coreprotectaddons.xrayheuristics.api;

import org.jspecify.annotations.NonNull;

/** Public API exposed by the X-ray Heuristics feature in either host. */
public interface XRayHeuristicsApi {
    /** Runs the configured handled-player workflow for an online player name. */
    void handlePlayer(@NonNull String playerName);

    /** Purges an online player's handled-player record without returning belongings. */
    void purgePlayer(@NonNull String playerName);

    /** Returns stored belongings to an online player and removes the handled-player record. */
    void absolvePlayer(@NonNull String playerName);
}

//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.util;

public class PlayerViewInfo {
    public final int page; //Page Player is on

    public String handledPlayerUuid;

    public PlayerViewInfo(int pag) {
        page = pag;
        handledPlayerUuid = null;
    }
}

//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

public class PlayerViewInfo {
    public final int page; //Page Player is on

    public String xrayerInvUUID; //Current or last viewed xrayer vault's Player UUID

    PlayerViewInfo(int pag) {
        page = pag;
        xrayerInvUUID = null;
    }
}

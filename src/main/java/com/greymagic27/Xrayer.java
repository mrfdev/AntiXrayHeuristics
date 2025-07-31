//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

public class Xrayer {

    public final String UUID;
    public int Handled;
    public final String FirstHandleTime;
    public final String HandleLocation;
    public final String Belongings;

    public Xrayer(String uuid, int handled, String firsthandletime, String handlelocation, String belongings) {
        UUID = uuid;
        Handled = handled;
        FirstHandleTime = firsthandletime;
        HandleLocation = handlelocation;
        Belongings = belongings;
    }
}

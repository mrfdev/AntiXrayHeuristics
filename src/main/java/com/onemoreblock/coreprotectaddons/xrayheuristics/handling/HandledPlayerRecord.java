//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.handling;

public class HandledPlayerRecord {

    public final String UUID;
    public final String FirstHandleTime;
    public final String HandleLocation;
    public final String Belongings;
    public int Handled;

    public HandledPlayerRecord(String uuid, int handled, String firstHandleTime, String handleLocation, String belongings) {
        UUID = uuid;
        Handled = handled;
        FirstHandleTime = firstHandleTime;
        HandleLocation = handleLocation;
        Belongings = belongings;
    }
}

package com.greymagic27.api;

/** Legacy API retained for source and binary compatibility. */
@Deprecated(forRemoval = false)
public interface APIAntiXrayHeuristics {
    void Xrayer(String xrayername);

    void PurgePlayer(String playerName);

    void AbsolvePlayer(String playerName);
}

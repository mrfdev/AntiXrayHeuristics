package com.greymagic27.api;

import com.greymagic27.AntiXrayHeuristics;
import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsPlugin;
import com.onemoreblock.coreprotectaddons.xrayheuristics.api.XRayHeuristicsApi;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/** Legacy method-name adapter for the maintained API. */
@Deprecated(forRemoval = false)
public final class APIAntiXrayHeuristicsImpl implements APIAntiXrayHeuristics {
    private final XRayHeuristicsApi delegate;

    public APIAntiXrayHeuristicsImpl(@NonNull AntiXrayHeuristics plugin) {
        this(requireCanonicalPlugin(plugin).getApi());
    }

    public APIAntiXrayHeuristicsImpl(@NonNull XRayHeuristicsApi delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void Xrayer(String xrayerName) {
        delegate.handlePlayer(xrayerName);
    }

    @Override
    public void PurgePlayer(String playerName) {
        delegate.purgePlayer(playerName);
    }

    @Override
    public void AbsolvePlayer(String playerName) {
        delegate.absolvePlayer(playerName);
    }

    private static @NonNull XRayHeuristicsPlugin requireCanonicalPlugin(@NonNull AntiXrayHeuristics plugin) {
        if (plugin instanceof XRayHeuristicsPlugin canonicalPlugin) {
            return canonicalPlugin;
        }
        throw new IllegalArgumentException("Unsupported legacy X-ray Heuristics plugin implementation: " + plugin.getClass().getName());
    }
}

package com.greymagic27;

import com.greymagic27.api.APIAntiXrayHeuristics;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Legacy superclass retained so integrations compiled against the original plugin entry point can
 * still discover the standalone plugin. New integrations should use
 * {@code com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsPlugin}.
 */
@Deprecated(forRemoval = false)
public abstract class AntiXrayHeuristics extends JavaPlugin {
    @SuppressWarnings("unused")
    public static AntiXrayHeuristics GetPlugin() {
        return JavaPlugin.getPlugin(AntiXrayHeuristics.class);
    }

    @SuppressWarnings("unused")
    public abstract APIAntiXrayHeuristics GetAPI();
}

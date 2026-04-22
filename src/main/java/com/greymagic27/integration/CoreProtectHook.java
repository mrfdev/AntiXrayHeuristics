package com.greymagic27.integration;

import com.greymagic27.AntiXrayHeuristics;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

public final class CoreProtectHook {
    private static final int REQUIRED_API_VERSION = 11;
    private final AntiXrayHeuristics plugin;
    private @Nullable CoreProtect coreProtect;
    private @Nullable CoreProtectAPI api;
    private String statusMessage = "Not checked yet.";

    public CoreProtectHook(@org.jspecify.annotations.NonNull AntiXrayHeuristics plugin) {
        this.plugin = plugin;
    }

    public boolean refresh() {
        Plugin candidate = plugin.getServer().getPluginManager().getPlugin("CoreProtect");
        if (!(candidate instanceof CoreProtect coreProtectPlugin)) {
            this.coreProtect = null;
            this.api = null;
            this.statusMessage = "CoreProtect plugin not found.";
            return false;
        }
        if (!candidate.isEnabled()) {
            this.coreProtect = null;
            this.api = null;
            this.statusMessage = "CoreProtect is installed but not enabled.";
            return false;
        }

        CoreProtectAPI candidateApi = coreProtectPlugin.getAPI();
        if (candidateApi == null) {
            this.coreProtect = null;
            this.api = null;
            this.statusMessage = "CoreProtect API was not available.";
            return false;
        }

        int apiVersion = candidateApi.APIVersion();
        if (apiVersion < REQUIRED_API_VERSION) {
            this.coreProtect = null;
            this.api = null;
            this.statusMessage = "CoreProtect API " + apiVersion + " is too old. Required: " + REQUIRED_API_VERSION + ".";
            return false;
        }

        this.coreProtect = coreProtectPlugin;
        this.api = candidateApi;
        this.statusMessage = "Hooked into CoreProtect " + coreProtectPlugin.getDescription().getVersion() + " (API " + apiVersion + ").";
        return true;
    }

    public boolean isHooked() {
        return this.coreProtect != null && this.api != null && this.api.isEnabled();
    }

    public @Nullable CoreProtectAPI getApi() {
        return api;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getCoreProtectVersion() {
        return coreProtect != null ? coreProtect.getDescription().getVersion() : "unavailable";
    }

    public String getCoreProtectApiVersion() {
        return api != null ? Integer.toString(api.APIVersion()) : "unavailable";
    }

    public String getSummaryLine() {
        return isHooked()
                ? "CoreProtect " + getCoreProtectVersion() + " (API " + getCoreProtectApiVersion() + ")"
                : statusMessage;
    }

    public String getJarPath() {
        if (coreProtect == null) {
            return "unavailable";
        }
        try {
            return new File(
                    Objects.requireNonNull(coreProtect.getClass().getProtectionDomain().getCodeSource()).getLocation().toURI()
            ).getAbsolutePath();
        } catch (URISyntaxException | NullPointerException e) {
            return "unavailable";
        }
    }

    public String getDataFolderPath() {
        return coreProtect != null ? coreProtect.getDataFolder().getAbsolutePath() : "unavailable";
    }
}

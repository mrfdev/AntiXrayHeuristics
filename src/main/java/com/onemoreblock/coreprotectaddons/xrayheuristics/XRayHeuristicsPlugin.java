package com.onemoreblock.coreprotectaddons.xrayheuristics;

import com.greymagic27.AntiXrayHeuristics;
import com.greymagic27.api.APIAntiXrayHeuristics;
import com.greymagic27.api.APIAntiXrayHeuristicsImpl;
import com.onemoreblock.coreprotectaddons.xrayheuristics.api.XRayHeuristicsApi;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.BuildMetadata;
import java.io.File;
import java.util.Objects;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.NonNull;

/** Standalone Paper adapter for the reusable X-ray Heuristics feature runtime. */
@SuppressWarnings("deprecation")
public final class XRayHeuristicsPlugin extends AntiXrayHeuristics {
    private XRayHeuristicsModule module;
    private APIAntiXrayHeuristics legacyApi;

    @Override
    public void onEnable() {
        validateStandaloneBuildMetadata(BuildMetadata.load(this));
        module = new XRayHeuristicsModule(this, resolveStandaloneDataDirectory());
        if (!module.enable()) {
            getLogger().severe("X-ray Heuristics could not start. Disabling the standalone plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        legacyApi = new APIAntiXrayHeuristicsImpl(module.getApi());
        module.registerStandaloneCommand(Objects.requireNonNull(getCommand("xrayer"), "Missing /xrayer command metadata."));
    }

    @Override
    public void onDisable() {
        if (module != null) {
            module.disable();
        }
    }

    public @NonNull XRayHeuristicsModule getModule() {
        return Objects.requireNonNull(module, "X-ray Heuristics module has not been initialized yet.");
    }

    public @NonNull XRayHeuristicsApi getApi() {
        return getModule().getApi();
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NonNull APIAntiXrayHeuristics GetAPI() {
        return Objects.requireNonNull(legacyApi, "Legacy X-ray Heuristics API has not been initialized yet.");
    }

    public @NonNull File getPluginDataDirectory() {
        return module != null ? module.getPluginDataDirectory() : resolveStandaloneDataDirectory();
    }

    public @NonNull File getPluginDataFile(@NonNull String fileName) {
        return new File(getPluginDataDirectory(), fileName);
    }

    @Override
    public FileConfiguration getConfig() {
        return module != null ? module.getConfig() : super.getConfig();
    }

    @Override
    public void reloadConfig() {
        if (module != null) {
            module.reloadConfig();
        } else {
            super.reloadConfig();
        }
    }

    @Override
    public void saveConfig() {
        if (module != null) {
            module.saveConfig();
        } else {
            super.saveConfig();
        }
    }

    @Override
    public void saveDefaultConfig() {
        if (module != null) {
            module.saveDefaultConfig();
        } else {
            super.saveDefaultConfig();
        }
    }

    @Override
    public void saveResource(@NonNull String resourcePath, boolean replace) {
        if (module != null) {
            module.saveResource(resourcePath, replace);
        } else {
            super.saveResource(resourcePath, replace);
        }
    }

    private @NonNull File resolveStandaloneDataDirectory() {
        File defaultDataDirectory = super.getDataFolder();
        File pluginsDirectory = defaultDataDirectory.getParentFile();
        if (pluginsDirectory == null) {
            pluginsDirectory = defaultDataDirectory;
        }
        return new File(pluginsDirectory, XRayHeuristicsModule.STANDALONE_DATA_DIRECTORY_NAME);
    }

    private void validateStandaloneBuildMetadata(@NonNull BuildMetadata metadata) {
        if (!getPluginMeta().getVersion().equals(metadata.artifactVersion())) {
            throw new IllegalStateException(
                    "plugin.yml version '" + getPluginMeta().getVersion()
                            + "' does not match build metadata '" + metadata.artifactVersion() + "'."
            );
        }
        if (!getPluginMeta().getAPIVersion().equals(metadata.declaredApiVersion())) {
            throw new IllegalStateException(
                    "plugin.yml api-version '" + getPluginMeta().getAPIVersion()
                            + "' does not match build metadata '" + metadata.declaredApiVersion() + "'."
            );
        }
    }
}

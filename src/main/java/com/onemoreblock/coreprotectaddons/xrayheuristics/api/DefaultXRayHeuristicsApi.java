package com.onemoreblock.coreprotectaddons.xrayheuristics.api;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public final class DefaultXRayHeuristicsApi implements XRayHeuristicsApi {
    private final XRayHeuristicsModule module;

    public DefaultXRayHeuristicsApi(@NonNull XRayHeuristicsModule module) {
        this.module = Objects.requireNonNull(module, "module");
    }

    @Override
    public void handlePlayer(@NonNull String playerName) {
        module.getHandledPlayerService().handlePlayer(playerName);
    }

    @Override
    public void purgePlayer(@NonNull String playerName) {
        Player target = Bukkit.getServer().getPlayer(playerName);
        if (target != null) {
            module.handledPlayerVault.XrayerDataRemover(playerName, false);
        }
    }

    @Override
    public void absolvePlayer(@NonNull String playerName) {
        Player target = Bukkit.getServer().getPlayer(playerName);
        if (target != null) {
            final String targetUUID = target.getUniqueId().toString();
            Bukkit.getScheduler().runTaskAsynchronously(module.getHostPlugin(), () -> module.handledPlayerStore.GetXrayerBelongings(targetUUID, belongings -> {
                if (module.getHandledPlayerService().restoreBelongings(targetUUID, belongings)) {
                    module.handledPlayerVault.XrayerDataRemover(playerName, false);
                }
            }));
        }
    }
}

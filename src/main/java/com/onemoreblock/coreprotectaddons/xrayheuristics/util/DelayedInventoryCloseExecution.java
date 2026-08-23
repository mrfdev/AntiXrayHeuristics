//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.util;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;

public class DelayedInventoryCloseExecution implements Runnable {
    private final HumanEntity p;
    private final XRayHeuristicsModule module;

    public DelayedInventoryCloseExecution(HumanEntity player, XRayHeuristicsModule mca) {
        p = player;
        module = mca;
    }

    public void run() //Xrayer vault clean-up, and additional clean-up if no one is inspecting the vault:
    {
        if (!(p.getOpenInventory().title().equals(Component.text("Xrayer Vault")))) {
            module.handledPlayerVault.RemovePlayerAsViewer(p.getName()); //Remove the player as viewer inconditionally
            //Clear loaded xrayer information in vault from RAM if no one is still viewing the GUI:
            if (module.handledPlayerVault.CheckIfNoViewers()) {
                module.handledPlayerVault.ClearXrayerInfoLists(false);
            }
            if (Objects.equals(module.getStorageType(), "JSON")) {
                //Flush stored xrayer data from HandledPlayerStore in RAM if no one is still viewing the GUI:
                if (module.handledPlayerVault.CheckIfNoViewers()) {
                    module.handledPlayerStore.JSONFlushLoadedXrayerData();
                }
            }
        }
    }
}

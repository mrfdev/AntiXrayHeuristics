//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;

public class DelayedInventoryCloseExecution implements Runnable {
    private final HumanEntity p;
    private final AntiXrayHeuristics mainClassAccess;

    DelayedInventoryCloseExecution(HumanEntity player, AntiXrayHeuristics mca) {
        p = player;
        mainClassAccess = mca;
    }

    public void run() //Xrayer vault cleanup, and additional cleanup if no one is inspecting the vault:
    {
        if (!(p.getOpenInventory().title().equals(Component.text("Xrayer Vault")))) {
            mainClassAccess.vault.RemovePlayerAsViewer(p.getName()); //Remove the player as viewer inconditionally
            //Clear loaded xrayer information in vault from RAM if no one is still viewing the GUI:
            if (mainClassAccess.vault.CheckIfNoViewers()) {
                mainClassAccess.vault.ClearXrayerInfoLists(false);
            }
            if (Objects.equals(mainClassAccess.getConfig().getString("StorageMethod"), "JSON")) {
                //Flush stored xrayer data from MemoryManager in RAM if no one is still viewing the GUI:
                if (mainClassAccess.vault.CheckIfNoViewers()) {
                    mainClassAccess.mm.JSONFlushLoadedXrayerData();
                }
            }
        }
    }
}

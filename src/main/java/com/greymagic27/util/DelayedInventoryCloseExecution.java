//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27.util;

import com.greymagic27.AntiXrayHeuristics;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;

public class DelayedInventoryCloseExecution implements Runnable {
    private final HumanEntity p;
    private final AntiXrayHeuristics mainClassAccess;

    public DelayedInventoryCloseExecution(HumanEntity player, AntiXrayHeuristics mca) {
        p = player;
        mainClassAccess = mca;
    }

    public void run() //Xrayer vault clean-up, and additional clean-up if no one is inspecting the vault:
    {
        if (!(p.getOpenInventory().title().equals(Component.text("Xrayer Vault")))) {
            mainClassAccess.vault.RemovePlayerAsViewer(p.getName()); //Remove the player as viewer inconditionally
            //Clear loaded xrayer information in vault from RAM if no one is still viewing the GUI:
            if (mainClassAccess.vault.CheckIfNoViewers()) {
                mainClassAccess.vault.ClearXrayerInfoLists(false);
            }
            if (Objects.equals(mainClassAccess.getStorageType(), "JSON")) {
                //Flush stored xrayer data from MemoryManager in RAM if no one is still viewing the GUI:
                if (mainClassAccess.vault.CheckIfNoViewers()) {
                    mainClassAccess.mm.JSONFlushLoadedXrayerData();
                }
            }
        }
    }
}

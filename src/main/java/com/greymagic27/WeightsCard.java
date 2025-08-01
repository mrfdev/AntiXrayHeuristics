//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class WeightsCard {

    private static File weightsFile;
    private static FileConfiguration weightsConfiguration;

    private static void SetDefaultFileEntries() //Sets the default language entries in english
    {
        List<BlockWeightInfo> worldWeights = new ArrayList<>();
        // COAL_ORE
        worldWeights.add(new BlockWeightInfo(Material.COAL_ORE, 5, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.COAL_ORE, 65, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_COAL_ORE, 0, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_COAL_ORE, 40, 5.0f));

        // REDSTONE_ORE
        worldWeights.add(new BlockWeightInfo(Material.REDSTONE_ORE, 5, 9.0f));
        worldWeights.add(new BlockWeightInfo(Material.REDSTONE_ORE, 15, 9.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_REDSTONE_ORE, 0, 9.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_REDSTONE_ORE, 30, 9.0f));

        // IRON_ORE
        worldWeights.add(new BlockWeightInfo(Material.IRON_ORE, 5, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.IRON_ORE, 60, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_IRON_ORE, 0, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_IRON_ORE, 30, 5.0f));

        // GOLD_ORE
        worldWeights.add(new BlockWeightInfo(Material.GOLD_ORE, 5, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.GOLD_ORE, 30, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_GOLD_ORE, 0, 5.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_GOLD_ORE, 15, 5.0f));

        // COPPER_ORE
        worldWeights.add(new BlockWeightInfo(Material.COPPER_ORE, 0, 15.0f));
        worldWeights.add(new BlockWeightInfo(Material.COPPER_ORE, 45, 2.5f));
        worldWeights.add(new BlockWeightInfo(Material.COPPER_ORE, 70, 15.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_COPPER_ORE, 20, 2.5f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_COPPER_ORE, 50, 2.5f));

        // DIAMOND_ORE
        worldWeights.add(new BlockWeightInfo(Material.DIAMOND_ORE, 5, 15.0f));
        worldWeights.add(new BlockWeightInfo(Material.DIAMOND_ORE, 5, 15.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_DIAMOND_ORE, 0, 15.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_DIAMOND_ORE, 20, 15.0f));

        // EMERALD_ORE
        worldWeights.add(new BlockWeightInfo(Material.EMERALD_ORE, 5, 22.0f));
        worldWeights.add(new BlockWeightInfo(Material.EMERALD_ORE, 30, 22.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_EMERALD_ORE, 0, 22.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_EMERALD_ORE, 20, 22.0f));

        // LAPIS_ORE
        worldWeights.add(new BlockWeightInfo(Material.LAPIS_ORE, 0, 8.0f));
        worldWeights.add(new BlockWeightInfo(Material.LAPIS_ORE, 15, 2.5f));
        worldWeights.add(new BlockWeightInfo(Material.LAPIS_ORE, 30, 8.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_LAPIS_ORE, 0, 8.0f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_LAPIS_ORE, 20, 2.5f));
        worldWeights.add(new BlockWeightInfo(Material.DEEPSLATE_LAPIS_ORE, 40, 8.0f));


        weightsConfiguration.addDefault("wild", worldWeights);
        weightsConfiguration.addDefault("general", worldWeights);

        List<BlockWeightInfo> netherWeights = new ArrayList<>();
        netherWeights.add(new BlockWeightInfo(Material.NETHER_QUARTZ_ORE, 5, 5.0f));
        netherWeights.add(new BlockWeightInfo(Material.NETHER_QUARTZ_ORE, 60, 5.0f));
        netherWeights.add(new BlockWeightInfo(Material.NETHER_GOLD_ORE, 0, 5.0f));
        netherWeights.add(new BlockWeightInfo(Material.NETHER_GOLD_ORE, 255, 5.0f));
        netherWeights.add(new BlockWeightInfo(Material.ANCIENT_DEBRIS, 0, 22.0f));
        netherWeights.add(new BlockWeightInfo(Material.ANCIENT_DEBRIS, 255, 22.0f));

        weightsConfiguration.addDefault("nether", netherWeights);
    }

    public static void setup(String pluginName) //Finds or generates custom config file
    {
        weightsFile = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin(pluginName)).getDataFolder(), "weights.yml");

        if (!weightsFile.exists()) {
            try {
                weightsFile.createNewFile(); //Creates the file
            } catch (IOException e) {
                System.out.print("[AntiXrayHeuristics] Could not create weights file.");
            }
        }
        weightsConfiguration = YamlConfiguration.loadConfiguration(weightsFile);
        SetDefaultFileEntries(); //Sets default entries
    }

    public static FileConfiguration get() {
        return weightsConfiguration;
    }

    public static void save() {
        try {
            weightsConfiguration.save(weightsFile);
        } catch (IOException e) {
            System.out.print("[AntiXrayHeuristics] Could not save weights file.");
        }
    }

    public static void reload() //Used from ARGReload AXH command argument in order to reload the weights file
    {
        weightsConfiguration = YamlConfiguration.loadConfiguration(weightsFile);
    }
}































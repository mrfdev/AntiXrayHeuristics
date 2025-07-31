package com.greymagic27;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BlockWeightInfo implements ConfigurationSerializable {
    public final Material blockMaterial;
    public final int blockHeight;
    public final float blockWeight;

    /*Standard constructor*/
    public BlockWeightInfo(Material material, int height, float weight) {
        blockMaterial = material;
        blockHeight = height;
        blockWeight = weight;
    }

    /*Constructor used when deserializing*/
    public BlockWeightInfo(@NotNull Map<String, Object> deserializedProperties) {
        blockMaterial = Material.valueOf((String) deserializedProperties.get("Material"));
        blockHeight = (int) deserializedProperties.get("Height");
        blockWeight = ((Number) deserializedProperties.get("Weight")).floatValue();
    }

    @Override
    /*Method used when serializing*/
    public final @NotNull Map<String, Object> serialize() {
        Map<String, Object> serializedProperties = new HashMap<>();
        serializedProperties.put("Material", blockMaterial.toString());
        serializedProperties.put("Height", blockHeight);
        serializedProperties.put("Weight", blockWeight);

        return serializedProperties;
    }
}

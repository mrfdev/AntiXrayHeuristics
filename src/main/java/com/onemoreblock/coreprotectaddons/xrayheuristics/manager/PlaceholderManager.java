package com.onemoreblock.coreprotectaddons.xrayheuristics.manager;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jspecify.annotations.NonNull;

public class PlaceholderManager {

    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    public static @NonNull String SubstitutePlayerNameAndColorCodePlaceholders(String toReplace, String player) {
        toReplace = toReplace.replace("{PlayerName}", player);
        Component component = legacySerializer.deserialize(toReplace);
        return legacySerializer.serialize(component);
    }

    public static @NonNull String SubstitutePlayerNameAndHandleTimesPlaceholders(String toReplace, String player, String handleTimes) {
        toReplace = toReplace.replace("{PlayerName}", player);
        toReplace = toReplace.replace("{TimesDetected}", handleTimes);
        Component component = legacySerializer.deserialize(toReplace);
        return legacySerializer.serialize(component);
    }

    public static @NonNull String SubstituteColorCodePlaceholders(String toReplace) {
        Component component = legacySerializer.deserialize(toReplace);
        return legacySerializer.serialize(component);
    }

    public static List<String> SubstituteXrayerDataAndColorCodePlaceholders(@NonNull List<String> toReplace, String handledTimesAmount, String firstHandleTime, String lastSeenTime) {
        for (int i = 0; i < toReplace.size(); i++) {
            String line = toReplace.get(i);
            line = line.replace("{HandledTimesAmount}", handledTimesAmount);
            line = line.replace("{FirstTimeDetected}", firstHandleTime);
            line = line.replace("{LastSeenTime}", lastSeenTime);
            Component component = legacySerializer.deserialize(line);
            toReplace.set(i, legacySerializer.serialize(component));
        }
        return toReplace;
    }
}
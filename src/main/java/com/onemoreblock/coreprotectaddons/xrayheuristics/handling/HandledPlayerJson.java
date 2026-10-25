package com.onemoreblock.coreprotectaddons.xrayheuristics.handling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** Preserves the existing JSON schema without reflectively mutating final record fields. */
public final class HandledPlayerJson {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(HandledPlayerRecord.class, (JsonDeserializer<HandledPlayerRecord>) (json, type, context) -> {
                JsonObject fields = json.getAsJsonObject();
                Integer handled = context.deserialize(fields.get("Handled"), Integer.class);
                return new HandledPlayerRecord(
                        context.deserialize(fields.get("UUID"), String.class),
                        handled == null ? 0 : handled,
                        context.deserialize(fields.get("FirstHandleTime"), String.class),
                        context.deserialize(fields.get("HandleLocation"), String.class),
                        context.deserialize(fields.get("Belongings"), String.class)
                );
            })
            .create();

    private HandledPlayerJson() {}

    public static String serialize(List<HandledPlayerRecord> records) {
        return GSON.toJson(records);
    }

    public static List<HandledPlayerRecord> deserialize(Reader reader) {
        return GSON.fromJson(reader, new TypeToken<ArrayList<HandledPlayerRecord>>() {}.getType());
    }
}

package com.onemoreblock.coreprotectaddons.xrayheuristics.handling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class HandledPlayerJsonTest {
    @Test
    void restoresLegacyFieldsAndSerializesWithoutSchemaChanges() {
        String legacy = """
                [{"UUID":"2fd6d450-7530-4a65-9f1b-d3b5c02a96da","Handled":2,
                  "FirstHandleTime":"2026/09/25 12:00:00","HandleLocation":"world,1,2,3,4,5",
                  "Belongings":"opaque-base64-item-data"}]
                """;
        var records = HandledPlayerJson.deserialize(new StringReader(legacy));
        var record = records.getFirst();
        assertEquals("2fd6d450-7530-4a65-9f1b-d3b5c02a96da", record.UUID);
        assertEquals(2, record.Handled);
        assertEquals("2026/09/25 12:00:00", record.FirstHandleTime);
        assertEquals("world,1,2,3,4,5", record.HandleLocation);
        assertEquals("opaque-base64-item-data", record.Belongings);
        assertEquals(JsonParser.parseString(legacy), JsonParser.parseString(HandledPlayerJson.serialize(records)));
        record.Handled++;
        assertEquals(3, HandledPlayerJson.deserialize(new StringReader(HandledPlayerJson.serialize(records))).getFirst().Handled);
    }

    @Test
    void preservesMissingAndNullFieldDefaultsAndIgnoresUnknownFields() {
        var records = HandledPlayerJson.deserialize(new StringReader("""
                [{"UUID":"test","Belongings":null,"futureField":true}]
                """));
        var record = records.getFirst();
        assertEquals("test", record.UUID);
        assertEquals(0, record.Handled);
        assertNull(record.Belongings);
        assertNull(record.FirstHandleTime);
        assertNull(record.HandleLocation);
    }

    @Test
    void rejectsMalformedRecordsRatherThanReturningEmptyData() {
        assertThrows(JsonParseException.class,
                () -> HandledPlayerJson.deserialize(new StringReader("[{\"Handled\":\"invalid\"}]")));
        assertEquals(0, HandledPlayerJson.deserialize(new StringReader("[]")).size());
    }
}

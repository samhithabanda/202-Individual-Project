package com.eventanalyzer.output;

import com.eventanalyzer.aggregate.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonOutputWriterTest {

    private final JsonOutputWriter writer = new JsonOutputWriter();

    @Test
    void writesEmptyObjectForEmptyResult(@TempDir Path dir) throws Exception {
        Path file = writer.write(dir, "devices", Map.of());
        assertEquals(dir.resolve("devices.json"), file);
        assertEquals("{}", Files.readString(file).strip());
    }

    @Test
    void prettyPrintsWithTwoSpaceIndentAndFormatsNumbers() {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("count", 2);
        inner.put("minimum", Statistics.roundOneDecimal(40.0));
        inner.put("average", Statistics.roundOneDecimal(21.25));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("humidity_percent", inner);
        data.put("flagged_users", List.of("user-42"));

        String expected = String.join("\n",
                "{",
                "  \"humidity_percent\": {",
                "    \"count\": 2,",
                "    \"minimum\": 40,",
                "    \"average\": 21.3",
                "  },",
                "  \"flagged_users\": [",
                "    \"user-42\"",
                "  ]",
                "}");
        assertEquals(expected, writer.toJson(data));
    }

    @Test
    void largeValuesAreNotWrittenInScientificNotation() {
        String json = writer.toJson(Map.of("v", Statistics.roundOneDecimal(123456789.25)));
        assertTrue(json.contains("123456789.3"), json);
    }
}

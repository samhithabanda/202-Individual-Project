package com.eventanalyzer.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** End-to-end tests: input file in, JSON files out. */
class AnalyzerApplicationTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String... args) {
        return new AnalyzerApplication(new AnalyzerFactory(),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8)).run(args);
    }

    private static JsonObject readJson(Path file) throws Exception {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    @Test
    void sampleFileProducesExpectedOutputs(@TempDir Path dir) throws Exception {
        Path input = dir.resolve("events.txt");
        try (InputStream in = getClass().getResourceAsStream("/sample_events.txt")) {
            Files.copy(in, input);
        }
        Path outDir = dir.resolve("results");

        assertEquals(AnalyzerApplication.EXIT_OK, run("--file", input.toString(), "--outdir", outDir.toString()));
        assertTrue(out.toString().contains("Processed 14 lines: 12 records, 2 skipped"), out.toString());

        JsonObject sensors = readJson(outDir.resolve("sensors.json"));
        JsonObject temp = sensors.getAsJsonObject("temperature_celsius");
        assertEquals(2, temp.get("count").getAsInt());
        assertEquals("21.5", temp.get("minimum").toString());
        assertEquals("23.3", temp.get("median").toString());
        assertEquals("23.3", temp.get("average").toString());
        assertEquals("25", temp.get("maximum").toString());
        assertEquals("1.8", temp.get("std_dev").toString());
        assertEquals("0", sensors.getAsJsonObject("humidity_percent").get("std_dev").toString());

        JsonObject devices = readJson(outDir.resolve("devices.json"));
        JsonObject bySeverity = devices.getAsJsonObject("by_severity");
        assertEquals(1, bySeverity.get("CRITICAL").getAsInt());
        assertEquals(1, bySeverity.get("INFO").getAsInt());
        JsonObject byDevice = devices.getAsJsonObject("by_device");
        assertEquals("WARN", byDevice.getAsJsonObject("thermostat-01").get("most_frequent_severity").getAsString());
        assertEquals("CRITICAL", byDevice.getAsJsonObject("door-lock-07").get("most_frequent_severity").getAsString());

        JsonObject doorA = readJson(outDir.resolve("access.json")).getAsJsonObject("door-A");
        JsonObject times = doorA.getAsJsonObject("decision_times");
        assertEquals(18, times.get("min").getAsInt());
        assertEquals(28, times.get("50_percentile").getAsInt());
        assertEquals(35, times.get("90_percentile").getAsInt());
        assertEquals(35, times.get("max").getAsInt());
        assertEquals(3, doorA.getAsJsonObject("results").get("DENIED").getAsInt());
        assertEquals("user-42", doorA.getAsJsonArray("flagged_users").get(0).getAsString());
    }

    @Test
    void emptyOrAllInvalidInputStillWritesThreeEmptyObjects(@TempDir Path dir) throws Exception {
        Path input = Files.writeString(dir.resolve("bad.txt"),
                "garbage\n\nts=1 sensor=x reading=abc\nts=1 severity=LOUD device=d\n");

        assertEquals(AnalyzerApplication.EXIT_OK, run("--file", input.toString(), "--outdir", dir.toString()));
        for (String name : new String[]{"sensors.json", "devices.json", "access.json"}) {
            assertEquals("{}", Files.readString(dir.resolve(name)).strip(), name);
        }
    }

    @Test
    void missingFileArgumentReturnsNonZeroWithMessage() {
        int code = run();
        assertNotEquals(0, code);
        assertTrue(err.toString().contains("Missing required argument --file"));
    }

    @Test
    void nonexistentFileReturnsNonZeroAndWritesNothing(@TempDir Path dir) {
        int code = run("--file", dir.resolve("nope.txt").toString(), "--outdir", dir.toString());
        assertNotEquals(0, code);
        assertTrue(err.toString().contains("Input file not found"));
        assertFalse(Files.exists(dir.resolve("sensors.json")));
    }

    @Test
    void invalidUtf8BytesAreSkippedNotFatal(@TempDir Path dir) throws Exception {
        Path input = dir.resolve("binary.txt");
        byte[] good = "ts=1 sensor=t reading=5\n".getBytes(StandardCharsets.UTF_8);
        byte[] bad = {(byte) 0xff, (byte) 0xfe, '\n'};
        byte[] all = new byte[good.length + bad.length];
        System.arraycopy(bad, 0, all, 0, bad.length);
        System.arraycopy(good, 0, all, bad.length, good.length);
        Files.write(input, all);

        assertEquals(AnalyzerApplication.EXIT_OK, run("--file", input.toString(), "--outdir", dir.toString()));
        assertEquals(1, readJson(dir.resolve("sensors.json")).getAsJsonObject("t").get("count").getAsInt());
    }
}

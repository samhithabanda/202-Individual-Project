package com.eventanalyzer.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Writes a result set as pretty-printed JSON (2-space indentation) to {@code <name>.json}. */
public class JsonOutputWriter implements OutputWriter {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    @Override
    public Path write(Path outputDir, String name, Map<String, Object> data) throws IOException {
        Path file = outputDir.resolve(name + ".json");
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(toJson(data));
            writer.write(System.lineSeparator());
        }
        return file;
    }

    /** Serialises the data to a pretty-printed JSON string. */
    public String toJson(Map<String, Object> data) {
        return gson.toJson(data);
    }
}

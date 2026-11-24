package com.eventanalyzer.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliArgumentsTest {

    @Test
    void parsesFileAndOutdir(@TempDir Path dir) throws Exception {
        Path input = Files.writeString(dir.resolve("events.txt"), "");
        CliArguments args = CliArguments.parse(
                new String[]{"--file", input.toString(), "--outdir", dir.resolve("out").toString()});
        assertEquals(input, args.inputFile());
        assertEquals(dir.resolve("out"), args.outputDir());
    }

    @Test
    void outdirDefaultsToCurrentDirectory(@TempDir Path dir) throws Exception {
        Path input = Files.writeString(dir.resolve("events.txt"), "");
        assertEquals(Path.of("."), CliArguments.parse(new String[]{"--file", input.toString()}).outputDir());
    }

    @Test
    void missingFileArgumentIsAnError() {
        CliException e = assertThrows(CliException.class, () -> CliArguments.parse(new String[]{}));
        assertTrue(e.getMessage().contains("--file"));
        assertThrows(CliException.class, () -> CliArguments.parse(new String[]{"--file"}));
        assertThrows(CliException.class, () -> CliArguments.parse(new String[]{"--file", "--outdir", "x"}));
    }

    @Test
    void nonexistentFileIsAnError() {
        CliException e = assertThrows(CliException.class,
                () -> CliArguments.parse(new String[]{"--file", "does/not/exist.txt"}));
        assertTrue(e.getMessage().contains("not found"));
    }

    @Test
    void directoryAsInputAndUnknownArgumentAreErrors(@TempDir Path dir) {
        assertThrows(CliException.class, () -> CliArguments.parse(new String[]{"--file", dir.toString()}));
        assertThrows(CliException.class, () -> CliArguments.parse(new String[]{"--verbose"}));
    }
}

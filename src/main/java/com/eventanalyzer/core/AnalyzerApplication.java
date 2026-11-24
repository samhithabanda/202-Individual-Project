package com.eventanalyzer.core;

import com.eventanalyzer.cli.CliArguments;
import com.eventanalyzer.cli.CliException;
import com.eventanalyzer.output.OutputWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Runs the whole application: validate arguments, read the file, analyse it, write outputs.
 * Returns an exit code instead of calling {@code System.exit} so it can be unit tested.
 */
public class AnalyzerApplication {

    public static final int EXIT_OK = 0;
    public static final int EXIT_USAGE_ERROR = 1;
    public static final int EXIT_IO_ERROR = 2;

    private final AnalyzerFactory factory;
    private final PrintStream out;
    private final PrintStream err;

    public AnalyzerApplication(AnalyzerFactory factory, PrintStream out, PrintStream err) {
        this.factory = factory;
        this.out = out;
        this.err = err;
    }

    public int run(String[] args) {
        CliArguments cli;
        try {
            cli = CliArguments.parse(args);
        } catch (CliException e) {
            err.println("Error: " + e.getMessage());
            err.println(CliArguments.USAGE);
            return EXIT_USAGE_ERROR;
        }

        EventStreamAnalyzer analyzer = factory.createAnalyzer();
        try {
            readLines(cli.inputFile(), analyzer);
            Files.createDirectories(cli.outputDir());
            for (Map.Entry<String, Map<String, Object>> entry : analyzer.results().entrySet()) {
                for (OutputWriter writer : factory.createOutputWriters()) {
                    Path written = writer.write(cli.outputDir(), entry.getKey(), entry.getValue());
                    out.println("Wrote " + written);
                }
            }
        } catch (IOException | UncheckedIOException e) {
            err.println("Error: " + e.getMessage());
            return EXIT_IO_ERROR;
        }

        out.printf("Processed %d lines: %d records, %d skipped%n",
                analyzer.linesRead(), analyzer.recordsAccepted(), analyzer.linesSkipped());
        return EXIT_OK;
    }

    /** Streams the file line by line; invalid UTF-8 bytes are replaced so a bad line is just skipped. */
    private static void readLines(Path file, EventStreamAnalyzer analyzer) throws IOException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file), decoder))) {
            String line;
            while ((line = reader.readLine()) != null) {
                analyzer.processLine(line);
            }
        }
    }
}

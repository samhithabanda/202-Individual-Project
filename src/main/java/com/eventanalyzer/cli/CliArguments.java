package com.eventanalyzer.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parsed and validated command-line options.
 * <pre>
 *   --file &lt;path&gt;     (required) input file
 *   --outdir &lt;dir&gt;    (optional) output directory, default: current directory
 * </pre>
 */
public record CliArguments(Path inputFile, Path outputDir) {

    public static final String USAGE =
            "Usage: java -jar analyzer.jar --file <events.txt> [--outdir <directory>]";

    /**
     * Parses the arguments and checks that the input file exists and is a readable regular file.
     *
     * @throws CliException with a user-friendly message if anything is wrong
     */
    public static CliArguments parse(String[] args) throws CliException {
        String file = null;
        String outdir = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--file" -> file = requireValue(args, ++i, "--file");
                case "--outdir" -> outdir = requireValue(args, ++i, "--outdir");
                default -> throw new CliException("Unknown argument: " + arg);
            }
        }

        if (file == null) {
            throw new CliException("Missing required argument --file");
        }

        Path inputFile = Paths.get(file);
        if (!Files.exists(inputFile)) {
            throw new CliException("Input file not found: " + inputFile);
        }
        if (!Files.isRegularFile(inputFile)) {
            throw new CliException("Input path is not a regular file: " + inputFile);
        }
        if (!Files.isReadable(inputFile)) {
            throw new CliException("Input file is not readable: " + inputFile);
        }

        Path outputDir = Paths.get(outdir == null ? "." : outdir);
        if (Files.exists(outputDir) && !Files.isDirectory(outputDir)) {
            throw new CliException("Output path exists but is not a directory: " + outputDir);
        }
        return new CliArguments(inputFile, outputDir);
    }

    private static String requireValue(String[] args, int index, String flag) throws CliException {
        if (index >= args.length || args[index].startsWith("--") || args[index].isBlank()) {
            throw new CliException("Missing value for " + flag);
        }
        return args[index];
    }
}

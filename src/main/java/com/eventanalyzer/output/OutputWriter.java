package com.eventanalyzer.output;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Strategy for persisting one aggregated result set. Adding a new output format
 * (for example CSV) only requires another implementation of this interface.
 */
public interface OutputWriter {

    /**
     * Writes {@code data} into {@code outputDir}.
     *
     * @param outputDir directory to write into (already exists)
     * @param name      result-set name, e.g. "sensors"; implementations append their own extension
     * @param data      aggregated data produced by an {@link com.eventanalyzer.aggregate.Aggregator}
     * @return the path of the file that was written
     */
    Path write(Path outputDir, String name, Map<String, Object> data) throws IOException;
}

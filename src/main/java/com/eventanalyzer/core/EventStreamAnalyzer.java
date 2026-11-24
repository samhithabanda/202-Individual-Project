package com.eventanalyzer.core;

import com.eventanalyzer.aggregate.Aggregator;
import com.eventanalyzer.classify.RecordClassifier;
import com.eventanalyzer.model.EventRecord;
import com.eventanalyzer.parser.LineParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordinates the pipeline for each line: parse -&gt; classify -&gt; aggregate.
 * <p>
 * It depends only on the {@link LineParser}, {@link RecordClassifier} and {@link Aggregator}
 * abstractions, so new formats and record types plug in without changing this class.
 * Lines that cannot be parsed or classified are skipped silently (only counted).
 */
public class EventStreamAnalyzer {

    private final LineParser parser;
    private final RecordClassifier classifier;
    private final List<Aggregator> aggregators;

    private long linesRead;
    private long recordsAccepted;

    public EventStreamAnalyzer(LineParser parser, RecordClassifier classifier, List<Aggregator> aggregators) {
        this.parser = Objects.requireNonNull(parser);
        this.classifier = Objects.requireNonNull(classifier);
        this.aggregators = List.copyOf(aggregators);
    }

    /** Processes all lines in order. */
    public void processAll(Iterable<String> lines) {
        for (String line : lines) {
            processLine(line);
        }
    }

    /**
     * Processes a single line.
     *
     * @return true if the line was recognised and aggregated, false if it was skipped
     */
    public boolean processLine(String line) {
        linesRead++;
        Optional<EventRecord> record = parser.parse(line).flatMap(classifier::classify);
        if (record.isEmpty()) {
            return false;
        }
        recordsAccepted++;
        for (Aggregator aggregator : aggregators) {
            aggregator.accept(record.get());
        }
        return true;
    }

    /** Returns each aggregator's result keyed by its name, in registration order. */
    public Map<String, Map<String, Object>> results() {
        Map<String, Map<String, Object>> results = new LinkedHashMap<>();
        for (Aggregator aggregator : aggregators) {
            results.put(aggregator.name(), aggregator.result());
        }
        return results;
    }

    public long linesRead() {
        return linesRead;
    }

    public long recordsAccepted() {
        return recordsAccepted;
    }

    public long linesSkipped() {
        return linesRead - recordsAccepted;
    }
}

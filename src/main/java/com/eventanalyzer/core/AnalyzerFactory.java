package com.eventanalyzer.core;

import com.eventanalyzer.aggregate.AccessRecordAggregator;
import com.eventanalyzer.aggregate.Aggregator;
import com.eventanalyzer.aggregate.DeviceEventAggregator;
import com.eventanalyzer.aggregate.SensorReadingAggregator;
import com.eventanalyzer.classify.AccessRecordClassifier;
import com.eventanalyzer.classify.DeviceEventClassifier;
import com.eventanalyzer.classify.RecordClassifier;
import com.eventanalyzer.classify.SensorReadingClassifier;
import com.eventanalyzer.output.JsonOutputWriter;
import com.eventanalyzer.output.OutputWriter;
import com.eventanalyzer.parser.KeyValueLineParser;
import com.eventanalyzer.parser.LineParser;

import java.util.List;

/**
 * Factory: the single place that knows which concrete parser, classifiers, aggregators and
 * writers make up the application. Supporting a new record type or format means registering
 * one more object here; the rest of the code only sees interfaces.
 */
public class AnalyzerFactory {

    /** Default input format: space-separated key=value pairs. */
    public LineParser createParser() {
        return new KeyValueLineParser();
    }

    /** Classification chain: Sensor Reading -&gt; Device Event -&gt; Access Record (first match wins). */
    public RecordClassifier createClassifierChain() {
        RecordClassifier head = new SensorReadingClassifier();
        head.linkWith(new DeviceEventClassifier())
            .linkWith(new AccessRecordClassifier());
        return head;
    }

    /** One aggregator per record type; each name becomes an output file (sensors, devices, access). */
    public List<Aggregator> createAggregators() {
        return List.of(
                new SensorReadingAggregator(),
                new DeviceEventAggregator(),
                new AccessRecordAggregator());
    }

    /** Output formats to write. */
    public List<OutputWriter> createOutputWriters() {
        return List.of(new JsonOutputWriter());
    }

    /** Builds a fully wired analyzer. */
    public EventStreamAnalyzer createAnalyzer() {
        return new EventStreamAnalyzer(createParser(), createClassifierChain(), createAggregators());
    }
}

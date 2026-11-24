package com.eventanalyzer.aggregate;

import com.eventanalyzer.model.EventRecord;

import java.util.Map;

/**
 * Strategy that accumulates statistics for one record type.
 * <p>
 * The analyzer offers every classified record to every aggregator; each aggregator
 * keeps only the records it understands (see {@link TypedAggregator}).
 */
public interface Aggregator {

    /** Logical name of the result set; also used as the output file base name (e.g. "sensors"). */
    String name();

    /** Offers a record to this aggregator. Records of other types are ignored. */
    void accept(EventRecord record);

    /**
     * Returns the aggregated result as nested maps/lists/numbers/strings, ready to be
     * serialised by any {@link com.eventanalyzer.output.OutputWriter}. An aggregator that
     * received no records returns an empty map.
     */
    Map<String, Object> result();
}

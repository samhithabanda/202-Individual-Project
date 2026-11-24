package com.eventanalyzer.aggregate;

import com.eventanalyzer.model.DeviceEvent;
import com.eventanalyzer.model.Severity;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Counts device events per severity level and per device, and finds each device's most
 * frequent severity. Ties are broken in favour of the higher severity
 * (CRITICAL &gt; ERROR &gt; WARN &gt; INFO).
 */
public class DeviceEventAggregator extends TypedAggregator<DeviceEvent> {

    public static final String NAME = "devices";

    private final Map<Severity, Integer> bySeverity = new EnumMap<>(Severity.class);
    private final Map<String, Map<Severity, Integer>> byDevice = new TreeMap<>();

    public DeviceEventAggregator() {
        super(NAME, DeviceEvent.class);
    }

    @Override
    protected void add(DeviceEvent record) {
        bySeverity.merge(record.severity(), 1, Integer::sum);
        byDevice.computeIfAbsent(record.device(), k -> new EnumMap<>(Severity.class))
                .merge(record.severity(), 1, Integer::sum);
    }

    @Override
    public Map<String, Object> result() {
        if (byDevice.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> severityCounts = new LinkedHashMap<>();
        for (Severity s : Severity.values()) {
            severityCounts.put(s.name(), bySeverity.getOrDefault(s, 0));
        }

        Map<String, Object> deviceSummaries = new LinkedHashMap<>();
        byDevice.forEach((device, counts) -> {
            int total = counts.values().stream().mapToInt(Integer::intValue).sum();
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("total", total);
            summary.put("most_frequent_severity", mostFrequent(counts).name());
            deviceSummaries.put(device, summary);
        });

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("by_severity", severityCounts);
        out.put("by_device", deviceSummaries);
        return out;
    }

    /** Highest count wins; because values() runs from highest to lowest severity, ties keep the higher one. */
    static Severity mostFrequent(Map<Severity, Integer> counts) {
        Severity best = null;
        int bestCount = -1;
        for (Severity s : Severity.values()) {
            int c = counts.getOrDefault(s, 0);
            if (c > bestCount) {
                best = s;
                bestCount = c;
            }
        }
        return best;
    }
}

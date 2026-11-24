package com.eventanalyzer.aggregate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Pure statistical helper functions used by the aggregators.
 * All list arguments must be non-empty; methods that need sorted input say so.
 */
public final class Statistics {

    private Statistics() {
    }

    /** Arithmetic mean. */
    public static double mean(List<Double> values) {
        requireNonEmpty(values);
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    /** Median of a sorted list: middle value for odd N, average of the two middle values for even N. */
    public static double median(List<Double> sorted) {
        requireNonEmpty(sorted);
        int n = sorted.size();
        int mid = n / 2;
        return (n % 2 == 1) ? sorted.get(mid) : (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
    }

    /** Population standard deviation (divides by N, not N - 1). */
    public static double populationStdDev(List<Double> values) {
        double mean = mean(values);
        double sumSquares = 0;
        for (double v : values) {
            double d = v - mean;
            sumSquares += d * d;
        }
        return Math.sqrt(sumSquares / values.size());
    }

    /**
     * Nearest-rank percentile of a sorted list: the value at 0-based index
     * {@code ceil(p / 100 * N) - 1}. Integer arithmetic is used to avoid floating-point error.
     *
     * @param sorted     values in ascending order
     * @param percentile p in the range 1..100
     */
    public static long percentileNearestRank(List<Long> sorted, int percentile) {
        requireNonEmpty(sorted);
        if (percentile < 1 || percentile > 100) {
            throw new IllegalArgumentException("percentile must be between 1 and 100: " + percentile);
        }
        long n = sorted.size();
        long rank = (percentile * n + 99) / 100; // ceil(p * N / 100)
        int index = (int) Math.max(0, rank - 1);
        return sorted.get(index);
    }

    /**
     * Rounds to one decimal place (half-up). Whole results are returned as a {@link Long} so they
     * are written without a decimal point (40, not 40.0); otherwise a {@link BigDecimal} with one
     * decimal digit is returned so it is never written in scientific notation.
     */
    public static Number roundOneDecimal(double value) {
        BigDecimal rounded = BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
        if (rounded.signum() == 0 || rounded.stripTrailingZeros().scale() <= 0) {
            return rounded.longValue();
        }
        return rounded;
    }

    private static void requireNonEmpty(List<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
    }
}

package com.eventanalyzer.aggregate;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatisticsTest {

    @Test
    void medianOfOddAndEvenSizedLists() {
        assertEquals(2.0, Statistics.median(List.of(1.0, 2.0, 9.0)));
        assertEquals(2.5, Statistics.median(List.of(1.0, 2.0, 3.0, 4.0)));
    }

    @Test
    void populationStandardDeviationDividesByN() {
        // values 2,4,4,4,5,5,7,9 -> mean 5, population variance 4, std dev 2 (sample std dev would be ~2.14)
        assertEquals(2.0, Statistics.populationStdDev(List.of(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)), 1e-9);
        assertEquals(0.0, Statistics.populationStdDev(List.of(42.0)));
    }

    @Test
    void nearestRankPercentile() {
        List<Long> sorted = List.of(15L, 20L, 35L, 40L, 50L);
        assertEquals(35L, Statistics.percentileNearestRank(sorted, 50)); // ceil(2.5)-1 = 2
        assertEquals(50L, Statistics.percentileNearestRank(sorted, 90)); // ceil(4.5)-1 = 4
        assertEquals(50L, Statistics.percentileNearestRank(sorted, 99));
        assertEquals(15L, Statistics.percentileNearestRank(List.of(15L), 50));
    }

    @Test
    void fiftiethPercentileCanDifferFromMedianForEvenN() {
        List<Long> sorted = List.of(10L, 20L, 30L, 40L);
        assertEquals(20L, Statistics.percentileNearestRank(sorted, 50));            // nearest rank
        assertEquals(25.0, Statistics.median(List.of(10.0, 20.0, 30.0, 40.0)));    // average of middle two
    }

    @Test
    void roundsToOneDecimalAndDropsDecimalPointForWholeNumbers() {
        assertEquals(40L, Statistics.roundOneDecimal(40.0));
        assertEquals(40L, Statistics.roundOneDecimal(39.96));
        assertEquals(new BigDecimal("21.3"), Statistics.roundOneDecimal(21.25));
        assertEquals(new BigDecimal("2.7"), Statistics.roundOneDecimal(2.6925));
        assertEquals(new BigDecimal("-1.5"), Statistics.roundOneDecimal(-1.45));
        assertEquals(0L, Statistics.roundOneDecimal(-0.04));
    }

    @Test
    void rejectsEmptyInputAndOutOfRangePercentile() {
        assertThrows(IllegalArgumentException.class, () -> Statistics.median(List.of()));
        assertThrows(IllegalArgumentException.class, () -> Statistics.percentileNearestRank(List.of(1L), 0));
    }
}

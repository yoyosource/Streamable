package de.yoyosource.numberstream;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.streams.NumberStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SummaryStatisticsTest {

    @Nested
    class Sequential {
        @Test
        void testSummaryStatistics() {
            NumberStream.SummaryStatistics<Integer> result = Streamable.of(1, 2, 3)
                    .as(NumberStream.NumberStream())
                    .summaryStatistics();
            Assertions.assertEquals(3, result.getCount());
            Assertions.assertTrue(result.getSum().isPresent());
            Assertions.assertTrue(result.getAverage().isPresent());
            Assertions.assertTrue(result.getMin().isPresent());
            Assertions.assertTrue(result.getMax().isPresent());
            Assertions.assertEquals(6, result.getSum().get());
            Assertions.assertEquals(2, result.getAverage().get());
            Assertions.assertEquals(1, result.getMin().get());
            Assertions.assertEquals(3, result.getMax().get());
        }

        @Test
        void testSummaryStatisticsNoResult() {
            NumberStream.SummaryStatistics<Integer> result = Streamable.<Integer>of()
                    .as(NumberStream.NumberStream())
                    .summaryStatistics();
            Assertions.assertEquals(0, result.getCount());
            Assertions.assertTrue(result.getSum().isEmpty());
            Assertions.assertTrue(result.getAverage().isEmpty());
            Assertions.assertTrue(result.getMin().isEmpty());
            Assertions.assertTrue(result.getMax().isEmpty());
        }
    }

    @Nested
    class Parallel {
        @Test
        void testSummaryStatistics() {
            NumberStream.SummaryStatistics<Integer> result = Streamable.of(1, 2, 3)
                    .parallel(3)
                    .as(NumberStream.NumberStream())
                    .summaryStatistics();
            Assertions.assertEquals(3, result.getCount());
            Assertions.assertTrue(result.getSum().isPresent());
            Assertions.assertTrue(result.getAverage().isPresent());
            Assertions.assertTrue(result.getMin().isPresent());
            Assertions.assertTrue(result.getMax().isPresent());
            Assertions.assertEquals(6, result.getSum().get());
            Assertions.assertEquals(2, result.getAverage().get());
            Assertions.assertEquals(1, result.getMin().get());
            Assertions.assertEquals(3, result.getMax().get());
        }

        @Test
        void testSummaryStatisticsNoResult() {
            NumberStream.SummaryStatistics<Integer> result = Streamable.<Integer>of()
                    .parallel(3)
                    .as(NumberStream.NumberStream())
                    .summaryStatistics();
            Assertions.assertEquals(0, result.getCount());
            Assertions.assertTrue(result.getSum().isEmpty());
            Assertions.assertTrue(result.getAverage().isEmpty());
            Assertions.assertTrue(result.getMin().isEmpty());
            Assertions.assertTrue(result.getMax().isEmpty());
        }
    }
}

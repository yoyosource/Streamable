package de.yoyosource.streamable.impl;

import de.yoyosource.streamable.Streamable;
import de.yoyosource.streamable.StreamableCollector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

public interface NumberStream<T extends Number> extends Streamable<T> {

    static <T extends Number> Class<NumberStream<T>> type() {
        return (Class<NumberStream<T>>) (Class) NumberStream.class;
    }

    default Optional<T> sum() {
        return collect(new StreamableCollector<>() {
            private T value;

            @Override
            public void apply(T input) {
                if (value == null) {
                    value = input;
                    return;
                }

                if (value instanceof Byte) {
                    value = (T) (Object) ((Byte) value + (Byte) input);
                } else if (value instanceof Short) {
                    value = (T) (Object) ((Short) value + (Short) input);
                } else if (value instanceof Integer) {
                    value = (T) (Object) ((Integer) value + (Integer) input);
                } else if (value instanceof Long) {
                    value = (T) (Object) ((Long) value + (Long) input);
                } else if (value instanceof Float) {
                    value = (T) (Object) ((Float) value + (Float) input);
                } else if (value instanceof Double) {
                    value = (T) (Object) ((Double) value + (Double) input);
                } else if (value instanceof BigDecimal) {
                    value = (T) (((BigDecimal) value).add((BigDecimal) input));
                } else if (value instanceof BigInteger) {
                    value = (T) (((BigInteger) value).add((BigInteger) input));
                }
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(value);
            }
        });
    }

    default Optional<T> min() {
        return collect(new StreamableCollector<>() {
            private T value;

            @Override
            public void apply(T input) {
                if (value == null) {
                    value = input;
                    return;
                }

                if (value instanceof Byte) {
                    value = (Byte) value > (Byte) input ? input : value;
                } else if (value instanceof Short) {
                    value = (Short) value > (Short) input ? input : value;
                } else if (value instanceof Integer) {
                    value = (Integer) value > (Integer) input ? input : value;
                } else if (value instanceof Long) {
                    value = (Long) value > (Long) input ? input : value;
                } else if (value instanceof Float) {
                    value = (Float) value > (Float) input ? input : value;
                } else if (value instanceof Double) {
                    value = (Double) value > (Double) input ? input : value;
                } else if (value instanceof BigDecimal) {
                    value = ((BigDecimal) value).compareTo((BigDecimal) input) > 0 ? input : value;
                } else if (value instanceof BigInteger) {
                    value = ((BigInteger) value).compareTo((BigInteger) input) > 0 ? input : value;
                }
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(value);
            }
        });
    }

    default Optional<T> max() {
        return collect(new StreamableCollector<>() {
            private T value;

            @Override
            public void apply(T input) {
                if (value == null) {
                    value = input;
                    return;
                }

                if (value instanceof Byte) {
                    value = (Byte) value < (Byte) input ? input : value;
                } else if (value instanceof Short) {
                    value = (Short) value < (Short) input ? input : value;
                } else if (value instanceof Integer) {
                    value = (Integer) value < (Integer) input ? input : value;
                } else if (value instanceof Long) {
                    value = (Long) value < (Long) input ? input : value;
                } else if (value instanceof Float) {
                    value = (Float) value < (Float) input ? input : value;
                } else if (value instanceof Double) {
                    value = (Double) value < (Double) input ? input : value;
                } else if (value instanceof BigDecimal) {
                    value = ((BigDecimal) value).compareTo((BigDecimal) input) < 0 ? input : value;
                } else if (value instanceof BigInteger) {
                    value = ((BigInteger) value).compareTo((BigInteger) input) < 0 ? input : value;
                }
            }

            @Override
            public Optional<T> finish() {
                return Optional.ofNullable(value);
            }
        });
    }

    default Optional<T> average() {
        return collect(new StreamableCollector<>() {
            private long count = 0;
            private T value;

            @Override
            public void apply(T input) {
                count++;
                if (value == null) {
                    value = input;
                    return;
                }

                if (value instanceof Byte) {
                    value = (Byte) value < (Byte) input ? input : value;
                } else if (value instanceof Short) {
                    value = (Short) value < (Short) input ? input : value;
                } else if (value instanceof Integer) {
                    value = (Integer) value < (Integer) input ? input : value;
                } else if (value instanceof Long) {
                    value = (Long) value < (Long) input ? input : value;
                } else if (value instanceof Float) {
                    value = (Float) value < (Float) input ? input : value;
                } else if (value instanceof Double) {
                    value = (Double) value < (Double) input ? input : value;
                } else if (value instanceof BigDecimal) {
                    value = ((BigDecimal) value).compareTo((BigDecimal) input) < 0 ? input : value;
                } else if (value instanceof BigInteger) {
                    value = ((BigInteger) value).compareTo((BigInteger) input) < 0 ? input : value;
                }
            }

            @Override
            public Optional<T> finish() {
                if (count == 0) return Optional.empty();
                if (value == null) return Optional.empty();
                if (count == 1) return Optional.of(value);
                if (value instanceof Byte) {
                    return Optional.of((T) (Byte) (byte) ((Byte) value / count));
                } else if (value instanceof Short) {
                    return Optional.of((T) (Short) (short) ((Short) value / count));
                } else if (value instanceof Integer) {
                    return Optional.of((T) (Integer) (int) ((Integer) value / count));
                } else if (value instanceof Long) {
                    return Optional.of((T) (Long) ((Long) value / count));
                } else if (value instanceof Float) {
                    return Optional.of((T) (Float) ((Float) value / count));
                } else if (value instanceof Double) {
                    return Optional.of((T) (Double) ((Double) value / count));
                } else if (value instanceof BigDecimal) {
                    return Optional.of((T) ((BigDecimal) value).divide(BigDecimal.valueOf(count)));
                } else if (value instanceof BigInteger) {
                    return Optional.of((T) (((BigInteger) value).divide(BigInteger.valueOf(count))));
                } else {
                    throw new IllegalStateException("Unknown Number Type");
                }
            }
        });
    }

    default SummaryStatistics<T> summaryStatistics() {
        return collect(new StreamableCollector<>() {
            private long count = 0;
            private T sum;
            private T min;
            private T max;

            @Override
            public void apply(T input) {
                count++;

                if (sum == null) {
                    sum = input;
                    min = input;
                    max = input;
                    return;
                }

                if (sum instanceof Byte) {
                    sum = (T) (Object) ((Byte) sum + (Byte) input);
                    min = (Byte) min > (Byte) input ? input : min;
                    max = (Byte) max < (Byte) input ? input : max;
                } else if (sum instanceof Short) {
                    sum = (T) (Object) ((Short) sum + (Short) input);
                    min = (Short) min > (Short) input ? input : min;
                    max = (Short) max < (Short) input ? input : max;
                } else if (sum instanceof Integer) {
                    sum = (T) (Object) ((Integer) sum + (Integer) input);
                    min = (Integer) min > (Integer) input ? input : min;
                    max = (Integer) max < (Integer) input ? input : max;
                } else if (sum instanceof Long) {
                    sum = (T) (Object) ((Long) sum + (Long) input);
                    min = (Long) min > (Long) input ? input : min;
                    max = (Long) max < (Long) input ? input : max;
                } else if (sum instanceof Float) {
                    sum = (T) (Object) ((Float) sum + (Float) input);
                    min = (Float) min > (Float) input ? input : min;
                    max = (Float) max < (Float) input ? input : max;
                } else if (sum instanceof Double) {
                    sum = (T) (Object) ((Double) sum + (Double) input);
                    min = (Double) min > (Double) input ? input : min;
                    max = (Double) max < (Double) input ? input : max;
                } else if (sum instanceof BigDecimal) {
                    sum = (T) (((BigDecimal) sum).add((BigDecimal) input));
                    min = ((BigDecimal) min).compareTo((BigDecimal) input) > 0 ? input : min;
                    max = ((BigDecimal) max).compareTo((BigDecimal) input) < 0 ? input : max;
                } else if (sum instanceof BigInteger) {
                    sum = (T) (((BigInteger) sum).add((BigInteger) input));
                    min = ((BigInteger) min).compareTo((BigInteger) input) > 0 ? input : min;
                    max = ((BigInteger) max).compareTo((BigInteger) input) < 0 ? input : max;
                }
            }

            @Override
            public SummaryStatistics<T> finish() {
                return new SummaryStatistics<>(count, Optional.ofNullable(sum), Optional.ofNullable(min), Optional.ofNullable(max));
            }
        });
    }

    class SummaryStatistics<T extends Number> {
        private final long count;
        private final Optional<T> sum;
        private final Optional<T> min;
        private final Optional<T> max;

        public SummaryStatistics(long count, Optional<T> sum, Optional<T> min, Optional<T> max) {
            this.count = count;
            this.sum = sum;
            this.min = min;
            this.max = max;
        }

        public long getCount() {
            return count;
        }

        public Optional<T> getSum() {
            return sum;
        }

        public Optional<T> getMin() {
            return min;
        }

        public Optional<T> getMax() {
            return max;
        }
    }
}

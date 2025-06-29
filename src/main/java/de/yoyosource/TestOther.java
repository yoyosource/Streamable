package de.yoyosource;

import org.w3c.dom.ls.LSOutput;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestOther {

    public interface StreamableLambda extends Serializable {
    }

    public interface StreamableFunction<I, O> extends StreamableLambda {
        O accept(I input);
    }

    public interface StreamablePredicate<I> extends StreamableLambda {
        boolean test(I input);
    }

    public interface StreamableConsumer<I> extends StreamableLambda {
        void accept(I input);
    }

    public interface StreamableRunnable extends StreamableLambda {
        void run();
    }

    private static void test() {
        System.out.println("Test");
    }

    public static void main(String[] args) throws Throwable {
        AtomicInteger counter = new AtomicInteger();
        long multiplier = 2;
        new PipelineBuilder()
                .<Integer, Long>map(input -> input * multiplier)
                .<Integer>filter(input -> input % multiplier == 0)
                .peek(() -> System.out.println("Hello World"))
                // .peek(() -> System.out.println("Hello World"))
                // .peek(() -> {
                //     for (int i = 0; i < 10; i++) {
                //         System.out.println("Hello World " + i);
                //         counter.accumulateAndGet(i, Integer::sum);
                //     }
                // })
                // .peek(TestOther::test)
                // .peek(() -> {
                //     System.out.println(Arrays.toString(args));
                // })
                // .peek(() -> {
                //     System.out.println(Arrays.toString(args));
                // })
                .build()
                .run();

        // TestOther2.test(() -> System.out.println("Hello World! " + Arrays.toString(args)));
    }
}

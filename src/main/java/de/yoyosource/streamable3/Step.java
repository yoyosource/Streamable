package de.yoyosource.streamable3;

public interface Step {

    void consume(Element element);

    final class Noop implements Step {

        public static final Noop INSTANCE = new Noop();

        private Noop() {
        }

        @Override
        public void consume(Element element) {
        }
    }

    final class Printer implements Step {

        public static final Printer INSTANCE = new Printer();

        private Printer() {
        }

        @Override
        public void consume(Element element) {
            System.out.println(element);
        }
    }
}

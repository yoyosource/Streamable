package de.yoyosource.streamable3;

public class Test2 {

    public static void main(String[] args) {
        Sequence<String> sequence = new Sequence<>();

        sequence.inserter()
                .release();

        while (sequence.hasUnreleasedInserter()) {
            Sequence.Inserter<String> inserter = sequence.inserter();
            for (String s : sequence) {
                System.out.println(s);
            }
            inserter.release();
        }
    }
}

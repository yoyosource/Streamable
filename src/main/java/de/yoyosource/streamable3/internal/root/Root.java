package de.yoyosource.streamable3.internal.root;

import de.yoyosource.streamable3.internal.Element;
import de.yoyosource.streamable3.internal.FinishException;
import de.yoyosource.streamable3.internal.StreamableSupplier;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

public class Root extends StreamableSupplier {

    private long index = 0;
    private Iterator iterator;

    @Setter
    @Getter
    private Throwable error = null;

    public Root(Iterator iterator) {
        this.iterator = iterator;
        this.root = this;
    }

    public void evaluate() {
        try {
            iterator.forEachRemaining(o -> {
                next.consume(new Element.Value(index++, o));
            });
            next.consume(new Element.Finish());
        } catch (FinishException e) {
            // Ignore
        }
    }
}

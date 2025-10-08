package de.yoyosource.streamable.internal;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SingleData<A> {
    public A first;

    @Override
    public String toString() {
        return "SingleData[" + first + ']';
    }
}

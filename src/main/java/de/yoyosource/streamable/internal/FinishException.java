package de.yoyosource.streamable.internal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FinishException extends RuntimeException {

    public static final FinishException INSTANCE = new FinishException();
}

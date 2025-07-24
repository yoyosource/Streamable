package de.yoyosource.streamable.internal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CloseException extends RuntimeException {

    public static final CloseException INSTANCE = new CloseException();
}

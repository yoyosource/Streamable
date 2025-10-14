package de.yoyosource.streamable.internal;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

public class GroupException extends RuntimeException {

    private final List<Throwable> throwables;

    public GroupException(List<Throwable> throwables) {
        this.throwables = throwables;
    }

    @Override
    public void printStackTrace() {
        for (Throwable throwable : throwables) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void printStackTrace(PrintStream s) {
        for (Throwable throwable : throwables) {
            throwable.printStackTrace(s);
        }
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        for (Throwable throwable : throwables) {
            throwable.printStackTrace(s);
        }
    }
}

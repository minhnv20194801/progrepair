package org.group10.utils;

import java.io.OutputStream;

/**
 * A dummy {@link OutputStream} implementation that discards all bytes written to it. <br>
 * Used to suppress the output, ie. ignore Java compiler error messages
 */
public class DummyOutputStream extends OutputStream {
    public void write(int b) {

    }
}

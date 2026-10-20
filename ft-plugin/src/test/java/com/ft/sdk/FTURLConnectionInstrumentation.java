package com.ft.sdk;

import java.net.Proxy;
import java.net.URLConnection;

/** Test-only Agent hook used to execute generated Plugin bytecode. */
public final class FTURLConnectionInstrumentation {
    public static Proxy proxy;
    public static int calls;
    public static URLConnection wrap(URLConnection connection, Proxy selected) {
        proxy = selected;
        calls++;
        return connection;
    }
}

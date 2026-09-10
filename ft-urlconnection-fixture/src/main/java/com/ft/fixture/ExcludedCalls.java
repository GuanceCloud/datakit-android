package com.ft.fixture;
import java.io.IOException;
import java.net.*;
public final class ExcludedCalls {
    public static URLConnection open(URL url) throws IOException { return url.openConnection(); }
    public static URLConnection open(URL url, Proxy proxy) throws IOException { return url.openConnection(proxy); }
}

package com.ft.fixtureaar;
import java.io.IOException;
import java.net.*;
public final class Calls {
    public static URLConnection open(URL url) throws IOException { return url.openConnection(); }
    public static URLConnection open(URL url, Proxy proxy) throws IOException { return url.openConnection(proxy); }
}

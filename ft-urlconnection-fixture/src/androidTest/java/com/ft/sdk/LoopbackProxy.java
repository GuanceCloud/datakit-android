package com.ft.sdk;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;

/** CONNECT tunnel to a single loopback test server. Never connects to an external address. */
final class LoopbackProxy implements Closeable {
    final ServerSocket listener = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"));
    final ExecutorService workers = Executors.newCachedThreadPool();
    final BlockingQueue<String> connects = new LinkedBlockingQueue<>();
    final BlockingQueue<Map<String, String>> requestHeaders = new LinkedBlockingQueue<>();
    final int targetPort;
    final boolean requireAuthentication;
    volatile boolean closed;

    LoopbackProxy(int targetPort) throws IOException {
        this(targetPort, false);
    }

    LoopbackProxy(int targetPort, boolean requireAuthentication) throws IOException {
        this.targetPort = targetPort;
        this.requireAuthentication = requireAuthentication;
        workers.execute(() -> {
            while (!closed) try {
                Socket client = listener.accept();
                workers.execute(() -> tunnel(client));
            } catch (IOException e) { if (!closed) throw new RuntimeException(e); }
        });
    }

    Proxy proxy() { return new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", listener.getLocalPort())); }

    private void tunnel(Socket client) {
        try (Socket downstream = client) {
            client.setSoTimeout(4000);
            InputStream in = client.getInputStream();
            while (true) {
                String request = LoopbackServer.line(in);
                if (request.isEmpty()) return;
                Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                for (String line = LoopbackServer.line(in); !line.isEmpty(); line = LoopbackServer.line(in)) {
                    int separator = line.indexOf(':');
                    if (separator > 0) headers.put(line.substring(0, separator), line.substring(separator + 1).trim());
                }
                connects.add(request);
                requestHeaders.add(headers);
                if (!requireAuthentication || headers.containsKey("Proxy-Authorization")) break;
                // Android retries CONNECT authentication on the same proxy socket.
                client.getOutputStream().write(("HTTP/1.1 407 Proxy Authentication Required\r\n"
                        + "Proxy-Authenticate: Basic realm=\"fixture-proxy\"\r\n"
                        + "Content-Length: 0\r\n\r\n").getBytes("UTF-8"));
                client.getOutputStream().flush();
            }
            try (Socket upstream = new Socket("127.0.0.1", targetPort)) {
                upstream.setSoTimeout(4000);
                client.getOutputStream().write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes("UTF-8"));
                client.getOutputStream().flush();
                Future<?> outgoing = workers.submit(() -> pipe(client, upstream));
                pipe(upstream, client);
                outgoing.get(5, TimeUnit.SECONDS);
            }
        } catch (Exception ignored) { }
    }

    private void pipe(Socket source, Socket target) {
        try {
            byte[] bytes = new byte[8192];
            for (int n; (n = source.getInputStream().read(bytes)) != -1;) {
                target.getOutputStream().write(bytes, 0, n); target.getOutputStream().flush();
            }
            target.shutdownOutput();
        } catch (IOException ignored) { }
    }

    @Override public void close() throws IOException { closed = true; listener.close(); workers.shutdownNow(); }
}

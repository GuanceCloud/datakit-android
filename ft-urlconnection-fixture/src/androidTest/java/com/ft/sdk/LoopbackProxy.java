package com.ft.sdk;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/** CONNECT tunnel to a single loopback test server. Never connects to an external address. */
final class LoopbackProxy implements Closeable {
    final ServerSocket listener = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"));
    final ExecutorService workers = Executors.newCachedThreadPool();
    final BlockingQueue<String> connects = new LinkedBlockingQueue<>();
    final int targetPort;
    volatile boolean closed;

    LoopbackProxy(int targetPort) throws IOException {
        this.targetPort = targetPort;
        workers.execute(() -> {
            while (!closed) try {
                Socket client = listener.accept();
                workers.execute(() -> tunnel(client));
            } catch (IOException e) { if (!closed) throw new RuntimeException(e); }
        });
    }

    Proxy proxy() { return new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", listener.getLocalPort())); }

    private void tunnel(Socket client) {
        try (Socket upstream = new Socket("127.0.0.1", targetPort); Socket downstream = client) {
            client.setSoTimeout(4000); upstream.setSoTimeout(4000);
            InputStream in = client.getInputStream();
            String request = LoopbackServer.line(in);
            while (!LoopbackServer.line(in).isEmpty()) { }
            connects.add(request);
            client.getOutputStream().write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes("UTF-8"));
            client.getOutputStream().flush();
            Future<?> outgoing = workers.submit(() -> pipe(client, upstream));
            pipe(upstream, client);
            outgoing.get(5, TimeUnit.SECONDS);
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

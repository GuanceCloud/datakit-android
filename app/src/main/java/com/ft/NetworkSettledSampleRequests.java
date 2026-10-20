package com.ft;

import android.os.Handler;
import android.os.Looper;

import com.ft.sdk.FTResourceEventListener;
import com.ft.sdk.FTResourceInterceptor;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** Bounded, per-run loopback server. No external endpoint or fixture dependency is required. */
final class NetworkSettledSampleRequests implements Closeable {
    interface Listener { void onMessage(String message); }

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ServerSocket server;
    private final ExecutorService responses = Executors.newFixedThreadPool(2,
            task -> new Thread(task, "ft-network-settled-response"));
    private final ThreadPoolExecutor calls = new ThreadPoolExecutor(2, 2, 0,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            task -> new Thread(task, "ft-network-settled-call"));
    private final Set<Socket> sockets = new HashSet<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicInteger remaining = new AtomicInteger();
    private final Thread acceptor;
    private final OkHttpClient client;
    private volatile Listener listener;
    private boolean pendingStart;
    private final Runnable deadline = () -> {
        notifyMessage("Scenario exceeded 8 seconds. Requests stopped. Check the RUM data and logs.");
        close();
    };
    private final Runnable late = () -> {
        pendingStart = false;
        request("/late");
    };

    NetworkSettledSampleRequests(Listener listener) throws IOException {
        this.listener = listener;
        server = new ServerSocket(0, 4, InetAddress.getByName("127.0.0.1"));
        calls.prestartAllCoreThreads();
        client = new OkHttpClient.Builder()
                .dispatcher(new Dispatcher(calls))
                .addInterceptor(new FTResourceInterceptor())
                .eventListenerFactory(new FTResourceEventListener.FTFactory())
                .retryOnConnectionFailure(false)
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .build();
        acceptor = new Thread(this::accept, "ft-network-settled-server");
        acceptor.start();
    }

    void start(String scenario) {
        boolean lateOnly = NetworkSettledTimingActivity.LATE_ONLY.equals(scenario);
        boolean leaveEarly = NetworkSettledTimingActivity.LEAVE_EARLY.equals(scenario);
        remaining.set(lateOnly ? 1 : leaveEarly ? 2 : 3);
        main.postDelayed(deadline, 8000);
        if (!lateOnly) {
            request("/fast");
            request("/slow");
        }
        if (!leaveEarly) {
            pendingStart = true;
            main.postDelayed(late, 350);
        }
    }

    void cancelPendingStart() {
        if (pendingStart) {
            pendingStart = false;
            main.removeCallbacks(late);
            completed();
        }
    }

    void detach() { listener = null; }

    private void request(String path) {
        if (closed.get()) return;
        Request request = new Request.Builder()
                .url("http://127.0.0.1:" + server.getLocalPort() + path).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException error) {
                notifyMessage(path + " request failed: " + error.getMessage());
                completed();
            }

            @Override public void onResponse(Call call, Response response) {
                try (Response body = response) {
                    // Reading the real response body triggers the SDK completion callback.
                    if (body.body() != null) body.body().string();
                    notifyMessage(path + " completed, HTTP " + body.code());
                } catch (IOException error) {
                    notifyMessage(path + " response read failed: " + error.getMessage());
                } finally {
                    completed();
                }
            }
        });
    }

    private void completed() {
        if (remaining.decrementAndGet() == 0) {
            notifyMessage("Requests finished. Use view_id to check network_settled_time in the RUM View.");
            main.post(this::close);
        }
    }

    private void notifyMessage(String message) {
        Listener current = listener;
        if (current != null) current.onMessage(message);
    }

    private void accept() {
        try {
            while (!closed.get()) {
                Socket socket = server.accept();
                synchronized (sockets) {
                    if (closed.get()) {
                        socket.close();
                        return;
                    }
                    sockets.add(socket);
                }
                try {
                    responses.execute(() -> respond(socket));
                } catch (java.util.concurrent.RejectedExecutionException stopped) {
                    socket.close();
                    return;
                }
            }
        } catch (IOException error) {
            if (!closed.get()) {
                notifyMessage("Local HTTP server failed: " + error.getMessage());
                close();
            }
        }
    }

    private void respond(Socket socket) {
        try (Socket connection = socket) {
            connection.setSoTimeout(3000);
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.US_ASCII));
            String requestLine = input.readLine();
            if (requestLine == null) return;
            String header;
            while ((header = input.readLine()) != null && !header.isEmpty()) { }
            String path = requestLine.split(" ")[1];
            long delay = "/fast".equals(path) ? 250 : "/slow".equals(path) ? 1200 : 1600;
            Thread.sleep(delay);
            connection.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Length: 2\r\n"
                    + "Content-Type: text/plain\r\nConnection: close\r\n\r\nOK")
                    .getBytes(StandardCharsets.US_ASCII));
            connection.getOutputStream().flush();
        } catch (InterruptedException stopped) {
            Thread.currentThread().interrupt();
        } catch (IOException error) {
            if (!closed.get()) notifyMessage("Local response failed: " + error.getMessage());
        } finally {
            synchronized (sockets) { sockets.remove(socket); }
        }
    }

    @Override public void close() {
        if (!closed.compareAndSet(false, true)) return;
        main.removeCallbacksAndMessages(null);
        listener = null;
        try { server.close(); } catch (IOException ignored) { }
        synchronized (sockets) {
            for (Socket socket : sockets) {
                try { socket.close(); } catch (IOException ignored) { }
            }
            sockets.clear();
        }
        client.dispatcher().cancelAll();
        client.connectionPool().evictAll();
        calls.shutdown();
        responses.shutdownNow();
        acceptor.interrupt();
    }
}

package com.ft.sdk.garble.http;

import com.ft.sdk.diagnostics.internal.DiagnosticsBridge;
import com.ft.sdk.garble.compress.DeflateInterceptor;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.InflaterInputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class DiagnosticCompressionTest {
    @Parameterized.Parameters(name = "compression={0}, diagnostics={1}, bytes={2}")
    public static Collection<Object[]> cases() {
        Collection<Object[]> cases = new ArrayList<>();
        for (boolean compression : new boolean[]{false, true}) {
            for (boolean diagnostics : new boolean[]{false, true}) {
                for (int size : new int[]{0, 21, 32768}) {
                    cases.add(new Object[]{compression, diagnostics, size});
                }
            }
        }
        return cases;
    }

    private final boolean compression;
    private final boolean diagnostics;
    private final int size;

    public DiagnosticCompressionTest(boolean compression, boolean diagnostics, int size) {
        this.compression = compression;
        this.diagnostics = diagnostics;
        this.size = size;
    }

    @Test public void uploadsCompleteBodyAndCountsEncodedBytes() throws Exception {
        byte[] payload = new byte[size];
        new Random(42).nextBytes(payload);
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("ok"));
        server.play();
        DiagnosticsBridge.Transfer transfer = new DiagnosticsBridge.Transfer();
        DiagnosticsBridge.transfer(diagnostics ? transfer : null);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .callTimeout(5, TimeUnit.SECONDS)
                .addNetworkInterceptor(new DiagnosticNetworkInterceptor());
        if (compression) builder.addInterceptor(new DeflateInterceptor());
        OkHttpClient client = builder.build();
        try {
            Request request = new Request.Builder().url(server.getUrl("/upload"))
                    .post(RequestBody.create(null, payload)).build();
            try (Response response = client.newCall(request).execute()) {
                assertEquals(200, response.code());
                assertEquals("ok", response.body().string());
            }
            RecordedRequest recorded = server.takeRequest();
            byte[] encoded = recorded.getBody();
            assertEquals(encoded.length, recorded.getBodySize());
            if (compression) {
                assertEquals("deflate", recorded.getHeader("Content-Encoding"));
                ByteArrayOutputStream decoded = new ByteArrayOutputStream();
                try (InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(encoded))) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = inflater.read(buffer)) != -1) decoded.write(buffer, 0, count);
                }
                assertArrayEquals(payload, decoded.toByteArray());
            } else {
                assertNull(recorded.getHeader("Content-Encoding"));
                assertArrayEquals(payload, encoded);
            }
            if (diagnostics) {
                assertEquals(encoded.length, transfer.tx);
                assertEquals(2, transfer.rx);
            }
        } finally {
            DiagnosticsBridge.transfer(null);
            client.connectionPool().evictAll();
            client.dispatcher().executorService().shutdown();
            server.shutdown();
        }
    }
}

package com.ft.sdk.garble.compress;

import com.ft.sdk.garble.utils.LogUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

import static org.junit.Assert.*;

public class DeflateInterceptorTest {
    @Before public void disableLogging() { LogUtils.setDebug(false); }
    @After public void restoreLogging() { LogUtils.setDebug(true); }

    @Test public void leavesCallerSinkOpen() throws Exception {
        final boolean[] closed = {false};
        BufferedSink sink = Okio.buffer(new ForwardingSink(new Buffer()) {
            @Override public void close() throws IOException { closed[0] = true; super.close(); }
        });
        writeBody(RequestBody.create(null, "payload"), sink);
        assertFalse(closed[0]);
        sink.writeUtf8("caller can still write");
        sink.flush();
        sink.close();
    }

    @Test public void propagatesWriteFailureDuringFinalization() {
        IOException failure = new IOException("transport write failed");
        BufferedSink sink = Okio.buffer(new ForwardingSink(new Buffer()) {
            @Override public void write(Buffer source, long byteCount) throws IOException { throw failure; }
        });
        // A small body stays buffered until compression is finalized.
        assertSame(failure, assertThrows(IOException.class,
                () -> writeBody(RequestBody.create(null, "payload"), sink)));
    }

    @Test public void propagatesFlushFailureDuringFinalization() {
        IOException failure = new IOException("transport flush failed");
        BufferedSink sink = Okio.buffer(new ForwardingSink(new Buffer()) {
            @Override public void flush() throws IOException { throw failure; }
        });
        assertSame(failure, assertThrows(IOException.class,
                () -> writeBody(RequestBody.create(null, "payload"), sink)));
    }

    @Test public void preservesBodyFailureWhenFinalizationAlsoFails() {
        IOException bodyFailure = new IOException("body failed");
        IOException transportFailure = new IOException("transport failed");
        RequestBody body = new RequestBody() {
            @Override public MediaType contentType() { return null; }
            @Override public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("partial payload");
                throw bodyFailure;
            }
        };
        BufferedSink sink = Okio.buffer(new ForwardingSink(new Buffer()) {
            @Override public void write(Buffer source, long byteCount) throws IOException { throw transportFailure; }
        });
        assertSame(bodyFailure, assertThrows(IOException.class, () -> writeBody(body, sink)));
        assertArrayEquals(new Throwable[]{transportFailure}, bodyFailure.getSuppressed());
    }

    private void writeBody(RequestBody body, BufferedSink sink) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(new DeflateInterceptor())
                .addInterceptor(chain -> {
                    chain.request().body().writeTo(sink);
                    return new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                            .code(200).message("OK").body(ResponseBody.create(null, "ok")).build();
                }).build();
        // The terminal application interceptor writes to the supplied sink; no socket is opened.
        try (Response ignored = client.newCall(new Request.Builder().url("http://localhost/upload")
                .post(body).build()).execute()) {
        } finally {
            client.connectionPool().evictAll();
            client.dispatcher().executorService().shutdown();
        }
    }
}

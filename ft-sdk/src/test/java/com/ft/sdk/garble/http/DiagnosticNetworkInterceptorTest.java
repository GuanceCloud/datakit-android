package com.ft.sdk.garble.http;
import com.ft.sdk.diagnostics.internal.DiagnosticsBridge;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.MockResponse;
import java.net.InetSocketAddress;
import java.io.*;
import java.util.zip.GZIPOutputStream;
import okhttp3.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class DiagnosticNetworkInterceptorTest {
    @Test public void countsEncodedBodiesAndPreservesResponseDecoding() throws Exception {
        ByteArrayOutputStream compressed=new ByteArrayOutputStream();
        try(GZIPOutputStream gzip=new GZIPOutputStream(compressed)){gzip.write("response data".getBytes("UTF-8"));}
        byte[] encoded=compressed.toByteArray();
        MockWebServer server=new MockWebServer();
        server.enqueue(new MockResponse().setBody(encoded).addHeader("Content-Encoding", "gzip"));
        server.play();
        DiagnosticsBridge.Transfer t=new DiagnosticsBridge.Transfer();DiagnosticsBridge.transfer(t);
        try {
            OkHttpClient client=new OkHttpClient.Builder().addNetworkInterceptor(new DiagnosticNetworkInterceptor()).build();
            Request request=new Request.Builder().url("http://127.0.0.1:"+server.getPort()+"/")
                .post(RequestBody.create(MediaType.parse("text/plain"),"hello")).build();
            try(Response response=client.newCall(request).execute()) { assertEquals("response data",response.body().string()); }
            assertEquals(5,t.tx);assertEquals(encoded.length,t.rx);
        } finally {DiagnosticsBridge.transfer(null);server.shutdown();}
    }
}

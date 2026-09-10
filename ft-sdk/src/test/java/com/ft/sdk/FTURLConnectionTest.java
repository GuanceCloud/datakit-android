package com.ft.sdk;

import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import org.junit.Test;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;
import static org.junit.Assert.*;

public class FTURLConnectionTest {
    static final class Recorder implements FTURLConnectionRequestState.Reporter {
        int starts, finishes, discards;
        ResourceParams params;
        NetStatusBean timing;
        @Override public boolean start(FTURLConnectionRequestState s) { starts++; return true; }
        @Override public void finish(FTURLConnectionRequestState s, ResourceParams p, NetStatusBean n) {
            finishes++; params = p; timing = n;
        }
        @Override public void discard(FTURLConnectionRequestState s) { discards++; }
    }
    static class Connection extends HttpURLConnection {
        InputStream input = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStream error;
        OutputStream output = new ByteArrayOutputStream();
        int status = 200, connects, disconnects;
        IOException failure;
        Connection() throws Exception { super(new URL("http://localhost/test")); }
        @Override public void connect() throws IOException { connects++; if (failure != null) throw failure; }
        @Override public void disconnect() { disconnects++; }
        @Override public boolean usingProxy() { return false; }
        @Override public int getResponseCode() { return status; }
        @Override public InputStream getInputStream() throws IOException {
            if (failure != null) throw failure;
            if (status >= 400) throw new FileNotFoundException("HTTP error");
            return input;
        }
        @Override public InputStream getErrorStream() { return error; }
        @Override public OutputStream getOutputStream() {
            if (getDoOutput() && getRequestMethod().equals("GET")) method = "POST";
            return output;
        }
        @Override public Map<String, List<String>> getHeaderFields() {
            Map<String,List<String>> headers = new HashMap<>();
            headers.put(null, Collections.singletonList("HTTP/1.1 " + status + " status"));
            headers.put("Content-Type", Collections.singletonList("text/plain"));
            return headers;
        }
    }
    static class Fixture {
        final Connection original;
        final Recorder recorder = new Recorder();
        final FTURLConnectionRequestState state;
        final HttpURLConnection wrapped;
        Fixture() throws Exception {
            original = new Connection();
            state = new FTURLConnectionRequestState(original.getURL().toString(), 0, recorder);
            wrapped = new FTHttpURLConnection(original, new FTURLConnectionDelegate(original, state));
        }
    }

    @Test public void configurationAndUnusedDisconnectNeverStart() throws Exception {
        Fixture f = new Fixture();
        f.wrapped.setReadTimeout(123); f.wrapped.setConnectTimeout(456);
        f.wrapped.setInstanceFollowRedirects(false); f.wrapped.setUseCaches(false);
        f.wrapped.setRequestProperty("X-Test", "value");
        assertEquals(123, f.original.getReadTimeout()); assertEquals(456, f.original.getConnectTimeout());
        assertFalse(f.original.getInstanceFollowRedirects()); assertFalse(f.original.getUseCaches());
        assertEquals("value", f.original.getRequestProperty("X-Test"));
        assertEquals(0, f.recorder.starts);
        assertNull(f.wrapped.getErrorStream()); f.wrapped.disconnect();
        assertEquals(1, f.recorder.discards); assertFalse(f.state.started);
        assertEquals(0, f.recorder.finishes);
        assertEquals(f.wrapped, f.wrapped);
    }

    @Test public void eofCloseAndDisconnectFinishOnlyOnce() throws Exception {
        Fixture f = new Fixture(); InputStream input = f.wrapped.getInputStream();
        assertSame(input, f.wrapped.getInputStream());
        byte[] bytes = new byte[8]; assertEquals(4, input.read(bytes));
        assertEquals(-1, input.read()); assertEquals(-1, input.read(bytes));
        input.close(); input.close(); f.wrapped.disconnect(); f.wrapped.disconnect();
        assertEquals(1, f.recorder.starts); assertEquals(1, f.recorder.finishes);
        assertEquals(4, f.recorder.timing.responseBodySize);
        assertFalse(f.recorder.timing.connectionReuseKnown); assertFalse(f.recorder.timing.phaseTimingKnown);
        assertEquals(-1, f.recorder.timing.dnsStartTime);
        assertEquals("eof", f.recorder.params.property.get("resource_completion"));
        assertTrue(f.recorder.params.enableResourceSize);
        assertEquals("", f.recorder.params.responseBody);
    }

    @Test public void uploadCloseDoesNotEndResponseAndEffectiveMethodIsPost() throws Exception {
        Fixture f = new Fixture(); f.wrapped.setDoOutput(true);
        OutputStream output = f.wrapped.getOutputStream(); assertSame(output, f.wrapped.getOutputStream());
        output.write(1); output.write(new byte[]{2,3}); output.write(new byte[]{0,4,0},1,1);
        output.flush(); output.close(); assertEquals(0, f.recorder.finishes);
        InputStream input = f.wrapped.getInputStream(); while (input.read() != -1) { }
        assertEquals(4, f.recorder.timing.requestBodySize); assertEquals("POST", f.recorder.params.resourceMethod);
        assertArrayEquals(new byte[]{1,2,3,4}, ((ByteArrayOutputStream)f.original.output).toByteArray());
    }

    @Test public void markResetSkipAndZeroReadsPreserveUniqueByteCount() throws Exception {
        Fixture f = new Fixture(); InputStream input = f.wrapped.getInputStream();
        input.mark(100); assertEquals(2, input.skip(2)); input.reset();
        assertEquals(1, input.read()); assertEquals(0, input.skip(0));
        assertEquals(0, input.read(new byte[0])); assertEquals(0, f.recorder.finishes);
        assertEquals(3, input.read(new byte[8],1,3)); assertEquals(-1,input.read());
        assertEquals(4, f.recorder.timing.responseBodySize);
    }

    @Test public void earlyCloseHasObservedBytesButNoCompleteSize() throws Exception {
        Fixture f = new Fixture(); InputStream input = f.wrapped.getInputStream(); input.read(); input.close();
        assertFalse(f.recorder.params.enableResourceSize);
        assertEquals(false, f.recorder.params.property.get("resource_body_complete"));
        assertEquals(1L, f.recorder.params.property.get("resource_response_bytes_observed"));
    }

    @Test public void httpErrorsWaitForErrorBodyIncludingNullErrorStream() throws Exception {
        for (int status : new int[]{404,500}) for (boolean body : new boolean[]{true,false}) {
            Fixture f = new Fixture(); f.original.status = status;
            f.original.error = body ? f.original.input : null;
            try { f.wrapped.getInputStream(); fail(); } catch (FileNotFoundException expected) { }
            assertEquals(0, f.recorder.finishes);
            InputStream error = f.wrapped.getErrorStream();
            if (body) { assertSame(error,f.wrapped.getErrorStream()); while(error.read()!=-1){} }
            else assertNull(error);
            f.wrapped.disconnect();
            assertEquals(1,f.recorder.finishes); assertEquals(status,f.recorder.params.resourceStatus);
            assertFalse(f.recorder.params.forceNetworkError);
            assertEquals(body ? 4 : 0,f.recorder.timing.responseBodySize);
        }
    }

    @Test public void noBodyResponsesEndOnHeadersButInformationalDoesNot() throws Exception {
        for (int status : new int[]{204,304,101}) {
            Fixture f = new Fixture(); f.original.status = status; f.wrapped.getResponseCode();
            assertEquals(status == 101 ? 0 : 1,f.recorder.finishes);
        }
        Fixture head = new Fixture(); head.wrapped.setRequestMethod("HEAD"); head.wrapped.getResponseCode();
        assertEquals(1,head.recorder.finishes); assertEquals(0,head.recorder.timing.responseBodySize);
    }

    @Test public void failureRethrowsOriginalAndKeepsObservedStatus() throws Exception {
        Fixture f = new Fixture(); IOException expected = new SocketTimeoutException("test");
        f.original.input = new InputStream() { @Override public int read() throws IOException { throw expected; } };
        InputStream input = f.wrapped.getInputStream();
        try { input.read(); fail(); } catch(IOException actual) { assertSame(expected,actual); }
        f.wrapped.disconnect(); assertEquals(1,f.recorder.finishes);
        assertEquals(200,f.recorder.params.resourceStatus); assertTrue(f.recorder.params.forceNetworkError);
    }

    @Test public void unsupportedResetAndInvalidArgumentsDoNotCreateNetworkErrors() throws Exception {
        Fixture f = new Fixture(); f.original.input = new InputStream() { @Override public int read(){return -1;} };
        InputStream input=f.wrapped.getInputStream();
        try { input.reset(); fail(); } catch(IOException expected) { }
        try { input.read(new byte[1],-1,1); fail(); } catch(IndexOutOfBoundsException expected) { }
        assertEquals(0,f.recorder.finishes);
    }

    @Test public void concurrentTerminalCallbacksAreUniqueAndDiscardCannotRestart() throws Exception {
        Fixture f = new Fixture(); f.wrapped.connect();
        ExecutorService pool=Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> jobs=new ArrayList<>();
            for(int i=0;i<50;i++)jobs.add(pool.submit(()->f.state.finish("disconnected",false,null)));
            for(Future<?> job:jobs)job.get();
        } finally {pool.shutdownNow();}
        assertEquals(1,f.recorder.finishes);
        Fixture abandoned=new Fixture(); abandoned.wrapped.connect(); abandoned.state.discard();
        abandoned.wrapped.getInputStream().close(); assertEquals(0,abandoned.recorder.finishes);
        assertEquals(1,abandoned.recorder.starts);
    }

    @Test public void largeCountsDoNotOverflowInt() throws Exception {
        Fixture f=new Fixture(); f.wrapped.connect();
        f.state.progress((long)Integer.MAX_VALUE+10,(long)Integer.MAX_VALUE+20);
        f.state.finish("eof",true,null);
        assertEquals((long)Integer.MAX_VALUE+10,f.recorder.timing.responseBodySize);
    }

    @Test public void everyPublicAndroidConnectionMethodHasAForwardingOverride() throws Exception {
        for(Class<?> type:new Class<?>[]{HttpURLConnection.class,HttpsURLConnection.class}) {
            Class<?> wrapper=type==HttpsURLConnection.class?FTHttpsURLConnection.class:FTHttpURLConnection.class;
            for(java.lang.reflect.Method method:type.getMethods()) {
                if(java.lang.reflect.Modifier.isStatic(method.getModifiers()) || java.lang.reflect.Modifier.isFinal(method.getModifiers())
                        || method.getDeclaringClass()==Object.class || method.getName().equals("setAuthenticator")
                        || method.getName().equals("getSSLSession"))continue; // JDK-only, absent from Android API 34.
                assertEquals(method.toString(),wrapper,wrapper.getMethod(method.getName(),method.getParameterTypes()).getDeclaringClass());
            }
        }
    }
    @Test public void knownApplicationOkHttpAdaptersAreExcludedByExactType() throws Exception {
        String[] paths = System.getProperty("ft.huc.adapterClasspath").split(File.pathSeparator);
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) urls[i] = new File(paths[i]).toURI().toURL();
        try (URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader().getParent())) {
            Class<?> clientType = loader.loadClass("okhttp3.OkHttpClient");
            Class<?> factoryType = loader.loadClass("okhttp3.OkUrlFactory");
            Object factory = factoryType.getConstructor(clientType).newInstance(clientType.getConstructor().newInstance());
            for (String protocol : new String[]{"http", "https"}) {
                Object connection = factoryType.getMethod("open", URL.class).invoke(factory, new URL(protocol + "://localhost/"));
                assertTrue(FTURLConnectionInstrumentation.isExcludedAdapter(connection.getClass()));
            }
        }
        assertFalse(FTURLConnectionInstrumentation.isExcludedAdapter(Connection.class));
    }

    @Test public void firstErrorStreamRetainsPreCallTimeAndHeadersWithoutProbingOrdinaryIoFailure() throws Exception {
        Recorder recorder = new Recorder();
        long[] entered = new long[1];
        Connection original = new Connection() {
            @Override public InputStream getErrorStream() {
                entered[0] = System.nanoTime();
                return input;
            }
        };
        original.status = 500; original.setRequestProperty("X-Test", "before-connect");
        FTURLConnectionRequestState state = new FTURLConnectionRequestState(original.getURL().toString(), 0, recorder);
        HttpURLConnection wrapped = new FTHttpURLConnection(original, new FTURLConnectionDelegate(original, state));
        InputStream error = wrapped.getErrorStream();
        assertTrue(state.startedAt <= entered[0]);
        while (error.read() != -1) { }
        assertEquals("before-connect", recorder.params.requestHeaderMap.get("X-Test").get(0));

        Recorder failedRecorder = new Recorder();
        IOException requestFailure = new SocketTimeoutException("original");
        Connection failed = new Connection() {
            @Override public InputStream getInputStream() throws IOException { throw requestFailure; }
            @Override public InputStream getErrorStream() { throw new AssertionError("Must not probe an I/O failure"); }
            @Override public int getResponseCode() { throw new AssertionError("Must not retry a failed request"); }
        };
        HttpURLConnection failedWrapper = new FTHttpURLConnection(failed, new FTURLConnectionDelegate(failed,
                new FTURLConnectionRequestState(failed.getURL().toString(), 0, failedRecorder)));
        try { failedWrapper.getInputStream(); fail(); } catch (IOException actual) { assertSame(requestFailure, actual); }
        assertEquals(1, failedRecorder.finishes);
    }

    @Test public void connectionFailuresPreserveOriginalExceptionAndHaveOnlyOneUnknownStatusResource() throws Exception {
        for (IOException expected : new IOException[]{new UnknownHostException("test"), new ConnectException("test"),
                new SocketTimeoutException("test"), new SSLHandshakeException("test")}) {
            Fixture f = new Fixture(); f.original.failure = expected;
            try { f.wrapped.connect(); fail(); } catch (IOException actual) { assertSame(expected, actual); }
            f.wrapped.disconnect();
            assertEquals(1, f.recorder.finishes); assertEquals(0, f.recorder.params.resourceStatus);
            assertTrue(f.recorder.params.forceNetworkError);
        }
    }

    @Test public void partialUploadFailureDoesNotGuessWrittenBytes() throws Exception {
        Fixture f = new Fixture(); IOException expected = new IOException("partial upload");
        f.original.output = new OutputStream() {
            @Override public void write(int value) throws IOException { throw expected; }
            @Override public void write(byte[] bytes) throws IOException { throw expected; }
        };
        f.wrapped.setDoOutput(true);
        try { f.wrapped.getOutputStream().write(new byte[50]); fail(); }
        catch (IOException actual) { assertSame(expected, actual); }
        assertEquals(1, f.recorder.finishes);
        assertEquals(0, f.recorder.timing.requestBodySize);
        assertFalse(f.recorder.params.enableResourceSize);
    }

    @Test public void nullErrorProbeCanBeFollowedByARealRequestWithANewResourceId() throws Exception {
        Fixture f = new Fixture(); String probeId = f.state.id;
        assertNull(f.wrapped.getErrorStream());
        assertNotEquals(probeId, f.state.id);
        InputStream input = f.wrapped.getInputStream(); while (input.read() != -1) { }
        assertEquals(1, f.recorder.finishes); assertEquals(4, f.recorder.timing.responseBodySize);
    }

    @Test public void differentOriginalResponseStreamsMakeCompleteSizeUnknown() throws Exception {
        Fixture f = new Fixture();
        f.wrapped.getInputStream().read();
        f.original.input = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        InputStream second = f.wrapped.getInputStream(); while (second.read() != -1) { }
        assertFalse(f.recorder.params.enableResourceSize);
        assertEquals(5, f.recorder.timing.responseBodySize);
    }

}

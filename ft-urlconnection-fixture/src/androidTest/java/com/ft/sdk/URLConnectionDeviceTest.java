package com.ft.sdk;

import android.content.Context;
import android.database.Cursor;
import android.os.Debug;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.ft.fixture.*;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.*;
import com.ft.sdk.garble.db.*;
import com.ft.sdk.garble.threadpool.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class URLConnectionDeviceTest {
    Context target, tests;
    FTRUMConfig config;

    @Before public void install() throws Exception {
        target=InstrumentationRegistry.getInstrumentation().getTargetContext();
        tests=InstrumentationRegistry.getInstrumentation().getContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            FTSdk.install(FTSDKConfig.builder().setAutoSync(false).setEnableDataFilter(false));
            FTSdk.clearAllData();
            config=new FTRUMConfig().setRumAppId("urlconnection-fixture")
                    .setEnableTraceUserResource(true).setEnableHttpURLConnectionResource(true);
            FTSdk.initRUMWithConfig(config);
            FTRUMGlobalManager.get().startView("urlconnection-acceptance");
        });
        drain();
        assertTrue(FTURLConnectionResourceReporter.isAccepting());
    }

    @After public void shutdown() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(FTSdk::shutDown);
    }

    static void drain() throws Exception {
        CountDownLatch latch=new CountDownLatch(1);
        EventConsumerThreadPool.get().execute(latch::countDown);
        assertTrue(latch.await(10,TimeUnit.SECONDS));
    }

    List<String> resources() throws Exception {
        // Storage insertion runs on the event consumer; poll only until expected producer tasks finish.
        drain();
        List<String> result=new ArrayList<>();
        for(SyncData data:FTDataStoreManager.get().queryDataByDataByTypeLimitDesc(0,DataType.RUM_APP)) {
            String value=data.getDataString();
            if(value.startsWith("resource,")&&value.contains("http_url_connection"))result.add(value);
        }
        return result;
    }

    void assertCounts(int expected) throws Exception {
        for(int i=0;i<100&&resources().size()<expected;i++)Thread.sleep(20);
        assertEquals(expected,resources().size());
        assertEquals(0,FTURLConnectionResourceReporter.activeCount());
        drain();
        try(Cursor cursor=target.getContentResolver().query(FTContentProvider.getUriViewData(),null,null,null,null)) {
            assertNotNull(cursor); assertTrue(cursor.moveToFirst());
            int pending=0,count=0;
            do { pending+=cursor.getInt(cursor.getColumnIndexOrThrow(FTSQL.RUM_COLUMN_PENDING_RESOURCE));
                count+=cursor.getInt(cursor.getColumnIndexOrThrow(FTSQL.RUM_COLUMN_RESOURCE_COUNT)); }
            while(cursor.moveToNext());
            assertEquals("pending",0,pending); assertEquals("view resources",expected,count);
        }
    }

    HttpURLConnection connection(LoopbackServer server,String path,boolean wrapped) throws Exception {
        HttpURLConnection result=(HttpURLConnection)(wrapped?Calls.open(server.url(path)):RawCalls.open(server.url(path)));
        server.configure(result); return result;
    }

    static byte[] consume(HttpURLConnection connection) throws Exception {
        int status=connection.getResponseCode();
        InputStream input=status>=400?connection.getErrorStream():connection.getInputStream();
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        if(input!=null)try(InputStream stream=input){byte[] buffer=new byte[4096];for(int n;(n=stream.read(buffer))!=-1;)bytes.write(buffer,0,n);}
        connection.disconnect(); return bytes.toByteArray();
    }

    @Test public void javaKotlinJarAarProxyEntrypointsAndOptionalDependencies() throws Exception {
        for(String unavailable:new String[]{"okhttp3.OkHttpClient","com.ft.sdk.sessionreplay.FTSessionReplay"}) {
            try {Class.forName(unavailable);fail("Unexpected optional dependency: "+unavailable);}catch(ClassNotFoundException expected){}
        }
        URL url=new URL("https://localhost/unused");
        String[] sources={"App Java", "App Java proxy", "App Kotlin", "App Kotlin proxy",
                "JAR", "JAR proxy", "AAR", "AAR proxy"};
        int source=0;
        for(URLConnection result:new URLConnection[]{Calls.open(url),Calls.open(url,Proxy.NO_PROXY),
                KotlinCalls.open(url),KotlinCalls.open(url,Proxy.NO_PROXY),
                com.ft.fixturejar.Calls.open(url),com.ft.fixturejar.Calls.open(url,Proxy.NO_PROXY),
                com.ft.fixtureaar.Calls.open(url),com.ft.fixtureaar.Calls.open(url,Proxy.NO_PROXY)}) {
            assertEquals(sources[source++],com.ft.fixture.BuildConfig.HOOK_ENABLED,result instanceof FTHttpsURLConnection);
            if(com.ft.fixture.BuildConfig.HOOK_ENABLED)assertSame(result,FTURLConnectionInstrumentation.wrap(result));
        }
        assertFalse(ExcludedCalls.open(url) instanceof FTHttpsURLConnection);
        assertFalse(RawCalls.open(url) instanceof FTHttpsURLConnection);
        assertEquals(0,FTURLConnectionResourceReporter.activeCount());
    }

    @Test public void httpAndHttpsRequestResponseParity() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        int count=0;
        for(boolean secure:new boolean[]{false,true})try(LoopbackServer server=new LoopbackServer(tests,secure,false)) {
            for(String method:new String[]{"GET","POST","HEAD"})for(String path:new String[]{"/data","/status/204","/status/304","/status/404","/status/500"}) {
                List<byte[]> responses=new ArrayList<>();
                for(boolean wrapped:new boolean[]{false,true}) {
                    HttpURLConnection c=connection(server,path,wrapped); c.setRequestMethod(method); c.setRequestProperty("X-Test","parity");
                    if(method.equals("POST")) { c.setDoOutput(true); c.setFixedLengthStreamingMode(4);try(OutputStream out=c.getOutputStream()){out.write(new byte[]{1,2,3,4});} }
                    responses.add(consume(c));
                }
                assertArrayEquals(responses.get(0),responses.get(1));
                assertEquals(server.requests.poll(3,TimeUnit.SECONDS),server.requests.poll(3,TimeUnit.SECONDS));
                assertNull("duplicate wire request",server.requests.poll()); count++;
            }
        }
        assertCounts(count);
        for(String record:resources()) {
            assertFalse(record.contains("resource_connection_reuse="));
            assertFalse(record.contains("resource_dns=")); assertFalse(record.contains("resource_tcp="));
            assertFalse(record.contains("resource_ssl=")); assertFalse(record.contains("resource_ttfb="));
        }
    }

    @Test public void errorStreamFirstAndChunkedUpload() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try(LoopbackServer server=new LoopbackServer(tests,true,false)) {
            HttpURLConnection c=connection(server,"/status/500",true);
            try{c.getInputStream();fail();}catch(FileNotFoundException expected){}
            InputStream error=c.getErrorStream(); assertNotNull(error);
            while(error.read()!=-1){} error.close(); c.disconnect();
            HttpURLConnection post=connection(server,"/data",true); post.setDoOutput(true); post.setChunkedStreamingMode(3);
            try(OutputStream output=post.getOutputStream()){output.write(new byte[]{1,2});output.write(3);output.write(new byte[]{0,4,0},1,1);}
            assertEquals(1,FTURLConnectionResourceReporter.activeCount());
            consume(post);
        }
        assertCounts(2);
    }

    @Test public void tlsMutualTlsAndDefaultSecurityArePreserved() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try(LoopbackServer server=new LoopbackServer(tests,true,true)) {
            HttpsURLConnection c=(HttpsURLConnection)connection(server,"/data",true);
            assertSame(server.tls.getSocketFactory().getClass(),c.getSSLSocketFactory().getClass());
            HostnameVerifier verifier=HttpsURLConnection.getDefaultHostnameVerifier();
            c.setHostnameVerifier(verifier); assertSame(verifier,c.getHostnameVerifier());
            assertEquals(200,c.getResponseCode()); assertNotNull(c.getCipherSuite());
            assertNotNull(c.getServerCertificates()); assertNotNull(c.getPeerPrincipal());
            assertNotNull(c.getLocalCertificates()); assertNotNull(c.getLocalPrincipal()); consume(c);
        }
        try(LoopbackServer server=new LoopbackServer(tests,true,false)) {
            for(boolean wrapped:new boolean[]{false,true}) {
                HttpsURLConnection c=(HttpsURLConnection)(wrapped?Calls.open(server.url("/data")):RawCalls.open(server.url("/data")));
                c.setConnectTimeout(2000);c.setReadTimeout(2000);
                try{c.getInputStream();fail("Self-signed identity must be rejected by default");}catch(SSLException expected){}
                c.disconnect();
            }
        }
        try (LoopbackServer server = new LoopbackServer(tests, true, false)) {
            URL wrongName = new URL("https://127.0.0.1:" + server.listener.getLocalPort() + "/data");
            HttpsURLConnection rejected = (HttpsURLConnection) Calls.open(wrongName);
            server.configure(rejected);
            try { rejected.getInputStream(); fail("A trusted certificate for a different hostname must fail"); }
            catch (SSLException expected) { } finally { rejected.disconnect(); }
            java.util.concurrent.atomic.AtomicBoolean checked = new java.util.concurrent.atomic.AtomicBoolean();
            HttpsURLConnection custom = (HttpsURLConnection) Calls.open(wrongName);
            server.configure(custom);
            custom.setHostnameVerifier((hostname, session) -> {
                checked.set(true);
                return hostname.equals("127.0.0.1") && HttpsURLConnection.getDefaultHostnameVerifier().verify("localhost", session);
            });
            consume(custom); assertTrue(checked.get());
        }
        assertCounts(4);
    }

    @Test public void redirectsGzipCookiesAndAuthenticationRemainPlatformOwned() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        CookieHandler previous=CookieHandler.getDefault();
        CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));
        Authenticator.setDefault(new Authenticator(){@Override protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication("fixture","test".toCharArray());}});
        try(LoopbackServer server=new LoopbackServer(tests,true,false)) {
            assertArrayEquals("response-body".getBytes("UTF-8"),consume(connection(server,"/redirect",true)));
            assertArrayEquals("response-body".getBytes("UTF-8"),consume(connection(server,"/gzip",true)));
            consume(connection(server,"/cookie",true));
            assertTrue(new String(consume(connection(server,"/echo-cookie",true)),"UTF-8").contains("fixture=value"));
            consume(connection(server,"/auth",true));
        } finally {CookieHandler.setDefault(previous);Authenticator.setDefault(null);}
        assertCounts(5);
    }

    @Test public void timeoutInterruptedResponseAndConcurrentIdenticalUrls() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try(LoopbackServer server=new LoopbackServer(tests,true,false)) {
            HttpURLConnection timeout=connection(server,"/stall",true);timeout.setReadTimeout(80);
            try{consume(timeout);fail();}catch(SocketTimeoutException expected){}finally{timeout.disconnect();}
            HttpURLConnection broken=connection(server,"/broken",true);
            try{consume(broken);fail();}catch(IOException expected){}finally{broken.disconnect();}
            ExecutorService pool=Executors.newFixedThreadPool(5);
            try {
                List<Future<?>> jobs=new ArrayList<>();
                for(int i=0;i<10;i++)jobs.add(pool.submit(()->{try{consume(connection(server,"/data",true));}catch(Exception e){throw new RuntimeException(e);}}));
                for(Future<?> job:jobs)job.get(10,TimeUnit.SECONDS);
            } finally {pool.shutdownNow();}
        }
        assertCounts(12);
    }

    @Test public void switchesFiltersCapacityAbandonmentAndShutdownIsolateLateStreams() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try(LoopbackServer server=new LoopbackServer(tests,true,false)) {
            config.setEnableHttpURLConnectionResource(false);assertFalse(Calls.open(server.url("/data")) instanceof FTHttpsURLConnection);
            config.setEnableHttpURLConnectionResource(true).setEnableTraceUserResource(false);
            assertFalse(Calls.open(server.url("/data")) instanceof FTHttpsURLConnection);
            config.setEnableTraceUserResource(true).setResourceUrlHandler(url->url.contains("excluded"));
            assertFalse(Calls.open(server.url("/excluded")) instanceof FTHttpsURLConnection);
            FTHttpConfigManager.get().setDatakitUrl(server.url("/").toString());
            assertFalse(Calls.open(server.url("/sdk-upload")) instanceof FTHttpsURLConnection);
            FTHttpConfigManager.get().setDatakitUrl("");
            HttpURLConnection abandoned=connection(server,"/data",true);InputStream late=abandoned.getInputStream();
            assertEquals(1,FTURLConnectionResourceReporter.activeCount());
            FTURLConnectionResourceReporter.cleanup(System.nanoTime()+FTURLConnectionResourceReporter.IDLE_NANOS);
            while(late.read()!=-1){}late.close();abandoned.disconnect();assertCounts(0);
            HttpURLConnection pending=connection(server,"/data",true);InputStream afterShutdown=pending.getInputStream();
            FTURLConnectionResourceReporter.shutdown();
            assertFalse(Calls.open(server.url("/data")) instanceof FTHttpsURLConnection);
            FTURLConnectionResourceReporter.activate();
            while(afterShutdown.read()!=-1){}afterShutdown.close();pending.disconnect();assertCounts(0);
            // Weak registry and active requests are both bounded even for unused retained connections.
            List<URLConnection> retained=new ArrayList<>();
            for(int i=0;i<1100;i++)retained.add(Calls.open(server.url("/unused")));
            int wrapped=0;for(URLConnection c:retained)if(c instanceof FTHttpsURLConnection)wrapped++;
            assertTrue(wrapped<=FTURLConnectionResourceReporter.MAX_REQUESTS);
        }
    }

    @Test public void largeDownloadComparison() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try(LoopbackServer server=new LoopbackServer(tests,true,false)) {
            for(boolean wrapped:new boolean[]{false,true}) {
                long[] elapsed=new long[10];long maxHeap=0;
                String allocatedBefore=android.os.Build.VERSION.SDK_INT>=23?Debug.getRuntimeStat("art.gc.bytes-allocated"):"unavailable";
                for(int i=0;i<elapsed.length;i++) {
                    long start=System.nanoTime();HttpURLConnection c=connection(server,"/large",wrapped);
                    long bytes=0;byte[] buffer=new byte[8192];
                    try(InputStream in=c.getInputStream()){for(int n;(n=in.read(buffer))!=-1;)bytes+=n;}
                    c.disconnect();assertEquals(2*1024*1024,bytes);elapsed[i]=System.nanoTime()-start;
                    maxHeap=Math.max(maxHeap,Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
                }
                Arrays.sort(elapsed);
                Log.i("HUC_BENCH","wrapped="+wrapped+" n=10 bytes=2097152 median_ns="+elapsed[5]+" p95_ns="+elapsed[9]
                        +" peak_sampled_heap="+maxHeap+" allocated_before="+allocatedBefore+" allocated_after="
                        +(android.os.Build.VERSION.SDK_INT>=23?Debug.getRuntimeStat("art.gc.bytes-allocated"):"unavailable"));
            }
        }
        assertCounts(10);
    }
    @Test public void realHttpsProxyPreservesConnectAndRequest() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try (LoopbackServer server = new LoopbackServer(tests, true, false);
             LoopbackProxy proxy = new LoopbackProxy(server.listener.getLocalPort())) {
            for (boolean wrapped : new boolean[]{false, true}) {
                URL url = server.url("/data");
                HttpURLConnection c = (HttpURLConnection) (wrapped ? Calls.open(url, proxy.proxy()) : RawCalls.open(url, proxy.proxy()));
                server.configure(c); c.setRequestProperty("X-Test", "proxy");
                assertArrayEquals("response-body".getBytes("UTF-8"), consume(c));
                assertTrue(c.usingProxy());
            }
            assertEquals(proxy.connects.poll(3, TimeUnit.SECONDS), proxy.connects.poll(3, TimeUnit.SECONDS));
            assertEquals(server.requests.poll(3, TimeUnit.SECONDS), server.requests.poll(3, TimeUnit.SECONDS));
        }
        assertCounts(1);
    }

    @Test public void cachedHttpsResponseStillHasOneResourceWithoutAnotherWireRequest() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        ResponseCache previous = ResponseCache.getDefault();
        android.net.http.HttpResponseCache cache = android.net.http.HttpResponseCache.install(
                new File(target.getCacheDir(), "huc-cache-" + UUID.randomUUID()), 1024 * 1024);
        try (LoopbackServer server = new LoopbackServer(tests, true, false)) {
            byte[] original = consume(connection(server, "/cache", false));
            byte[] wrapped = consume(connection(server, "/cache", true));
            assertArrayEquals(original, wrapped);
            assertNotNull(server.requests.poll(3, TimeUnit.SECONDS));
            assertNull(server.requests.poll());
            assertTrue(cache.getHitCount() > 0);
        } finally { cache.delete(); ResponseCache.setDefault(previous); }
        assertCounts(1);
    }

    @Test public void viewAndActionCreationBeforePendingWorksWithoutQueueDrain() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try (LoopbackServer server = new LoopbackServer(tests, true, false)) {
            // Hold the event consumer so View/Action creation and pending changes must queue in order.
            CountDownLatch entered = new CountDownLatch(1), unblock = new CountDownLatch(1);
            EventConsumerThreadPool.get().execute(() -> {
                entered.countDown();
                try { unblock.await(5, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            try {
                FTRUMGlobalManager.get().startView("immediate-request");
                FTRUMGlobalManager.get().startAction("request", "custom");
                consume(connection(server, "/data", true));
            } finally { unblock.countDown(); }
            assertCounts(1);
            try (Cursor cursor = target.getContentResolver().query(FTContentProvider.getUriActionData(), null, null, null, null)) {
                assertNotNull(cursor); assertTrue(cursor.moveToFirst());
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow(FTSQL.RUM_COLUMN_PENDING_RESOURCE)));
                assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow(FTSQL.RUM_COLUMN_RESOURCE_COUNT)));
            }
            HttpURLConnection old = connection(server, "/data", true);
            InputStream late = old.getInputStream();
            shutdown(); install();
            while (late.read() != -1) { }
            late.close(); old.disconnect(); assertCounts(0);
        }
    }

    @Test public void disabledOrOldPluginStillPerformsRealHttps() throws Exception {
        Assume.assumeFalse(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try (LoopbackServer server = new LoopbackServer(tests, true, false)) {
            assertArrayEquals("response-body".getBytes("UTF-8"), consume(connection(server, "/data", true)));
        }
        assertCounts(0);
    }

    @Test public void activeCapacityAndIdleCleanupBalanceActualPendingCounters() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        List<FTURLConnectionRequestState> states = new ArrayList<>();
        for (int i = 0; i < FTURLConnectionResourceReporter.MAX_REQUESTS + 1; i++) {
            FTURLConnectionRequestState state = new FTURLConnectionRequestState("https://localhost/unused",
                    FTURLConnectionResourceReporter.generation());
            state.begin("GET", null); states.add(state);
        }
        assertEquals(FTURLConnectionResourceReporter.MAX_REQUESTS, FTURLConnectionResourceReporter.activeCount());
        assertTrue(states.get(states.size() - 1).terminal);
        FTURLConnectionResourceReporter.cleanup(System.nanoTime() + FTURLConnectionResourceReporter.IDLE_NANOS);
        for (FTURLConnectionRequestState state : states) state.finish("eof", true, null);
        assertCounts(0);
        Field resources = FTRUMInnerManager.class.getDeclaredField("resourceBeanMap");
        resources.setAccessible(true);
        assertTrue(((Map<?, ?>) resources.get(FTRUMInnerManager.get())).isEmpty());
    }

    @Test public void firstErrorStreamKeepsOriginalViewWhileNetworkCallIsInFlight() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try (LoopbackServer server = new LoopbackServer(tests, true, false)) {
            HttpURLConnection connection = connection(server, "/slow-error", true);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<InputStream> response = executor.submit(connection::getErrorStream);
                assertNotNull(server.requests.poll(3, TimeUnit.SECONDS));
                FTRUMGlobalManager.get().startView("after-error-request-start");
                InputStream body = response.get(3, TimeUnit.SECONDS);
                assertNotNull(body); while (body.read() != -1) { } body.close(); connection.disconnect();
            } finally { executor.shutdownNow(); }
            for (int attempt = 0; attempt < 100 && resources().isEmpty(); attempt++) Thread.sleep(20);
            assertEquals(1, resources().size());
            assertTrue(resources().get(0).contains("view_name=urlconnection-acceptance"));
            assertEquals(0, FTURLConnectionResourceReporter.activeCount());
            // The closed original View may already have been serialized and removed from aggregates.
            boolean completedView = false;
            for (int attempt = 0; attempt < 50 && !completedView; attempt++) {
                for (SyncData data : FTDataStoreManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP)) {
                    String line = data.getDataString();
                    completedView |= line.startsWith("view,") && line.contains("view_name=urlconnection-acceptance")
                            && java.util.regex.Pattern.compile("(^|[ ,])view_resource_count=1(?:\\.0+)?i?(?=[ ,])").matcher(line).find();
                }
                if (!completedView) Thread.sleep(20);
            }
            assertTrue("Original View must receive the completed Resource count", completedView);
            try (Cursor cursor = target.getContentResolver().query(FTContentProvider.getUriViewData(), null, null, null, null)) {
                assertNotNull(cursor);
                while (cursor.moveToNext()) assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow(FTSQL.RUM_COLUMN_PENDING_RESOURCE)));
            }
        }
    }

    @Test public void cancelledRequestsCannotGrowThePendingDispatchQueueWithoutBound() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        CountDownLatch entered = new CountDownLatch(1), unblock = new CountDownLatch(1);
        EventConsumerThreadPool.get().execute(() -> {
            entered.countDown();
            try { unblock.await(10, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        assertTrue(entered.await(3, TimeUnit.SECONDS));
        try {
            for (int i = 0; i <= FTURLConnectionResourceReporter.MAX_REQUESTS; i++) {
                FTURLConnectionRequestState state = new FTURLConnectionRequestState("https://localhost/unused",
                        FTURLConnectionResourceReporter.generation());
                state.begin("GET", null);
                if (i == FTURLConnectionResourceReporter.MAX_REQUESTS) assertFalse(state.started);
                state.discard();
            }
            assertEquals(FTURLConnectionResourceReporter.MAX_REQUESTS, FTURLConnectionResourceReporter.pendingStartCount());
        } finally { unblock.countDown(); }
        assertCounts(0);
        assertEquals(0, FTURLConnectionResourceReporter.pendingStartCount());
    }

    @Test public void identityRegistryDoesNotKeepOriginalOrWrapperAlive() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        List<java.lang.ref.WeakReference<URLConnection>> references = weakConnectionPair();
        for (int attempt = 0; attempt < 50; attempt++) {
            System.gc(); System.runFinalization();
            if (references.get(0).get() == null && references.get(1).get() == null) break;
            Thread.sleep(20);
        }
        assertNull(references.get(0).get()); assertNull(references.get(1).get());
        assertCounts(0);
    }

    private static List<java.lang.ref.WeakReference<URLConnection>> weakConnectionPair() throws Exception {
        URLConnection original = RawCalls.open(new URL("https://localhost/unused"));
        URLConnection wrapped = FTURLConnectionInstrumentation.wrap(original);
        assertTrue(wrapped instanceof FTHttpsURLConnection);
        assertSame(wrapped, FTURLConnectionInstrumentation.wrap(original));
        assertSame(wrapped, FTURLConnectionInstrumentation.wrap(wrapped));
        return Arrays.asList(new java.lang.ref.WeakReference<>(original), new java.lang.ref.WeakReference<>(wrapped));
    }

    private static FTURLConnectionRequestState requestState(HttpURLConnection connection) throws Exception {
        Field field = connection.getClass().getDeclaredField("delegate");
        field.setAccessible(true);
        return ((FTURLConnectionDelegate) field.get(connection)).state;
    }

    private static String[] wireTraceIds(TraceType type, Map<String, String> headers) throws Exception {
        switch (type) {
            case DDTRACE:
                return new String[]{headers.get("x-datadog-trace-id"), headers.get("x-datadog-parent-id")};
            case ZIPKIN_MULTI_HEADER:
                return new String[]{headers.get("X-B3-TraceId"), headers.get("X-B3-SpanId")};
            case ZIPKIN_SINGLE_HEADER:
                return Arrays.copyOf(headers.get("b3").split("-"), 2);
            case TRACEPARENT:
                String[] parent = headers.get("traceparent").split("-");
                return new String[]{parent[1], parent[2]};
            case JAEGER:
                return Arrays.copyOf(headers.get("uber-trace-id").split(":"), 2);
            case SKYWALKING:
                String[] sw = headers.get("sw8").split("-");
                return new String[]{new String(android.util.Base64.decode(sw[1], 0), "UTF-8"),
                        new String(android.util.Base64.decode(sw[2], 0), "UTF-8") + sw[3]};
            default: throw new AssertionError(type);
        }
    }

    private static boolean hasTag(String record, String key, String value) {
        return java.util.regex.Pattern.compile("(^|,)" + key + "="
                + java.util.regex.Pattern.quote(value) + "(?=[ ,])").matcher(record).find();
    }

    @Test public void traceHeadersMatchStoredResourceIdsForEveryFormatWithoutOkHttp() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try { Class.forName("okhttp3.OkHttpClient"); fail("Trace must not require OkHttp"); }
        catch (ClassNotFoundException expected) { }
        int count = 0;
        Set<String> traceIds = new HashSet<>();
        for (boolean secure : new boolean[]{false, true}) {
            try (LoopbackServer server = new LoopbackServer(tests, secure, false)) {
                for (TraceType type : TraceType.values()) {
                    FTSdk.initTraceWithConfig(new FTTraceConfig().setTraceType(type)
                            .setSamplingRate(1f).setEnableAutoTrace(true).setEnableLinkRUMData(true));
                    for (String scenario : new String[]{"GET", "POST", "ERROR"}) {
                        HttpURLConnection c = connection(server, scenario.equals("ERROR") ? "/status/404" : "/data", true);
                        FTURLConnectionRequestState state = requestState(c);
                        if (scenario.equals("POST")) {
                            c.setDoOutput(true); c.setRequestMethod("POST");
                            try (OutputStream output = c.getOutputStream()) { output.write(new byte[]{1, 2, 3}); }
                        }
                        consume(c);
                        Map<String, String> headers = server.requestHeaders.poll(3, TimeUnit.SECONDS);
                        assertNotNull(headers);
                        String[] ids = wireTraceIds(type, headers);
                        assertNotNull(ids[0]); assertFalse(ids[0].isEmpty());
                        assertNotNull(ids[1]); assertFalse(ids[1].isEmpty());
                        assertTrue("Every request must have its own context", traceIds.add(ids[0]));
                        assertCounts(++count);
                        String record = null;
                        for (String line : resources()) if (hasTag(line, "resource_id", state.id)) record = line;
                        assertNotNull(record);
                        assertTrue(type + " trace_id", hasTag(record, "trace_id", ids[0]));
                        assertTrue(type + " span_id", hasTag(record, "span_id", ids[1]));
                        assertNull("Completed context must be released", FTTraceManager.get().getHandler(state.id));
                    }
                }
            }
        }
    }

    @Test public void traceSwitchesSamplingAndFiltersControlPropagationAndRumLink() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try (LoopbackServer server = new LoopbackServer(tests, true, false)) {
            int count = 0;
            for (boolean automatic : new boolean[]{false, true}) for (boolean linked : new boolean[]{false, true}) {
                FTSdk.initTraceWithConfig(new FTTraceConfig().setTraceType(TraceType.TRACEPARENT)
                        .setSamplingRate(0f).setEnableAutoTrace(automatic).setEnableLinkRUMData(linked));
                HttpURLConnection c = connection(server, "/data", true);
                FTURLConnectionRequestState state = requestState(c);
                consume(c);
                Map<String, String> headers = server.requestHeaders.poll(3, TimeUnit.SECONDS);
                assertNotNull(headers);
                assertEquals(automatic, headers.containsKey("traceparent"));
                if (automatic) assertTrue(headers.get("traceparent").endsWith("-00"));
                assertCounts(++count);
                String record = null;
                for (String line : resources()) if (hasTag(line, "resource_id", state.id)) record = line;
                assertNotNull(record);
                assertEquals(automatic && linked, record.contains(",trace_id="));
                assertEquals(automatic && linked, record.contains(",span_id="));
            }
            for (boolean intake : new boolean[]{false, true}) {
                config.setResourceUrlHandler(url -> !intake);
                if (intake) FTHttpConfigManager.get().setDatakitUrl(server.url("/").toString());
                HttpURLConnection excluded = connection(server, "/data", true);
                assertFalse(excluded instanceof FTHttpsURLConnection);
                consume(excluded);
                assertFalse(server.requestHeaders.poll(3, TimeUnit.SECONDS).containsKey("traceparent"));
            }
            assertCounts(count);
        }
    }

    @Test public void traceContextSurvivesRedirectAndCannotLeakAcrossShutdown() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        FTSdk.initTraceWithConfig(new FTTraceConfig().setTraceType(TraceType.TRACEPARENT)
                .setEnableAutoTrace(true).setEnableLinkRUMData(true));
        try (LoopbackServer server = new LoopbackServer(tests, true, false)) {
            HttpURLConnection c = connection(server, "/redirect", true);
            FTURLConnectionRequestState state = requestState(c);
            consume(c);
            String first = server.requestHeaders.poll(3, TimeUnit.SECONDS).get("traceparent");
            assertEquals(first, server.requestHeaders.poll(3, TimeUnit.SECONDS).get("traceparent"));
            assertCounts(1);
            assertTrue(hasTag(resources().get(0), "trace_id", state.traceId));
            HttpURLConnection pending = connection(server, "/data", true);
            FTURLConnectionRequestState pendingState = requestState(pending);
            pending.getResponseCode();
            assertNotNull(pendingState.traceId);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(FTSdk::shutDown);
            assertNull(pendingState.traceId); assertNull(pendingState.spanId);
            assertNull(FTTraceManager.get().getHandler(pendingState.id));
            consume(pending);
            assertEquals(0, FTURLConnectionResourceReporter.activeCount());
        }
    }

    @Test public void concurrentAndFailingRequestsKeepTheirOwnTraceContext() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        FTSdk.initTraceWithConfig(new FTTraceConfig().setTraceType(TraceType.TRACEPARENT)
                .setEnableAutoTrace(true).setEnableLinkRUMData(true));
        Map<String, String[]> expected = new ConcurrentHashMap<>();
        try (LoopbackServer server = new LoopbackServer(tests, true, false)) {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            try {
                List<Future<?>> tasks = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    final int scenario = i;
                    tasks.add(executor.submit(() -> {
                        try {
                            HttpURLConnection c = connection(server, scenario == 0 ? "/broken"
                                    : scenario == 1 ? "/status/204" : scenario == 2 ? "/slow-error" : "/data", true);
                            FTURLConnectionRequestState state = requestState(c);
                            c.setRequestProperty("X-Resource-Id", state.id);
                            if (scenario == 3) c.setRequestMethod("HEAD");
                            try {
                                if (scenario == 2) {
                                    try (InputStream error = c.getErrorStream()) {
                                        assertNotNull(error); while (error.read() != -1) { }
                                    }
                                } else consume(c);
                                if (scenario == 0) fail("Broken response must fail");
                            } catch (IOException error) {
                                if (scenario != 0) throw error;
                            } finally { c.disconnect(); }
                            expected.put(state.id, new String[]{state.traceId, state.spanId});
                        } catch (Exception e) { throw new RuntimeException(e); }
                    }));
                }
                for (Future<?> task : tasks) task.get(10, TimeUnit.SECONDS);
            } finally { executor.shutdownNow(); }
            assertCounts(8);
            Set<String> traceIds = new HashSet<>();
            for (int i = 0; i < 8; i++) {
                Map<String, String> headers = server.requestHeaders.poll(3, TimeUnit.SECONDS);
                assertNotNull(headers);
                String id = headers.get("X-Resource-Id");
                String[] ids = wireTraceIds(TraceType.TRACEPARENT, headers);
                assertArrayEquals(expected.get(id), ids);
                assertTrue(traceIds.add(ids[0]));
                String record = null;
                for (String line : resources()) if (hasTag(line, "resource_id", id)) record = line;
                assertNotNull(record);
                assertTrue(hasTag(record, "trace_id", ids[0]));
                assertTrue(hasTag(record, "span_id", ids[1]));
                assertNull(FTTraceManager.get().getHandler(id));
            }
        }
    }

    private void enableVisitor(String prefix) throws Exception {
        FTVisitorIdConfig visitor = new FTVisitorIdConfig().setHeader("x-visitor-id");
        if (prefix == null) visitor.addMatch(java.util.regex.Pattern.compile("^https?://localhost:[0-9]+/api/"));
        else visitor.addMatch(prefix);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FTSdk.initRUMWithConfig(config.setVisitorIdConfig(visitor));
            FTRUMGlobalManager.get().startView("visitor-urlconnection");
        });
        drain();
    }

    @Test public void visitorConfigLeavesNativeRedirectsAndResourceCollectionIntact() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        int count = 0;
        enableVisitor(null);
        for (boolean secure : new boolean[]{false, true}) {
            try (LoopbackServer server = new LoopbackServer(tests, secure, false)) {
                server.redirects.put("/api/start", "/outside");
                server.redirects.put("/outside", "/api/final");
                HttpURLConnection c = connection(server, "/api/start", true);
                String id = FTVisitorIdManager.get().getId();
                FTURLConnectionRequestState state = requestState(c);
                c.connect();
                FTSdk.resetVisitorId();
                assertNotEquals(id, FTVisitorIdManager.get().getId());
                assertEquals(200, c.getResponseCode());
                if (secure) assertNotNull(((HttpsURLConnection)c).getServerCertificates());
                consume(c);
                assertNull(server.requestHeaders.poll(3, TimeUnit.SECONDS).get("x-visitor-id"));
                assertNull(server.requestHeaders.poll(3, TimeUnit.SECONDS).get("x-visitor-id"));
                assertNull(server.requestHeaders.poll(3, TimeUnit.SECONDS).get("x-visitor-id"));
                assertTrue(c.getInstanceFollowRedirects());
                assertEquals(server.url("/api/final"), c.getURL());
                assertCounts(++count);
                boolean stored = false;
                for (String record : resources()) if (hasTag(record, "resource_id", state.id)) {
                    // Preserve the pre-existing native RUM event-time ID, without claiming
                    // that an ID was injected into or frozen for this network request.
                    assertFalse(hasTag(record, "visitor_id", id));
                    assertTrue(hasTag(record, "visitor_id", FTVisitorIdManager.get().getId()));
                    stored = true;
                }
                assertTrue(stored);
            }
        }
    }

    @Test public void visitorRedirectsRetainHttpsProxyTlsCookiesAndAuthentication() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        CookieHandler previous = CookieHandler.getDefault();
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        Authenticator.setDefault(new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("fixture", "test".toCharArray());
            }
        });
        try (LoopbackServer server = new LoopbackServer(tests, true, true);
             LoopbackProxy proxy = new LoopbackProxy(server.listener.getLocalPort(), true)) {
            enableVisitor(server.url("/").toString());
            server.redirects.put("/api/start", "/auth");
            server.extraHeaders.put("/api/start", "Set-Cookie: visitor-fixture=kept; Path=/\r\n");
            HttpURLConnection c = (HttpURLConnection) Calls.open(server.url("/api/start"), proxy.proxy());
            server.configure(c);
            assertEquals("Redirect and server authentication must complete", 200, c.getResponseCode());
            consume(c);
            Map<String, String> first = server.requestHeaders.poll(3, TimeUnit.SECONDS);
            Map<String, String> challenge = server.requestHeaders.poll(3, TimeUnit.SECONDS);
            // Platform authentication caches can answer preemptively, without another 401.
            Map<String, String> authenticated = challenge;
            if (authenticated == null || !authenticated.containsKey("Authorization")) {
                authenticated = server.requestHeaders.poll(3, TimeUnit.SECONDS);
            }
            assertNotNull(authenticated);
            assertNull(first.get("x-visitor-id"));
            assertNull(challenge.get("x-visitor-id"));
            assertNull(authenticated.get("x-visitor-id"));
            assertTrue(authenticated.get("Authorization").startsWith("Basic "));
            assertTrue(authenticated.get("Cookie").contains("visitor-fixture=kept"));
            assertNotNull(proxy.connects.poll(3, TimeUnit.SECONDS));
            assertNotNull(proxy.connects.poll(3, TimeUnit.SECONDS));
            int authenticatedTunnels = 0;
            for (Map<String, String> headers : proxy.requestHeaders) {
                if (headers.containsKey("Proxy-Authorization")) {
                    assertTrue(headers.get("Proxy-Authorization").startsWith("Basic "));
                    authenticatedTunnels++;
                }
            }
            assertTrue("Every redirected connection must retain proxy authentication", authenticatedTunnels >= 2);
            assertCounts(1);
        } finally { CookieHandler.setDefault(previous); Authenticator.setDefault(null); }
    }

    @Test public void visitorUploadRedirectsMatchPlatformForBufferedFixedAndChunkedBodies() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        int count = 0;
        try (LoopbackServer server = new LoopbackServer(tests, false, false)) {
            enableVisitor(server.url("/api/").toString());
            for (int status : new int[]{301,302,303,307,308}) for (int mode=0;mode<3;mode++) {
                server.redirects.put("/api/upload", "/api/final");
                server.redirectCodes.put("/api/upload", status);
                for (boolean wrapped : new boolean[]{false,true}) {
                    HttpURLConnection c=connection(server,"/api/upload",wrapped);
                    c.setDoOutput(true);
                    if(mode==1)c.setFixedLengthStreamingMode(4);
                    if(mode==2)c.setChunkedStreamingMode(2);
                    try(OutputStream output=c.getOutputStream()){output.write(new byte[]{1,2,3,4});}
                    assertEquals(status<307?200:status,c.getResponseCode());
                    consume(c);
                    assertTrue(server.requests.poll(3,TimeUnit.SECONDS).startsWith("POST /api/upload"));
                    Map<String,String> first=server.requestHeaders.poll(3,TimeUnit.SECONDS);
                    assertFalse(first.containsKey("x-visitor-id"));
                    if(status<307) {
                        assertTrue(server.requests.poll(3,TimeUnit.SECONDS).startsWith("GET /api/final"));
                        assertFalse(server.requestHeaders.poll(3,TimeUnit.SECONDS).containsKey("x-visitor-id"));
                    }
                }
                count++;
            }
        }
        assertCounts(count);
    }

    @Test public void visitorOnlyDoesNotWrapConnectionsOrInjectHeaders() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try (LoopbackServer server=new LoopbackServer(tests,false,false)) {
            config.setEnableTraceUserResource(false).setEnableHttpURLConnectionResource(false).setSamplingRate(0);
            enableVisitor(server.url("/api/").toString());
            server.redirects.put("/outside","/api/final");
            HttpURLConnection c=connection(server,"/outside",true);
            assertFalse(c instanceof FTHttpURLConnection);
            consume(c);
            assertNull(server.requestHeaders.poll(3,TimeUnit.SECONDS).get("x-visitor-id"));
            assertNull(server.requestHeaders.poll(3,TimeUnit.SECONDS).get("x-visitor-id"));
            assertEquals(0,resources().size());
            assertEquals(0,FTURLConnectionResourceReporter.activeCount());
            // Both old and new hooks leave Visitor-only connections untouched.
            HttpURLConnection legacy=(HttpURLConnection)FTURLConnectionInstrumentation.wrap(RawCalls.open(server.url("/api/legacy")));
            consume(legacy);
            assertNull(server.requestHeaders.poll(3,TimeUnit.SECONDS).get("x-visitor-id"));
        }
    }

    @Test public void visitorCrossOriginAndTlsRejectionRemainEnforced() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        try(LoopbackServer first=new LoopbackServer(tests,true,false);
            LoopbackServer other=new LoopbackServer(tests,true,false)) {
            enableVisitor(first.url("/api/").toString());
            first.redirects.put("/api/start",other.url("/api/final").toString());
            HttpURLConnection c=connection(first,"/api/start",true);
            c.setRequestProperty("Authorization","Bearer fixture-only");
            consume(c);
            assertNull(first.requestHeaders.poll(3,TimeUnit.SECONDS).get("x-visitor-id"));
            Map<String,String> last=other.requestHeaders.poll(3,TimeUnit.SECONDS);
            assertNull(last.get("x-visitor-id")); assertNull(last.get("Authorization"));
            first.redirects.put("/api/start","https://127.0.0.1:"+other.listener.getLocalPort()+"/api/final");
            c=connection(first,"/api/start",true);
            try {consume(c);fail("Redirected TLS hostname must still be verified");}
            catch(SSLException expected){}finally{c.disconnect();}
            assertNull(other.requestHeaders.poll());
            assertCounts(2);
        }
    }

    @Test public void visitorConfigPreservesKeepAliveAcrossNativeRedirects() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        enableVisitor(null);
        for (boolean secure : new boolean[]{false, true}) {
            for (boolean wrapped : new boolean[]{false, true}) {
                try (LoopbackServer server = new LoopbackServer(tests, secure, false)) {
                    server.keepAlive = true;
                    server.redirects.put("/api/start", "/outside");
                    server.redirects.put("/outside", "/api/final");
                    HttpURLConnection c = connection(server, "/api/start", wrapped);
                    assertArrayEquals("response-body".getBytes("UTF-8"), consume(c));
                    Integer first = server.requestConnectionIds.poll(3, TimeUnit.SECONDS);
                    assertNotNull(first);
                    assertEquals("First redirect must reuse the socket", first,
                            server.requestConnectionIds.poll(3, TimeUnit.SECONDS));
                    assertEquals("Second redirect must reuse the socket", first,
                            server.requestConnectionIds.poll(3, TimeUnit.SECONDS));
                    assertEquals("No additional TCP/TLS connection", 1, server.acceptedConnections.get());
                    for (int hop = 0; hop < 3; hop++) {
                        assertNull(server.requestHeaders.poll(3, TimeUnit.SECONDS).get("x-visitor-id"));
                    }
                }
            }
        }
        assertCounts(2);
    }

    @Test public void visitorConfigPreservesCancellationDuringRedirectedResponse() throws Exception {
        Assume.assumeTrue(com.ft.fixture.BuildConfig.HOOK_ENABLED);
        enableVisitor(null);
        for (boolean secure : new boolean[]{false, true}) {
            for (boolean wrapped : new boolean[]{false, true}) {
                try (LoopbackServer server = new LoopbackServer(tests, secure, false)) {
                    server.keepAlive = true;
                    server.redirects.put("/api/start", "/api/blocked");
                    server.blockedPath = "/api/blocked";
                    HttpURLConnection c = connection(server, "/api/start", wrapped);
                    c.setReadTimeout(15000);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    try {
                        Future<IOException> response = executor.submit(() -> {
                            try { consume(c); return null; }
                            catch (IOException error) { return error; }
                        });
                        assertTrue("Second hop must be in flight",
                                server.blockedRequest.await(3, TimeUnit.SECONDS));
                        c.disconnect();
                        IOException error = response.get(3, TimeUnit.SECONDS);
                        assertNotNull("Cancellation must interrupt the active response", error);
                        assertFalse("Must cancel before the read timeout", error instanceof SocketTimeoutException);
                        assertEquals(1, server.acceptedConnections.get());
                    } finally {
                        c.disconnect();
                        server.releaseBlockedResponse.countDown();
                        executor.shutdownNow();
                        assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));
                    }
                }
            }
        }
        assertCounts(2);
    }

}

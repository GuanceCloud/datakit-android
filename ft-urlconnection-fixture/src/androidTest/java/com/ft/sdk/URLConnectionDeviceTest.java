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
        for(URLConnection result:new URLConnection[]{Calls.open(url),Calls.open(url,Proxy.NO_PROXY),
                KotlinCalls.open(url),KotlinCalls.open(url,Proxy.NO_PROXY),
                com.ft.fixturejar.Calls.open(url),com.ft.fixtureaar.Calls.open(url)}) {
            assertEquals(com.ft.fixture.BuildConfig.HOOK_ENABLED,result instanceof FTHttpsURLConnection);
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

}

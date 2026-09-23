package com.ft.sdk.diagnostics;
import com.ft.sdk.diagnostics.internal.DiagnosticsBridge;
import org.json.*;
import org.junit.Test;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import static org.junit.Assert.*;

public class FTDiagnosticsTest {
    private File root() throws IOException { return Files.createTempDirectory("ft-diagnostics-test").toFile(); }
    @Test public void lifecycleAndSessionIsolation() throws Exception {
        try (FTDiagnostics d=new FTDiagnostics(root(),new FTDiagnosticsConfig())) {
            String first=d.start();
            DiagnosticsBridge.record("storage_write","log",3,60,"sqlite_insert");
            DiagnosticsBridge.Attempt old=DiagnosticsBridge.begin("log",30);
            JSONObject ended=d.stop();
            assertEquals(1,ended.getLong("in_flight_uploads"));assertTrue(ended.getBoolean("incomplete"));
            assertEquals(3,ended.getJSONObject("counters").getDouble("log.storage_write.count"),0);
            String second=d.start();old.finish(200,30,0,"test");
            DiagnosticsBridge.Attempt current=DiagnosticsBridge.begin("rum",42);
            current.finish(200,20,5,"test");current.finish(200,20,5,"test");
            JSONObject summary=d.stop();assertEquals(0,summary.getLong("in_flight_uploads"));
            assertFalse(summary.getJSONObject("counters").has("log.upload_end.observations"));
            assertEquals(1,summary.getJSONObject("counters").getDouble("rum.upload_end.success"),0);
            assertEquals(1,d.records(first,0,100).length());
            assertEquals(1,d.records(second,1,1).length());
            File export=new File(root(),"export");d.exportSession(second,export);
            assertTrue(new File(export,"summary.json").isFile());
            d.deleteSession(first);assertEquals(1,d.sessions().length());
        }
        assertFalse(DiagnosticsBridge.enabled());
    }
    @Test public void capsKeepAggregatesAndDoNotEvictActiveSession() throws Exception {
        File root=root();
        try(FTDiagnostics d=new FTDiagnostics(root,new FTDiagnosticsConfig(128*1024,128*1024,86400000))) {
            String first=d.start();
            for(int i=0;i<8000;i++)DiagnosticsBridge.record("retry","replay",1,-1,"retry_scheduled");
            JSONObject summary=d.stop();
            assertEquals(8000,summary.getJSONObject("counters").getDouble("replay.retry.count"),0);
            assertTrue(summary.getLong("omitted_records")>0);
            assertTrue(new File(root,first+"/events.jsonl").length()<=64*1024);
            String second=d.start();assertFalse(new File(root,first).exists());
            try { d.deleteSession(second); fail(); } catch(IllegalStateException expected) { }
            d.stop();
        }
    }
    @Test public void interruptionRecoveryAndDirectoryOwnership() throws Exception {
        File root=root();String id;
        try(FTDiagnostics d=new FTDiagnostics(root,new FTDiagnosticsConfig())) {
            id=d.start();
            try { new FTDiagnostics(root,new FTDiagnosticsConfig()); fail(); }catch(IOException expected) { }
            d.stop();
        }
        File summaryFile=new File(root,id+"/summary.json");
        JSONObject saved=new JSONObject(new String(Files.readAllBytes(summaryFile.toPath()),"UTF-8"));saved.put("state","active");
        Files.write(summaryFile.toPath(),saved.toString().getBytes("UTF-8"));
        try(FTDiagnostics d=new FTDiagnostics(root,new FTDiagnosticsConfig())) {
            assertEquals("interrupted",d.summary(id).getString("state"));
            try {d.summary("../../other");fail();}catch(IllegalArgumentException expected) { }
        }
    }
    @Test public void diagnosticWriteFailureDoesNotThrowIntoSDK() throws Exception {
        File root=root();try(FTDiagnostics d=new FTDiagnostics(root,new FTDiagnosticsConfig())) {
            String id=d.start();File events=new File(root,id+"/events.jsonl");assertTrue(events.delete());assertTrue(events.mkdir());
            DiagnosticsBridge.record("retry","log",1,-1,"retry_scheduled");
            JSONObject summary=d.stop();assertEquals("local_write_failed",summary.getString("last_error"));assertTrue(summary.getBoolean("incomplete"));
        }
    }
    @Test public void disabledHooksAndStreamCounters() throws Exception {
        DiagnosticsBridge.uninstall(null);assertFalse(DiagnosticsBridge.enabled());
        assertNull(DiagnosticsBridge.begin("log",0));
        ByteArrayOutputStream raw=new ByteArrayOutputStream();assertSame(raw,DiagnosticsBridge.output(raw));
        DiagnosticsBridge.Transfer t=new DiagnosticsBridge.Transfer();DiagnosticsBridge.transfer(t);
        try {
            OutputStream out=DiagnosticsBridge.output(raw);out.write(new byte[]{1,2,3});out.write(4);assertEquals(4,t.tx);
            InputStream in=DiagnosticsBridge.input(new ByteArrayInputStream(raw.toByteArray()));byte[] bytes=new byte[8];assertEquals(4,in.read(bytes));assertEquals(-1,in.read());assertEquals(4,t.rx);
        } finally {DiagnosticsBridge.transfer(null);}
    }
    @Test public void concurrentCompletionsCannotCrossStopBoundary() throws Exception {
        try(FTDiagnostics d=new FTDiagnostics(root(),new FTDiagnosticsConfig())) {
            d.start();
            List<DiagnosticsBridge.Attempt> attempts=new ArrayList<>();
            for(int i=0;i<100;i++)attempts.add(DiagnosticsBridge.begin("log",1));
            java.util.concurrent.CountDownLatch go=new java.util.concurrent.CountDownLatch(1);
            Thread worker=new Thread(() -> {
                try {go.await();}catch(InterruptedException e){throw new AssertionError(e);}
                for(DiagnosticsBridge.Attempt attempt:attempts)attempt.finish(200,1,0,"test");
            });
            worker.start();go.countDown();JSONObject first=d.stop();
            d.start();worker.join();JSONObject second=d.stop();
            long completed=first.getJSONObject("counters").optLong("log.upload_end.observations",0);
            assertEquals(100,completed+first.getLong("in_flight_uploads"));
            assertEquals(0,second.getJSONObject("counters").length());
        }
    }
    @Test public void unreadableCheckpointRecoversAndRetentionUsesCheckpointTime() throws Exception {
        File root=root();String id;
        try(FTDiagnostics d=new FTDiagnostics(root,new FTDiagnosticsConfig())){id=d.start();d.stop();}
        File checkpoint=new File(root,id+"/summary.json");
        Files.write(checkpoint.toPath(),"broken".getBytes("UTF-8"));
        try(FTDiagnostics d=new FTDiagnostics(root,new FTDiagnosticsConfig())) {
            JSONObject recovered=d.summary(id);
            assertEquals("interrupted",recovered.getString("state"));assertTrue(recovered.getBoolean("incomplete"));
            recovered.put("updated_ms",System.currentTimeMillis()-8L*24*3600*1000);
            Files.write(checkpoint.toPath(),recovered.toString().getBytes("UTF-8"));
            d.cleanExpired();assertEquals(0,d.sessions().length());
        }
    }

}

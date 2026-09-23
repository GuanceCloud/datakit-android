package com.ft.sdk.diagnostics;

import com.ft.sdk.diagnostics.internal.DiagnosticsBridge;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/** Separate, local-only SDK diagnostics. Management methods perform IO: call off the UI thread.
 * Use a private, dedicated directory for each process; never pass the SDK business storage path. */
public final class FTDiagnostics implements Closeable {
    private static final long RESERVE = 64*1024;
    private final File root;
    private final FTDiagnosticsConfig config;
    private final RandomAccessFile lockFile;
    private final FileLock fileLock;
    private Session active;
    private boolean closed;
    public FTDiagnostics(File processDirectory, FTDiagnosticsConfig config) throws IOException {
        this.root = processDirectory.getCanonicalFile(); this.config = config;
        if (!root.isDirectory() && !root.mkdirs()) throw new IOException("Cannot create diagnostics directory");
        lockFile = new RandomAccessFile(new File(root, ".lock"), "rw");
        FileLock acquired;
        try { acquired = lockFile.getChannel().tryLock(); }
        catch (RuntimeException e) { lockFile.close(); throw new IOException("Diagnostics directory in use", e); }
        if (acquired == null) { lockFile.close(); throw new IOException("Diagnostics directory in use"); }
        fileLock = acquired;
        try { recover(); prune(0); } catch (IOException e) { fileLock.release(); lockFile.close(); throw e; }
    }
    public synchronized String start() throws IOException {
        checkOpen();
        if (active != null) throw new IllegalStateException("A session is already active");
        prune(config.sessionBytes);
        String id = UUID.randomUUID().toString();
        Session s = new Session(new File(root, id), id);
        if (!DiagnosticsBridge.install(s)) { deleteTree(s.dir); throw new IllegalStateException("Another collector is active"); }
        active = s; s.thread.start(); return id;
    }
    public synchronized JSONObject stop() throws IOException {
        checkOpen();
        if (active == null) throw new IllegalStateException("No active session");
        Session s = active;
        DiagnosticsBridge.uninstall(s);
        synchronized (s) { s.accepting = false; s.state = "stopped"; }
        try { s.thread.join(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException("Stop interrupted", e); }
        active = null;
        return s.snapshot();
    }
    public synchronized JSONObject status() {
        return active == null ? json("state", closed ? "closed" : "idle") : active.snapshot();
    }
    public synchronized JSONArray sessions() throws IOException {
        checkOpen(); JSONArray result = new JSONArray();
        for (File dir : directories()) result.put(summary(dir.getName()));
        return result;
    }
    public synchronized JSONObject summary(String id) throws IOException {
        checkOpen();
        if (active != null && active.id.equals(id)) return active.snapshot();
        return readJSON(new File(sessionDirectory(id), "summary.json"));
    }
    /** Offset is a JSONL record index; page size is bounded to 1000. */
    public synchronized JSONArray records(String id, long offset, int limit) throws IOException {
        checkOpen();
        if (offset < 0 || limit < 1 || limit > 1000) throw new IllegalArgumentException("Invalid page");
        JSONArray result = new JSONArray(); File file = new File(sessionDirectory(id), "events.jsonl");
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line; long n = 0;
            while ((line = r.readLine()) != null && result.length() < limit) {
                if (n++ >= offset) try { result.put(new JSONObject(line)); }
                catch (org.json.JSONException e) { break; } // Interrupted final line.
            }
        }
        return result;
    }
    /** Exports a stopped session as JSON + JSONL into a new caller-owned directory. */
    public synchronized void exportSession(String id, File destination) throws IOException {
        checkOpen(); requireInactive(id);
        File source = sessionDirectory(id), dest = destination.getCanonicalFile();
        if (dest.equals(root) || dest.getPath().startsWith(root.getPath()+File.separator) || root.getPath().startsWith(dest.getPath()+File.separator))
            throw new IOException("Export must be outside diagnostic storage");
        if (dest.exists() || !dest.mkdirs()) throw new IOException("Export destination must be new");
        for (String name : Arrays.asList("summary.json", "events.jsonl")) copy(new File(source, name), new File(dest, name));
    }
    public synchronized void deleteSession(String id) throws IOException {
        checkOpen(); requireInactive(id); deleteTree(sessionDirectory(id));
    }
    public synchronized void cleanExpired() throws IOException { checkOpen(); prune(0); }
    public synchronized void close() throws IOException {
        if (closed) return;
        if (active != null) stop();
        fileLock.release(); lockFile.close(); closed = true;
    }
    private void checkOpen() { if (closed) throw new IllegalStateException("Collector is closed"); }
    private void requireInactive(String id) { if (active != null && active.id.equals(id)) throw new IllegalStateException("Stop session first"); }
    private File sessionDirectory(String id) throws IOException {
        if (id == null || !id.matches("[0-9a-fA-F-]{36}")) throw new IllegalArgumentException("Invalid session ID");
        File dir = new File(root, id);
        if (!dir.getCanonicalFile().equals(dir) || !dir.isDirectory()) throw new IOException("Unknown session");
        return dir;
    }
    private List<File> directories() {
        File[] files = root.listFiles(f -> { try { return f.isDirectory() && f.getCanonicalFile().equals(f) && f.getName().matches("[0-9a-fA-F-]{36}"); } catch (IOException e) { return false; } });
        List<File> dirs = new ArrayList<>(files == null ? Collections.emptyList() : Arrays.asList(files));
        Collections.sort(dirs, (a,b) -> Long.compare(retainedTimestamp(a), retainedTimestamp(b))); return dirs;
    }
    private long retainedTimestamp(File dir) {
        try { return readJSON(new File(dir,"summary.json")).optLong("updated_ms",dir.lastModified()); }
        catch(IOException e) { return dir.lastModified(); }
    }
    private void recover() throws IOException {
        for (File dir : directories()) {
            JSONObject value;
            try { value = readJSON(new File(dir, "summary.json")); }
            catch (IOException e) {
                value = json("schema_version",1); put(value,"session_id",dir.getName());
                put(value,"state","active");put(value,"updated_ms",dir.lastModified());put(value,"last_error","checkpoint_unavailable");
            }
            if ("active".equals(value.optString("state"))) {
                put(value, "state", "interrupted"); put(value, "incomplete", true);
                writeJSON(new File(dir, "summary.json"), value);
            }
        }
    }
    private void prune(long reserve) throws IOException {
        List<File> dirs = directories(); long total = 0;
        for (File dir : dirs) total += size(dir);
        for (File dir : dirs) {
            if (active != null && dir.equals(active.dir)) continue;
            if (System.currentTimeMillis() - retainedTimestamp(dir) > config.retentionMs || total + reserve > config.totalBytes) {
                long bytes = size(dir); deleteTree(dir); total -= bytes;
            }
        }
        if (total + reserve > config.totalBytes) throw new IOException("Diagnostic storage limit reached");
    }
    private final class Session implements DiagnosticsBridge.Sink {
        final File dir; final String id;
        final long started = System.currentTimeMillis();
        final ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<>(1024);
        final Map<String, Double> counters = new TreeMap<>();
        final Thread thread;
        boolean accepting = true, incomplete;
        String state = "active", lastError;
        long omitted, records, detailBytes, inFlight;
        Session(File dir, String id) throws IOException {
            this.dir = dir; this.id = id;
            if (!dir.mkdirs()) throw new IOException("Cannot create session");
            new File(dir, "events.jsonl").createNewFile();
            thread = new Thread(this::writeLoop, "ft-diagnostics"); thread.setDaemon(true);
            writeJSON(new File(dir, "summary.json"), snapshot());
        }
        public synchronized void record(Map<String, Object> event) {
            if (!accepting) return;
            String kind = (String) event.get("event_type"), channel = (String) event.get("channel");
            if (!Arrays.asList("log", "rum", "replay", "other").contains(channel)) return;
            if (!Arrays.asList("storage_write", "storage_remove", "discard", "backlog", "batch", "retry", "upload_start", "upload_end").contains(kind)) return;
            String prefix = channel + "." + kind;
            add(prefix + ".observations", 1);
            Object reason=event.get("reason");
            if (reason instanceof String) {
                String safeReason=Arrays.asList("cache_policy","sqlite_update","historical_insert","sqlite_full_cleanup","sqlite_insert","sqlite_reinsert","sqlite_partial_or_failed","sqlite_delete_by_id","expired","file_write","file_append","file_delete","upload_filter","retry_scheduled","non_retryable_response","write_failed","item_size_limit","batch_retained","capacity_cleanup_count_unavailable","purged","IntakeCode","Flushed","Invalid","Purged").contains(reason) ? (String)reason : "other";
                add(prefix+".reason."+safeReason,1);
            }
            for (String field : Arrays.asList("count", "bytes", "payload_bytes", "request_body_bytes", "response_body_bytes", "duration_ms", "age_ms")) {
                Object value = event.get(field);
                if (value instanceof Number && !Double.isNaN(((Number)value).doubleValue()) && !Double.isInfinite(((Number)value).doubleValue()) && ((Number)value).doubleValue() >= 0) {
                    if (kind.equals("backlog")) {
                        counters.put(prefix + "." + field, ((Number)value).doubleValue());
                        counters.put(prefix + ".observed_ms", ((Number)event.get("time_ms")).doubleValue());
                    }
                    else {
                        double number=((Number)value).doubleValue();
                        add(prefix + "." + field, number); add(prefix + "." + field + ".samples",1);
                        String maxKey=prefix + "." + field + ".max";
                        counters.put(maxKey,Math.max(number,counters.containsKey(maxKey)?counters.get(maxKey):0));
                    }
                }
            }
            if (kind.equals("upload_start")) inFlight++;
            if (kind.equals("upload_end")) { inFlight = Math.max(0, inFlight-1); add(prefix + "." + ("success".equals(event.get("outcome")) ? "success" : "failure"), 1); }
            // Writes remain aggregates: high-volume SDK events do not become diagnostic event records.
            if (kind.equals("storage_write")) return;
            JSONObject row = new JSONObject(event); put(row, "schema_version", 1); put(row, "session_id", id);
            if (!queue.offer(row)) { omitted++; incomplete = true; }
        }
        private void add(String key, double n) { counters.put(key, (counters.containsKey(key) ? counters.get(key) : 0.0) + n); }
        synchronized JSONObject snapshot() {
            JSONObject value = json("schema_version", 1);
            put(value, "session_id", id); put(value, "platform", "android"); put(value, "state", state);
            put(value, "started_ms", started); put(value, "updated_ms", System.currentTimeMillis());
            put(value, "process_scope", "caller_process");
            put(value, "process_directory", root.getName());
            try { put(value,"process_id",android.os.Process.myPid()); } catch(RuntimeException ignored) { put(value,"process_id",JSONObject.NULL); }
            put(value,"backlog_semantics","last_successful_storage_observation; log/rum=events,replay=files; not cross-process additive");
            put(value, "counters", new JSONObject(counters)); put(value, "omitted_records", omitted);
            put(value, "records", records); put(value,"pending_records",queue.size()); put(value, "in_flight_uploads", inFlight);
            put(value, "incomplete", incomplete || !state.equals("active") && inFlight > 0);
            put(value, "last_error", lastError == null ? JSONObject.NULL : lastError);
            put(value, "http_scope", "observed_body_bytes_excludes_headers_tls_tcp; consult per-attempt byte_source");
            return value;
        }
        void writeLoop() {
            long lastSnapshot = 0;
            while (true) {
                synchronized (this) { if (!accepting && queue.isEmpty()) break; }
                try {
                    JSONObject row = queue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (row != null) {
                        byte[] bytes = (row.toString() + "\n").getBytes("UTF-8");
                        boolean allowed;
                        synchronized (this) { allowed = detailBytes + bytes.length <= config.sessionBytes - RESERVE; if (!allowed) { omitted++; incomplete = true; } }
                        if (allowed) {
                            try (FileOutputStream out = new FileOutputStream(new File(dir, "events.jsonl"), true)) { out.write(bytes); }
                            synchronized (this) { detailBytes += bytes.length; records++; }
                        }
                    }
                    if (System.currentTimeMillis() - lastSnapshot >= 1000) {
                        writeJSON(new File(dir, "summary.json"), snapshot()); lastSnapshot = System.currentTimeMillis();
                    }
                } catch (Exception e) { synchronized (this) { lastError = "local_write_failed"; incomplete = true; omitted++; } }
            }
            try { writeJSON(new File(dir, "summary.json"), snapshot()); }
            catch (IOException e) { synchronized (this) { lastError = "summary_write_failed"; incomplete = true; } }
        }
    }
    private static JSONObject json(String k, Object v) { JSONObject o = new JSONObject(); put(o,k,v); return o; }
    private static void put(JSONObject o, String k, Object v) { try { o.put(k,v); } catch (org.json.JSONException e) { throw new IllegalArgumentException(e); } }
    private static JSONObject readJSON(File file) throws IOException {
        if (file.length() > RESERVE) throw new IOException("Invalid summary size");
        try (InputStream in = new FileInputStream(file); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[4096]; int n; while ((n = in.read(b)) != -1) out.write(b,0,n);
            try { return new JSONObject(out.toString("UTF-8")); } catch (org.json.JSONException e) { throw new IOException("Invalid summary",e); }
        }
    }
    private static void writeJSON(File file, JSONObject value) throws IOException {
        byte[] bytes = value.toString().getBytes("UTF-8");
        if (bytes.length > RESERVE/2) throw new IOException("Summary limit exceeded");
        File tmp = new File(file.getParentFile(), file.getName()+".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) { out.write(bytes); }
        if (!tmp.renameTo(file)) throw new IOException("Cannot replace summary");
    }
    private static long size(File file) { if (!file.isDirectory()) return file.length(); long n=0; File[] children=file.listFiles(); if(children!=null) for(File c:children)n+=size(c); return n; }
    private static void deleteTree(File file) throws IOException {
        if (!file.getCanonicalFile().equals(file.getAbsoluteFile())) throw new IOException("Symbolic links not supported");
        File[] children=file.listFiles(); if(children!=null)for(File c:children)deleteTree(c);
        if (!file.delete() && file.exists()) throw new IOException("Cannot delete diagnostic file");
    }
    private static void copy(File from, File to) throws IOException {
        try (InputStream in=new FileInputStream(from); OutputStream out=new FileOutputStream(to)) { byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n); }
    }
}

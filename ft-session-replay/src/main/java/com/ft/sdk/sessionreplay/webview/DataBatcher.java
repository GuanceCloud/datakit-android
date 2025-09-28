package com.ft.sdk.sessionreplay.webview;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedRecord;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DataBatcher {
    private final int MAX_BATCH_SIZE;
    private final long TIMEOUT_MS;
    private final boolean FLUSH_ON_VIEW_SWITCH;

    private final ScheduledExecutorService scheduler;
    private final Map<String, Batch> batchMap = new ConcurrentHashMap<>();
    private final RecordWriter writer;

    private String lastViewId = null;
    // Map to store slotIDs that need local file checking, with automatic size limit of 50
    private final Map<String, Boolean> needCheckSlots = new LinkedHashMap<String, Boolean>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > 50;
        }
    };

    private final boolean isDCWebview;

    public DataBatcher(RecordWriter writer, boolean isDCWebview) {
        this(writer, 20, 500, true, isDCWebview);
    }

    public DataBatcher(RecordWriter writer, int maxBatchSize, long timeoutMs,
                       boolean flushOnViewSwitch, boolean isDCWebview) {
        this.writer = writer;
        this.MAX_BATCH_SIZE = maxBatchSize;
        this.TIMEOUT_MS = timeoutMs;
        this.FLUSH_ON_VIEW_SWITCH = flushOnViewSwitch;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.isDCWebview = isDCWebview;
    }

    public void onData(SessionReplayRumContext context, String jsonString) {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            // Process local CSS files
            if (isDCWebview) {
                JsonElement slotIdElement = jsonObject.get("slotId");
                if (slotIdElement != null && slotIdElement.isJsonPrimitive()) {
                    String slotId = slotIdElement.getAsString();
                    checkLocalFiles(jsonObject, slotId);
                }
            }

            MobileRecord data = MobileRecord.MobileWebviewSnapshotRecord.fromJsonObject(jsonObject);
            if (FLUSH_ON_VIEW_SWITCH) {
                synchronized (this) {
                    if (lastViewId != null && !lastViewId.equals(context.getViewId())) {
                        Batch prev = batchMap.get(lastViewId);
                        if (prev != null) prev.flushNow();
                    }
                    lastViewId = context.getViewId();
                }
            }

            Batch batch = batchMap.get(context.getViewId());
            if (batch == null) {
                Batch created = new Batch(context);
                Batch existing = batchMap.get(context.getViewId());
                batch = (existing != null) ? existing : created;
            }
            batch.addData(data);
        } catch (Exception e) {
            //ignore
        }
    }


    public void flush(String viewId) {
        Batch batch = batchMap.get(viewId);
        if (batch != null) batch.flushNow();
    }


    public void shutdown() {
        for (Batch b : batchMap.values()) {
            b.flushNow();
        }
        scheduler.shutdownNow();
        batchMap.clear();
    }

    /**
     * Entry method for checking local files
     *
     * @param jsonObject data object
     * @param slotId     slotID
     */
    private void checkLocalFiles(JsonObject jsonObject, String slotId) {
        try {
            JsonElement typeElement = jsonObject.get("type");
            if (typeElement == null || !typeElement.isJsonPrimitive()) {
                return;
            }

            int type = typeElement.getAsInt();

            if (type == 4) {
                // Check if it's a local file
                JsonElement dataElement = jsonObject.get("data");
                if (dataElement != null && dataElement.isJsonObject()) {
                    JsonObject data = dataElement.getAsJsonObject();
                    JsonElement hrefElement = data.get("href");
                    if (hrefElement != null && hrefElement.isJsonPrimitive()) {
                        String href = hrefElement.getAsString();
                        if (href != null && href.startsWith("file://")) {
                            // Mark this slotID for local file checking
                            synchronized (needCheckSlots) {
                                needCheckSlots.put(slotId, true);
                            }
                        }
                    }
                }
            } else if ((type == 2 || type == 3) && needCheckSlots.containsKey(slotId)) {
                // Process data containing complete screen snapshot or incremental data
                synchronized (needCheckSlots) {
                    if (needCheckSlots.containsKey(slotId)) {
                        JsonElement dataElement = jsonObject.get("data");
                        if (dataElement != null && dataElement.isJsonObject()) {
                            JsonObject data = dataElement.getAsJsonObject();
                            
                            if (type == 2) {
                                // Process complete screen snapshot
                                JsonElement nodeElement = data.get("node");
                                if (nodeElement != null && nodeElement.isJsonObject()) {
                                    JsonObject node = nodeElement.getAsJsonObject();
                                    addCssTextToHrefWithFileScheme(node);
                                }
                            } else if (type == 3) {
                                // Process incremental data
                                JsonElement addsElement = data.get("adds");
                                if (addsElement != null && addsElement.isJsonArray()) {
                                    JsonArray adds = addsElement.getAsJsonArray();
                                    if (adds.size() > 0) {
                                        for (JsonElement addElement : adds) {
                                            if (addElement.isJsonObject()) {
                                                JsonObject addNode = addElement.getAsJsonObject();
                                                JsonElement nodeElement = addNode.get("node");
                                                if (nodeElement != null && nodeElement.isJsonObject()) {
                                                    JsonObject node = nodeElement.getAsJsonObject();
                                                    addCssTextToHrefWithFileScheme(node);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Protection: if data type doesn't match, don't affect checkLocalFiles context execution
            // Ignore close exception
        }
    }

    /**
     * Recursive method for processing nodes
     *
     * @param rootNodeDict root node
     */
    private void addCssTextToHrefWithFileScheme(JsonObject rootNodeDict) {
        if (rootNodeDict == null) return;

        // 1. First process current node (check if it meets href contains file:// condition)
        processSingleNode(rootNodeDict);

        // 2. Recursively process child nodes of current node (handle nested structure)
        JsonElement childNodesElement = rootNodeDict.get("childNodes");
        if (childNodesElement != null && childNodesElement.isJsonArray()) {
            JsonArray childNodes = childNodesElement.getAsJsonArray();
            for (JsonElement childElement : childNodes) {
                if (childElement.isJsonObject()) {
                    addCssTextToHrefWithFileScheme(childElement.getAsJsonObject());
                }
            }
        }
    }

    /**
     * Process single node (check href and add _cssText)
     *
     * @param nodeDict node dictionary
     */
    private void processSingleNode(JsonObject nodeDict) {
        if (nodeDict == null) return;

        // Step 1: First check if tagName is "link", if not return directly (don't process subsequent logic)
        JsonElement tagNameElement = nodeDict.get("tagName");
        if (tagNameElement == null || !tagNameElement.isJsonPrimitive()) {
            return; // Not a link node, no need to process href
        }

        String nodeTagName = tagNameElement.getAsString();
        if (!"link".equals(nodeTagName)) {
            return; // Not a link node, no need to process href
        }

        // Step 2: Get current node's attributes dictionary
        JsonElement attributesElement = nodeDict.get("attributes");
        if (attributesElement == null || !attributesElement.isJsonObject()) {
            return; // No attributes dictionary, return directly
        }

        JsonObject attributes = attributesElement.getAsJsonObject();

        // Step 3: Check if href exists in attributes and value contains file://
        JsonElement hrefElement = attributes.get("href");
        if (hrefElement == null || !hrefElement.isJsonPrimitive()) {
            return;
        }

        String hrefValue = hrefElement.getAsString();
        if (hrefValue != null && hrefValue.startsWith("file://") && attributes.get("_cssText") == null) {
            // Step 4: Read file and add _cssText:file to attributes dictionary
            String cssPath = hrefValue.replace("file://", "");
            try {
                File cssFile = new File(cssPath);
                if (cssFile.exists()) {
                    String cssString = readFileAsString(cssFile);
                    if (cssString != null && !cssString.isEmpty()) {
                        attributes.addProperty("_cssText", cssString);
                    }
                }
            } catch (IOException e) {
                // Ignore close exception
            }
        }
    }

    /**
     * Read file content as string, compatible with API 21
     *
     * @param file the file to read
     * @return file content as string
     * @throws IOException if file reading fails
     */
    private String readFileAsString(File file) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            StringBuilder result = new StringBuilder();
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                result.append(new String(buffer, 0, bytesRead, "UTF-8"));
            }
            return result.toString();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Ignore close exception
                }
            }
        }
    }

    private class Batch {
        private final SessionReplayRumContext context;
        private final List<MobileRecord> buffer = new ArrayList<>();
        private ScheduledFuture<?> flushTask;

        Batch(SessionReplayRumContext context) {
            this.context = context;
        }

        synchronized void addData(MobileRecord data) {
            buffer.add(data);

            if (buffer.size() >= MAX_BATCH_SIZE) {
                flushNow();
                return;
            }

            if (flushTask != null) {
                flushTask.cancel(false);
                flushTask = null;
            }
            flushTask = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    flushTimeout();
                }
            }, TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }

        private synchronized void flushTimeout() {
            doFlush();
        }

        synchronized void flushNow() {
            if (flushTask != null) {
                flushTask.cancel(false);
                flushTask = null;
            }
            doFlush();
        }

        private void doFlush() {
            List<MobileRecord> toSend;
            synchronized (this) {
                if (buffer.isEmpty()) return;
                toSend = new ArrayList<>(buffer);
                buffer.clear();
            }

            try {
                writer.write(new EnrichedRecord(context.getApplicationId(),
                        context.getSessionId(), context.getViewId(), true, toSend));

            } catch (Throwable t) {
                t.printStackTrace();
            }
            Batch cur = batchMap.get(context.getViewId());
            if (cur == this) {
                batchMap.remove(context.getViewId());
            }
        }
    }
}
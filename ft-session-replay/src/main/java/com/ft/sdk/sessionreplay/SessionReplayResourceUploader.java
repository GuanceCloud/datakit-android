package com.ft.sdk.sessionreplay;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.internal.processor.EnrichedResource;
import com.ft.sdk.sessionreplay.internal.resources.ResourceUploadKey;
import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for session replay resource upload logic
 */
public class SessionReplayResourceUploader implements IUploader {

    private static final String TAG = "SessionReplayResourceUploader";

    public static final String KEY_APP_ID = "app_id";
    public static final String KEY_FILES = "files";
    public static final String KEY_TAGS = "tags";

    private final InternalLogger internalLogger;
    private final SessionReplayResourceUploadCallback uploadCallback;

    /**
     * Constructor
     *
     * @param internalLogger the internal logger
     * @param uploadCallback the upload callback
     */
    public SessionReplayResourceUploader(InternalLogger internalLogger, SessionReplayResourceUploadCallback uploadCallback) {
        this.internalLogger = internalLogger;
        this.uploadCallback = uploadCallback;
    }

    /**
     * Upload session replay resource files
     *
     * @param context the session replay context
     * @param batchData the batch events to upload
     * @param byteArray the raw byte data
     * @return upload result
     * @throws Exception if upload fails
     */
    @Override
    public UploadResult upload(SessionReplayContext context, List<RawBatchEvent> batchData, byte[] byteArray) throws Exception {
        if (batchData == null || batchData.isEmpty()) {
            return UploadResult.createErrorResult();
        }

        String appId = null;
        Map<String, ResourceUploadGroup> uploadGroups = new LinkedHashMap<>();

        for (RawBatchEvent event : batchData) {
            String fileName = EnrichedResource.extractFileName(event.getMetadata());
            String applicationId = EnrichedResource.extractApplicationId(event.getMetadata());
            Map<String, Object> metadataGlobalContext = EnrichedResource.extractGlobalContext(event.getMetadata());
            
            if (fileName != null) {
                if (appId == null && applicationId != null) {
                    appId = applicationId;
                }
                String routeKey = ResourceUploadKey.extractWgtId(metadataGlobalContext);
                ResourceUploadGroup group = uploadGroups.get(routeKey);
                if (group == null) {
                    group = new ResourceUploadGroup(metadataGlobalContext);
                    uploadGroups.put(routeKey, group);
                }
                group.add(fileName, event);
            }
        }

        if (uploadGroups.isEmpty() || appId == null) {
            internalLogger.w(TAG, "No valid files to upload or missing app_id");
            return UploadResult.createErrorResult();
        }

        if (uploadCallback == null) {
            internalLogger.e(TAG, "Upload callback is null");
            return UploadResult.createErrorResult();
        }

        UploadResult lastResult = new UploadResult(HttpURLConnection.HTTP_OK, "", "");
        for (ResourceUploadGroup group : uploadGroups.values()) {
            UploadResult result = uploadGroup(appId, group);
            if (result == null || !result.isSuccess()) {
                return result;
            }
            lastResult = result;
        }
        return lastResult;
    }

    private UploadResult uploadGroup(String appId, ResourceUploadGroup group) {
        ExistingFilesCheckResult existingFilesResult = checkExistingFiles(
                appId,
                group.fileNames,
                group.globalContext
        );
        if (!existingFilesResult.isSuccess()) {
            internalLogger.w(TAG, "Skip resource upload because checking existing files failed");
            return existingFilesResult.getFailureResult() != null
                    ? existingFilesResult.getFailureResult()
                    : UploadResult.createErrorResult();
        }

        List<RawBatchEvent> filesNeedUpload = new ArrayList<>();
        for (int i = 0; i < group.files.size(); i++) {
            String fileName = group.fileNames.get(i);
            if (!existingFilesResult.existingFiles.contains(fileName)) {
                filesNeedUpload.add(group.files.get(i));
            }
        }

        if (filesNeedUpload.isEmpty()) {
            internalLogger.d(TAG, "All files already exist, no upload needed");
            return new UploadResult(HttpURLConnection.HTTP_OK, "", "");
        }

        return uploadFiles(appId, filesNeedUpload, group.globalContext);
    }

    /**
     * Check existing files from server
     *
     * @param appId the application id
     * @param fileNames the list of file names to check
     * @return list of existing file names
     */
    private ExistingFilesCheckResult checkExistingFiles(String appId, List<String> fileNames, Map<String, Object> globalContext) {
        List<String> existingFiles = new ArrayList<>();

        if (uploadCallback != null) {
            UploadResult result = uploadCallback.onCheckFilesExist(appId, fileNames, globalContext);
            if (result != null && result.isSuccess() && result.getResponse() != null
                    && !result.getResponse().isEmpty()) {
                try {
                    JsonObject responseJson = new Gson().fromJson(result.getResponse(), JsonObject.class);
                    if (responseJson != null && responseJson.has("content")) {
                        JsonObject contentObject = responseJson.getAsJsonObject("content");
                        if (contentObject != null) {
                            for (String fileName : contentObject.keySet()) {
                                if (contentObject.get(fileName).getAsBoolean()) {
                                    existingFiles.add(fileName);
                                }
                            }
                        }
                    }
                    internalLogger.d(TAG, "Check existing files response: " + result.getResponse());
                } catch (Exception e) {
                    internalLogger.e(TAG, "Parse check response error: " + e.getMessage(), e);
                    return new ExistingFilesCheckResult(false, existingFiles, null);
                }
                return new ExistingFilesCheckResult(true, existingFiles, null);
            } else {
                internalLogger.w(TAG, "Check existing files failed or returned null: "
                        + (result != null ? result.getResponse() : "null"));
                return new ExistingFilesCheckResult(false, existingFiles,
                        result != null && result.isNeedReTry() ? result : null);
            }
        }

        return new ExistingFilesCheckResult(false, existingFiles, null);
    }

    /**
     * Upload resource files to server
     *
     * @param appId the application id
     * @param filesToUpload the list of files to upload
     * @return upload result
     */
    private UploadResult uploadFiles(String appId, List<RawBatchEvent> filesToUpload, Map<String, Object> globalContext) {
        if (uploadCallback != null) {
            UploadResult result = uploadCallback.onUploadFiles(appId, filesToUpload, globalContext);
            if (result != null) {
                if (result.isSuccess()) {
                    internalLogger.d(TAG, "Resource Upload Success. " + result.getPkgId()
                            + ",app_id:" + appId + ",count:" + filesToUpload.size()
                            + formatGlobalContext(globalContext));
                } else {
                    internalLogger.e(TAG, "Resource Upload Failed." + result.getPkgId()
                            + ",app_id:" + appId + ",count:" + filesToUpload.size()
                            + ",code:" + result.getCode() + ",response:" + result.getResponse());
                }
            }
            return result;
        }
        return UploadResult.createErrorResult();
    }

    private String formatGlobalContext(Map<String, Object> globalContext) {
        if (globalContext == null || globalContext.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : globalContext.entrySet()) {
            builder.append(",").append(entry.getKey()).append(":").append(entry.getValue());
        }
        return builder.toString();
    }

    private static class ExistingFilesCheckResult {
        private final boolean success;
        private final List<String> existingFiles;
        private final UploadResult failureResult;

        private ExistingFilesCheckResult(boolean success, List<String> existingFiles,
                                         UploadResult failureResult) {
            this.success = success;
            this.existingFiles = existingFiles;
            this.failureResult = failureResult;
        }

        private boolean isSuccess() {
            return success;
        }

        private UploadResult getFailureResult() {
            return failureResult;
        }
    }

    private static class ResourceUploadGroup {
        private final List<String> fileNames = new ArrayList<>();
        private final List<RawBatchEvent> files = new ArrayList<>();
        private final Map<String, Object> globalContext = new HashMap<>();

        private ResourceUploadGroup(Map<String, Object> globalContext) {
            if (globalContext != null) {
                this.globalContext.putAll(globalContext);
            }
        }

        private void add(String fileName, RawBatchEvent event) {
            fileNames.add(fileName);
            files.add(event);
        }
    }
}

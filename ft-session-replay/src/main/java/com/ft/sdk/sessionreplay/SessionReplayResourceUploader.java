package com.ft.sdk.sessionreplay;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.internal.processor.EnrichedResource;
import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for session replay resource upload logic
 */
public class SessionReplayResourceUploader implements IUploader {

    private static final String TAG = "SessionReplayResourceUploader";

    public static final String KEY_APP_ID = "app_id";
    public static final String KEY_FILES = "files";

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

        List<RawBatchEvent> filesToUpload = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        String appId = null;

        for (RawBatchEvent event : batchData) {
            String fileName = EnrichedResource.extractFileName(event.getMetadata());
            String applicationId = EnrichedResource.extractApplicationId(event.getMetadata());
            
            if (fileName != null) {
                fileNames.add(fileName);
                filesToUpload.add(event);
                if (appId == null && applicationId != null) {
                    appId = applicationId;
                }
            }
        }

        if (filesToUpload.isEmpty() || appId == null) {
            internalLogger.w(TAG, "No valid files to upload or missing app_id");
            return UploadResult.createErrorResult();
        }

        if (uploadCallback == null) {
            internalLogger.e(TAG, "Upload callback is null");
            return UploadResult.createErrorResult();
        }

        List<String> existingFiles = checkExistingFiles(appId, fileNames);
        
        List<RawBatchEvent> filesNeedUpload = new ArrayList<>();
        for (int i = 0; i < filesToUpload.size(); i++) {
            String fileName = fileNames.get(i);
            if (!existingFiles.contains(fileName)) {
                filesNeedUpload.add(filesToUpload.get(i));
            }
        }

        if (filesNeedUpload.isEmpty()) {
            internalLogger.d(TAG, "All files already exist, no upload needed");
            return new UploadResult(HttpURLConnection.HTTP_OK, "", "");
        }

        return uploadFiles(appId, filesNeedUpload);
    }

    /**
     * Check existing files from server
     *
     * @param appId the application id
     * @param fileNames the list of file names to check
     * @return list of existing file names
     */
    private List<String> checkExistingFiles(String appId, List<String> fileNames) {
        List<String> existingFiles = new ArrayList<>();

        if (uploadCallback != null) {
            UploadResult result = uploadCallback.onCheckFilesExist(appId, fileNames);
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
                }
            } else {
                internalLogger.w(TAG, "Check existing files failed or returned null: "
                        + (result != null ? result.getResponse() : "null"));
            }
        }

        return existingFiles;
    }

    /**
     * Upload resource files to server
     *
     * @param appId the application id
     * @param filesToUpload the list of files to upload
     * @return upload result
     */
    private UploadResult uploadFiles(String appId, List<RawBatchEvent> filesToUpload) {
        if (uploadCallback != null) {
            UploadResult result = uploadCallback.onUploadFiles(appId, filesToUpload);
            if (result != null) {
                if (result.isSuccess()) {
                    internalLogger.d(TAG, "Resource Upload Success. " + result.getPkgId()
                            + ",app_id:" + appId + ",count:" + filesToUpload.size());
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
}

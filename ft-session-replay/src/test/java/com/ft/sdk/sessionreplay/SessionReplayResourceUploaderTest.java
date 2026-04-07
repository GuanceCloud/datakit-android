package com.ft.sdk.sessionreplay;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedResource;
import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SessionReplayResourceUploaderTest {

    @Test
    public void upload_shouldStopWhenCheckExistingFilesFails() throws Exception {
        TestUploadCallback callback = new TestUploadCallback(
                new UploadResult(500, "check failed", ""),
                new UploadResult(200, "upload success", "pkg-id")
        );
        SessionReplayResourceUploader uploader =
                new SessionReplayResourceUploader(new NoOpLogger(), callback);

        UploadResult result = uploader.upload(
                null,
                Collections.singletonList(createEvent("app-id", "resource-1.png")),
                null
        );

        assertTrue(result.isInvalid());
        assertFalse(callback.uploadFilesCalled);
    }

    @Test
    public void upload_shouldSkipExistingFilesAndUploadMissingFiles() throws Exception {
        TestUploadCallback callback = new TestUploadCallback(
                new UploadResult(200, "{\"content\":{\"resource-1.png\":true}}", ""),
                new UploadResult(200, "upload success", "pkg-id")
        );
        SessionReplayResourceUploader uploader =
                new SessionReplayResourceUploader(new NoOpLogger(), callback);

        UploadResult result = uploader.upload(
                null,
                Collections.singletonList(createEvent("app-id", "resource-1.png")),
                null
        );

        assertTrue(result.isSuccess());
        assertFalse(callback.uploadFilesCalled);
    }

    @Test
    public void upload_shouldReturnErrorWhenBatchIsEmpty() throws Exception {
        SessionReplayResourceUploader uploader =
                new SessionReplayResourceUploader(new NoOpLogger(), new TestUploadCallback(null, null));

        UploadResult result = uploader.upload(null, Collections.<RawBatchEvent>emptyList(), null);

        assertTrue(result.isInvalid());
    }

    @Test
    public void upload_shouldReturnErrorWhenCallbackIsNull() throws Exception {
        SessionReplayResourceUploader uploader =
                new SessionReplayResourceUploader(new NoOpLogger(), null);

        UploadResult result = uploader.upload(
                null,
                Collections.singletonList(createEvent("app-id", "resource-1.png")),
                null
        );

        assertTrue(result.isInvalid());
    }

    @Test
    public void upload_shouldReturnErrorWhenMetadataHasNoAppId() throws Exception {
        TestUploadCallback callback = new TestUploadCallback(
                new UploadResult(200, "{\"content\":{}}", ""),
                new UploadResult(200, "upload success", "pkg-id")
        );
        SessionReplayResourceUploader uploader =
                new SessionReplayResourceUploader(new NoOpLogger(), callback);

        UploadResult result = uploader.upload(
                null,
                Collections.singletonList(createEvent(null, "resource-1.png")),
                null
        );

        assertTrue(result.isInvalid());
        assertFalse(callback.checkFilesCalled);
        assertFalse(callback.uploadFilesCalled);
    }

    @Test
    public void upload_shouldReturnErrorWhenCheckResponseIsMalformed() throws Exception {
        TestUploadCallback callback = new TestUploadCallback(
                new UploadResult(200, "{\"content\":{\"resource-1.png\":", ""),
                new UploadResult(200, "upload success", "pkg-id")
        );
        SessionReplayResourceUploader uploader =
                new SessionReplayResourceUploader(new NoOpLogger(), callback);

        UploadResult result = uploader.upload(
                null,
                Collections.singletonList(createEvent("app-id", "resource-1.png")),
                null
        );

        assertTrue(result.isInvalid());
        assertTrue(callback.checkFilesCalled);
        assertFalse(callback.uploadFilesCalled);
    }

    @Test
    public void upload_shouldOnlyUploadMissingFiles() throws Exception {
        TestUploadCallback callback = new TestUploadCallback(
                new UploadResult(200, "{\"content\":{\"resource-1.png\":true}}", ""),
                new UploadResult(200, "upload success", "pkg-id")
        );
        SessionReplayResourceUploader uploader =
                new SessionReplayResourceUploader(new NoOpLogger(), callback);

        UploadResult result = uploader.upload(
                null,
                java.util.Arrays.asList(
                        createEvent("app-id", "resource-1.png"),
                        createEvent("app-id", "resource-2.png")
                ),
                null
        );

        assertTrue(result.isSuccess());
        assertTrue(callback.uploadFilesCalled);
        // Only the missing file should be forwarded to the upload callback.
        assertEquals(1, callback.uploadedFiles.size());
        assertEquals("resource-2.png",
                EnrichedResource.extractFileName(callback.uploadedFiles.get(0).getMetadata()));
    }

    @Test
    public void upload_shouldReturnNullWhenUploadCallbackReturnsNullResult() throws Exception {
        TestUploadCallback callback = new TestUploadCallback(
                new UploadResult(200, "{\"content\":{}}", ""),
                null
        );
        SessionReplayResourceUploader uploader =
                new SessionReplayResourceUploader(new NoOpLogger(), callback);

        UploadResult result = uploader.upload(
                null,
                Collections.singletonList(createEvent("app-id", "resource-1.png")),
                null
        );

        assertNull(result);
        assertTrue(callback.uploadFilesCalled);
    }

    private RawBatchEvent createEvent(String appId, String fileName) {
        byte[] metadata = new EnrichedResource(new byte[]{1}, fileName).asBinaryMetadata(appId);
        return new RawBatchEvent(new byte[]{1, 2, 3}, metadata);
    }

    // Captures the uploader's branching behavior without introducing a mocking dependency.
    private static class TestUploadCallback implements SessionReplayResourceUploadCallback {
        private final UploadResult checkResult;
        private final UploadResult uploadResult;
        private boolean checkFilesCalled;
        private boolean uploadFilesCalled;
        private List<RawBatchEvent> uploadedFiles;

        private TestUploadCallback(UploadResult checkResult, UploadResult uploadResult) {
            this.checkResult = checkResult;
            this.uploadResult = uploadResult;
        }

        @Override
        public UploadResult onCheckFilesExist(String appId, List<String> fileNames) {
            checkFilesCalled = true;
            return checkResult;
        }

        @Override
        public UploadResult onUploadFiles(String appId, List<RawBatchEvent> files) {
            uploadFilesCalled = true;
            uploadedFiles = files;
            return uploadResult;
        }
    }

    private static class NoOpLogger implements InternalLogger {
        @Override
        public void i(String tag, String message) {
        }

        @Override
        public void i(String tag, String message, boolean onlyOnce) {
        }

        @Override
        public void d(String tag, String message) {
        }

        @Override
        public void d(String tag, String message, boolean onlyOnce) {
        }

        @Override
        public void e(String tag, String message) {
        }

        @Override
        public void e(String tag, String message, boolean onlyOnce) {
        }

        @Override
        public void e(String tag, String message, Throwable e) {
        }

        @Override
        public void e(String tag, String message, Throwable e, boolean onlyOnce) {
        }

        @Override
        public void v(String tag, String message) {
        }

        @Override
        public void v(String tag, String message, boolean onlyOnce) {
        }

        @Override
        public void w(String tag, String message) {
        }

        @Override
        public void w(String tag, String message, boolean onlyOnce) {
        }

        @Override
        public void w(String tag, String message, Throwable e) {
        }

        @Override
        public void w(String tag, String message, Throwable e, boolean onlyOnce) {
        }
    }
}

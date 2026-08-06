package com.ft.sdk.sessionreplay;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;

import java.util.List;
import java.util.Map;

/**
 * Callback used to plug custom resource file existence checks and uploads into
 * Session Replay.
 *
 * <p>The callbacks are invoked synchronously from the Session Replay upload pipeline with no
 * guaranteed thread affinity. Implementations must be thread-safe, return promptly and must not
 * mutate the SDK-owned lists or maps. Throwing or returning {@code null} causes the current resource
 * upload attempt to fail.</p>
 *
 * <p>The same non-null {@code globalContext} snapshot is supplied to the existence check and upload
 * for one resource group. It contains the RUM link values selected with
 * {@link FTSessionReplayConfig#enableLinkRUMKeys(String[])} and may be empty. Resources with different
 * {@code wgt_id} values are delivered in separate groups, so implementations must forward the context
 * as request tags to preserve backend routing.</p>
 *
 * <p>Implementations of the former two-parameter callback methods must add the
 * {@code globalContext} parameter to both methods.</p>
 */
public interface SessionReplayResourceUploadCallback {
    /**
     * Checks whether resource files already exist on the backend.
     *
     * <p>A successful result must contain a non-empty JSON response in the form
     * {@code {"content":{"fileName":true}}}. A missing file name or a {@code false} value means that
     * file must be uploaded. A null, empty or malformed response is treated as a failed check.</p>
     *
     * @param appId non-null application id associated with the resources
     * @param fileNames non-empty, SDK-owned resource file names to check; treat as read-only
     * @param globalContext non-null, SDK-owned RUM link context for this route; may be empty and
     *                      must be forwarded as request tags
     * @return non-null upload result whose success, retry and response values describe the check
     */
    UploadResult onCheckFilesExist(String appId, List<String> fileNames,
                                   Map<String, Object> globalContext);

    /**
     * Uploads resource files required by Session Replay segments.
     *
     * @param appId non-null application id associated with the resources
     * @param files non-empty, SDK-owned resource files to upload; treat as read-only
     * @param globalContext non-null, SDK-owned RUM link context for this route; may be empty and
     *                      must be forwarded as request tags
     * @return non-null upload result whose success and retry values describe the upload outcome
     */
    UploadResult onUploadFiles(String appId, List<RawBatchEvent> files,
                               Map<String, Object> globalContext);
}

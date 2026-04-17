package com.ft.sdk.sessionreplay.internal;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.SessionReplayFeature;
import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;
import com.ft.sdk.sessionreplay.internal.processor.EnrichedRecord;

import java.util.HashMap;
import java.util.Map;

public class SessionReplayRecordCallback implements RecordCallback {

    private final FeatureSdkCore featureSdkCore;

    public SessionReplayRecordCallback(FeatureSdkCore featureSdkCore) {
        this.featureSdkCore = featureSdkCore;
    }

    @Override
    public void onRecordForViewSent(EnrichedRecord record) {
        int recordsSize = record.getRecords().size();
        if (recordsSize > 0) {
            featureSdkCore.updateFeatureContext(SessionReplayFeature.SESSION_REPLAY_FEATURE_NAME, new UpdateCallBack() {
                @Override
                public void onUpdate(Map<String, Object> stringObjectMap) {
                    String viewId = record.getViewId();
                    Map<String, Object> viewMetadata = stringObjectMap.containsKey(viewId) ?
                            (Map<String, Object>) stringObjectMap.get(viewId) : new HashMap<>();
                    if (viewMetadata != null) {
                        viewMetadata.put(HAS_REPLAY_KEY, true);
                        viewMetadata.put(SAMPLED_ON_ERROR,
                                featureSdkCore.getConsentProvider() == TrackingConsent.SAMPLED_ON_ERROR_SESSION);
                        updateRecordsCount(viewMetadata, recordsSize);
                        stringObjectMap.put(viewId, viewMetadata);
                    }
                }
            });
        }
    }

    private void updateRecordsCount(Map<String, Object> viewMetadata, int recordsCount) {
        Long currentRecords = (Long) viewMetadata.get(VIEW_RECORDS_COUNT_KEY);
        long newRecords = (currentRecords == null ? 0 : currentRecords) + recordsCount;
        viewMetadata.put(VIEW_RECORDS_COUNT_KEY, newRecords);
    }

    public static final String HAS_REPLAY_KEY = "has_replay";
    public static final String VIEW_RECORDS_COUNT_KEY = "records_count";
    public static final String SAMPLED_ON_ERROR = "sampled_on_reply_error";

    public interface UpdateCallBack {
        void onUpdate(Map<String, Object> stringObjectMap);
    }
}

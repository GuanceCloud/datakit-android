package com.ft.sdk.internal.anr.historical;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.utils.Utils;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class HistoricalAnrDataId {
    private static final int DATA_ID_LENGTH = 16;
    private static final String PENDING_RUM_APP = "historical_pending_rum_app";
    private static final String PENDING_RUM_APP_ERROR_SAMPLED =
            "historical_pending_rum_app_error_sampled";

    private HistoricalAnrDataId() {
    }

    @Nullable
    public static String fromDedupeKey(String dedupeKey) {
        if (dedupeKey == null || dedupeKey.length() == 0) {
            return null;
        }
        String digest = Utils.toMD5(dedupeKey);
        if (digest == null || digest.length() < DATA_ID_LENGTH) {
            return null;
        }
        return digest.substring(0, DATA_ID_LENGTH);
    }

    public static boolean canonicalize(String dedupeKey, SyncData data) {
        String dataId = fromDedupeKey(dedupeKey);
        if (dataId == null || data == null || data.getUuid() == null
                || data.getUuid().length() == 0) {
            return false;
        }
        data.setDataString(data.getDataString(dataId));
        data.setUuid(dataId);
        return true;
    }

    @Nullable
    public static String pendingType(DataType committedType) {
        if (committedType == DataType.RUM_APP) {
            return PENDING_RUM_APP;
        }
        if (committedType == DataType.RUM_APP_ERROR_SAMPLED) {
            return PENDING_RUM_APP_ERROR_SAMPLED;
        }
        return null;
    }

    public static boolean isPendingType(String value) {
        return PENDING_RUM_APP.equals(value)
                || PENDING_RUM_APP_ERROR_SAMPLED.equals(value);
    }
}

package com.ft.sdk.garble.db;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface HistoricalRumDataStore {
    InsertResult prepareHistoricalRum(@NonNull String dedupeKey,
                                      @NonNull String viewId,
                                      @NonNull SyncData data);

    InsertResult commitHistoricalRum(@NonNull String dedupeKey,
                                     @NonNull DataType committedType);
}

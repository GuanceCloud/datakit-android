package com.ft.sdk.sessionreplay.resources;

import static com.ft.sdk.feature.Feature.SESSION_REPLAY_RESOURCES_FEATURE_NAME;

import android.text.format.DateUtils;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.model.ResourceHashesEntry;
import com.ft.sdk.storage.DataStoreContent;
import com.ft.sdk.storage.DataStoreHandler;
import com.ft.sdk.storage.DataStoreReadCallback;
import com.ft.sdk.storage.DataStoreWriteCallback;
import com.ft.sdk.storage.Deserializer;
import com.ft.sdk.storage.Serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ResourceDataStoreManager {

    private final FeatureSdkCore featureSdkCore;
    private final Serializer<ResourceHashesEntry> resourceHashesSerializer;
    private final Deserializer<String, ResourceHashesEntry> resourceHashesDeserializer;

    private final Set<String> knownResources;
    private final AtomicLong storedLastUpdateDateNs;
    private final AtomicBoolean isInitialized;

    public ResourceDataStoreManager(FeatureSdkCore featureSdkCore,
                                    Serializer<ResourceHashesEntry> resourceHashesSerializer,
                                    Deserializer<String, ResourceHashesEntry> resourceHashesDeserializer) {
        this.featureSdkCore = featureSdkCore;
        this.resourceHashesSerializer = resourceHashesSerializer;
        this.resourceHashesDeserializer = resourceHashesDeserializer;

        knownResources = Collections.synchronizedSet(new HashSet<>());
        storedLastUpdateDateNs = new AtomicLong(System.nanoTime());
        isInitialized = new AtomicBoolean(false);

        fetchStoredResourceHashes(
                new FetchSuccessCallback() {
                    @Override
                    public void onSuccess(DataStoreContent<ResourceHashesEntry> dataStoreContent) {
                        ResourceHashesEntry storedData = dataStoreContent == null ? null : dataStoreContent.getData();
                        if (storedData == null) {
                            finishedInitializingManager();
                            return;
                        }

                        long lastUpdateDateNs = storedData.getLastUpdateDateNs();
                        List<String> storedHashes = storedData.getResourceHashes();

                        if (didDataStoreExpire(lastUpdateDateNs)) {
                            deleteStoredHashesEntry(new DataStoreWriteCallback() {
                                @Override
                                public void onSuccess() {
                                    finishedInitializingManager();
                                }

                                @Override
                                public void onFailure() {
                                    finishedInitializingManager();
                                }
                            });
                        } else {
                            ResourceDataStoreManager.this.storedLastUpdateDateNs.set(lastUpdateDateNs);
                            knownResources.addAll(storedHashes);
                            finishedInitializingManager();
                        }
                    }
                },
                new FetchFailCallback() {
                    @Override
                    public void onFailure() {
                        finishedInitializingManager();
                    }
                }
        );
    }

    public boolean isPreviouslySentResource(String resourceHash) {
        return knownResources.contains(resourceHash);
    }

    public void cacheResourceHash(String resourceHash) {
        knownResources.add(resourceHash);
        writeResourcesToStore();
    }

    public boolean isReady() {
        return isInitialized.get();
    }

    private void finishedInitializingManager() {
        isInitialized.set(true);
    }

    private void writeResourcesToStore() {
        ResourceHashesEntry data = new ResourceHashesEntry(storedLastUpdateDateNs.get(), new ArrayList<>(knownResources));

        DataStoreHandler dataStore = featureSdkCore.getFeature(SESSION_REPLAY_RESOURCES_FEATURE_NAME).getDataStore();
        if (dataStore != null) {
            dataStore.setValue(DATASTORE_HASHES_ENTRY_NAME, data, null, null, resourceHashesSerializer);
        }
    }

    private void fetchStoredResourceHashes(FetchSuccessCallback onFetchSuccessful,
                                           FetchFailCallback onFetchFailure) {
        DataStoreHandler dataStore = featureSdkCore.getFeature(SESSION_REPLAY_RESOURCES_FEATURE_NAME).getDataStore();
        if (dataStore != null) {
            dataStore.value(DATASTORE_HASHES_ENTRY_NAME, null, new DataStoreReadCallback<ResourceHashesEntry>() {
                @Override
                public void onSuccess(DataStoreContent<ResourceHashesEntry> dataStoreContent) {
                    onFetchSuccessful.onSuccess(dataStoreContent);
                }

                @Override
                public void onFailure() {
                    onFetchFailure.onFailure();
                }
            }, resourceHashesDeserializer);
        } else {
            onFetchFailure.onFailure();
        }
    }

    private void deleteStoredHashesEntry(DataStoreWriteCallback callback) {
        DataStoreHandler dataStore = featureSdkCore.getFeature(SESSION_REPLAY_RESOURCES_FEATURE_NAME).getDataStore();
        if (dataStore != null) {
            dataStore.removeValue(DATASTORE_HASHES_ENTRY_NAME, callback);
        }
    }

    private boolean didDataStoreExpire(long lastUpdateDate) {
        return System.nanoTime() - lastUpdateDate > DATASTORE_EXPIRATION_NS;
    }

    public static final long DATASTORE_EXPIRATION_NS = DateUtils.DAY_IN_MILLIS * 30 * 1000 * 1000;
    public static final String DATASTORE_HASHES_ENTRY_NAME = "resource-hash-store";

    interface FetchSuccessCallback {
        void onSuccess(DataStoreContent<ResourceHashesEntry> dataStoreContent);
    }

    interface FetchFailCallback {
        void onFailure();
    }
}

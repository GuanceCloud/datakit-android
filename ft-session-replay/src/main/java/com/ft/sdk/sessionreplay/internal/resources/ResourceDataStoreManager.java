package com.ft.sdk.sessionreplay.internal.resources;

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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ResourceDataStoreManager {

    private static final int MAX_KNOWN_RESOURCES = 10_000;

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

        knownResources = Collections.synchronizedSet(new LinkedHashSet<>());
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
                            for (String storedHash : storedHashes) {
                                addKnownResource(storedHash);
                            }
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
        return isPreviouslySentResource(resourceHash, Collections.<String, Object>emptyMap());
    }

    public boolean isPreviouslySentResource(String resourceHash, Map<String, Object> globalContext) {
        return knownResources.contains(ResourceUploadKey.from(resourceHash, globalContext).toStorageKey());
    }

    public void cacheResourceHash(String resourceHash) {
        cacheResourceHash(resourceHash, Collections.<String, Object>emptyMap());
    }

    public void cacheResourceHash(String resourceHash, Map<String, Object> globalContext) {
        ResourceUploadKey uploadKey = ResourceUploadKey.from(resourceHash, globalContext);
        addKnownResource(uploadKey.toStorageKey());
        if (uploadKey.isRouted()) {
            addKnownResource(ResourceUploadKey.main(resourceHash).toStorageKey());
        }
        writeResourcesToStore();
    }

    public boolean isReady() {
        return isInitialized.get();
    }

    private void finishedInitializingManager() {
        isInitialized.set(true);
    }

    private void writeResourcesToStore() {
        List<String> resourceKeys;
        synchronized (knownResources) {
            resourceKeys = new ArrayList<>(knownResources);
        }
        ResourceHashesEntry data = new ResourceHashesEntry(storedLastUpdateDateNs.get(), resourceKeys);

        DataStoreHandler dataStore = featureSdkCore.getFeature(SESSION_REPLAY_RESOURCES_FEATURE_NAME).getDataStore();
        if (dataStore != null) {
            dataStore.setValue(DATASTORE_HASHES_ENTRY_NAME, data, null, null, resourceHashesSerializer);
        }
    }

    private void addKnownResource(String resourceKey) {
        synchronized (knownResources) {
            knownResources.add(resourceKey);
            while (knownResources.size() > MAX_KNOWN_RESOURCES) {
                Iterator<String> iterator = knownResources.iterator();
                if (!iterator.hasNext()) {
                    break;
                }
                iterator.next();
                iterator.remove();
            }
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

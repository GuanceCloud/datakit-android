package com.ft.sdk.garble.db.file;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTSQL;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * File-backed sync data queue.
 */
public class FTSyncFileDataStore {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTSyncFileDataStore";
    private static final String FILE_SUFFIX = ".json";
    private static final String SEQUENCE_FILE_NAME = "sequence";

    private final FTFileStorePaths paths;
    private final FTFileLock lock;

    public FTSyncFileDataStore(FTFileStorePaths paths) {
        this.paths = paths;
        this.lock = new FTFileLock(paths.getLockFile());
    }

    public boolean updateOrInsertSyncData(@NonNull final SyncData data) {
        if (data.getUuid() == null || data.getUuid().isEmpty()) {
            LogUtils.e(TAG, "updateOrInsertSyncData failed: UUID is null or empty");
            return false;
        }
        try {
            return lock.withLock(new FTFileLock.LockedOperation<Boolean>() {
                @Override
                public Boolean run() throws Exception {
                    paths.ensureReady();
                    SyncRecord existing = findRecordByUuid(data.getUuid());
                    if (existing == null) {
                        return false;
                    }
                    data.setId(existing.data.getId());
                    writeRecord(existing.file, data);
                    return true;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "updateOrInsertSyncData failed: " + e.getMessage());
            return false;
        }
    }

    public boolean insertFtOperation(final SyncData data, boolean reInsert) {
        ArrayList<SyncData> list = new ArrayList<>();
        list.add(data);
        return insertFtOptList(list, reInsert);
    }

    public boolean insertFtOptList(@NonNull final List<SyncData> dataList, final boolean reInsert) {
        if (dataList.isEmpty()) {
            return true;
        }
        try {
            return lock.withLock(new FTFileLock.LockedOperation<Boolean>() {
                @Override
                public Boolean run() throws Exception {
                    paths.ensureReady();
                    long nextId = readNextId();
                    int insertedCount = 0;
                    for (SyncData data : dataList) {
                        long id = nextId++;
                        data.setId(id);
                        if (reInsert) {
                            String uuid = Utils.getGUID_16();
                            data.setDataString(data.getDataString(uuid));
                            data.setUuid(uuid);
                        }
                        writeRecord(getRecordFile(id), data);
                        insertedCount++;
                    }
                    writeNextId(nextId);
                    return insertedCount > 0;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "insertFtOptList failed: " + e.getMessage());
            return false;
        }
    }

    public List<SyncData> queryDataByDataByTypeLimit(int limit, DataType dataType) {
        return queryRecords(limit, new DataType[]{dataType}, true);
    }

    public List<SyncData> queryDataByDataByTypeLimitDesc(int limit, DataType dataType) {
        return queryRecords(limit, new DataType[]{dataType}, false);
    }

    public List<SyncData> queryDataByDescLimit(int limit) {
        return queryRecords(limit, null, true);
    }

    public int updateDataType(final DataType dataType, final long errorDateline) {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<Integer>() {
                @Override
                public Integer run() throws Exception {
                    int count = 0;
                    String originType = dataType.getValue();
                    String targetType = originType.replace(DataType.ERROR_SAMPLED_SUFFIX, "");
                    DataType targetDataType = findDataType(targetType);
                    if (targetDataType == null) {
                        return 0;
                    }
                    ArrayList<SyncRecord> records = readRecords();
                    for (SyncRecord record : records) {
                        SyncData data = record.data;
                        if (originType.equals(data.getDataType().getValue()) && data.getTime() <= errorDateline) {
                            SyncData updated = new SyncData(targetDataType);
                            updated.setId(data.getId());
                            updated.setTime(data.getTime());
                            updated.setUuid(data.getUuid());
                            updated.setDataString(data.getDataString());
                            writeRecord(record.file, updated);
                            count++;
                        }
                    }
                    return count;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
            return 0;
        }
    }

    public int deleteExpireCache(final DataType dataType, final long now, final long expireDuration) {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<Integer>() {
                @Override
                public Integer run() throws Exception {
                    int count = 0;
                    long expireTimeline = now - expireDuration;
                    ArrayList<SyncRecord> records = readRecords();
                    for (SyncRecord record : records) {
                        SyncData data = record.data;
                        if (data.getDataType() == dataType && data.getTime() < expireTimeline) {
                            if (deleteFile(record.file)) {
                                count++;
                            }
                        }
                    }
                    return count;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
            return 0;
        }
    }

    public int queryTotalCount(final DataType dataType) {
        return queryTotalCount(new DataType[]{dataType});
    }

    public int queryTotalCount(final DataType[] list) {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<Integer>() {
                @Override
                public Integer run() throws Exception {
                    return filterByType(readRecords(), list).size();
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
            return 0;
        }
    }

    public void deleteOldestData(final DataType type, final int limit) {
        deleteOldestData(new DataType[]{type}, limit);
    }

    public void deleteOldestData(final int limit) {
        deleteOldestData((DataType[]) null, limit);
    }

    public void deleteOldestData(final DataType[] list, final int limit) {
        if (limit <= 0) return;
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    ArrayList<SyncRecord> records = filterByType(readRecords(), list);
                    Collections.sort(records, new Comparator<SyncRecord>() {
                        @Override
                        public int compare(SyncRecord o1, SyncRecord o2) {
                            return Long.compare(o1.data.getTime(), o2.data.getTime());
                        }
                    });
                    int count = Math.min(limit, records.size());
                    for (int i = 0; i < count; i++) {
                        deleteFile(records.get(i).file);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void delete(final List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() {
                    for (Long id : ids) {
                        if (id != null) {
                            deleteFile(getRecordFile(id));
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public void deleteAll() {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() {
                    File[] files = paths.getSyncDir().listFiles();
                    if (files != null) {
                        for (File file : files) {
                            deleteFile(file);
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public long size() {
        return directorySize(paths.getSyncDir());
    }

    public boolean isEmpty() {
        return queryRecords(1, null, true).isEmpty();
    }

    public void insertMigratedData(final List<SyncData> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        replaceAll(list);
    }

    public void replaceAll(final List<SyncData> list) {
        try {
            lock.withLock(new FTFileLock.LockedOperation<Void>() {
                @Override
                public Void run() throws Exception {
                    paths.ensureReady();
                    deleteAllFiles();
                    long nextId = 1;
                    if (list != null) {
                        for (SyncData data : list) {
                            if (data.getId() <= 0) {
                                data.setId(nextId);
                            }
                            nextId = Math.max(nextId, data.getId() + 1);
                            writeRecord(getRecordFile(data.getId()), data);
                        }
                    }
                    writeNextId(nextId);
                    return null;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "replaceAll failed: " + e.getMessage());
        }
    }

    private List<SyncData> queryRecords(final int limit, final DataType[] types, final boolean asc) {
        try {
            return lock.withLock(new FTFileLock.LockedOperation<List<SyncData>>() {
                @Override
                public List<SyncData> run() throws Exception {
                    ArrayList<SyncRecord> records = filterByType(readRecords(), types);
                    Collections.sort(records, new Comparator<SyncRecord>() {
                        @Override
                        public int compare(SyncRecord o1, SyncRecord o2) {
                            int result = Long.compare(o1.data.getId(), o2.data.getId());
                            return asc ? result : -result;
                        }
                    });
                    ArrayList<SyncData> result = new ArrayList<>();
                    for (SyncRecord record : records) {
                        result.add(record.data);
                        if (limit > 0 && result.size() >= limit) {
                            break;
                        }
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "queryRecords failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private ArrayList<SyncRecord> filterByType(ArrayList<SyncRecord> records, DataType[] types) {
        if (types == null || types.length == 0) {
            return records;
        }
        Set<DataType> typeSet = new HashSet<>();
        Collections.addAll(typeSet, types);
        ArrayList<SyncRecord> filtered = new ArrayList<>();
        for (SyncRecord record : records) {
            if (typeSet.contains(record.data.getDataType())) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    private SyncRecord findRecordByUuid(String uuid) throws IOException {
        ArrayList<SyncRecord> records = readRecords();
        for (SyncRecord record : records) {
            if (uuid.equals(record.data.getUuid())) {
                return record;
            }
        }
        return null;
    }

    private ArrayList<SyncRecord> readRecords() throws IOException {
        paths.ensureReady();
        ArrayList<SyncRecord> records = new ArrayList<>();
        File[] files = paths.getSyncDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(FILE_SUFFIX);
            }
        });
        if (files == null) {
            return records;
        }
        for (File file : files) {
            SyncData data = readRecord(file);
            if (data != null) {
                records.add(new SyncRecord(file, data));
            }
        }
        return records;
    }

    private SyncData readRecord(File file) throws IOException {
        try {
            JSONObject json = new JSONObject(FTAtomicFileHelper.readUtf8(file));
            DataType dataType = findDataType(json.optString(FTSQL.RECORD_COLUMN_DATA_TYPE));
            if (dataType == null) {
                LogUtils.e(TAG, "Unknown sync data type in file: " + file.getAbsolutePath());
                return null;
            }
            SyncData data = new SyncData(dataType);
            data.setId(json.optLong(FTSQL.RECORD_COLUMN_ID));
            data.setTime(json.optLong(FTSQL.RECORD_COLUMN_TM));
            data.setUuid(json.optString(FTSQL.RECORD_COLUMN_DATA_UUID, null));
            data.setDataString(json.optString(FTSQL.RECORD_COLUMN_DATA, null));
            return data;
        } catch (JSONException e) {
            LogUtils.e(TAG, "Failed to parse sync file: " + file.getAbsolutePath());
            return null;
        }
    }

    private void writeRecord(File file, SyncData data) throws IOException, JSONException {
        JSONObject json = new JSONObject();
        json.put(FTSQL.RECORD_COLUMN_ID, data.getId());
        json.put(FTSQL.RECORD_COLUMN_TM, data.getTime());
        json.put(FTSQL.RECORD_COLUMN_DATA_UUID, data.getUuid());
        json.put(FTSQL.RECORD_COLUMN_DATA, data.getDataString());
        json.put(FTSQL.RECORD_COLUMN_DATA_TYPE, data.getDataType().getValue());
        FTAtomicFileHelper.writeUtf8(file, json.toString());
    }

    private DataType findDataType(String value) {
        for (DataType type : DataType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }

    private File getRecordFile(long id) {
        return new File(paths.getSyncDir(), id + FILE_SUFFIX);
    }

    private File getSequenceFile() {
        return new File(paths.getSyncDir(), SEQUENCE_FILE_NAME);
    }

    private long readNextId() throws IOException {
        File file = getSequenceFile();
        if (!file.exists()) {
            return findMaxId() + 1;
        }
        String content = FTAtomicFileHelper.readUtf8(file);
        if (content.length() == 0) {
            return 1;
        }
        try {
            return Long.parseLong(content);
        } catch (NumberFormatException e) {
            return findMaxId() + 1;
        }
    }

    private void writeNextId(long nextId) throws IOException {
        FTAtomicFileHelper.writeUtf8(getSequenceFile(), String.valueOf(nextId));
    }

    private long findMaxId() throws IOException {
        long maxId = 0;
        ArrayList<SyncRecord> records = readRecords();
        for (SyncRecord record : records) {
            maxId = Math.max(maxId, record.data.getId());
        }
        return maxId;
    }

    private boolean deleteFile(File file) {
        return !file.exists() || file.delete();
    }

    private void deleteAllFiles() {
        File[] files = paths.getSyncDir().listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            deleteFile(file);
        }
    }

    private long directorySize(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }
        if (dir.isFile()) {
            return dir.length();
        }
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += directorySize(file);
            }
        }
        return size;
    }

    private static class SyncRecord {
        final File file;
        final SyncData data;

        SyncRecord(File file, SyncData data) {
            this.file = file;
            this.data = data;
        }
    }
}

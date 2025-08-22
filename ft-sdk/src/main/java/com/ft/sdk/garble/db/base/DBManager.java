package com.ft.sdk.garble.db.base;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ft.sdk.garble.threadpool.DBScheduleThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BY huangDianHua
 * DATE:2019-12-02 11:23
 * Description: Database management, get database objects and data shutdown
 * <p>
 * Improved version: Uses granular locking strategy and better connection management
 * API 21 compatible version
 */
public abstract class DBManager {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "DBManager";
    private SQLiteOpenHelper databaseHelper;
    private static final int TRANSACTION_LIMIT_COUNT = 10;

    // Use separate locks for different operations to reduce contention
    private final Object writeLock = new Object(); // For write operations
    private final Object dbLock = new Object();    // For database lifecycle operations

    // Connection pool management
    private final ConcurrentHashMap<Thread, SQLiteDatabase> activeConnections = new ConcurrentHashMap<>();
    private final AtomicInteger openCounter = new AtomicInteger(0);
    private final AtomicLong lastUsedTime = new AtomicLong(0);
    private static final long IDLE_TIMEOUT = 20_000;// 20 seconds
    private static final long DB_SIZE_CHECK_INTERVAL = 10_000; // Database size check interval: 10 seconds
    private long lastDbSizeCheckTime = 0;

    private ScheduledFuture<?> pendingCloseTask;

    // Database size cache
    private volatile long pageSize = 0;

    protected abstract SQLiteOpenHelper initDataBaseHelper();

    SQLiteOpenHelper getDataBaseHelper() {
        if (databaseHelper == null) {
            synchronized (dbLock) {
                if (databaseHelper == null) {
                    databaseHelper = initDataBaseHelper();
                }
            }
        }
        return databaseHelper;
    }

    /**
     * Get database connection with connection pooling
     */
    private SQLiteDatabase getDatabaseConnection(boolean write) {
        Thread currentThread = Thread.currentThread();

        // Check if current thread already has a connection
        SQLiteDatabase existingConnection = activeConnections.get(currentThread);
        if (existingConnection != null && existingConnection.isOpen()) {
            return existingConnection;
        }

        // Create new connection
        SQLiteOpenHelper helper = getDataBaseHelper();
        SQLiteDatabase db = write ? helper.getWritableDatabase() : helper.getReadableDatabase();

        if (db != null && db.isOpen()) {
            // Store connection for current thread
            activeConnections.put(currentThread, db);
        }

        return db;
    }

    /**
     * Release database connection for current thread
     */
    private void releaseConnection() {
        try {
            Thread currentThread = Thread.currentThread();
            activeConnections.remove(currentThread);
        } catch (Exception e) {
            LogUtils.e(TAG, "Error releasing connection: " + e.getMessage());
        }
    }

    /**
     * Synchronization lock to make database operations thread-safe
     *
     * @param write
     * @param callBack
     */
    public void getDB(boolean write, DataBaseCallBack callBack) {
        getDB(write, 1, callBack);
    }

    /**
     * Improved version: Uses granular locking strategy to reduce contention
     *
     * @param write
     * @param operationCount Whether to enable transactions
     * @param callback
     */
    public void getDB(boolean write, int operationCount, DataBaseCallBack callback) {
        SQLiteDatabase db = null;
        try {
            // Get database connection
            db = getDatabaseConnection(write);

            if (db != null && db.isOpen()) {
                acquire();

                // Use different locking strategies based on operation type
                if (write) {
                    // Write operations use write lock
                    synchronized (writeLock) {
                        executeWriteOperation(db, operationCount, callback);
                    }
                } else {
                    // Read operations use read lock (allows concurrent reads)
                    executeReadOperation(db, callback);
                }

                // Optimize database size check frequency
                if (write && shouldCheckDatabaseSize()) {
                    checkDatabaseSize(db);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Database operation failed: " + e.getMessage());
        } finally {
            tryToClose();
            releaseConnection();
        }
    }

    /**
     * Execute write operation with transaction support
     */
    private void executeWriteOperation(SQLiteDatabase db, int operationCount, DataBaseCallBack callback) {
        boolean useTransaction = operationCount > TRANSACTION_LIMIT_COUNT;

        if (useTransaction && !db.inTransaction()) {
            db.beginTransaction();
            try {
                callback.run(db);
                if (db.inTransaction()) {
                    db.setTransactionSuccessful();
                }
            } finally {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
            }
        } else {
            callback.run(db);
        }
    }

    /**
     * Execute read operation
     */
    private void executeReadOperation(SQLiteDatabase db, DataBaseCallBack callback) {
        callback.run(db);
    }

    /**
     * Determine if database size check is needed
     */
    private boolean shouldCheckDatabaseSize() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDbSizeCheckTime > DB_SIZE_CHECK_INTERVAL) {
            lastDbSizeCheckTime = currentTime;
            return true;
        }
        return false;
    }

    /**
     * Calculate the current db size by getting page_size * page_count through PRAGMA
     *
     * @param db
     */
    private void checkDatabaseSize(SQLiteDatabase db) {
        if (db != null) {
            // Get database file size
//            File dbFile = new File(db.getPath());
            if (enableDBSizeLimit()) {

                if (pageSize <= 0) {
                    Cursor cursor = db.rawQuery("PRAGMA page_size;", null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            pageSize = cursor.getInt(0);
                        }
                        cursor.close();
                    }

                }
                long pageCount = 0;
                Cursor cursor = db.rawQuery("PRAGMA page_count;", null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        pageCount = cursor.getInt(0);
                    }
                    cursor.close();
                }
                long fileSize = pageCount * pageSize; // File size (bytes)
                // If limit is exceeded, perform cleanup operation
                onDBSizeCacheChange(db, fileSize);
            }
        }
    }

    protected abstract boolean enableDBSizeLimit();

    protected abstract void onDBSizeCacheChange(SQLiteDatabase db, long reachLimit);

    /**
     * Close db
     */
    private void closeDB() {
        synchronized (dbLock) {
            if (databaseHelper != null) {
                databaseHelper.close();
                LogUtils.d(TAG, "DB close");
            }
        }
    }

    /**
     * Establish db connection
     */
    public void acquire() {
        openCounter.incrementAndGet();
        lastUsedTime.set(System.currentTimeMillis());
        cancelPendingClose();

        if (openCounter.get() > 10) {
            LogUtils.w(TAG, "High connection count: " + openCounter.get() +
                    ", Thread: " + Thread.currentThread().getName());
        }
    }

    /**
     * Periodically monitor and close database
     */
    private void scheduleCloseIfIdle() {
        cancelPendingClose();
        if (openCounter.get() == 0) {
            pendingCloseTask = DBScheduleThreadPool.get().schedule(new Runnable() {
                @Override
                public void run() {
                    if (openCounter.get() == 0 && (System.currentTimeMillis() - lastUsedTime.get()) >= IDLE_TIMEOUT) {
                        closeDB();
                    }
                }
            }, IDLE_TIMEOUT);
        }
    }

    /**
     * Cancel pending task
     */
    private void cancelPendingClose() {
        if (pendingCloseTask != null) {
            pendingCloseTask.cancel(false);
            pendingCloseTask = null;
        }
    }

    /**
     * Try to close database
     */
    public void tryToClose() {
        if (DBScheduleThreadPool.get().poolRunning()) {
            openCounter.set(Math.max(0, openCounter.get() - 1));
            lastUsedTime.set(System.currentTimeMillis());
            scheduleCloseIfIdle();
        }
    }

    /**
     * Close and release database file objects
     */
    protected void shutDown() {
        synchronized (dbLock) {
            cancelPendingClose();

            // Clear all active connections
            activeConnections.clear();

            DBScheduleThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    closeDB();
                    databaseHelper = null;
                    DBScheduleThreadPool.get().shutDown();
                }
            });
        }
    }

    /**
     * Get current connection pool statistics
     */
    public String getConnectionStats() {
        return String.format(
                "Connection Stats - Active: %d, Total: %d, Thread: %s",
                activeConnections.size(),
                openCounter.get(),
                Thread.currentThread().getName()
        );
    }
}

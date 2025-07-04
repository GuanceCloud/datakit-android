package com.ft.sdk.garble.db.base;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ft.sdk.garble.threadpool.DBScheduleThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.ScheduledFuture;

/**
 * BY huangDianHua
 * DATE:2019-12-02 11:23
 * Description: Database management, get database objects and data shutdown
 */
public abstract class DBManager {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "DBManager";
    private SQLiteOpenHelper databaseHelper;
    private static final int TRANSACTION_LIMIT_COUNT = 10;
    private final Object lock = new Object();
    private int openCounter = 0;
    private long lastUsedTime = 0; // Last active time
    private static final long IDLE_TIMEOUT = 20_000;// 20 seconds
    private ScheduledFuture<?> pendingCloseTask;

    protected abstract SQLiteOpenHelper initDataBaseHelper();

    SQLiteOpenHelper getDataBaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = initDataBaseHelper();
        }
        return databaseHelper;
    }

    /**
     * Synchronization lock to make database operations thread-safe
     *
     * @param write
     * @param callBack
     */
    protected void getDB(boolean write, DataBaseCallBack callBack) {
        getDB(write, 1, callBack);
    }

    /**
     * Synchronization lock to make database operations thread-safe
     *
     * @param write
     * @param operationCount Whether to enable transactions
     * @param callback
     */
    protected void getDB(boolean write, int operationCount, DataBaseCallBack callback) {

        SQLiteDatabase db = null;
        SQLiteOpenHelper helper;
        try {
            synchronized (lock) {
                helper = getDataBaseHelper();
            }
            db = write ? helper.getWritableDatabase() : helper.getReadableDatabase();
            if (db.isOpen()) {
                acquire();
                // Automatic judgment: use transactions when write operations and operation count is greater than threshold (default 10)
                boolean useTransaction = write && operationCount > TRANSACTION_LIMIT_COUNT;
                if (useTransaction && !db.inTransaction()) {
                    db.beginTransaction();
                    try {
                        synchronized (lock) {
                            callback.run(db);
                        }
                        if (db.inTransaction()) {
                            db.setTransactionSuccessful();
                        }
                    } finally {
                        if (db.inTransaction()) {
                            db.endTransaction();
                        }
                    }
                } else {
                    if (write) {
                        synchronized (lock) {
                            callback.run(db);
                        }
                    } else {
                        callback.run(db);
                    }
                }
                if (write) {
                    checkDatabaseSize(db);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        } finally {
            tryToClose();
        }
    }

    private long pageSize = 0;


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

    /**
     * Whether to enable db limit
     *
     * @return
     */
    protected abstract boolean enableDBSizeLimit();


    /**
     * Notify db to calculate size
     *
     * @param db
     * @param reachLimit
     */
    protected abstract void onDBSizeCacheChange(SQLiteDatabase db, long reachLimit);

    /**
     * Close db
     */
    private void closeDB() {
        synchronized (lock) {
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
        synchronized (lock) {
            openCounter++;
            lastUsedTime = System.currentTimeMillis();
            cancelPendingClose();
        }
    }

    /**
     * Periodically monitor and close database
     */
    private void scheduleCloseIfIdle() {
        cancelPendingClose();
        if (openCounter == 0) {
            pendingCloseTask = DBScheduleThreadPool.get().schedule(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (openCounter == 0 && (System.currentTimeMillis() - lastUsedTime) >= IDLE_TIMEOUT) {
                            closeDB();
                        }
                    }
                }

            }, IDLE_TIMEOUT);
        }
        // For example, close after 10 seconds of no operation
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
        synchronized (lock) {
            if (DBScheduleThreadPool.get().poolRunning()) {
                openCounter = Math.max(0, openCounter - 1);
                lastUsedTime = System.currentTimeMillis();
                scheduleCloseIfIdle();
            }
        }
    }

    /**
     * Close and release database file objects
     */
    protected void shutDown() {
        synchronized (lock) {
            cancelPendingClose();
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
}

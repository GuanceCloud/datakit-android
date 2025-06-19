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
 * Description:数据库管理，获取数据库对象以及数据关闭
 */
public abstract class DBManager {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "DBManager";
    private SQLiteOpenHelper databaseHelper;
    private static final int TRANSACTION_LIMIT_COUNT = 10;
    private final Object lock = new Object();
    private int openCounter = 0;
    private long lastUsedTime = 0; // 最后活跃时间
    private static final long IDLE_TIMEOUT = 20_000;// 20秒
    private ScheduledFuture<?> pendingCloseTask;

    protected abstract SQLiteOpenHelper initDataBaseHelper();

    SQLiteOpenHelper getDataBaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = initDataBaseHelper();
        }
        return databaseHelper;
    }

    /**
     * 同步锁，使数据库操作线程安全
     *
     * @param write
     * @param callBack
     */
    protected void getDB(boolean write, DataBaseCallBack callBack) {
        getDB(write, 1, callBack);
    }

    /**
     * 同步锁，使数据库操作线程安全
     *
     * @param write
     * @param operationCount 是否开启事务
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
                // 自动判断：写操作且操作数量大于阈值(默认10)时使用事务
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
     * 通过 PRAGMA 获取 page_size *page_count 来计算获取当前 db 的 大小
     *
     * @param db
     */
    private void checkDatabaseSize(SQLiteDatabase db) {
        if (db != null) {
            // 获取数据库文件的大小
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
                long fileSize = pageCount * pageSize; // 文件大小（字节）
                // 如果超过限制，执行清理操作
                onDBSizeCacheChange(db, fileSize);
            }
        }
    }

    /**
     * 是否开启 db 限制
     *
     * @return
     */
    protected abstract boolean enableDBSizeLimit();


    /**
     * 通知 db 计算大小
     *
     * @param db
     * @param reachLimit
     */
    protected abstract void onDBSizeCacheChange(SQLiteDatabase db, long reachLimit);

    /**
     * 关闭 db
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
     * 建立 db 链接
     */
    public void acquire() {
        synchronized (lock) {
            openCounter++;
            lastUsedTime = System.currentTimeMillis();
            cancelPendingClose();
        }
    }

    /**
     * 周期监测关闭数据库
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
        // 例如 10 秒后无操作就关闭
    }

    /**
     * 取消等待任务
     */
    private void cancelPendingClose() {
        if (pendingCloseTask != null) {
            pendingCloseTask.cancel(false);
            pendingCloseTask = null;
        }
    }

    /**
     * 尝试关闭数据库
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
     * 关闭并释放数据库文件对象
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

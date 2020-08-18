
package com.ft.sdk.garble.manager;

import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.FTTrackInner;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.ThreadPoolUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * author: huangDianHua
 * time: 2020/7/22 11:16:58
 * description: 本地打印日志同步管理类
 */
public class TrackLogManager {
    private static TrackLogManager instance;
    private List<LogBean> logBeanList = new CopyOnWriteArrayList<>();
    private LinkedBlockingQueue<LogBean> logQueue = new LinkedBlockingQueue<>();
    private volatile boolean isRunning;

    private TrackLogManager() {
    }

    public static TrackLogManager get() {
        synchronized (TrackLogManager.class) {
            if (instance == null) {
                instance = new TrackLogManager();
            }
            return instance;
        }
    }

    public synchronized void trackLog(LogBean logBean) {
        //防止内存中队列容量超过一定限制，这里同样使用同步丢弃策略
        if(logQueue.size() >= Constants.MAX_DB_CACHE_NUM){
            switch (FTDBCachePolicy.get().getLogCacheDiscardStrategy()){
                case DISCARD:
                    break;
                case DISCARD_OLDEST:
                    logQueue.poll();
                    logQueue.add(logBean);
                    break;
                default:
                    logQueue.add(logBean);
            }
        }else{
            logQueue.add(logBean);
        }
        rotationSync();
    }

    private void rotationSync() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        FutureTask<Boolean> futureTask = new FutureTask(() -> {
            try {
                //当队列中有数据时，不断执行取数据操作
                LogBean logBean;
                //take 为阻塞方法，所以该线程会一直在运行中
                while ((logBean = logQueue.take()) != null) {
                    isRunning = true;
                    logBeanList.add(logBean);//取出数据放到集合中
                    if (logBeanList.size() >= 5 || logQueue.peek() == null) {//当取出的数据大于等于5条或者没有下一条数据时执行插入数据库操作
                        FTTrackInner.getInstance().logBackgroundSync(logBeanList);
                        logBeanList.clear();//插入完成后执行清除集合操作
                    }
                }
            } catch (Exception e) {
            } finally {
                isRunning = false;
            }
            return true;
        });
        ThreadPoolUtils.get().execute(futureTask);
    }
}

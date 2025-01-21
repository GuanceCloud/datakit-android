
package com.ft.sdk;

import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.bean.BaseContentBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.threadpool.LogConsumerThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * author: huangDianHua
 * time: 2020/7/22 11:16:58
 * description: 本地打印日志同步管理类
 */
public class TrackLogManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "TrackLogManager";
    private static TrackLogManager instance;
    private final List<BaseContentBean> logBeanList = new CopyOnWriteArrayList<>();
    /**
     * log 输入队列
     */
    private final LinkedBlockingQueue<LogBean> logQueue = new LinkedBlockingQueue<>();
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

    /**
     * @param logBean {@link LogBean} 发送日志数据
     */
    public synchronized void trackLog(LogBean logBean, boolean isSilence) {
        FTLoggerConfig config = FTLoggerConfigManager.get().getConfig();
        if (config == null) return;
        if (Utils.enableTraceSamplingRate(config.getSamplingRate())) {
            HashMap<String, Object> rumTags = null;
            if (config.isEnableLinkRumData()) {
                rumTags = FTRUMConfigManager.get().getRUMPublicDynamicTags(true);
                FTRUMInnerManager.get().attachRUMRelative(rumTags, false);
                logBean.appendTags(rumTags);
            }
        } else {
            LogUtils.w(TAG, "根据 FTLogConfig SampleRate 计算，将被丢弃=>" + logBean.getContent());
            return;
        }
        //防止内存中队列容量超过一定限制，这里同样使用同步丢弃策略
        if (logQueue.size() >= FTDBCachePolicy.get().getLogLimitCount()) {
            switch (FTDBCachePolicy.get().getLogCacheDiscardStrategy()) {
                case DISCARD:
                    break;
                case DISCARD_OLDEST:
                    logQueue.poll();
                    logQueue.add(logBean);
                    break;
                default:
                    logQueue.add(logBean);
            }
        } else {
            logQueue.add(logBean);
        }
        rotationSync(isSilence);
    }

    private void rotationSync(boolean isSilence) {
        if (isRunning) {
            return;
        }
        isRunning = true;
        LogConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //当队列中有数据时，不断执行取数据操作
                    LogBean logBean;
                    //take 为阻塞方法，所以该线程会一直在运行中
                    while ((logBean = logQueue.take()) != null) {
                        isRunning = true;
                        logBeanList.add(logBean);//取出数据放到集合中
                        if (logBeanList.size() >= 20 || logQueue.peek() == null) {//当取出的数据大于等于20条或者没有下一条数据时执行插入数据库操作
                            FTTrackInner.getInstance().batchLogBeanSync(logBeanList, isSilence);
                            logBeanList.clear();//插入完成后执行清除集合操作
                        }
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                } finally {
                    isRunning = false;
                }
            }
        });
    }
}

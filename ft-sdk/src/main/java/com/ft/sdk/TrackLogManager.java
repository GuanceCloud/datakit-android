
package com.ft.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.bean.BaseContentBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * author: huangDianHua
 * time: 2020/7/22 11:16:58
 * description: 本地打印日志同步管理类
 */
public class TrackLogManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "TrackLogManager";

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            rotationSync(msg.arg1 == 1, false);
        }
    };
    private static final int MSG_FINISH_FLUSH = 1;
    private static TrackLogManager instance;
    private final List<BaseContentBean> logBeanList = new CopyOnWriteArrayList<>();
    //    /**
//     * log 输入队列
//     */
//    private final LinkedBlockingQueue<LogBean> logQueue = new LinkedBlockingQueue<>();
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
    public void trackLog(LogBean logBean, boolean isSilence) {
        synchronized (logBeanList) {
            //防止内存中队列容量超过一定限制，这里同样使用同步丢弃策略
            if (logBeanList.size() >= FTDBCachePolicy.get().getLimitCount()) {
                switch (FTDBCachePolicy.get().getLogCacheDiscardStrategy()) {
                    case DISCARD:
                        break;
                    case DISCARD_OLDEST:
                        logBeanList.remove(0);
                        logBeanList.add(logBean);
                        break;
                    default:
                        logBeanList.add(logBean);
                }
            } else {
                logBeanList.add(logBean);
            }
            rotationSync(isSilence, true);

        }

    }


    private void rotationSync(boolean isSilence, boolean wait) {
        //当队列中有数据时，不断执行取数据操作
        if (logBeanList.size() >= 20 || !wait) {
            try {
                FTTrackInner.getInstance().batchLogBeanBackground(logBeanList, false);
                logBeanList.clear();//插入完成后执行清除集合操作

            } catch (Exception e) {
                LogUtils.d(TAG, Log.getStackTraceString(e));
            }
        }

        if (wait) {
            handler.removeMessages(MSG_FINISH_FLUSH);
            Message message = new Message();
            message.what = MSG_FINISH_FLUSH;
            message.arg1 = isSilence ? 1 : 0;
            handler.sendMessageDelayed(message, 300);

        }
    }
}

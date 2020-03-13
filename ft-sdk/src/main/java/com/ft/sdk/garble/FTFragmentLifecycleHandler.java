package com.ft.sdk.garble;

import com.ft.sdk.garble.utils.LogUtils;

/**
 * create: by huangDianHua
 * time: 2020/3/13 09:33:49
 * description: Fragment 生命周期处理类
 */
public class FTFragmentLifecycleHandler {

    private static FTFragmentLifecycleHandler instance;
    private FTFragmentLifecycleHandler(){

    }
    public static FTFragmentLifecycleHandler getInstance(){
        synchronized (FTFragmentLifecycleHandler.class){
            if(instance == null){
                instance = new FTFragmentLifecycleHandler();
            }
            return instance;
        }
    }
    /**
     * Fragment 显示
     * @param fragment
     */
    public void fragmentShow(Class fragment,Class activity){
        //LogUtils.d("Fragment[\nshow=====>fragment:"+fragment.getSimpleName()+",activity:"+activity.getSimpleName());
    }

    /**
     * Fragment 隐藏
     * @param fragment
     */
    public void fragmentHidden(Class fragment,Class activity){
        //LogUtils.d("Fragment[\nhidden=====>fragment:"+fragment.getSimpleName()+",activity:"+activity.getSimpleName());
    }

}

package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.Utils;

import java.util.Map;

/**
 * create: by huangDianHua
 * time: 2020/5/18 14:07:49
 * description:页面和事件别名配置类
 */
public class FTAliasConfig {
    private static FTAliasConfig instance;
    //页面别名对应 map
    private Map<String,String> pageAliasMap;
    //事件别名对应 map
    private Map<String,String> eventAliasMap;

    private boolean flowChartAlias = false;
    private FTAliasConfig(){}

    public static FTAliasConfig get(){
        if(instance == null){
            instance = new FTAliasConfig();
        }
        return instance;
    }

    public void release(){
        pageAliasMap = null;
        eventAliasMap = null;
        instance = null;
    }

    public void initParams(FTSDKConfig ftsdkConfig){
        this.pageAliasMap = ftsdkConfig.getPageDescMap();
        this.eventAliasMap = ftsdkConfig.getVtpDescMap();
        this.flowChartAlias = ftsdkConfig.isFlowShowDesc();
    }

    public String getPageAlias(String page){
        if(pageAliasMap == null){
            return page;
        }
        String pageDesc = pageAliasMap.get(page);
        if(Utils.isNullOrEmpty(pageDesc)){
            return page;
        }else{
            return pageDesc;
        }
    }

    public String getEventAlias(String vtp){
        if(eventAliasMap == null){
            return vtp;
        }
        String vtpDesc = eventAliasMap.get(vtp);
        if(Utils.isNullOrEmpty(vtpDesc)){
            return vtp;
        }else{
            return vtpDesc;
        }
    }

    public boolean isFlowChartAlias() {
        return flowChartAlias;
    }
}

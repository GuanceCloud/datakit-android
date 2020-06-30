package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.Constants;
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

    @Deprecated
    private boolean flowChartAlias = false;
    private boolean pageVtpAlias = false;
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
        this.flowChartAlias = ftsdkConfig.isFlowChartDescEnabled();
        this.pageVtpAlias = ftsdkConfig.isPageVtpDescEnabled();
    }

    /**
     * 返回流程图描述
     * @param page
     * @return
     */
    public String getFlowChartDesc(String page){
        if(!flowChartAlias){
            return page;
        }
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

    /**
     * 返回视图树描述
     * @param vtp
     * @return
     */
    public String getVtpDesc(String vtp){
        if(!pageVtpAlias){
            return Constants.UNKNOWN;
        }
        if(eventAliasMap == null){
            return Constants.UNKNOWN;
        }
        String vtpDesc = eventAliasMap.get(vtp);
        if(Utils.isNullOrEmpty(vtpDesc)){
            return Constants.UNKNOWN;
        }else{
            return vtpDesc;
        }
    }

    /**
     * 返回页面描述
     * @param page
     * @return
     */
    public String getPageDesc(String page){
        if(!pageVtpAlias){
            return Constants.UNKNOWN;
        }
        if(pageAliasMap == null){
            return Constants.UNKNOWN;
        }
        String pageDesc = pageAliasMap.get(page);
        if(Utils.isNullOrEmpty(pageDesc)){
            return Constants.UNKNOWN;
        }else{
            return pageDesc;
        }
    }
}

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
    }

    public String getPageAlias(String page){
        if(pageAliasMap == null){
            return Constants.UNKNOWN;
        }
        page = pageAliasMap.get(page);
        if(Utils.isNullOrEmpty(page)){
            return Constants.UNKNOWN;
        }else{
            return page;
        }
    }

    public String getEventAlias(String vtp){
        if(eventAliasMap == null){
            return Constants.UNKNOWN;
        }
        vtp = eventAliasMap.get(vtp);
        if(Utils.isNullOrEmpty(vtp)){
            return Constants.UNKNOWN;
        }else{
            return vtp;
        }
    }
}

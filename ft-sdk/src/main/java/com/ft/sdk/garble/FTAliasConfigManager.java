package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.Utils;

import java.util.Map;

/**
 * create: by huangDianHua
 * time: 2020/5/18 14:07:49
 * description:页面和事件别名配置类
 */
public class FTAliasConfigManager {
    private static FTAliasConfigManager instance;
    //页面别名对应 map
    private Map<String, String> pageAliasMap;
    //事件别名对应 map
    private Map<String, String> eventAliasMap;

    private boolean pageVtpAlias = false;

    private FTAliasConfigManager() {
    }

    public static FTAliasConfigManager get() {
        if (instance == null) {
            instance = new FTAliasConfigManager();
        }
        return instance;
    }

    public void release() {
        pageAliasMap = null;
        eventAliasMap = null;
        instance = null;
    }

    public void initParams(FTSDKConfig ftsdkConfig) {
//        this.pageAliasMap = ftsdkConfig.getPageDescMap();
//        this.eventAliasMap = ftsdkConfig.getVtpDescMap();
//        this.pageVtpAlias = ftsdkConfig.isPageVtpDescEnabled();
//        if (pageVtpAlias) {
//            ThreadPoolUtils.get().execute(() -> {
//                Map<String, String>[] maps = DescXmlParse.readXmlBySAX();
//                if (maps != null && maps.length == 2) {
//                    if (pageAliasMap != null) {
//                        pageAliasMap.putAll(maps[0]);
//                    } else {
//                        pageAliasMap = maps[0];
//                    }
//                    if (eventAliasMap != null) {
//                        eventAliasMap.putAll(maps[1]);
//                    } else {
//                        eventAliasMap = maps[1];
//                    }
//                }
//            });
//        }
    }


    /**
     * 返回视图树描述
     *
     * @param vtp
     * @return
     */
    public String getVtpDesc(String vtp) {
        if (!pageVtpAlias) {
            return "";
        }
        if (eventAliasMap == null) {
            return "";
        }
        String vtpDesc = eventAliasMap.get(vtp);
        if (Utils.isNullOrEmpty(vtpDesc)) {
            return "";
        } else {
            return vtpDesc;
        }
    }

    /**
     * 返回页面描述
     *
     * @param page
     * @return
     */
    public String getPageDesc(String page) {
        if (!pageVtpAlias) {
            return "";
        }
        if (pageAliasMap == null) {
            return "";
        }
        String pageDesc = pageAliasMap.get(page);
        if (Utils.isNullOrEmpty(pageDesc)) {
            return "";
        } else {
            return pageDesc;
        }
    }
}

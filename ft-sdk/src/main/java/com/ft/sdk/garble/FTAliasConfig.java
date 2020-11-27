package com.ft.sdk.garble;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DescXmlParse;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
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
    private Map<String, String> pageAliasMap;
    //事件别名对应 map
    private Map<String, String> eventAliasMap;

    private boolean pageVtpAlias = false;

    private FTAliasConfig() {
    }

    public static FTAliasConfig get() {
        if (instance == null) {
            instance = new FTAliasConfig();
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
            return Constants.UNKNOWN;
        }
        if (eventAliasMap == null) {
            return Constants.UNKNOWN;
        }
        String vtpDesc = eventAliasMap.get(vtp);
        if (Utils.isNullOrEmpty(vtpDesc)) {
            return Constants.UNKNOWN;
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
            return Constants.UNKNOWN;
        }
        if (pageAliasMap == null) {
            return Constants.UNKNOWN;
        }
        String pageDesc = pageAliasMap.get(page);
        if (Utils.isNullOrEmpty(pageDesc)) {
            return Constants.UNKNOWN;
        } else {
            return pageDesc;
        }
    }
}

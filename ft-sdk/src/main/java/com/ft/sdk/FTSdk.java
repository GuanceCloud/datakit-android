package com.ft.sdk;

/**
 * BY huangDianHua
 * DATE:2019-11-29 17:15
 * Description:
 */
public class FTSdk {
    private static FTSdk ftSdk;
    private FTSdk(){}
    public static synchronized FTSdk newBuilder(){
        if (ftSdk == null) {
            ftSdk = new FTSdk();
        }
        return ftSdk;
    }
    private String dbPath;

    public String getDbPath() {
        return dbPath;
    }

    public FTSdk setDbPath(String dbPath){
        this.dbPath = dbPath;
        return this;
    }
}

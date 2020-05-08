package com.ft.sdk.garble.http;

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:42:36
 * description:
 */
public class EngineFactory {
    private static Class<? extends INetEngine> engineClass;

    public static void setEngineClass(Class<? extends INetEngine> engineClass) {
        EngineFactory.engineClass = engineClass;
    }

    public static INetEngine createEngine(){
        if(engineClass != null){
            try {
                return engineClass.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return new NativeNetEngine();
    }
}

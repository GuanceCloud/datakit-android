package com.ft.plugin.garble;


/**
 * BY huangDianHua
 * DATE:2019-12-03 14:20
 * Description:
 */
public class FTSubMethodCell {
    /**
     * 方法所属类
     */
    public String className;
    /**
     * 采集数据的方法名
     */
    public String agentName;
    /**
     * 采集数据的方法描述
     */
    public String agentDesc;

    /**
     * 类型
     */
    public FTMethodType type;

    /**
     * 值
     */
    public int value;

    public boolean itf;

    public FTSubMethodCell(FTMethodType type, String className, String agentName, String agentDesc,boolean itf) {
        this.className = className;
        this.agentName = agentName;
        this.agentDesc = agentDesc;
        this.type = type;
        this.itf = itf;
    }
    public FTSubMethodCell(FTMethodType type,int value) {
        this.type = type;
        this.value = value;
    }
}

package com.ft.plugin.garble;


/**
 * BY huangDianHua
 * DATE:2019-12-03 14:20
 * Description:
 */
public class FTSubMethodCell {
    /**
     * Class to which the method belongs
     */
    public String className;
    /**
     * Method name for data collection
     */
    public String agentName;
    /**
     * Method description for data collection
     */
    public String agentDesc;

    /**
     * Type
     */
    public FTMethodType type;

    /**
     * Value
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
    public FTSubMethodCell(FTMethodType type,String className) {
        this.type = type;
        this.className = className;
    }
    public FTSubMethodCell(FTMethodType type) {
        this.type = type;
    }
}

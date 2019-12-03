package com.ft.plugin.garble;

import java.util.List;

/**
 * BY huangDianHua
 * DATE:2019-12-03 14:20
 * Description:
 */
public class FTMethodCell {
    /**
     * 原方法名
     */
    public String name;
    /**
     * 原方法描述
     */
    public String desc;
    /**
     * 方法所在的接口或类
     */
    public String parent;
    /**
     * 采集数据的方法名
     */
    public String agentName;
    /**
     * 采集数据的方法描述
     */
    public String agentDesc;
    /**
     * 采集数据的方法参数起始索引（ 0：this，1+：普通参数 ）
     */
    public int paramsStart;
    /**
     * 采集数据的方法参数个数
     */
    public int paramsCount;
    /**
     * 参数类型对应的ASM指令，加载不同类型的参数需要不同的指令
     */
    public List<Integer> opcodes;

    FTMethodCell(String name, String desc, String agentName) {
        this.name = name;
        this.desc = desc;
        this.agentName = agentName;
    }

    FTMethodCell(String name, String desc, String parent, String agentName, String agentDesc, int paramsStart, int paramsCount, List<Integer> opcodes) {
        this.name = name;
        this.desc = desc;
        this.parent = parent;
        this.agentName = agentName;
        this.agentDesc = agentDesc;
        this.paramsStart = paramsStart;
        this.paramsCount = paramsCount;
        this.opcodes = opcodes;
    }
}

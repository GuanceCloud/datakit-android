package com.ft.plugin.garble;

/*
 * Created by wangzhuozhou on 2015/08/12.
 * Copyright 2015－2020 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;

/**
 * 本类借鉴修改了来自 Sensors Data 的项目 https://github.com/sensorsdata/sa-sdk-android-plugin2
 * 中的 SensorsAnalyticsMethodCell.groovy 类
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

    public List<FTSubMethodCell> subMethodCellList;

    FTMethodCell(String name, String desc, String agentName) {
        this.name = name;
        this.desc = desc;
        this.agentName = agentName;
    }

    /**
     * 当知道被插桩的类时用该方法
     * @param name
     * @param desc
     * @param agentName
     * @param agentDesc
     * @param subMethodCells
     */
    FTMethodCell(String name, String desc, String agentName, String agentDesc, List<FTSubMethodCell> subMethodCells) {
        this.name = name;
        this.desc = desc;
        this.agentName = agentName;
        this.agentDesc = agentDesc;
        this.subMethodCellList = subMethodCells;
    }

    /**
     * 当知道不知道被插桩的类时用该方法
     * @param name
     * @param desc
     * @param agentName
     * @param agentDesc
     * @param subMethodCells
     */
    FTMethodCell(String name, String desc,String parent, String agentName, String agentDesc, List<FTSubMethodCell> subMethodCells) {
        this.name = name;
        this.desc = desc;
        this.parent = parent;
        this.agentName = agentName;
        this.agentDesc = agentDesc;
        this.subMethodCellList = subMethodCells;
    }


    /**
     * 当插桩的函数参数和被插桩的函数参数一致时用该构造函数
     * @param name
     * @param desc
     * @param parent
     * @param agentName
     * @param agentDesc
     * @param paramsStart
     * @param paramsCount
     * @param opcodes
     */
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

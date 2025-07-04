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
 * This class is modified based on the Sensors Data project <a href="https://github.com/sensorsdata/sa-sdk-android-plugin2">sa-sdk-android-plugin2</a>
 * SensorsAnalyticsMethodCell.groovy class
 */
public class FTMethodCell {
    /**
     * Original method name
     */
    public String name;
    /**
     * Original method description
     */
    public String desc;
    /**
     * Interface or class where the method is located
     */
    public String parent;
    /**
     * Method name for data collection
     */
    public String agentName;
    /**
     * Method description for data collection
     */
    public String agentDesc;
    /**
     * Starting index of data collection method parameters (0: this, 1+: normal parameters)
     */
    public int paramsStart;
    /**
     * Number of data collection method parameters
     */
    public int paramsCount;
    /**
     * ASM instructions corresponding to parameter types, different instructions are needed to load different types of parameters
     */
    public List<Integer> opcodes;

    public List<FTSubMethodCell> subMethodCellList;

    FTMethodCell(String name, String desc, String agentName) {
        this.name = name;
        this.desc = desc;
        this.agentName = agentName;
    }

    /**
     * Use this method when the class to be instrumented is known
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
     * Use this method when the class to be instrumented is not known
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
     * Use this constructor when the instrumented function parameters match the parameters of the function to be instrumented
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

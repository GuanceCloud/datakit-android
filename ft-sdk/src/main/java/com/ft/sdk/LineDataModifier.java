package com.ft.sdk;

import java.util.HashMap;
import java.util.Map;

/**
 * 可以针对某一行进行判断，再决定是否需要替换某一个数值，如果只做某个字段全局替换，请使用 {@link DataModifier}
 */
public interface LineDataModifier {
    /**
     * 修改逻辑，只返回被修改的 key-value 对
     *
     * @param measurement 测量名
     * @param data        合并后的
     * @return 被修改过的键值对（返回 null 或空 map 均为不更改）
     */
    Map<String, Object> modify(String measurement, HashMap<String, Object> data);
}

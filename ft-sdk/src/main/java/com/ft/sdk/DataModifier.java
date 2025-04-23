package com.ft.sdk;

/**
 * 字段替换，适合全局字段替换场景，如果期望逐条分析，实现条数据的替换，请求使用 {@link LineDataModifier }
 * 字段替换性能上 {@link DataModifier} > {@link LineDataModifier }
 */
public interface DataModifier {

    /**
     * @param key   字段名
     * @param value 字段值（原始值）
     * @return 新的值，如果不修改就返回原始值；返回 null 表示不做更改
     */
    Object modify(String key, Object value);
}
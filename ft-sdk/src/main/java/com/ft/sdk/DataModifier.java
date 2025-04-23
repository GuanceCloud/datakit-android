package com.ft.sdk;

public interface DataModifier {

    /**
     * @param key   字段名
     * @param value 字段值（原始值）
     * @return 新的值，如果不修改就返回原始值；返回 null 表示不做更改
     */
    Object modify(String key, Object value);
}
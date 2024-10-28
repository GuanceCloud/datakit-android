package com.ft.sdk;

public enum HttpRequestCompression {
    NONE, // 不使用压缩，默认
    GZIP, // 使用 gzip 压缩
    DEFLATE // 使用 deflate 压缩
}
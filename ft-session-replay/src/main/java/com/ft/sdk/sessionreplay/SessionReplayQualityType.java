package com.ft.sdk.sessionreplay;

/**
 * Session Replay 画质类型
 *
 */
public enum SessionReplayQualityType {
    /**
     * 按照 0.6 倍率缩放
     */
    HIGH(0.6f, 25),
    /**
     * 按照 0.5 倍率缩放，默认值
     */
    MEDIUM(0.5f, 25),
    /**
     * 按照 0.3 倍率缩放
     */
    LOW(0.3f, 25);

    private final float rate;
    private final int compressQuality;

    SessionReplayQualityType(float rate, int quality) {
        this.rate = rate;
        this.compressQuality = quality;
    }

    /**
     * 获取倍率
     * @return
     */
    public float getRate() {
        return rate;
    }

    /**
     * 获取压缩质量
     * @return
     */
    public int getCompressQuality() {
        return compressQuality;
    }
}

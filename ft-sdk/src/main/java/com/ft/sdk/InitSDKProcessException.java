package com.ft.sdk;

/**
 * author: huangDianHua
 * time: 2020/8/3 14:37:43
 * description:
 */
public class InitSDKProcessException extends RuntimeException {
    /**
     * Constructs a {@code NullPointerException} with no detail message.
     */
    public InitSDKProcessException() {
        super();
    }

    /**
     * Constructs a {@code NullPointerException} with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public InitSDKProcessException(String s) {
        super(s);
    }
}

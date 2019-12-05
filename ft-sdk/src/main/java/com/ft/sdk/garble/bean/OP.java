package com.ft.sdk.garble.bean;

/**
 * BY huangDianHua
 * DATE:2019-12-02 14:00
 * Description:
 */
public enum OP {
    LANC("lanc"),CLS("cls"),CLK("clk"),OPEN("open"),LEAVE("leave"),BACK("back");
    public String value;
    OP(String value){
        this.value = value;
    }
}

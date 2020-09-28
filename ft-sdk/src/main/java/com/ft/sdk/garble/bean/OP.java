package com.ft.sdk.garble.bean;

/**
 * BY huangDianHua
 * DATE:2019-12-02 14:00
 * Description:
 */
public enum OP {
    LANC("lanc"), CLK("clk"), CSTM("cstm"), FLOW_CHAT("flow_chat"), OPEN("open"),
    CLS_ACT("cls_act"), OPEN_ACT("opn_act"),
    CLS_FRA("cls_fra"), OPEN_FRA("open_fra"), LOG("log"),
    KEYEVENT("keyevent"), OBJECT("object"), BLOCK("block"),CRASH("crash"),ANR("anr");
    public String value;

    OP(String value) {
        this.value = value;
    }
}

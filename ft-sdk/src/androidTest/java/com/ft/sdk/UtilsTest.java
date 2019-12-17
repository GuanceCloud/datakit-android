package com.ft.sdk;

import com.ft.sdk.garble.utils.Utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * BY huangDianHua
 * DATE:2019-12-16 18:06
 * Description:
 */
public class UtilsTest {

    @Test
    public void isNetworkAvailable() {
        assertTrue(Utils.isNetworkAvailable());
    }

    @Test
    public void contentMD5Encode() {
        assertEquals("M1QEWjl2Ic2SQG8fmM3ikg==", Utils.contentMD5Encode("1122334455"));
    }

    @Test
    public void getHMacSha1() {
        assertEquals("4me5NXJallTGFmZiO3csizbWI90=", Utils.getHMacSha1("screct", "123456"));
    }
}

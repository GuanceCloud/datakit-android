package com.ft.sdk;

import com.ft.sdk.garble.utils.Utils;

import org.junit.Assert;
import org.junit.Test;

public class FlowProductTest {
    @Test
    public void isLegalProduct0(){
        Assert.assertTrue(Utils.isLegalProduct("123wwe-_A"));
    }

    @Test
    public void isLegalProduct1(){
        Assert.assertFalse(Utils.isLegalProduct("1,.23wwe-_A"));
    }

    @Test
    public void isLegalProduct2(){
        Assert.assertFalse(Utils.isLegalProduct("123wwe-_A22wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww"));
    }

    @Test
    public void isLegalProduct3(){
        Assert.assertFalse(Utils.isLegalProduct("123w,,.we-_A22wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww"));
    }

    @Test
    public void isLegalProduct4(){
        Assert.assertFalse(Utils.isLegalProduct("哈哈哈哈哈--/看，"));
    }

    @Test
    public void isLegalProduct5(){
        Assert.assertFalse(Utils.isLegalProduct("123wwe-      00000"));
    }
}

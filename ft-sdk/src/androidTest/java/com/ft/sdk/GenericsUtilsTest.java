package com.ft.sdk;

import com.ft.sdk.garble.utils.GenericsUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * BY huangDianHua
 * DATE:2019-12-18 14:04
 * Description:
 */
public class GenericsUtilsTest {
    class BaseGenerics{

    }

    class SubBaseGenerics extends BaseGenerics{

    }

    interface TestGenerics<T extends BaseGenerics>{

    }

    @Test
    public void getInterfaceClassGenricType(){
        TestGenerics testGenerics = new TestGenerics<SubBaseGenerics>(){};
        assertEquals(SubBaseGenerics.class, GenericsUtils.getInterfaceClassGenricType(testGenerics.getClass()));
    }
}

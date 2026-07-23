package com.ft.sdk.garble.bean;

import org.junit.Assert;
import org.junit.Test;

public class NetworkStateBeanTest {

    @Test
    public void setNetworkNotAvailableClearsNetworkSnapshot() {
        NetworkStateBean bean = new NetworkStateBean();
        bean.setNetworkAvailable(true);
        bean.setNetworkType("wifi");
        bean.setNetworkValidated(true);
        bean.setNetworkDownlinkKbps(1200);
        bean.setNetworkUplinkKbps(300);
        bean.setNetworkSignalStrength(-70);

        bean.setNetworkNotAvailable();

        Assert.assertFalse(bean.isNetworkAvailable());
        Assert.assertEquals("", bean.getNetworkType());
        Assert.assertNull(bean.getNetworkValidated());
        Assert.assertNull(bean.getNetworkDownlinkKbps());
        Assert.assertNull(bean.getNetworkUplinkKbps());
        Assert.assertNull(bean.getNetworkSignalStrength());
    }
}

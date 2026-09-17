package com.ft.sdk;

import org.junit.Assert;
import org.junit.Test;

public class FTRUMConfigDeviceMetricsTest {

    @Test
    public void selectsMultipleMetricsAndSupportsChaining() {
        FTRUMConfig config = new FTRUMConfig();

        Assert.assertSame(config, config.setDeviceMetricsMonitorTypes(
                DeviceMetricsMonitorType.CPU,
                DeviceMetricsMonitorType.MEMORY,
                DeviceMetricsMonitorType.FPS));
        Assert.assertEquals(DeviceMetricsMonitorType.CPU.getValue()
                        | DeviceMetricsMonitorType.MEMORY.getValue()
                        | DeviceMetricsMonitorType.FPS.getValue(),
                config.getDeviceMetricsMonitorType());
        Assert.assertSame(DetectFrequency.DEFAULT, config.getDeviceMetricsDetectFrequency());
    }

    @Test
    public void replacesSelectionAndPreservesConfiguredFrequency() {
        FTRUMConfig config = new FTRUMConfig().setDeviceMetricsMonitorType(
                DeviceMetricsMonitorType.ALL.getValue(), DetectFrequency.RARE);

        config.setDeviceMetricsMonitorTypes(DeviceMetricsMonitorType.CPU);

        Assert.assertEquals(DeviceMetricsMonitorType.CPU.getValue(), config.getDeviceMetricsMonitorType());
        Assert.assertSame(DetectFrequency.RARE, config.getDeviceMetricsDetectFrequency());
    }

    @Test
    public void emptyArgumentsDisableMetricsWithoutChangingFrequency() {
        FTRUMConfig config = new FTRUMConfig().setDeviceMetricsMonitorType(
                DeviceMetricsMonitorType.ALL.getValue(), DetectFrequency.FREQUENT);

        config.setDeviceMetricsMonitorTypes();

        Assert.assertEquals(DeviceMetricsMonitorType.NO_SET, config.getDeviceMetricsMonitorType());
        Assert.assertSame(DetectFrequency.FREQUENT, config.getDeviceMetricsDetectFrequency());
    }

    @Test
    public void duplicateTypesHaveNoAdditionalEffect() {
        FTRUMConfig config = new FTRUMConfig().setDeviceMetricsMonitorTypes(
                DeviceMetricsMonitorType.CPU, DeviceMetricsMonitorType.CPU);

        Assert.assertEquals(DeviceMetricsMonitorType.CPU.getValue(), config.getDeviceMetricsMonitorType());
    }

    @Test
    public void allRetainsExistingBitMaskSemantics() {
        FTRUMConfig config = new FTRUMConfig().setDeviceMetricsMonitorTypes(DeviceMetricsMonitorType.ALL);
        Assert.assertEquals(DeviceMetricsMonitorType.ALL.getValue(), config.getDeviceMetricsMonitorType());

        config.setDeviceMetricsMonitorTypes(DeviceMetricsMonitorType.ALL, DeviceMetricsMonitorType.BATTERY);
        Assert.assertEquals(DeviceMetricsMonitorType.ALL.getValue(), config.getDeviceMetricsMonitorType());

        config.setDeviceMetricsMonitorType(DeviceMetricsMonitorType.MEMORY.getValue());
        Assert.assertEquals(DeviceMetricsMonitorType.MEMORY.getValue(), config.getDeviceMetricsMonitorType());
    }

    @Test
    public void nullArrayIsRejectedWithoutChangingConfiguration() {
        assertInvalidTypesPreserveConfiguration((DeviceMetricsMonitorType[]) null);
    }

    @Test
    public void nullElementIsRejectedWithoutApplyingPartialSelection() {
        assertInvalidTypesPreserveConfiguration(DeviceMetricsMonitorType.CPU, null);
    }

    @Test
    public void frequencyOverloadReplacesBothSettingsAndSupportsChaining() {
        FTRUMConfig config = new FTRUMConfig().setDeviceMetricsMonitorType(
                DeviceMetricsMonitorType.ALL.getValue(), DetectFrequency.FREQUENT);

        Assert.assertSame(config, config.setDeviceMetricsMonitorTypes(DetectFrequency.RARE,
                DeviceMetricsMonitorType.CPU, DeviceMetricsMonitorType.MEMORY));

        Assert.assertEquals(DeviceMetricsMonitorType.CPU.getValue() | DeviceMetricsMonitorType.MEMORY.getValue(),
                config.getDeviceMetricsMonitorType());
        Assert.assertSame(DetectFrequency.RARE, config.getDeviceMetricsDetectFrequency());

        config.setDeviceMetricsMonitorTypes(DeviceMetricsMonitorType.FPS);
        Assert.assertEquals(DeviceMetricsMonitorType.FPS.getValue(), config.getDeviceMetricsMonitorType());
        Assert.assertSame(DetectFrequency.RARE, config.getDeviceMetricsDetectFrequency());
    }

    @Test
    public void frequencyOverloadSupportsAllDuplicatesAndDisabling() {
        FTRUMConfig config = new FTRUMConfig().setDeviceMetricsMonitorTypes(DetectFrequency.RARE,
                DeviceMetricsMonitorType.CPU, DeviceMetricsMonitorType.CPU);
        Assert.assertEquals(DeviceMetricsMonitorType.CPU.getValue(), config.getDeviceMetricsMonitorType());

        config.setDeviceMetricsMonitorTypes(DetectFrequency.DEFAULT, DeviceMetricsMonitorType.ALL);
        Assert.assertEquals(DeviceMetricsMonitorType.ALL.getValue(), config.getDeviceMetricsMonitorType());
        Assert.assertSame(DetectFrequency.DEFAULT, config.getDeviceMetricsDetectFrequency());

        config.setDeviceMetricsMonitorTypes(DetectFrequency.FREQUENT);
        Assert.assertEquals(DeviceMetricsMonitorType.NO_SET, config.getDeviceMetricsMonitorType());
        Assert.assertSame(DetectFrequency.FREQUENT, config.getDeviceMetricsDetectFrequency());
    }

    @Test
    public void nullFrequencyIsRejectedWithoutChangingConfiguration() {
        assertInvalidFrequencyOrTypesPreserveConfiguration(null, DeviceMetricsMonitorType.CPU);
    }

    @Test
    public void frequencyOverloadRejectsNullArrayWithoutChangingConfiguration() {
        assertInvalidFrequencyOrTypesPreserveConfiguration(DetectFrequency.FREQUENT,
                (DeviceMetricsMonitorType[]) null);
    }

    @Test
    public void frequencyOverloadRejectsNullElementWithoutChangingEitherSetting() {
        assertInvalidFrequencyOrTypesPreserveConfiguration(DetectFrequency.FREQUENT,
                DeviceMetricsMonitorType.CPU, null);
    }

    private void assertInvalidFrequencyOrTypesPreserveConfiguration(DetectFrequency frequency,
                                                                  DeviceMetricsMonitorType... types) {
        FTRUMConfig config = new FTRUMConfig().setDeviceMetricsMonitorType(
                DeviceMetricsMonitorType.MEMORY.getValue(), DetectFrequency.RARE);

        Assert.assertThrows(IllegalArgumentException.class,
                () -> config.setDeviceMetricsMonitorTypes(frequency, types));

        Assert.assertEquals(DeviceMetricsMonitorType.MEMORY.getValue(), config.getDeviceMetricsMonitorType());
        Assert.assertSame(DetectFrequency.RARE, config.getDeviceMetricsDetectFrequency());
    }

    private void assertInvalidTypesPreserveConfiguration(DeviceMetricsMonitorType... types) {
        FTRUMConfig config = new FTRUMConfig().setDeviceMetricsMonitorType(
                DeviceMetricsMonitorType.MEMORY.getValue(), DetectFrequency.RARE);

        Assert.assertThrows(IllegalArgumentException.class, () -> config.setDeviceMetricsMonitorTypes(types));

        Assert.assertEquals(DeviceMetricsMonitorType.MEMORY.getValue(), config.getDeviceMetricsMonitorType());
        Assert.assertSame(DetectFrequency.RARE, config.getDeviceMetricsDetectFrequency());
    }
}

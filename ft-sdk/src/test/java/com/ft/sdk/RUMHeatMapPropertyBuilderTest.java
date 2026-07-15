package com.ft.sdk;

import android.view.MotionEvent;
import android.view.View;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.Constants;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class RUMHeatMapPropertyBuilderTest {

    @Test
    public void buildActionPropertiesAddsHeatMapFields() throws Exception {
        View target = new View(RuntimeEnvironment.application);
        target.layout(10, 20, 110, 70);
        MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN,
                12.5f, 24.5f, 0);

        HashMap<String, Object> properties =
                RUMHeatMapPropertyBuilder.buildActionProperties(target, motionEvent);

        JSONObject position = new JSONObject((String) properties.get(Constants.KEY_RUM_ACTION_POSITION));
        Assert.assertEquals(12.5d, position.getDouble("x"), 0.01d);
        Assert.assertEquals(24.5d, position.getDouble("y"), 0.01d);

        JSONObject targetJson = new JSONObject((String) properties.get(Constants.KEY_RUM_ACTION_TARGET));
        Assert.assertEquals(100, targetJson.getInt("width"));
        Assert.assertEquals(50, targetJson.getInt("height"));
        Assert.assertTrue(targetJson.getString("permanent_id").matches("[0-9a-f]{32}"));

        JSONObject display = new JSONObject((String) properties.get(Constants.KEY_RUM_DISPLAY));
        JSONObject viewport = display.getJSONObject("viewport");
        Assert.assertTrue(viewport.getInt("width") > 0);
        Assert.assertTrue(viewport.getInt("height") > 0);
    }

    @Test
    public void buildActionPropertiesSkipsPositionWhenMotionEventIsMissing() {
        View target = new View(RuntimeEnvironment.application);
        target.layout(0, 0, 100, 50);

        HashMap<String, Object> properties =
                RUMHeatMapPropertyBuilder.buildActionProperties(target, null);

        Assert.assertFalse(properties.containsKey(Constants.KEY_RUM_ACTION_POSITION));
        Assert.assertTrue(properties.containsKey(Constants.KEY_RUM_ACTION_TARGET));
    }

    @Test
    public void actionTargetPermanentIdUsesSharedResolver() throws Exception {
        View target = new View(RuntimeEnvironment.application);
        target.layout(0, 0, 100, 50);

        HashMap<String, Object> properties =
                RUMHeatMapPropertyBuilder.buildActionProperties(target, null);

        JSONObject targetJson = new JSONObject((String) properties.get(Constants.KEY_RUM_ACTION_TARGET));
        Assert.assertEquals(FTViewPermanentIdResolver.resolve(target),
                targetJson.getString("permanent_id"));
    }

    @Test
    public void clickOnlyActionUsesLastWindowTouchPosition() throws Exception {
        View target = new View(RuntimeEnvironment.application);
        target.layout(10, 20, 110, 70);
        MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP,
                25f, 45f, 0);
        RUMTouchPositionTracker.record(motionEvent);

        HashMap<String, Object> properties =
                RUMHeatMapPropertyBuilder.buildActionProperties(target, null);
        RUMTouchPositionTracker.appendPositionIfAvailable(properties, target, null);

        JSONObject position = new JSONObject((String) properties.get(Constants.KEY_RUM_ACTION_POSITION));
        Assert.assertTrue(position.getDouble("x") >= 0);
        Assert.assertTrue(position.getDouble("x") <= target.getWidth());
        Assert.assertTrue(position.getDouble("y") >= 0);
        Assert.assertTrue(position.getDouble("y") <= target.getHeight());
    }

    @Test
    public void rumBodyContentAddsDisplayWhenMissing() throws Exception {
        SyncDataHelper helper = new SyncDataHelper();
        HashMap<String, Object> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("field", "value");

        helper.getBodyContent(Constants.FT_MEASUREMENT_RUM_VIEW, tags, fields,
                1L, DataType.RUM_APP, "uuid");

        JSONObject display = new JSONObject((String) fields.get(Constants.KEY_RUM_DISPLAY));
        JSONObject viewport = display.getJSONObject("viewport");
        Assert.assertTrue(viewport.getInt("width") > 0);
        Assert.assertTrue(viewport.getInt("height") > 0);
    }
}

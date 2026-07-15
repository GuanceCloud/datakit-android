package com.ft.sdk;

import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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
@Config(manifest = Config.NONE, sdk = 28)
public class RUMHeatMapActionPropertyBuilderTest {

    @Test
    public void menuItemActionUsesTouchedToolbarView() throws Exception {
        TouchFixture fixture = recordWindowTouch(SystemClock.uptimeMillis());
        PopupMenu popupMenu = new PopupMenu(fixture.context, fixture.target);
        MenuItem menuItem = popupMenu.getMenu().add(0, 1001, 0, "Privacy Override");
        fixture.target.setId(menuItem.getItemId());

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(menuItem, null);

        assertActionTargetMatches(fixture.target, properties);
        JSONObject position = new JSONObject(
                (String) properties.get(Constants.KEY_RUM_ACTION_POSITION));
        Assert.assertEquals(fixture.target.getWidth() / 2f,
                position.getDouble("x"), 0.01d);
        Assert.assertEquals(fixture.target.getHeight() / 2f,
                position.getDouble("y"), 0.01d);
    }

    @Test
    public void menuItemActionDoesNotUseExpiredTouchedView() {
        TouchFixture fixture = recordWindowTouch(SystemClock.uptimeMillis() - 2000L);
        PopupMenu popupMenu = new PopupMenu(fixture.context, fixture.target);
        MenuItem menuItem = popupMenu.getMenu().add(0, 1002, 0, "Expired");
        fixture.target.setId(menuItem.getItemId());

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(menuItem, null);

        Assert.assertFalse(properties.containsKey(Constants.KEY_RUM_ACTION_TARGET));
    }

    @Test
    public void menuItemActionDoesNotUseUnrelatedTouchedView() {
        TouchFixture fixture = recordWindowTouch(SystemClock.uptimeMillis());
        fixture.target.setId(2001);
        PopupMenu popupMenu = new PopupMenu(fixture.context, fixture.target);
        MenuItem menuItem = popupMenu.getMenu().add(0, 2002, 0, "Different");

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(menuItem, null);

        Assert.assertFalse(properties.containsKey(Constants.KEY_RUM_ACTION_TARGET));
    }

    @Test
    public void menuItemActionRejectsMatchingTitleWhenIdsDiffer() {
        TouchFixture fixture = recordWindowTouch(SystemClock.uptimeMillis());
        fixture.target.setId(3001);
        fixture.target.setContentDescription("Same title");
        PopupMenu popupMenu = new PopupMenu(fixture.context, fixture.target);
        MenuItem menuItem = popupMenu.getMenu().add(0, 3002, 0, "Same title");

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(menuItem, null);

        Assert.assertFalse(properties.containsKey(Constants.KEY_RUM_ACTION_TARGET));
    }

    @Test
    public void menuItemActionUsesMatchingContentDescription() throws Exception {
        TouchFixture fixture = recordWindowTouch(SystemClock.uptimeMillis());
        fixture.target.setContentDescription("Privacy Override");
        PopupMenu popupMenu = new PopupMenu(fixture.context, fixture.target);
        MenuItem menuItem = popupMenu.getMenu().add("Privacy Override");

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(menuItem, null);

        assertActionTargetMatches(fixture.target, properties);
    }

    @Test
    public void menuItemActionPrefersCustomActionViewOverTouchedView() throws Exception {
        TouchFixture fixture = recordWindowTouch(SystemClock.uptimeMillis());
        View customActionView = new View(fixture.context);
        customActionView.layout(0, 0, 80, 40);
        PopupMenu popupMenu = new PopupMenu(fixture.context, fixture.target);
        MenuItem menuItem = popupMenu.getMenu().add("Custom");
        menuItem.setActionView(customActionView);

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(menuItem, null);

        assertActionTargetMatches(customActionView, properties);
    }

    @Test
    public void explicitViewActionDoesNotUseUnrelatedTouchedView() throws Exception {
        TouchFixture fixture = recordWindowTouch(SystemClock.uptimeMillis());
        View explicitTarget = new View(fixture.context);
        explicitTarget.layout(0, 0, 60, 30);

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(explicitTarget, null);

        assertActionTargetMatches(explicitTarget, properties);
    }

    @Test
    public void dialogButtonPositionUsesClickedButton() {
        View button = new View(RuntimeEnvironment.application);
        View resolved = RUMActionTargetResolver.selectDialogTarget(
                DialogInterface.BUTTON_POSITIVE, button, null, null);

        Assert.assertSame(button, resolved);
    }

    @Test
    public void dialogListPositionUsesClickedRow() {
        View row = new View(RuntimeEnvironment.application);
        View listView = new View(RuntimeEnvironment.application);
        View resolved = RUMActionTargetResolver.selectDialogTarget(
                1, null, row, listView);

        Assert.assertSame(row, resolved);
    }

    @Test
    public void radioGroupActionUsesCheckedRadioButton() throws Exception {
        RadioGroup group = new RadioGroup(RuntimeEnvironment.application);
        RadioButton checkedButton = new RadioButton(RuntimeEnvironment.application);
        checkedButton.setId(1001);
        group.addView(checkedButton);
        group.layout(0, 0, 200, 100);
        checkedButton.layout(0, 0, 100, 50);
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("checkedId", checkedButton.getId());

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(group, extra);

        assertActionTargetMatches(checkedButton, properties);
    }

    @Test
    public void tabHostActionUsesLastTouchedTabView() throws Exception {
        TouchFixture fixture = recordWindowTouch(SystemClock.uptimeMillis());
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("tabId", "tab-1");
        fixture.target.setTag("tab-1");

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(null, extra);

        assertActionTargetMatches(fixture.target, properties);
    }

    @Test
    public void tabHostActionDoesNotUseUnrelatedTouchedView() {
        TouchFixture fixture = recordWindowTouch(SystemClock.uptimeMillis());
        fixture.target.setTag("other-tab");
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("tabId", "tab-1");

        HashMap<String, Object> properties =
                RUMHeatMapActionPropertyBuilder.build(null, extra);

        Assert.assertFalse(properties.containsKey(Constants.KEY_RUM_ACTION_TARGET));
    }

    private TouchFixture recordWindowTouch(long eventTime) {
        Context context = RuntimeEnvironment.application;
        View target = new View(context);
        target.setClickable(true);
        target.layout(0, 0, 100, 50);
        int[] location = new int[2];
        target.getLocationOnScreen(location);
        MotionEvent motionEvent = MotionEvent.obtain(eventTime, eventTime, MotionEvent.ACTION_UP,
                location[0] + target.getWidth() / 2f,
                location[1] + target.getHeight() / 2f, 0);
        RUMTouchPositionTracker.record(motionEvent, target);
        return new TouchFixture(context, target);
    }

    private void assertActionTargetMatches(View expected, HashMap<String, Object> properties)
            throws Exception {
        Assert.assertTrue(properties.containsKey(Constants.KEY_RUM_ACTION_TARGET));
        JSONObject targetJson = new JSONObject(
                (String) properties.get(Constants.KEY_RUM_ACTION_TARGET));
        Assert.assertEquals(expected.getWidth(), targetJson.getInt("width"));
        Assert.assertEquals(expected.getHeight(), targetJson.getInt("height"));
        Assert.assertEquals(FTViewPermanentIdResolver.resolve(expected),
                targetJson.getString("permanent_id"));
    }

    private static class TouchFixture {
        final Context context;
        final View target;

        TouchFixture(Context context, View target) {
            this.context = context;
            this.target = target;
        }
    }
}

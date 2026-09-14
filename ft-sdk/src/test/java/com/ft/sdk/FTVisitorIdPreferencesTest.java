package com.ft.sdk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.ft.sdk.garble.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class FTVisitorIdPreferencesTest {
    private Field applicationField;
    private Object originalApplication;
    private SharedPreferences preferences;

    @Before public void setUp() throws Exception {
        applicationField = FTApplication.class.getDeclaredField("instance");
        applicationField.setAccessible(true);
        originalApplication = applicationField.get(null);
        Application application = RuntimeEnvironment.getApplication();
        applicationField.set(null, application);
        preferences = application.getSharedPreferences("ft_visitor_id_" + Utils.getCurrentProcessName(),
                Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }

    @After public void tearDown() throws Exception {
        preferences.edit().clear().commit();
        applicationField.set(null, originalApplication);
    }

    @Test public void actualPreferencesPersistAndResetAcrossManagerLifecycles() {
        FTVisitorIdManager first = new FTVisitorIdManager();
        first.init(FTVisitorIdManagerTest.enabled());
        String id = first.getId();
        assertTrue(id.startsWith("p"));
        assertTrue(preferences.getString("record", "").startsWith(id + ":"));
        first.stop();
        FTVisitorIdManager restarted = new FTVisitorIdManager();
        restarted.init(FTVisitorIdManagerTest.enabled());
        assertEquals(id, restarted.getId());
        restarted.reset();
        String reset = restarted.getId();
        assertNotEquals(id, reset);
        restarted.stop();
        restarted.init(FTVisitorIdManagerTest.enabled());
        assertEquals(reset, restarted.getId());
    }

    @Test public void wrongPreferenceTypeIsReplacedWithoutCrashing() {
        preferences.edit().putInt("record", 42).commit();
        FTVisitorIdManager manager = new FTVisitorIdManager();
        manager.init(FTVisitorIdManagerTest.enabled());
        assertTrue(manager.getId().startsWith("p"));
        assertTrue(preferences.getString("record", "").startsWith(manager.getId()));
    }
}

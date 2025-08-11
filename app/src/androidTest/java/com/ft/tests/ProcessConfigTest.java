package com.ft.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.ft.AllTests.hasPrepare;
import static org.hamcrest.Matchers.allOf;

import android.content.Intent;
import android.os.Looper;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.BaseTest;
import com.ft.DebugMainActivity;
import com.ft.R;
import com.ft.sdk.FTSdk;
import com.ft.service.TestService;
import com.ft.utils.CrossProcessSetting;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/***
 *   Verify {@link com.ft.sdk.FTSDKConfig#setOnlySupportMainProcess(boolean)} effectiveness
 */
@RunWith(AndroidJUnit4.class)
public class ProcessConfigTest extends BaseTest {
    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);


    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
    }

    /**
     * Verify OnlyMainProcess is false, {@link  FTSdk#get()} can be initialized in the subprocess
     *
     * @throws InterruptedException
     */
    @Test
    public void notOnlyMainProcess() throws InterruptedException {
        CrossProcessSetting.setOnlyMainProcess(getContext(), false);

        Thread.sleep(2000);
        onView(withId(R.id.main_start_service)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(1000);

        ViewInteraction textView = Espresso.onView(allOf(withId(R.id.main_start_service)));
        textView.check(matches(withText(getContext().getString(R.string.start_service_installed))));

    }

    /**
     * Verify OnlyMainProcess is true, {@link  FTSdk#get()} will not be initialized in the subprocess
     *
     * @throws InterruptedException
     */
    @Test
    public void onlyMainProcess() throws InterruptedException {
        CrossProcessSetting.setOnlyMainProcess(getContext(), true);

        Thread.sleep(2000);
        onView(withId(R.id.main_start_service)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(1000);
        ViewInteraction textView = Espresso.onView(allOf(withId(R.id.main_start_service)));
        textView.check(matches(withText(getContext().getString(R.string.start_service_not_install))));
    }

    @Override
    public void tearDown() {
        super.tearDown();
        CrossProcessSetting.clearProcessSetting(getContext());
        //TestService.onDestroy calls to end the process
        getContext().stopService(new Intent(getContext(), TestService.class));
    }
}
package com.ft.sdk.tests;

import static com.ft.sdk.FTTraceHandler.W3C_TRACEPARENT_KEY;
import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTResourceEventListener;
import com.ft.sdk.FTResourceInterceptor;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.FTTraceInterceptor;
import com.ft.sdk.FTTraceManager;
import com.ft.sdk.RUMCacheDiscard;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceID;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;
import com.ft.test.utils.LineProtocolData;
import com.ft.test.utils.RequestUtil;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RUMTest extends FTBaseTest {


    public static final String FIRST_VIEW = "FirstView";
    public static final String SECOND_VIEW = "SecondView";
    public static final String ROOT = "root";
    public static final String ACTION_NAME = "action";
    public static final long DURATION = 1000L;
    public static final String ACTION_TYPE_NAME = "action test";
    public static final String PROPERTY_NAME = "Property";
    public static final String PROPERTY_VALUE = "Property Value";
    public static final String PROPERTY_OVERRIDE_VALUE = "Property Value 2";
    public static final String ANY_ACTION = "AnyAction";
    public static final String LONG_TASK = "longTask";
    public static final String ERROR = "error";
    public static final String ERROR_MESSAGE = "error message";
    public static final String LONG_TASK_MESSAGE = "long task message";

    /**
     * Simulate http return header parameters
     */
    public static final String RESOURCE_REQUEST_HEADER = "{x-datadog-parent-id=73566521391796532, x-datadog-sampling-priority=1, " +
            "ft-dio-key=a44e0ab0-232f-4f93-a6fd-b7a45cf8d20c, x-datadog-origin=rum, x-datadog-trace-id=123622877354441421}";
    /**
     * Simulate http request header parameters
     */
    public static final String RESOURCE_RESPONSE_HEADER = "{date=[Fri, 26 Nov 2021 06:08:47 GMT], server=[sffe], content-length=[1437]," +
            " expires=[Fri, 01 Jan 1990 00:00:00 GMT], vary=[Accept-Encoding], content-encoding=[gzip]," +
            " pragma=[no-cache], last-modified=[Fri, 19 Jun 2020 10:30:00 GMT], x-xss-protection=[0], " +
            "x-content-type-options=[nosniff], content-type=[text/html]," +
            " content-security-policy-report-only=[script-src 'nonce-e-zcQTJtEfSCU-KEHJAWqw'" +
            " 'report-sample' 'strict-dynamic' 'unsafe-eval' 'unsafe-inline' http: https:;" +
            " object-src 'none'; report-uri https://csp.withgoogle.com/csp/static-on-bigtable; " +
            "base-uri 'none']," +
            " report-to=[{\"group\":\"static-on-bigtable\",\"max_age\":2592000,\"endpoints\"" +
            ":[{\"url\":\"https://csp.withgoogle.com/csp/report-to/static-on-bigtable\"}]}]," +
            " cross-origin-resource-policy=[cross-origin], cache-control=[no-cache, must-revalidate]," +
            " accept-ranges=[bytes], cross-origin-opener-policy-report-only=[same-origin; report-to=\"static-on-bigtable\"]}";
    public static final String BODY_CONTENT = "body content";

    @BeforeClass
    public static void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
        avoidCleanData();
    }

    @Before
    public void initRUM() {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL).setDebug(true));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
    }

    /**
     * {@link FTRUMGlobalManager#startAction(String, String)} Data generation test
     *
     * @throws Exception
     */
    @Test
    public void actionGenerateTest() throws Exception {
        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();
        ArrayList<ActionBean> list = FTDBManager.get().querySumAction(0);

        ActionBean action = list.get(0);
        Assert.assertTrue(action.isClose());
        Assert.assertEquals(action.getActionName(), ACTION_NAME);
        Assert.assertEquals(action.getActionType(), ACTION_TYPE_NAME);


    }

    /**
     * {@link FTRUMGlobalManager#addAction(String, String, long, long)} } Data generation test
     *
     * @throws Exception
     */
    @Test
    public void addActionTest() throws Exception {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);

        FTRUMGlobalManager.get().addAction(ACTION_NAME, ACTION_TYPE_NAME, DURATION, property);
        waitEventConsumeInThreadPool();
        Thread.sleep(500);

        List<SyncData> list = FTDBManager.get().queryDataByDataByTypeLimit(0, DataType.RUM_APP);

        Assert.assertFalse(list.isEmpty());

        SyncData data = list.get(0);
//        Assert.assertTrue(action.isClose());
        LineProtocolData lineProtocolData = new LineProtocolData(data.getDataString());
        Assert.assertEquals(lineProtocolData.getTagAsString("action_name"), ACTION_NAME);
        Assert.assertEquals(lineProtocolData.getTagAsString("action_type"), ACTION_TYPE_NAME);
        Assert.assertEquals(lineProtocolData.getField(PROPERTY_NAME), PROPERTY_VALUE);
    }

    /**
     * Data continuous write
     *
     * @throws InterruptedException
     */
    @Test
    public void multiActionData() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            FTRUMGlobalManager.get().addAction(ACTION_NAME, ACTION_TYPE_NAME);
        }
        Thread.sleep(2000);
        List<SyncData> list = FTDBManager.get().queryDataByDataByTypeLimit(0, DataType.RUM_APP);
        Assert.assertEquals(1000, list.size());
    }

    /**
     * Action with dynamic parameter test
     *
     * @throws Exception
     */
    @Test
    public void actionGenerateParamsTest() throws Exception {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME, property);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();
        ArrayList<ActionBean> list = FTDBManager.get().querySumAction(0);
        ActionBean action = list.get(0);
        Assert.assertEquals(action.getProperty().get(PROPERTY_NAME), PROPERTY_VALUE);

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_ACTION, DataType.RUM_APP, false));
    }

    /**
     * View data generation test
     *
     * @throws Exception
     */

    @Test
    public void viewGenerateTest() throws Exception {
        FTRUMGlobalManager.get().onCreateView(FIRST_VIEW, DURATION);
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().stopView();

        Thread.sleep(300);

        FTRUMGlobalManager.get().startView(SECOND_VIEW);

        waitEventConsumeInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0, true);
        Assert.assertEquals(list.size(), 2);

        ViewBean firstView = list.get(0);
        ViewBean secondView = list.get(1);

        Assert.assertNotEquals(firstView.getId(), secondView.getId());

        Assert.assertTrue(firstView.isClose());
        Assert.assertFalse(secondView.isClose());

        Assert.assertTrue(firstView.getLoadTime() > 0);
        Assert.assertEquals(-1, secondView.getLoadTime());

    }

    /**
     * View with dynamic parameter test
     *
     * @throws Exception
     */
    @Test
    public void viewGenerateParamsTest() throws Exception {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);

        FTRUMGlobalManager.get().onCreateView(FIRST_VIEW, DURATION);
        FTRUMGlobalManager.get().startView(FIRST_VIEW, property);

        waitEventConsumeInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0);
        ViewBean viewStart = list.get(0);
        Assert.assertEquals(viewStart.getProperty().get(PROPERTY_NAME), PROPERTY_VALUE);

        invokeGenerateRumData();

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_VIEW, DataType.RUM_APP, false));

        property.put(PROPERTY_NAME, PROPERTY_OVERRIDE_VALUE);
        FTRUMGlobalManager.get().stopView(property);
        list = FTDBManager.get().querySumView(0);
        ViewBean viewStop = list.get(0);
        Assert.assertEquals(viewStop.getProperty().get(PROPERTY_NAME), PROPERTY_VALUE);

        invokeGenerateRumData();

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_OVERRIDE_VALUE,
                Constants.FT_MEASUREMENT_RUM_VIEW, DataType.RUM_APP, false));

    }

    /**
     * Simulate continuous view jump operation,
     * and finally verify that each {@link ViewBean#viewReferrer} corresponds to the correct one
     *
     * @throws InterruptedException
     */
    @Test
    public void viewMapTest() throws InterruptedException {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().stopView();

        Thread.sleep(300);

        FTRUMGlobalManager.get().startView(SECOND_VIEW);
        FTRUMGlobalManager.get().stopView();

        waitEventConsumeInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0, true);
        Assert.assertEquals(list.size(), 2);

        ViewBean firstView = list.get(0);
        ViewBean secondView = list.get(1);

        Assert.assertEquals(firstView.getViewName(), FIRST_VIEW);
        Assert.assertEquals(secondView.getViewName(), SECOND_VIEW);

        Assert.assertEquals(firstView.getViewReferrer(), ROOT);
        Assert.assertEquals(secondView.getViewReferrer(), FIRST_VIEW);


    }

    /**
     * Simulate entering View after an Action, a Resource, an Error, and a LongTask
     * <p>
     * Finally verify the number of {@link ViewBean#resourceCount}
     * {@link ViewBean#errorCount}
     * {@link ViewBean#longTaskCount}
     * {@link ViewBean#actionCount} The number of whether it is correct
     *
     * @throws Exception
     */
    @Test
    public void viewActionSumTest() throws Exception {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);

        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);

        Request request = new Request.Builder().url(TEST_FAKE_URL).build();

        String resourceId = Utils.identifyRequest(request);

        Assert.assertFalse(resourceId.isEmpty());

        FTRUMGlobalManager.get().startResource(resourceId);
        FTRUMGlobalManager.get().stopResource(resourceId);

        FTRUMGlobalManager.get().addError(ERROR, ERROR_MESSAGE, ErrorType.JAVA, AppState.RUN);

        FTRUMGlobalManager.get().addLongTask(LONG_TASK_MESSAGE, DURATION);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();

        ArrayList<ActionBean> actionList = FTDBManager.get().querySumAction(0);
        Assert.assertEquals(actionList.size(), 1);

        ActionBean action = actionList.get(0);
        Assert.assertEquals(action.getResourceCount(), 1);
        Assert.assertEquals(action.getErrorCount(), 1);
        Assert.assertEquals(action.getLongTaskCount(), 1);

        waitEventConsumeInThreadPool();

        ArrayList<ViewBean> viewList = FTDBManager.get().querySumView(0);
        ViewBean view = viewList.get(0);

        Assert.assertEquals(view.getResourceCount(), 1);
        Assert.assertEquals(view.getErrorCount(), 1);
        Assert.assertEquals(view.getLongTaskCount(), 1);
        Assert.assertEquals(view.getActionCount(), 1);

    }

    /**
     * Simulate session id expiration, after a session id expires, the operation will generate a new session id
     *
     * @throws Exception
     */
    @Test
    public void sessionIdTest() throws Exception {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();

        ArrayList<ActionBean> actionList = FTDBManager.get().querySumAction(0);
        ActionBean action = actionList.get(0);
        ArrayList<ViewBean> viewList = FTDBManager.get().querySumView(0);

        ViewBean view = viewList.get(0);
        Assert.assertFalse(action.getSessionId().isEmpty());
        Assert.assertEquals(action.getSessionId(), view.getSessionId());
        setSessionExpire();

        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();

        ArrayList<ActionBean> newActionList = FTDBManager.get().querySumAction(0);
        ActionBean newAction = newActionList.get(1);

        Assert.assertNotEquals(action.getSessionId(), newAction.getSessionId());

    }

    /**
     * Simulate initiating a Resource request
     *
     * @throws InterruptedException
     */
    @Test
    public void resourceDataTest() throws InterruptedException {
        sendResource();

        Thread.sleep(2000);

        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncData recordData : recordDataList) {
            LineProtocolData data = new LineProtocolData(recordData.getDataString());

            if (Constants.FT_MEASUREMENT_RUM_RESOURCE.equals(data.getMeasurement())) {
                Assert.assertTrue(data.getFieldSize() > 0);
                break;
            }

        }
    }

    /**
     * Simulate Resource request with dynamic parameters
     *
     * @throws InterruptedException
     */
    @Test
    public void resourceDataParamsTest() throws InterruptedException {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        sendResource(property, null);

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_RESOURCE, DataType.RUM_APP, false));

    }

    /**
     * Resource dynamic parameter override test
     *
     * @throws InterruptedException
     */
    @Test
    public void resourceDataParamsOverrideTest() throws InterruptedException {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        HashMap<String, Object> propertyOverride = new HashMap<>();
        propertyOverride.put(PROPERTY_NAME, PROPERTY_OVERRIDE_VALUE);

        sendResource(property, propertyOverride);

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_OVERRIDE_VALUE,
                Constants.FT_MEASUREMENT_RUM_RESOURCE, DataType.RUM_APP, false));
    }

    /**
     * Error data test
     *
     * @throws InterruptedException
     */
    @Test
    public void errorDataTest() throws InterruptedException {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
        FTRUMGlobalManager.get().addError(ERROR, ERROR_MESSAGE, ErrorType.JAVA, AppState.RUN);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.RUM_APP,
                new String[]{ERROR, ERROR_MESSAGE}));
    }

    /**
     * Error data with dynamic parameter test
     *
     * @throws InterruptedException
     */
    @Test
    public void errorDataParamsTest() throws InterruptedException {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        FTRUMGlobalManager.get().addError(ERROR, ERROR_MESSAGE, ErrorType.JAVA, AppState.RUN, property);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_ERROR, DataType.RUM_APP, false));
    }

    /**
     * LongTask data test
     *
     * @throws InterruptedException
     */
    @Test
    public void longTaskTest() throws InterruptedException {
        FTRUMGlobalManager.get().addLongTask(LONG_TASK_MESSAGE, DURATION);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkDynamicValue(Constants.KEY_RUM_LONG_TASK_STACK, LONG_TASK_MESSAGE,
                Constants.FT_MEASUREMENT_RUM_LONG_TASK, DataType.RUM_APP, false));
    }

    /**
     * LongTask data with dynamic parameter test
     *
     * @throws InterruptedException
     */
    @Test
    public void longTaskParamsTest() throws InterruptedException {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        FTRUMGlobalManager.get().addLongTask(LONG_TASK_MESSAGE, DURATION, property);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_LONG_TASK, DataType.RUM_APP, false));
    }

    /**
     * {@link FTRUMConfig#samplingRate} Sampling rate 0 test
     *
     * @throws Exception
     */
    @Test
    public void sampleRateZeroTest() throws Exception {
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID).setSamplingRate(0));

        generateRUMData();

        List<SyncData> recordDataList = FTDBManager
                .get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        Assert.assertEquals(0, recordDataList.size());

        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID).setSamplingRate(1f));

    }

    /**
     * {@link FTRUMConfig#samplingRate} Sampling rate 100% test
     *
     * @throws Exception
     */
    @Test
    public void sampleRate100Test() throws Exception {
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID).setSamplingRate(1f));

        generateRUMData();

        List<SyncData> recordDataList = FTDBManager
                .get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        Assert.assertTrue(recordDataList.size() >= 5);

    }

    /**
     * View Action Error LongTask data generation
     *
     * @throws Exception
     */
    private void generateRUMData() throws Exception {
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().addError(ERROR, ERROR_MESSAGE, ErrorType.JAVA, AppState.RUN);
        FTRUMGlobalManager.get().addLongTask(LONG_TASK, DURATION);
        FTRUMGlobalManager.get().startAction(ANY_ACTION, "Any");
        invokeCheckActionClose();
        sendResource();
        FTRUMGlobalManager.get().stopView();
        waitEventConsumeInThreadPool();
        Thread.sleep(3000L);
    }

    /**
     * Simulate sending Resource data
     */
    private void sendResource() {
        sendResource(null, null);
    }

    /**
     * Simulate sending Resource data
     *
     * @param property         Dynamic parameters
     * @param propertyOverride Parameters to be overridden
     */
    private void sendResource(HashMap<String, Object> property, HashMap<String, Object> propertyOverride) {
        String resourceId = Utils.getGUID_16();
        FTRUMGlobalManager.get().startResource(resourceId, property);
        ResourceParams params = new ResourceParams();
        params.resourceMethod = "GET";
        params.requestHeader = RESOURCE_REQUEST_HEADER;
        params.responseHeader = RESOURCE_RESPONSE_HEADER;

        params.responseBody = BODY_CONTENT;
        params.responseConnection = "";
        params.responseContentEncoding = "";
        params.responseContentType = "";
        params.url = TEST_FAKE_URL;
        params.resourceStatus = 200;

        NetStatusBean bean = new NetStatusBean();
        FTRUMGlobalManager.get().stopResource(resourceId, propertyOverride);
        FTRUMGlobalManager.get().addResource(resourceId, params, bean);
    }

    /**
     * {@link FTTraceConfig#enableLinkRUMData} When true, check whether RUM contains link-related data
     *
     * @throws InterruptedException
     * @throws IOException
     */

    @Test
    public void traceLinkRUMDataEnable() throws InterruptedException, IOException {
        Assert.assertTrue(checkTraceHasLinkRumData(true));
    }

    /**
     * {@link FTTraceConfig#enableLinkRUMData} When false, check whether RUM contains link-related data
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void traceLinkRUMDataDisable() throws InterruptedException, IOException {
        Assert.assertFalse(checkTraceHasLinkRumData(false));
    }

    /**
     * Check whether RUM data {@link Constants#KEY_RUM_RESOURCE_TRACE_ID},
     * {@link Constants#KEY_RUM_RESOURCE_SPAN_ID } is included
     *
     * @param enableLinkRUMData
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private boolean checkTraceHasLinkRumData(boolean enableLinkRUMData) throws InterruptedException, IOException {
        MockWebServer mockWebServer = new MockWebServer();

        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody("");
        mockResponse.setResponseCode(HttpURLConnection.HTTP_OK);
        mockWebServer.enqueue(mockResponse);
        mockWebServer.play();

        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));

        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setTraceType(TraceType.DDTRACE)
                .setEnableLinkRUMData(enableLinkRUMData)
        );
        Thread.sleep(1000);

        RequestUtil.okhttpRequestUrl(mockWebServer.getUrl("/").toString());

        Thread.sleep(1000);

        List<SyncData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        String tracId = "";
        String spanId = "";

        for (SyncData recordData : recordDataList) {

            LineProtocolData data = new LineProtocolData(recordData.getDataString());

            if (Constants.FT_MEASUREMENT_RUM_RESOURCE.equals(data.getMeasurement())) {
                tracId = data.getTagAsString(Constants.KEY_RUM_RESOURCE_TRACE_ID, "");
                spanId = data.getTagAsString(Constants.KEY_RUM_RESOURCE_SPAN_ID, "");
                break;
            }

        }
        mockWebServer.shutdown();
        return !tracId.isEmpty() && !spanId.isEmpty();
    }

    private final static String CUSTOM_TRACE_HEADER = "replace_trace_id";

    @Test
    public void customTraceHeader() throws IOException, InterruptedException {
        MockWebServer mockWebServer = new MockWebServer();

        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody("");
        mockResponse.setResponseCode(HttpURLConnection.HTTP_OK);
        mockWebServer.enqueue(mockResponse);
        mockWebServer.play();

        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));

        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setTraceType(TraceType.TRACEPARENT)
                .setEnableAutoTrace(true)
                .setEnableLinkRUMData(true)
        );
        Thread.sleep(1000);


        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new FTTraceInterceptor(new FTTraceInterceptor.HeaderHandler() {
                    private String[] splits;

                    @Override
                    public HashMap<String, String> getTraceHeader(Request request) {
                        HashMap<String, String> map = new HashMap<>();
                        String replaceTrace = request.header(CUSTOM_TRACE_HEADER);//Get request
                        String headerString = FTTraceManager.get().
                                getTraceHeader(request.url().toString())
                                .get(W3C_TRACEPARENT_KEY); //get trace header string

                        splits = headerString.split("-");
                        String originTraceId = splits[1];
                        splits[1] = replaceTrace;
                        map.put(W3C_TRACEPARENT_KEY, headerString.replace(originTraceId, replaceTrace));
                        return map;
                    }

                    @Override
                    public String getSpanID() {
                        if (splits != null) {
                            return splits[2];
                        }
                        return null;
                    }

                    @Override
                    public String getTraceID() {
                        if (splits != null) {
                            return splits[1];
                        }
                        return null;
                    }
                }))
                .addInterceptor(new FTResourceInterceptor())
                .eventListenerFactory(new FTResourceEventListener.FTFactory()).build();


        Request.Builder builder = new Request.Builder().url(mockWebServer.getUrl("/").toString())
                .method(RequestMethod.GET.name(), null);

        String replaceTraceHeader = Utils.randomUUID();
        Request request = null;
        try {
            Response response = client.newCall(builder.header(CUSTOM_TRACE_HEADER,
                    replaceTraceHeader).build()).execute();
            request = response.request();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.sleep(1000);

        List<SyncData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        String tracId = "";
        String spanId = "";

        for (SyncData recordData : recordDataList) {

            LineProtocolData data = new LineProtocolData(recordData.getDataString());

            if (Constants.FT_MEASUREMENT_RUM_RESOURCE.equals(data.getMeasurement())) {
                tracId = data.getTagAsString(Constants.KEY_RUM_RESOURCE_TRACE_ID, "");
                spanId = data.getTagAsString(Constants.KEY_RUM_RESOURCE_SPAN_ID, "");
                break;
            }

        }
        mockWebServer.shutdown();

        Assert.assertTrue(!tracId.isEmpty() && !spanId.isEmpty());
        Assert.assertTrue(request.header(W3C_TRACEPARENT_KEY).contains(replaceTraceHeader));
        Assert.assertEquals(tracId, replaceTraceHeader);
    }

    /**
     * Check if the data is correct during the Resource process, and whether the Action is monitored
     *
     * @throws InterruptedException
     */
    @Test
    public void resourceWithOutActionId() throws InterruptedException {
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().startAction(ANY_ACTION, "Any");
        Thread.sleep(100);

        FTRUMGlobalManager.get().stopView();
        sendResource();

        waitEventConsumeInThreadPool();

        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        Assert.assertFalse(recordDataList.isEmpty());
        for (SyncData recordData : recordDataList) {
            LineProtocolData data = new LineProtocolData(recordData.getDataString());
            if (Constants.FT_MEASUREMENT_RUM_RESOURCE.equals(data.getMeasurement())) {
                Assert.assertNull(data.getTagAsString(Constants.KEY_RUM_ACTION_ID));
                break;
            }
        }

    }

    /**
     * Test large number of data insertion, whether to trigger the discard policy
     *
     * @throws InterruptedException
     */
    @Test
    public void triggerRUMDiscardPolicyTest() throws InterruptedException {
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
        batchRUM(10);

    }

    /**
     * Test large number of RUM data insertion, whether to trigger the discard policy
     *
     * @throws InterruptedException
     */
    @Test
    public void triggerRUMDiscardOldPolicyTest() throws InterruptedException {
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID).setRumCacheDiscardStrategy(RUMCacheDiscard.DISCARD_OLDEST));
        batchRUM(19);
    }

    /**
     * Batch RUM
     *
     * @param expectCount Expected number
     * @throws InterruptedException
     */
    private void batchRUM(int expectCount) throws InterruptedException {
        FTDBCachePolicy.get().optRUMCount(99990);
        for (int i = 0; i < 20; i++) {
            FTRUMGlobalManager.get().addAction(ANY_ACTION, ACTION_NAME);
            Thread.sleep(10);
        }
        Thread.sleep(2000);
        int count = CheckUtils.getCount(DataType.RUM_APP, ACTION_NAME, 0);
        System.out.println("count=" + count);
        //Thread.sleep(300000);
        Assert.assertTrue(expectCount >= count);
    }


    /**
     * ResourceID test
     */
    @Test
    public void uuidTest() {
        ResourceID resourceID = new ResourceID();
        Request request = new Request.Builder().url(TEST_FAKE_URL).tag(ResourceID.class, resourceID).build();
        String resourceId = Utils.identifyRequest(request);
        Assert.assertEquals(resourceID.getUuid(), resourceId);
    }

}

package com.ft.sdk.tests;

import static org.junit.Assert.assertEquals;

import com.ft.sdk.InnerConfigSet;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.HttpURLConnection;

/**
 * BY huangDianHua
 * DATE:2019-12-16 13:29
 * Description: Simulate network request test
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class HttpTest {
    /**
     * Simulate successful JSON return
     */
    private static final String SUCCESS_WITH_JSON_RESPONSE = "{\"code\":200,\"errorCode\":\"\",\"message\":\"\"}";

    /**
     * Simulate no data return
     */
    private static final String EMPTY_RESPONSE = "";
    private MockWebServer mMockWebServer;
    private static final String FORMAT_REQUEST_HOST_WITH_PORT = "http://127.0.0.1:%s/";

    @Before
    public void setUp() {
        mMockWebServer = new MockWebServer();
        InnerConfigSet.enableNetwork();
    }

    @After
    public void tearDown() throws Exception {
        mMockWebServer.shutdown();
    }

    /**
     * Simulate network 200 situation
     *
     * @throws Exception
     */
    @Test
    public void mockedRequest200() throws Exception {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody(EMPTY_RESPONSE);
        mockResponse.setResponseCode(HttpURLConnection.HTTP_OK);
        mMockWebServer.enqueue(mockResponse);
        mMockWebServer.play();
        String url = String.format(FORMAT_REQUEST_HOST_WITH_PORT, mMockWebServer.getPort());

        FTResponseData result = HttpBuilder.Builder()
                .setUrl(url)
                .setMethod(RequestMethod.POST)
                .setBodyString("")
                .executeSync();
        assertEquals(HttpURLConnection.HTTP_OK, result.getCode());
    }

    /**
     * Simulate network 400 situation
     *
     * @throws Exception
     */
    @Test
    public void mockedRequest400() throws Exception {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody(EMPTY_RESPONSE);
        mockResponse.setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
        mMockWebServer.enqueue(mockResponse);
        mMockWebServer.play();
        String url = String.format(FORMAT_REQUEST_HOST_WITH_PORT, mMockWebServer.getPort());
        FTResponseData result = HttpBuilder.Builder()
                .setUrl(url)
                .setMethod(RequestMethod.POST)
                .setBodyString("")
                .executeSync();
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getCode());
    }

    /**
     * Simulate network 200 and return data in correct JSON format
     *
     * @throws Exception
     */
    @Test
    public void mockedRequestJSON() throws Exception {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody(SUCCESS_WITH_JSON_RESPONSE);
        mockResponse.setResponseCode(HttpURLConnection.HTTP_OK);
        mMockWebServer.enqueue(mockResponse);
        mMockWebServer.play();
        String url = String.format(FORMAT_REQUEST_HOST_WITH_PORT, mMockWebServer.getPort());
        FTResponseData result = HttpBuilder.Builder()
                .setUrl(url)
                .setMethod(RequestMethod.POST)
                .setBodyString("")
                .executeSync();
        assertEquals(HttpURLConnection.HTTP_OK, result.getCode());
    }

}

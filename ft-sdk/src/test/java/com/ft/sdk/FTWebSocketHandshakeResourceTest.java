package com.ft.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.ResourceID;
import com.ft.sdk.garble.bean.ResourceType;
import com.ft.sdk.garble.utils.Utils;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

public class FTWebSocketHandshakeResourceTest {

    @Test
    public void resourceIdMatchesRequestIdentityUsedByTraceInterceptor() throws Exception {
        Request request = new Request.Builder()
                .url("wss://example.com/socket")
                .tag(ResourceID.class, new ResourceID())
                .build();

        FTWebSocketHandshakeResource resource = new FTWebSocketHandshakeResource(request);
        Field resourceId = FTWebSocketHandshakeResource.class.getDeclaredField("resourceId");
        resourceId.setAccessible(true);

        assertEquals(Utils.identifyRequest(request), resourceId.get(resource));
    }

    @Test
    public void successHandshakeUsesWebSocketTagsWithoutResourceSize() {
        Request request = new Request.Builder().url("wss://example.com/socket").build();
        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(101)
                .message("Switching Protocols")
                .header("Connection", "Upgrade")
                .build();

        ResourceParams params = new FTWebSocketHandshakeResource(request).createResourceParams(
                FTWebSocketHandshakeResource.HANDSHAKE_STATE_SUCCESS, response, null, false);

        assertEquals(ResourceType.WEBSOCKET.getValue(), params.resourceType);
        assertEquals(FTWebSocketHandshakeResource.COLLECTION_LEVEL_HANDSHAKE,
                params.resourceWebSocketCollectionLevel);
        assertEquals(FTWebSocketHandshakeResource.HANDSHAKE_STATE_SUCCESS,
                params.resourceWebSocketHandshakeState);
        assertEquals(101, params.resourceStatus);
        assertEquals("GET", params.resourceMethod);
        assertEquals(Protocol.HTTP_1_1.toString(), params.resourceProtocol);
        assertFalse(params.enableResourceSize);
        assertFalse(params.forceNetworkError);
        assertEquals("", params.requestErrorStack);
    }

    @Test
    public void rejectedHandshakeKeepsHttpStatusAndDoesNotForceNetworkError() {
        Request request = new Request.Builder().url("https://example.com/socket").build();
        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(403)
                .message("Forbidden")
                .build();

        assertEquals(FTWebSocketHandshakeResource.HANDSHAKE_STATE_REJECTED,
                FTWebSocketHandshakeResource.getFailureState(response));
        ResourceParams params = new FTWebSocketHandshakeResource(request).createResourceParams(
                FTWebSocketHandshakeResource.HANDSHAKE_STATE_REJECTED, response,
                new IOException("Forbidden"), false);

        assertEquals(403, params.resourceStatus);
        assertFalse(params.forceNetworkError);
    }

    @Test
    public void nonSwitchingSuccessfulHttpResponseIsRejected() {
        Request request = new Request.Builder().url("https://example.com/socket").build();
        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(204)
                .message("No Content")
                .build();

        assertEquals(FTWebSocketHandshakeResource.HANDSHAKE_STATE_REJECTED,
                FTWebSocketHandshakeResource.getFailureState(response));
    }

    @Test
    public void failedProtocolValidationWith101ForcesNetworkError() {
        Request request = new Request.Builder().url("https://example.com/socket").build();
        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(101)
                .message("Switching Protocols")
                .build();

        assertEquals(FTWebSocketHandshakeResource.HANDSHAKE_STATE_FAILED,
                FTWebSocketHandshakeResource.getFailureState(response));
        ResourceParams params = new FTWebSocketHandshakeResource(request).createResourceParams(
                FTWebSocketHandshakeResource.HANDSHAKE_STATE_FAILED, response,
                new IOException("Invalid upgrade response"), true);

        assertTrue(params.forceNetworkError);
        assertEquals(101, params.resourceStatus);
        assertTrue(params.requestErrorStack.contains("Invalid upgrade response"));
    }
}

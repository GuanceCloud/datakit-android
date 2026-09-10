package com.ft;

import android.app.Activity;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/** Small requests through URL.openConnection(), collected by ft-plugin instrumentation. */
final class HttpsURLConnectionSample {
    private final Activity activity;
    private final Button[] buttons = new Button[3];
    private EditText baseUrl;
    private TextView result;

    HttpsURLConnectionSample(Activity activity) {
        this.activity = activity;
    }

    void bind() {
        baseUrl = activity.findViewById(R.id.et_https_base_url);
        result = activity.findViewById(R.id.tv_https_result);
        buttons[0] = activity.findViewById(R.id.btn_https_get);
        buttons[1] = activity.findViewById(R.id.btn_https_post);
        buttons[2] = activity.findViewById(R.id.btn_https_error);
        buttons[0].setOnClickListener(v -> request("GET", "/get"));
        buttons[1].setOnClickListener(v -> request("POST", "/post"));
        buttons[2].setOnClickListener(v -> request("GET", "/status/404"));
    }

    private void request(String method, String path) {
        String base = baseUrl.getText().toString().trim().replaceAll("/+$", "");
        if (!base.startsWith("https://")) {
            result.setText("Enter an HTTPS base URL, for example https://httpbin.org");
            return;
        }
        String address = base + path;
        for (Button button : buttons) {
            button.setEnabled(false);
        }
        result.setText(method + " " + address + "\nSending request...");
        new Thread(() -> {
            long start = SystemClock.elapsedRealtime();
            HttpsURLConnection connection = null;
            String message;
            boolean instrumented = false;
            try {
                // Keep the ordinary platform API here so this demonstrates automatic collection.
                connection = (HttpsURLConnection) new URL(address).openConnection();
                instrumented = "com.ft.sdk.FTHttpsURLConnection".equals(
                        connection.getClass().getName());
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setUseCaches(false);
                connection.setRequestMethod(method);
                if ("POST".equals(method)) {
                    byte[] body = "{\"sample\":\"https-urlconnection\"}"
                            .getBytes(StandardCharsets.UTF_8);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    connection.setFixedLengthStreamingMode(body.length);
                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(body);
                    }
                }
                int status = connection.getResponseCode();
                long bytes = 0;
                ByteArrayOutputStream preview = new ByteArrayOutputStream();
                try (InputStream input = status >= 400
                        ? connection.getErrorStream() : connection.getInputStream()) {
                    if (input != null) {
                        byte[] buffer = new byte[4096];
                        int count;
                        while ((count = input.read(buffer)) != -1) {
                            bytes += count;
                            int retained = Math.min(count, 16384 - preview.size());
                            if (retained > 0) {
                                preview.write(buffer, 0, retained);
                            }
                        }
                    }
                }
                byte[] body = preview.toByteArray();
                message = "HTTP " + status + "\nResponse body: " + bytes + " bytes"
                        + (bytes <= body.length ? echoedTraceIds(body) : "")
                        + "\nResponse preview (up to 512 bytes):\n"
                        + new String(body, 0, Math.min(body.length, 512), StandardCharsets.UTF_8);
            } catch (Exception e) {
                message = "Request failed: " + e.getClass().getSimpleName() + "\n" + e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            String completed = method + " " + address
                    + "\nElapsed: " + (SystemClock.elapsedRealtime() - start) + " ms"
                    + "\n" + (instrumented ? "Collection wrapper: active"
                    : "Collection wrapper: inactive. Check Plugin and RUM settings.")
                    + "\n" + message;
            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    result.setText(completed);
                    for (Button button : buttons) {
                        button.setEnabled(true);
                    }
                }
            });
        }, "sample-https-urlconnection").start();
    }
    /** httpbin echoes the request headers; these are the server-observed default Datadog IDs. */
    private static String echoedTraceIds(byte[] response) {
        try {
            JSONObject headers = new JSONObject(new String(response, StandardCharsets.UTF_8))
                    .optJSONObject("headers");
            if (headers == null) return "";
            String traceId = "";
            String spanId = "";
            Iterator<String> keys = headers.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if ("x-datadog-trace-id".equalsIgnoreCase(key)) traceId = headers.optString(key);
                if ("x-datadog-parent-id".equalsIgnoreCase(key)) spanId = headers.optString(key);
            }
            if (!traceId.isEmpty() && !spanId.isEmpty()) {
                return "\nServer-echoed trace_id: " + traceId + "\nServer-echoed span_id: " + spanId;
            }
        } catch (Exception ignored) {
            // Custom endpoints and HTTP errors need not return JSON or echo request headers.
        }
        return "";
    }

}

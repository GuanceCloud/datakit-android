package com.ft.sdk;

import android.util.Base64;

import java.util.HashMap;

/**
 * Trace context that carries headers, traceId and spanId in a single object.
 * <p>
 * Integrators can provide trace data in one call without maintaining intermediate state
 * between {@link FTTraceInterceptor} and RUM linking.
 */
public abstract class TraceContext extends FTTraceInterceptor.TraceRUMLinkable {

    /**
     * Returns the HTTP headers to inject into the request for trace propagation.
     *
     * @return map of header name to value, never null
     */
    public abstract HashMap<String, String> getHeaders();

    /**
     * Returns the trace ID for RUM linkage.
     *
     * @return trace ID, or null if not available
     */
    public abstract String getTraceId();

    /**
     * Returns the span ID for RUM linkage.
     *
     * @return span ID, or null if not available
     */
    public abstract String getSpanId();

    @Override
    public final String getTraceID() {
        return getTraceId();
    }

    @Override
    public final String getSpanID() {
        return getSpanId();
    }

    /**
     * Simple implementation of {@link TraceContext} with fixed values.
     * <p>
     * Use constructor {@link #Simple(HashMap, String, String)} for custom traceId/spanId.
     * Use factory methods for extracting from various trace header formats.
     */
    public static class Simple extends TraceContext {
        private final HashMap<String, String> headers;
        private final String traceId;
        private final String spanId;

        /**
         * Creates TraceContext with given headers and IDs. Use this for custom traceId/spanId.
         *
         * @param headers  headers to inject, can be modified to contain custom IDs
         * @param traceId custom trace ID for RUM linkage
         * @param spanId  custom span ID for RUM linkage
         */
        public Simple(HashMap<String, String> headers, String traceId, String spanId) {
            this.headers = headers != null ? headers : new HashMap<>();
            this.traceId = traceId;
            this.spanId = spanId;
        }

        @Override
        public HashMap<String, String> getHeaders() {
            return headers;
        }

        @Override
        public String getTraceId() {
            return traceId;
        }

        @Override
        public String getSpanId() {
            return spanId;
        }

        /**
         * Extracts TraceContext from headers by auto-detecting format from header keys.
         * Tries each known format (Datadog, Zipkin, W3C, Jaeger, SkyWalking) in order.
         *
         * @param headers map containing trace headers
         * @return TraceContext, or null if headers is null or no known format matches
         */
        public static Simple fromTraceType(HashMap<String, String> headers) {
            if (headers == null) return null;
            Simple result;
            if (headers.containsKey(FTTraceHandler.DD_TRACE_TRACE_ID_KEY)) {
                result = fromDatadogHeaders(headers);
            } else if (headers.containsKey(FTTraceHandler.ZIPKIN_TRACE_ID)) {
                result = fromZipkinMultiHeaders(headers);
            } else if (headers.containsKey(FTTraceHandler.ZIPKIN_B3_HEADER)) {
                result = fromZipkinSingleHeader(headers);
            } else if (headers.containsKey(FTTraceHandler.W3C_TRACEPARENT_KEY)) {
                result = fromTraceparentHeader(headers);
            } else if (headers.containsKey(FTTraceHandler.JAEGER_KEY)) {
                result = fromJaegerHeader(headers);
            } else if (headers.containsKey(FTTraceHandler.SKYWALKING_V3_SW_8)) {
                result = fromSkyWalkingSw8(headers);
            } else {
                result = null;
            }
            return result;
        }

        /**
         * Extracts TraceContext from headers by trace type. Use when headers are generated
         * by {@link FTTraceManager#getTraceHeader(String)} with the given TraceType.
         *
         * @param headers   map containing trace headers
         * @param traceType format of the headers
         * @return TraceContext, or null if headers is null
         */
        public static Simple fromTraceType(HashMap<String, String> headers, TraceType traceType) {
            if (headers == null || traceType == null) {
                return null;
            }
            switch (traceType) {
                case DDTRACE:
                    return fromDatadogHeaders(headers);
                case ZIPKIN_MULTI_HEADER:
                    return fromZipkinMultiHeaders(headers);
                case ZIPKIN_SINGLE_HEADER:
                    return fromZipkinSingleHeader(headers);
                case TRACEPARENT:
                    return fromTraceparentHeader(headers);
                case JAEGER:
                    return fromJaegerHeader(headers);
                case SKYWALKING:
                    return fromSkyWalkingSw8(headers);
                default:
                    return null;
            }
        }

        /**
         * Datadog: x-datadog-trace-id, x-datadog-parent-id
         */
        public static Simple fromDatadogHeaders(HashMap<String, String> headers) {
            return fromHeaders(headers,
                    FTTraceHandler.DD_TRACE_TRACE_ID_KEY,
                    FTTraceHandler.DD_TRACE_PARENT_SPAN_ID_KEY);
        }

        /**
         * Zipkin multi-header: X-B3-TraceId, X-B3-SpanId
         */
        public static Simple fromZipkinMultiHeaders(HashMap<String, String> headers) {
            return fromHeaders(headers,
                    FTTraceHandler.ZIPKIN_TRACE_ID,
                    FTTraceHandler.ZIPKIN_SPAN_ID);
        }

        /**
         * Zipkin single-header: b3 format "traceId-spanId-sampled"
         */
        public static Simple fromZipkinSingleHeader(HashMap<String, String> headers) {
            if (headers == null) return null;
            String b3 = headers.get(FTTraceHandler.ZIPKIN_B3_HEADER);
            if (b3 == null) return null;
            String[] parts = b3.split("-");
            if (parts.length < 2) return null;
            return new Simple(headers, parts[0], parts[1]);
        }

        /**
         * W3C traceparent: format "version-traceId-parentId-flags"
         */
        public static Simple fromTraceparentHeader(HashMap<String, String> headers) {
            if (headers == null) return null;
            String traceparent = headers.get(FTTraceHandler.W3C_TRACEPARENT_KEY);
            if (traceparent == null) return null;
            String[] parts = traceparent.split("-");
            if (parts.length < 4) return null;
            return new Simple(headers, parts[1], parts[2]);
        }

        /**
         * Jaeger: uber-trace-id format "traceId:spanId:parentSpanId:flags"
         */
        public static Simple fromJaegerHeader(HashMap<String, String> headers) {
            if (headers == null) return null;
            String uberTraceId = headers.get(FTTraceHandler.JAEGER_KEY);
            if (uberTraceId == null) return null;
            String[] parts = uberTraceId.split(":");
            if (parts.length < 2) return null;
            return new Simple(headers, parts[0], parts[1]);
        }

        /**
         * SkyWalking V3: sw8 format "sampled-base64TraceId-base64ParentTraceId-0-..."
         */
        public static Simple fromSkyWalkingSw8(HashMap<String, String> headers) {
            if (headers == null) return null;
            String sw8 = headers.get(FTTraceHandler.SKYWALKING_V3_SW_8);
            if (sw8 == null) return null;
            String[] parts = sw8.split("-");
            if (parts.length < 3) return null;
            try {
                String traceId = new String(Base64.decode(parts[1], Base64.NO_WRAP));
                String spanId = new String(Base64.decode(parts[2], Base64.NO_WRAP));
                return new Simple(headers, traceId, spanId);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Generic extraction with explicit header keys. Use for custom or unsupported formats.
         *
         * @param headers     map containing trace headers
         * @param traceIdKey  header key for trace ID
         * @param spanIdKey   header key for span ID
         * @return TraceContext, or null if headers is null
         */
        public static Simple fromHeaders(HashMap<String, String> headers,
                                        String traceIdKey, String spanIdKey) {
            if (headers == null) {
                return null;
            }
            String traceId = traceIdKey != null ? headers.get(traceIdKey) : null;
            String spanId = spanIdKey != null ? headers.get(spanIdKey) : null;
            return new Simple(headers, traceId, spanId);
        }
    }
}

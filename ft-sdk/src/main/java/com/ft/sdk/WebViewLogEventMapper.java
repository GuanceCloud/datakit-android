package com.ft.sdk;

import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Maps the Browser Logs SDK bridge payload to the native Log representation.
 * The mappings mirror browser-core dataMap.browser_log.
 */
final class WebViewLogEventMapper {

    private static final long NANOSECONDS_PER_MILLISECOND = 1_000_000L;
    private static final long MAX_MILLISECONDS = Long.MAX_VALUE / NANOSECONDS_PER_MILLISECOND;
    private static final String[] RUM_LINK_TAG_KEYS = {
            Constants.KEY_RUM_APP_ID,
            Constants.KEY_RUM_SESSION_ID,
            Constants.KEY_RUM_SESSION_TYPE,
            "session_is_forced",
            "session_sampling",
            Constants.KEY_RUM_VIEW_ID,
            Constants.KEY_RUM_VIEW_REFERRER,
            "view_url",
            "view_host",
            "view_path",
            Constants.KEY_RUM_VIEW_NAME,
            "view_path_group",
            "view_path_name",
            Constants.KEY_RUM_ACTION_ID
    };
    private static final String[] RUM_LINK_FIELD_KEYS = {
            "application",
            "session",
            "view",
            "action",
            "user_action",
            "view_url_query",
            Constants.KEY_RUM_ACTION_ID,
            "action_ids",
            "view_in_foreground",
            "session_has_replay"
    };

    private WebViewLogEventMapper() {
    }

    static LogBean map(JSONObject event) {
        if (event == null) {
            return null;
        }

        Object rawMessage = valueAtPath(event, "message");
        if (isNull(rawMessage)) {
            return null;
        }

        String message = String.valueOf(toLineProtocolValue(rawMessage));
        String status = normalizeStatus(valueAtPath(event, "status"));
        HashMap<String, Object> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        Set<String> consumedKeys = new HashSet<>();
        consumedKeys.add("date");
        consumedKeys.add("type");
        consumedKeys.add("custom_keys");
        consumedKeys.add(Constants.KEY_RUM_VIEW_IS_WEB_VIEW);

        mapCommonTags(event, tags, consumedKeys);
        mapCommonFields(event, fields, consumedKeys);
        mapBrowserLogTags(event, tags, consumedKeys);
        mapBrowserLogFields(event, fields, consumedKeys);
        mapRemainingTopLevelFields(event, fields, consumedKeys);

        LogBean logBean = new LogBean(message, resolveTimeNano(event));
        logBean.setStatus(status);
        logBean.appendTags(tags);
        logBean.appendFields(fields);
        logBean.appendTags(singletonTag(Constants.KEY_RUM_VIEW_IS_WEB_VIEW, true));
        return logBean;
    }

    static void removeRumLinkData(LogBean logBean) {
        if (logBean == null) {
            return;
        }
        removeKeys(logBean.getTags(), RUM_LINK_TAG_KEYS);
        removeKeys(logBean.getFields(), RUM_LINK_FIELD_KEYS);
    }

    static void replaceRumLinkData(LogBean logBean, String applicationId, String sessionId) {
        if (logBean == null) {
            return;
        }
        replaceRumLinkId(logBean, Constants.KEY_RUM_APP_ID, "application", applicationId);
        replaceRumLinkId(logBean, Constants.KEY_RUM_SESSION_ID, "session", sessionId);
    }

    private static void replaceRumLinkId(LogBean logBean, String tagKey,
                                         String fieldKey, String id) {
        if (Utils.isNullOrEmpty(id)) {
            return;
        }
        logBean.getTags().put(tagKey, id);
        Object value = logBean.getFields().get(fieldKey);
        if (value == null) {
            return;
        }
        try {
            JSONObject object = value instanceof JSONObject
                    ? (JSONObject) value : new JSONObject(String.valueOf(value));
            object.put("id", id);
            logBean.getFields().put(fieldKey, object.toString());
        } catch (Exception ignored) {
            // Keep non-standard custom values while the flattened native tag remains authoritative.
        }
    }

    private static void removeKeys(HashMap<String, Object> values, String[] keys) {
        for (String key : keys) {
            values.remove(key);
        }
    }

    private static void mapCommonTags(JSONObject event, HashMap<String, Object> tags,
                                      Set<String> consumedKeys) {
        addTag(event, tags, consumedKeys, "sdk_name", "_gc.sdk_name");
        addTag(event, tags, consumedKeys, "sdk_version", "_gc.sdk_version");
        addTag(event, tags, consumedKeys, "app_id", "application.id");
        addTag(event, tags, consumedKeys, "env", "env");
        addTag(event, tags, consumedKeys, "service", "service");
        addTag(event, tags, consumedKeys, "version", "version");
        addTag(event, tags, consumedKeys, "source", "source");
        addTag(event, tags, consumedKeys, "userid", "user.id");
        addTag(event, tags, consumedKeys, "user_email", "user.email");
        addTag(event, tags, consumedKeys, "user_name", "user.name");
        addTag(event, tags, consumedKeys, "session_id", "session.id");
        addTag(event, tags, consumedKeys, "session_type", "session.type");
        addTag(event, tags, consumedKeys, "session_is_forced", "session.is_forced_session");
        addTag(event, tags, consumedKeys, "session_sampling", "session.is_sampling");
        addTag(event, tags, consumedKeys, "is_signin", "user.is_signin");
        addTag(event, tags, consumedKeys, "os", "device.os");
        addTag(event, tags, consumedKeys, "os_version", "device.os_version");
        addTag(event, tags, consumedKeys, "os_version_major", "device.os_version_major");
        addTag(event, tags, consumedKeys, "browser", "device.browser");
        addTag(event, tags, consumedKeys, "browser_version", "device.browser_version");
        addTag(event, tags, consumedKeys, "browser_version_major", "device.browser_version_major");
        addTag(event, tags, consumedKeys, "screen_size", "device.screen_size");
        addTag(event, tags, consumedKeys, "network_type", "device.network_type");
        addTag(event, tags, consumedKeys, "time_zone", "device.time_zone");
        addTag(event, tags, consumedKeys, "device", "device.device");
        addTag(event, tags, consumedKeys, "user_agent", "device.user_agent");
        addTag(event, tags, consumedKeys, "view_id", "view.id");
        addTag(event, tags, consumedKeys, "view_referrer", "view.referrer");
        addTag(event, tags, consumedKeys, "view_url", "view.url");
        addTag(event, tags, consumedKeys, "view_host", "view.host");
        addTag(event, tags, consumedKeys, "view_path", "view.path");
        addTag(event, tags, consumedKeys, "view_name", "view.name");
        addTag(event, tags, consumedKeys, "view_path_group", "view.path_group");
        addTag(event, tags, consumedKeys, "view_path_name", "view.pathname");
    }

    private static void mapCommonFields(JSONObject event, HashMap<String, Object> fields,
                                        Set<String> consumedKeys) {
        addField(event, fields, consumedKeys, "view_url_query", "view.url_query");
        addField(event, fields, consumedKeys, "action_id", "action.id");
        addField(event, fields, consumedKeys, "action_ids", "action.ids");
        addField(event, fields, consumedKeys, "view_in_foreground", "view.in_foreground");
        addField(event, fields, consumedKeys, "display", "display");
        addField(event, fields, consumedKeys, "session_has_replay", "session.has_replay");
        addField(event, fields, consumedKeys, "is_login", "user.is_login");
        addField(event, fields, consumedKeys, "page_states", "_gc.page_states");
        addField(event, fields, consumedKeys, "session_sample_rate",
                "_gc.configuration.session_sample_rate");
        addField(event, fields, consumedKeys, "session_replay_sample_rate",
                "_gc.configuration.session_replay_sample_rate");
        addField(event, fields, consumedKeys, "session_on_error_sample_rate",
                "_gc.configuration.session_on_error_sample_rate");
        addField(event, fields, consumedKeys, "session_replay_on_error_sample_rate",
                "_gc.configuration.session_replay_on_error_sample_rate");
        addField(event, fields, consumedKeys, "drift", "_gc.drift");
    }

    private static void mapBrowserLogTags(JSONObject event, HashMap<String, Object> tags,
                                          Set<String> consumedKeys) {
        addTag(event, tags, consumedKeys, "error_source", "error.source");
        addTag(event, tags, consumedKeys, "error_type", "error.type");
        addTag(event, tags, consumedKeys, "error_resource_url", "http.url");
        addTag(event, tags, consumedKeys, "error_resource_url_host", "http.url_host");
        addTag(event, tags, consumedKeys, "error_resource_url_path", "http.url_path");
        addTag(event, tags, consumedKeys, "error_resource_url_path_group", "http.url_path_group");
        addTag(event, tags, consumedKeys, "error_resource_status", "http.status_code");
        addTag(event, tags, consumedKeys, "error_resource_status_group", "http.status_group");
        addTag(event, tags, consumedKeys, "error_resource_method", "http.method");
        addTag(event, tags, consumedKeys, "action_id", "user_action.id");
        addTag(event, tags, consumedKeys, "service", "service");
        addTag(event, tags, consumedKeys, "status", "status");
    }

    private static void mapBrowserLogFields(JSONObject event, HashMap<String, Object> fields,
                                            Set<String> consumedKeys) {
        addField(event, fields, consumedKeys, "message", "message");
        addField(event, fields, consumedKeys, "error_message", "error.message");
        addField(event, fields, consumedKeys, "error_stack", "error.stack");
    }

    private static void mapRemainingTopLevelFields(JSONObject event,
                                                   HashMap<String, Object> fields,
                                                   Set<String> consumedKeys) {
        Iterator<String> keys = event.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (consumedKeys.contains(key)) {
                continue;
            }
            Object value = event.opt(key);
            if (!isNull(value)) {
                fields.put(key, toLineProtocolValue(value));
            }
        }
    }

    private static void addTag(JSONObject event, HashMap<String, Object> tags,
                               Set<String> consumedKeys, String targetKey, String sourcePath) {
        consumedKeys.add(targetKey);
        Object value = valueAtPath(event, sourcePath);
        if (isTruthyTagValue(value)) {
            tags.put(targetKey, toLineProtocolValue(value));
        }
    }

    private static void addField(JSONObject event, HashMap<String, Object> fields,
                                 Set<String> consumedKeys, String targetKey, String sourcePath) {
        consumedKeys.add(targetKey);
        Object value = valueAtPath(event, sourcePath);
        if (!isNull(value)) {
            fields.put(targetKey, toLineProtocolValue(value));
        }
    }

    private static Object valueAtPath(JSONObject object, String path) {
        String[] segments = path.split("\\.");
        Object current = object;
        for (String segment : segments) {
            if (!(current instanceof JSONObject)) {
                return null;
            }
            current = ((JSONObject) current).opt(segment);
            if (isNull(current)) {
                return null;
            }
        }
        return current;
    }

    private static boolean isTruthyTagValue(Object value) {
        if (isNull(value)) {
            return false;
        }
        if (value instanceof Number) {
            return true;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        return true;
    }

    private static boolean isNull(Object value) {
        return value == null || JSONObject.NULL.equals(value);
    }

    private static Object toLineProtocolValue(Object value) {
        if (value instanceof JSONObject || value instanceof JSONArray) {
            return value.toString();
        }
        return value;
    }

    private static String normalizeStatus(Object value) {
        if (isNull(value)) {
            return Status.INFO.name;
        }
        String status = String.valueOf(value).trim();
        if (status.isEmpty()) {
            return Status.INFO.name;
        }
        if ("warn".equals(status)) {
            return Status.WARNING.name;
        }
        return status;
    }

    private static long resolveTimeNano(JSONObject event) {
        Object value = event.opt("date");
        if (value instanceof Number) {
            double millisecondsDouble = ((Number) value).doubleValue();
            long milliseconds = ((Number) value).longValue();
            if (!Double.isNaN(millisecondsDouble)
                    && !Double.isInfinite(millisecondsDouble)
                    && milliseconds > 0
                    && milliseconds <= MAX_MILLISECONDS) {
                return milliseconds * NANOSECONDS_PER_MILLISECOND;
            }
        }
        return Utils.getCurrentNanoTime();
    }

    private static HashMap<String, Object> singletonTag(String key, Object value) {
        HashMap<String, Object> tag = new HashMap<>();
        tag.put(key, value);
        return tag;
    }
}

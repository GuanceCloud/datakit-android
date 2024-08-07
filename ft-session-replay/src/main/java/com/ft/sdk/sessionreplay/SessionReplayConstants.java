package com.ft.sdk.sessionreplay;

public class SessionReplayConstants {
    /**
     * SessionReplay 上传地址
     */
    public static final String URL_MODEL_SESSION_REPLAY = "v1/write/rum/replay";

    /**
     * Session Replay 缓存地址
     */
    public static final String PATH_SESSION_REPLAY = "sessionReplayTmp";

    public static final String KEY_RUM_SESSION_ID = "session_id";

    public static final String SESSION_REPLAY_BUS_MESSAGE_TYPE_KEY = "type";
    public static final String RUM_SESSION_RENEWED_BUS_MESSAGE = "rum_session_renewed";
    public static final String RUM_KEEP_SESSION_BUS_MESSAGE_KEY = "keepSession";
    public static final String RUM_SESSION_ID_BUS_MESSAGE_KEY = "sessionId";
}

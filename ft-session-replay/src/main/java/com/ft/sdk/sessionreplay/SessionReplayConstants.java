package com.ft.sdk.sessionreplay;

public class SessionReplayConstants {
    /**
     * SessionReplay upload address
     */
    public static final String URL_MODEL_SESSION_REPLAY = "v1/write/rum/replay";

    /** 
     * Session Replay cache address
     */
    public static final String PATH_SESSION_REPLAY = "sessionReplayTmp";
    public static final String PATH_SESSION_REPLAY_ERROR_SAMPLED = "sessionReplayErrorSampledTmp";

    public static final String PATH_SESSION_REPLAY_RESOURCE = "replayResourceTmp";
    public static final String PATH_SESSION_REPLAY_ERROR_RESOURCE_SAMPLED = "replayResourceErrorSampledTmp";

    public static final String SESSION_REPLAY_BUS_MESSAGE_TYPE_KEY = "type";
    public static final String RUM_SESSION_RENEWED_BUS_MESSAGE = "rum_session_renewed";
    public static final String RUM_KEEP_SESSION_BUS_COLLECT_TYPE_KEY = "collect_key";
    public static final String RUM_SESSION_ID_BUS_MESSAGE_KEY = "sessionId";

    public static final double DECREASE_PERCENT = 0.90;
    public static final double INCREASE_PERCENT = 1.10;

    public static final int BATTERY_LIMIT = 20;
}

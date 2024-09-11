package com.ft.sdk.sessionreplay.utils;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Provides the necessary RUM context for Session Replay records.
 */
public class SessionReplayRumContext {
    public static final String NULL_UUID = new UUID(0, 0).toString();

    private String applicationId;
    private String sessionId;
    private String viewId;

    public SessionReplayRumContext() {
        this.applicationId = NULL_UUID;
        this.sessionId = NULL_UUID;
        this.viewId = NULL_UUID;
    }

    public SessionReplayRumContext(String applicationId, String sessionId, String viewId) {
        this.applicationId = applicationId != null ? applicationId : NULL_UUID;
        this.sessionId = sessionId != null ? sessionId : NULL_UUID;
        this.viewId = viewId != null ? viewId : NULL_UUID;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId != null ? applicationId : NULL_UUID;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId != null ? sessionId : NULL_UUID;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId != null ? viewId : NULL_UUID;
    }

    public boolean isNotValid() {
        return applicationId.equals(NULL_UUID) || sessionId.equals(NULL_UUID) || viewId.equals(NULL_UUID);
    }

    public boolean isValid() {
        return !applicationId.equals(NULL_UUID) && !sessionId.equals(NULL_UUID) && !viewId.equals(NULL_UUID);
    }

    public SessionReplayRumContext clone() {
        return new SessionReplayRumContext(applicationId, sessionId, viewId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionReplayRumContext that = (SessionReplayRumContext) o;
        return Objects.equals(applicationId, that.applicationId) && Objects.equals(sessionId, that.sessionId) && Objects.equals(viewId, that.viewId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, sessionId, viewId);
    }

    @Override
    public String toString() {
        return "SessionReplayRumContext{" +
                "applicationId='" + applicationId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", viewId='" + viewId + '\'' +
                '}';
    }
}
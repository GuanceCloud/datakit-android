package com.ft.sdk.sessionreplay.internal.storage;

public abstract class RemovalReason {

    public boolean includeInMetrics() {
        return !(this instanceof Flushed);
    }

    public static class IntakeCode extends RemovalReason {
        private final int responseCode;

        public IntakeCode(int responseCode) {
            this.responseCode = responseCode;
        }

        @Override
        public String toString() {
            return "intake-code-" + responseCode;
        }
    }

    public static class Invalid extends RemovalReason {
        @Override
        public String toString() {
            return "invalid";
        }
    }

    public static class Purged extends RemovalReason {
        @Override
        public String toString() {
            return "purged";
        }
    }

    public static class Obsolete extends RemovalReason {
        @Override
        public String toString() {
            return "obsolete";
        }
    }

    public static class Flushed extends RemovalReason {
        @Override
        public String toString() {
            return "flushed";
        }
    }
}

package com.ft.service;

import android.content.Context;
import android.content.Intent;

/**
 * Stress test configuration class
 * Used to manage different stress test scenarios and parameters
 */
public class StressTestConfig {
    
    /**
     * Stress test scenario enumeration
     */
    public enum StressTestScenario {
        LIGHT("Light Stress Test", 50, 100, 25, 30000),
        MEDIUM("Medium Stress Test", 100, 200, 50, 60000),
        HEAVY("Heavy Stress Test", 200, 500, 100, 120000),
        EXTREME("Extreme Stress Test", 500, 1000, 200, 300000),
        CUSTOM("Custom Stress Test", 0, 0, 0, 0);

        private final String description;
        private final int defaultHttpCount;
        private final int defaultLogCount;
        private final int defaultActionCount;
        private final int defaultDuration;

        StressTestScenario(String description, int defaultHttpCount, int defaultLogCount, 
                         int defaultActionCount, int defaultDuration) {
            this.description = description;
            this.defaultHttpCount = defaultHttpCount;
            this.defaultLogCount = defaultLogCount;
            this.defaultActionCount = defaultActionCount;
            this.defaultDuration = defaultDuration;
        }

        public String getDescription() {
            return description;
        }

        public int getDefaultHttpCount() {
            return defaultHttpCount;
        }

        public int getDefaultLogCount() {
            return defaultLogCount;
        }

        public int getDefaultActionCount() {
            return defaultActionCount;
        }

        public int getDefaultDuration() {
            return defaultDuration;
        }
    }

    /**
     * Stress test configuration parameters
     */
    public static class Config {
        private final int httpCount;
        private final int logCount;
        private final int actionCount;
        private final int duration;
        private final boolean enableRandomDelay;
        private final int minDelay;
        private final int maxDelay;

        private Config(Builder builder) {
            this.httpCount = builder.httpCount;
            this.logCount = builder.logCount;
            this.actionCount = builder.actionCount;
            this.duration = builder.duration;
            this.enableRandomDelay = builder.enableRandomDelay;
            this.minDelay = builder.minDelay;
            this.maxDelay = builder.maxDelay;
        }

        public int getHttpCount() { return httpCount; }
        public int getLogCount() { return logCount; }
        public int getActionCount() { return actionCount; }
        public int getDuration() { return duration; }
        public boolean isEnableRandomDelay() { return enableRandomDelay; }
        public int getMinDelay() { return minDelay; }
        public int getMaxDelay() { return maxDelay; }

        /**
         * Create stress test Intent
         */
        public Intent createIntent(Context context) {
            return TestService.createStressTestIntent(
                context, true, httpCount, logCount, actionCount, duration
            );
        }

        /**
         * Configuration builder
         */
        public static class Builder {
            private int httpCount = 100;
            private int logCount = 200;
            private int actionCount = 50;
            private int duration = 30000;
            private boolean enableRandomDelay = true;
            private int minDelay = 10;
            private int maxDelay = 50;

            public Builder setHttpCount(int httpCount) {
                this.httpCount = httpCount;
                return this;
            }

            public Builder setLogCount(int logCount) {
                this.logCount = logCount;
                return this;
            }

            public Builder setActionCount(int actionCount) {
                this.actionCount = actionCount;
                return this;
            }

            public Builder setDuration(int duration) {
                this.duration = duration;
                return this;
            }

            public Builder setRandomDelay(boolean enable, int minDelay, int maxDelay) {
                this.enableRandomDelay = enable;
                this.minDelay = minDelay;
                this.maxDelay = maxDelay;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }
    }

    /**
     * Get predefined stress test configuration
     */
    public static Config getConfig(StressTestScenario scenario) {
        return new Config.Builder()
            .setHttpCount(scenario.getDefaultHttpCount())
            .setLogCount(scenario.getDefaultLogCount())
            .setActionCount(scenario.getDefaultActionCount())
            .setDuration(scenario.getDefaultDuration())
            .build();
    }

    /**
     * Get custom stress test configuration
     */
    public static Config getCustomConfig(int httpCount, int logCount, int actionCount, int duration) {
        return new Config.Builder()
            .setHttpCount(httpCount)
            .setLogCount(logCount)
            .setActionCount(actionCount)
            .setDuration(duration)
            .build();
    }

    /**
     * Get stress test scenario description
     */
    public static String getScenarioDescription(StressTestScenario scenario) {
        Config config = getConfig(scenario);
        return String.format("%s - HTTP:%d, Log:%d, Action:%d, Duration:%ds",
            scenario.getDescription(),
            config.getHttpCount(),
            config.getLogCount(),
            config.getActionCount(),
            config.getDuration() / 1000
        );
    }
}

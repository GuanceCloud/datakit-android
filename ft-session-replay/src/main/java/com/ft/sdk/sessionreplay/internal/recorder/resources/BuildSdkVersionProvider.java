package com.ft.sdk.sessionreplay.internal.recorder.resources;

import androidx.annotation.ChecksSdkIntAtLeast;

public interface BuildSdkVersionProvider {

    int getVersion();

    @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.N)
    boolean isAtLeastN();

    @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.O)
    boolean isAtLeastO();

    @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.P)
    boolean isAtLeastP();

    @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.Q)
    boolean isAtLeastQ();

    @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.R)
    boolean isAtLeastR();

    @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.S)
    boolean isAtLeastS();

    @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.TIRAMISU)
    boolean isAtLeastTiramisu();

    BuildSdkVersionProvider DEFAULT = new BuildSdkVersionProvider() {
        @ChecksSdkIntAtLeast
        @Override
        public int getVersion() {
            return android.os.Build.VERSION.SDK_INT;
        }

        @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.N)
        @Override
        public boolean isAtLeastN() {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N;
        }

        @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.O)
        @Override
        public boolean isAtLeastO() {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O;
        }

        @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.P)
        @Override
        public boolean isAtLeastP() {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P;
        }

        @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.Q)
        @Override
        public boolean isAtLeastQ() {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;
        }

        @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.R)
        @Override
        public boolean isAtLeastR() {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R;
        }

        @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.S)
        @Override
        public boolean isAtLeastS() {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S;
        }

        @ChecksSdkIntAtLeast(api = android.os.Build.VERSION_CODES.TIRAMISU)
        @Override
        public boolean isAtLeastTiramisu() {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU;
        }
    };
}
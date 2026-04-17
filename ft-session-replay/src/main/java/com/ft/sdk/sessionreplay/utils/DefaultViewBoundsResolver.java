package com.ft.sdk.sessionreplay.utils;

import android.view.View;

public class DefaultViewBoundsResolver implements ViewBoundsResolver {

    private static class SingletonHolder {
        private static final DefaultViewBoundsResolver INSTANCE = new DefaultViewBoundsResolver();
    }

    public static DefaultViewBoundsResolver get() {
        return DefaultViewBoundsResolver.SingletonHolder.INSTANCE;
    }

    @Override
    public GlobalBounds resolveViewGlobalBounds(View view, float screenDensity) {
        float inverseDensity = (screenDensity == 0f) ? 1f : 1f / screenDensity;

        int[] coordinates = new int[2];
        view.getLocationOnScreen(coordinates);

        long x = (long) (coordinates[0] * inverseDensity);
        long y = (long) (coordinates[1] * inverseDensity);
        long width = (long) (view.getWidth() * inverseDensity);
        long height = (long) (view.getHeight() * inverseDensity);

        return new GlobalBounds(x, y, width, height);
    }

    @Override
    public GlobalBounds resolveViewPaddedBounds(View view, float screenDensity) {
        float inverseDensity = (screenDensity == 0f) ? 1f : 1f / screenDensity;

        int[] coordinates = new int[2];
        view.getLocationOnScreen(coordinates);

        long x = (long) ((coordinates[0] + view.getPaddingLeft()) * inverseDensity);
        long y = (long) ((coordinates[1] + view.getPaddingTop()) * inverseDensity);
        long width = (long) ((view.getWidth() - view.getPaddingLeft() - view.getPaddingRight()) * inverseDensity);
        long height = (long) ((view.getHeight() - view.getPaddingTop() - view.getPaddingBottom()) * inverseDensity);

        return new GlobalBounds(x, y, width, height);
    }
}

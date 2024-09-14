package androidx.appcompat.widget;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class FTSDKContainerAccessor {

    private final ActionBarContainer container;

    @SuppressLint("RestrictedApi")
    public FTSDKContainerAccessor(ActionBarContainer container) {
        this.container = container;
    }

    @SuppressLint("RestrictedApi")

    public Drawable getBackgroundDrawable() {
        return container.mBackground;
    }

    @SuppressLint("RestrictedApi")
    public void setBackgroundDrawable(Drawable drawable) {
        container.setBackground(drawable);
    }
}

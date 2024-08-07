package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.os.Build;
import android.widget.CompoundButton;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

public abstract class CheckableCompoundButtonMapper<T extends CompoundButton> extends CheckableTextViewMapper<T> {
    public static final long MIN_PADDING_IN_PX = 20L;
    public static final long DEFAULT_CHECKABLE_HEIGHT_IN_PX = 84L;

    public CheckableCompoundButtonMapper(
            TextViewMapper<T> textWireframeMapper,
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper) {
        super(textWireframeMapper, viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    @Override
    public GlobalBounds resolveCheckableBounds(T view, float pixelsDensity) {
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, pixelsDensity);
        long checkBoxHeight = DEFAULT_CHECKABLE_HEIGHT_IN_PX;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view.getButtonDrawable() != null) {
                checkBoxHeight = view.getButtonDrawable().getIntrinsicHeight();
            }
        }
        // minus the padding
        checkBoxHeight -= MIN_PADDING_IN_PX * 2;
        checkBoxHeight = Utils.densityNormalized(checkBoxHeight, pixelsDensity);
        return new GlobalBounds(
                viewGlobalBounds.getX() + Utils.densityNormalized(MIN_PADDING_IN_PX, pixelsDensity),
                viewGlobalBounds.getY() + (viewGlobalBounds.getHeight() - checkBoxHeight) / 2,
                checkBoxHeight,
                checkBoxHeight
        );
    }


}

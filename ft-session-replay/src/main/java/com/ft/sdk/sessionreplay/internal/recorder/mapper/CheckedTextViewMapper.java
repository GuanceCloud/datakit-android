package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;

import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

public class CheckedTextViewMapper extends CheckableTextViewMapper<CheckedTextView> {

    public CheckedTextViewMapper(
            @NonNull TextViewMapper<CheckedTextView> textWireframeMapper,
            @NonNull ViewIdentifierResolver viewIdentifierResolver,
            @NonNull ColorStringFormatter colorStringFormatter,
            @NonNull ViewBoundsResolver viewBoundsResolver,
            @NonNull DrawableToColorMapper drawableToColorMapper
    ) {
        super(textWireframeMapper, viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    @Override
    public String resolveCheckableColor(@NonNull CheckedTextView view) {
        int color = view.getCheckMarkTintList() != null ?
                view.getCheckMarkTintList().getDefaultColor() :
                view.getCurrentTextColor();
        return colorStringFormatter.formatColorAndAlphaAsHexString(color, OPAQUE_ALPHA_VALUE);
    }

    @UiThread
    @Override
    public GlobalBounds resolveCheckableBounds(@NonNull CheckedTextView view, float pixelsDensity) {
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, pixelsDensity);
        long textViewPaddingRight = view.getTotalPaddingRight();
        long checkBoxHeight = 0L;
        android.graphics.drawable.Drawable checkMarkDrawable = view.getCheckMarkDrawable();
        if (checkMarkDrawable != null && checkMarkDrawable.getIntrinsicHeight() > 0) {
            int height = checkMarkDrawable.getIntrinsicHeight() -
                    view.getTotalPaddingTop() -
                    view.getTotalPaddingBottom();
            checkBoxHeight = (long) height;
        }

        return new GlobalBounds(
                viewGlobalBounds.getX() + viewGlobalBounds.getWidth() - textViewPaddingRight,
                viewGlobalBounds.getY(),
                checkBoxHeight,
                checkBoxHeight
        );
    }
}

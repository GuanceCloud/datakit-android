package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
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
            TextViewMapper<CheckedTextView> textWireframeMapper,
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(textWireframeMapper, viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    @Override
    public String resolveCheckableColor(CheckedTextView view) {
        int color = (view.getCheckMarkTintList() != null) ? view.getCheckMarkTintList().getDefaultColor() : view.getCurrentTextColor();
        return colorStringFormatter.formatColorAndAlphaAsHexString(color, OPAQUE_ALPHA_VALUE);
    }

    @UiThread
    @Override
    public GlobalBounds resolveCheckableBounds(CheckedTextView view, float pixelsDensity) {
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, pixelsDensity);
        long textViewPaddingRight = densityNormalized(view.getTotalPaddingRight(), pixelsDensity);

        long checkBoxHeight = 0;
        Drawable checkMarkDrawable = view.getCheckMarkDrawable();
        if (checkMarkDrawable != null && checkMarkDrawable.getIntrinsicHeight() > 0) {
            int height = checkMarkDrawable.getIntrinsicHeight() - view.getTotalPaddingTop() - view.getTotalPaddingBottom();
            checkBoxHeight = densityNormalized(height, pixelsDensity);
        }

        return new GlobalBounds(
                viewGlobalBounds.getX() + viewGlobalBounds.getWidth() - textViewPaddingRight,
                viewGlobalBounds.getY(),
                checkBoxHeight,
                checkBoxHeight
        );
    }

    @Override
    public Drawable getCheckableDrawable(CheckedTextView view) {
        // The drawable of CheckedTextView cannot be directly obtained according to the state
        // Therefore, here two hard-coded indexes are used to get the drawable of "checked" and "unchecked".
        int checkableDrawableIndex = view.isChecked() ? CHECK_BOX_CHECKED_DRAWABLE_INDEX : CHECK_BOX_NOT_CHECKED_DRAWABLE_INDEX;

        Drawable.ConstantState constantState = (view.getCheckMarkDrawable() != null) ? view.getCheckMarkDrawable().getConstantState() : null;
        if (constantState instanceof DrawableContainer.DrawableContainerState) {
            return ((DrawableContainer.DrawableContainerState) constantState).getChild(checkableDrawableIndex);
        }
        return null;
    }

    @Override
    public Drawable cloneCheckableDrawable(CheckedTextView view, Drawable drawable) {
        if (drawable.getConstantState() == null) {
            return null;
        }
        Drawable clonedDrawable = drawable.getConstantState().newDrawable(view.getResources());
        if (clonedDrawable != null) {
            // Set the state, so that the drawable is correctly applied according to the state
            clonedDrawable.setState(view.getDrawableState());

            // If the button declares the `checkMarkTint` attribute, set the tint for the drawable.
            if (view.getCheckMarkTintList() != null) {
                clonedDrawable.setTintList(view.getCheckMarkTintList());
            }
        }
        return clonedDrawable;
    }

    private long densityNormalized(int value, float density) {
        return (long) (value / density);
    }
}

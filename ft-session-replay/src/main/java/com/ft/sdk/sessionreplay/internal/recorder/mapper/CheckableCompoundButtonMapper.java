package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
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

import java.lang.reflect.Field;

import kotlin.Suppress;

public abstract class CheckableCompoundButtonMapper<T extends CompoundButton> extends CheckableTextViewMapper<T> {
    public static final long MIN_PADDING_IN_PX = 20L;
    public static final long DEFAULT_CHECKABLE_HEIGHT_IN_PX = 84L;
    private static final long DEFAULT_CHECKABLE_HEIGHT_IN_DP = 32L;
    private static final String GET_DRAWABLE_FAIL_MESSAGE = "Failed to get buttonDrawable from the checkable compound button.";
    private static final String NULL_BUTTON_DRAWABLE_MSG = "ButtonDrawable of the compound button is null";
    private static Field mButtonDrawableField;

    static {
        try {
            mButtonDrawableField = CompoundButton.class.getDeclaredField("mButtonDrawable");
            mButtonDrawableField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException | NullPointerException e) {
            mButtonDrawableField = null;
        }
    }
    public CheckableCompoundButtonMapper(
            TextViewMapper<T> textWireframeMapper,
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper) {
        super(textWireframeMapper, viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    public GlobalBounds resolveCheckableBounds(T view, float pixelsDensity) {
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, pixelsDensity);
        long checkBoxHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Drawable buttonDrawable = view.getButtonDrawable();
            checkBoxHeight = (buttonDrawable != null && buttonDrawable.getIntrinsicHeight() > 0)
                    ? Utils.densityNormalized(buttonDrawable.getIntrinsicHeight(), pixelsDensity)
                    : DEFAULT_CHECKABLE_HEIGHT_IN_DP;
        } else {
            checkBoxHeight = DEFAULT_CHECKABLE_HEIGHT_IN_DP;
        }
        return new GlobalBounds(
                viewGlobalBounds.getX(),
                viewGlobalBounds.getY() + (viewGlobalBounds.getHeight() - checkBoxHeight) / 2,
                checkBoxHeight,
                checkBoxHeight
        );
    }

    public Drawable getCheckableDrawable(T view) {
        Drawable originCheckableDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkableDrawableIndex = view.isChecked() ? CHECK_BOX_CHECKED_DRAWABLE_INDEX : CHECK_BOX_NOT_CHECKED_DRAWABLE_INDEX;
            Drawable buttonDrawable = view.getButtonDrawable();
            if (buttonDrawable != null && buttonDrawable.getConstantState() instanceof DrawableContainer.DrawableContainerState) {
                originCheckableDrawable = ((DrawableContainer.DrawableContainerState) buttonDrawable.getConstantState()).getChild(checkableDrawableIndex);
            } else {
                logError(NULL_BUTTON_DRAWABLE_MSG, view.getClass().getCanonicalName());
                originCheckableDrawable = null;
            }
        } else {
            try {
                if (mButtonDrawableField != null) {
                    originCheckableDrawable = (Drawable) mButtonDrawableField.get(view);
                } else {
                    originCheckableDrawable = null;
                }
            } catch (IllegalAccessException | IllegalArgumentException e) {
                logError(GET_DRAWABLE_FAIL_MESSAGE, e);
                originCheckableDrawable = null;
            }
        }
        return originCheckableDrawable;
    }

    public Drawable cloneCheckableDrawable(T view, Drawable drawable) {
        Drawable.ConstantState constantState = drawable.getConstantState();
        if (constantState == null) return null;

        Drawable newDrawable = constantState.newDrawable(view.getResources());
        newDrawable.setState(view.getDrawableState());
        if (view.getButtonTintList() != null) {
            newDrawable.setTintList(view.getButtonTintList());
        }
        return newDrawable;
    }

    private void logError(String message, Object additionalInfo) {
        // 实现你的日志记录逻辑，这里可以替换为你的 InternalLogger 逻辑
        System.err.println(message + ": " + additionalInfo);
    }


}

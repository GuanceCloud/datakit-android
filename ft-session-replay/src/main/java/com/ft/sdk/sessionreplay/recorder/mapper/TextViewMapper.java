package com.ft.sdk.sessionreplay.recorder.mapper;

import static com.ft.sdk.sessionreplay.ColorConstant.OPAQUE_ALPHA_VALUE;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.recorder.obfuscator.StringObfuscator;
import com.ft.sdk.sessionreplay.model.Alignment;
import com.ft.sdk.sessionreplay.model.Horizontal;
import com.ft.sdk.sessionreplay.model.Padding;
import com.ft.sdk.sessionreplay.model.TextPosition;
import com.ft.sdk.sessionreplay.model.TextStyle;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.Vertical;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.Utils;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;

public class TextViewMapper<T extends TextView> extends BaseAsyncBackgroundWireframeMapper<T> {

    public static final String FIXED_INPUT_MASK = "***";
    private static final String SANS_SERIF_FAMILY_NAME = "roboto, sans-serif";
    private static final String SERIF_FAMILY_NAME = "serif";
    private static final String MONOSPACE_FAMILY_NAME = "monospace";

    public TextViewMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @UiThread
    public List<Wireframe> map(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        List<Wireframe> wireframes = new ArrayList<>();

        // add background if needed
        wireframes.addAll(super.map(view, mappingContext, asyncJobStatusCallback, internalLogger));

        float density = mappingContext.getSystemInformation().getScreenDensity();
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, density);

        wireframes.add(createTextWireframe(view, mappingContext, viewGlobalBounds));

        wireframes.addAll(mappingContext.getImageWireframeHelper().createCompoundDrawableWireframes(
                view,
                mappingContext,
                wireframes.size(),
                null,
                asyncJobStatusCallback
        ));

        return wireframes;
    }

    protected String resolveCapturedText(
            T textView,
            TextAndInputPrivacy textAndInputPrivacy,
            boolean isOption) {

        String originalText = resolveLayoutText(textView);

        switch (textAndInputPrivacy) {
            case MASK_SENSITIVE_INPUTS:
                return originalText;

            case MASK_ALL:
                return isOption ? FIXED_INPUT_MASK
                        : StringObfuscator.getStringObfuscator().obfuscate(originalText);

            case MASK_ALL_INPUTS:
                return isOption ? FIXED_INPUT_MASK : originalText;

            default:
                throw new IllegalArgumentException("Unknown TextAndInputPrivacy: " + textAndInputPrivacy);
        }
    }

    private String resolveLayoutText(T textView) {
        CharSequence text = (textView.getLayout() != null && textView.getLayout().getText() != null)
                ? textView.getLayout().getText()
                : textView.getText();

        return text != null ? text.toString() : "";
    }

    protected TextWireframe createTextWireframe(
            T textView,
            MappingContext mappingContext,
            GlobalBounds viewGlobalBounds
    ) {
        String capturedText = resolveCapturedText(textView, mappingContext.getTextAndInputPrivacy(), mappingContext.isHasOptionSelectorParent());
        return new TextWireframe(
                resolveViewId(textView),
                viewGlobalBounds.getX(),
                viewGlobalBounds.getY(),
                viewGlobalBounds.getWidth(),
                viewGlobalBounds.getHeight(),
                null,
                null,
                null,
                capturedText,
                resolveTextStyle(textView, mappingContext.getSystemInformation().getScreenDensity()),
                resolveTextPosition(textView, mappingContext.getSystemInformation().getScreenDensity())
        );
    }

    private TextStyle resolveTextStyle(T textView, float pixelsDensity) {
        return new TextStyle(
                resolveFontFamily(textView.getTypeface()),
                Utils.densityNormalized(textView.getTextSize(), pixelsDensity),
                resolveTextColor(textView)
        );
    }

    private String resolveTextColor(T textView) {
        if (textView.getText() == null || textView.getText().toString().isEmpty()) {
            return resolveHintTextColor(textView);
        } else {
            return colorStringFormatter.formatColorAndAlphaAsHexString(textView.getCurrentTextColor(), OPAQUE_ALPHA_VALUE);
        }
    }

    private String resolveHintTextColor(T textView) {
        if (textView.getHintTextColors() != null) {
            return colorStringFormatter.formatColorAndAlphaAsHexString(textView.getHintTextColors().getDefaultColor(), OPAQUE_ALPHA_VALUE);
        } else {
            return colorStringFormatter.formatColorAndAlphaAsHexString(textView.getCurrentTextColor(), OPAQUE_ALPHA_VALUE);
        }
    }

    private String resolveFontFamily(Typeface typeface) {
        if (Typeface.SANS_SERIF.equals(typeface)) {
            return SANS_SERIF_FAMILY_NAME;
        } else if (Typeface.MONOSPACE.equals(typeface)) {
            return MONOSPACE_FAMILY_NAME;
        } else if (Typeface.SERIF.equals(typeface)) {
            return SERIF_FAMILY_NAME;
        } else {
            return SANS_SERIF_FAMILY_NAME;
        }
    }

    private TextPosition resolveTextPosition(T textView, float pixelsDensity) {
        return new TextPosition(
                resolvePadding(textView, pixelsDensity),
                resolveAlignment(textView)
        );
    }

    private Padding resolvePadding(T textView, float pixelsDensity) {
        return new Padding(
                (long) Utils.densityNormalized(textView.getTotalPaddingTop(), pixelsDensity),
                (long) Utils.densityNormalized(textView.getTotalPaddingBottom(), pixelsDensity),
                (long) Utils.densityNormalized(textView.getTotalPaddingStart(), pixelsDensity),
                (long) Utils.densityNormalized(textView.getTotalPaddingEnd(), pixelsDensity)
        );
    }

    private Alignment resolveAlignment(T textView) {
        switch (textView.getTextAlignment()) {
            case TextView.TEXT_ALIGNMENT_CENTER:
                return new Alignment(
                        Horizontal.CENTER,
                        Vertical.CENTER
                );
            case TextView.TEXT_ALIGNMENT_TEXT_END:
            case TextView.TEXT_ALIGNMENT_VIEW_END:
                return new Alignment(
                        Horizontal.RIGHT,
                        Vertical.CENTER
                );
            case TextView.TEXT_ALIGNMENT_TEXT_START:
            case TextView.TEXT_ALIGNMENT_VIEW_START:
                return new Alignment(
                        Horizontal.LEFT,
                        Vertical.CENTER
                );
            case TextView.TEXT_ALIGNMENT_GRAVITY:
                return resolveAlignmentFromGravity(textView);
            default:
                return new Alignment(
                        Horizontal.LEFT,
                        Vertical.CENTER
                );
        }
    }

    private Alignment resolveAlignmentFromGravity(T textView) {
        Horizontal horizontalAlignment;
        switch (textView.getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.START:
            case Gravity.LEFT:
                horizontalAlignment = Horizontal.LEFT;
                break;
            case Gravity.END:
            case Gravity.RIGHT:
                horizontalAlignment = Horizontal.RIGHT;
                break;
            case Gravity.CENTER_HORIZONTAL:
            case Gravity.CENTER:
                horizontalAlignment = Horizontal.CENTER;
                break;
            default:
                horizontalAlignment = Horizontal.LEFT;
                break;
        }

        Vertical verticalAlignment;
        switch (textView.getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
                verticalAlignment = Vertical.TOP;
                break;
            case Gravity.BOTTOM:
                verticalAlignment = Vertical.BOTTOM;
                break;
            case Gravity.CENTER_VERTICAL:
            case Gravity.CENTER:
                verticalAlignment = Vertical.CENTER;
                break;
            default:
                verticalAlignment = Vertical.CENTER;
                break;
        }

        return new Alignment(horizontalAlignment, verticalAlignment);
    }
}

package com.ft.sdk.sessionreplay.recorder.mapper;

import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.recorder.obfuscator.StringObfuscator;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EditTextMapper extends TextViewMapper<EditText> {

    public EditTextMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @Override
    protected String resolveCapturedText(EditText textView, TextAndInputPrivacy textAndInputPrivacy, boolean isOption) {
        String text = textView.getText() != null ? textView.getText().toString() : "";
        String hint = textView.getHint() != null ? textView.getHint().toString() : "";

        return !text.isEmpty()
                ? resolveCapturedText(textView, text, textAndInputPrivacy)
                : resolveCapturedHint(hint, textAndInputPrivacy);
    }

    private String resolveCapturedText(TextView textView, String text, TextAndInputPrivacy textAndInputPrivacy) {
        int inputTypeVariation = textView.getInputType() & InputType.TYPE_MASK_VARIATION;
        int inputTypeClass = textView.getInputType() & InputType.TYPE_MASK_CLASS;

        boolean isSensitiveText = (inputTypeClass == InputType.TYPE_CLASS_TEXT) &&
                SENSITIVE_TEXT_VARIATIONS.contains(inputTypeVariation);

        boolean isSensitiveNumber = (inputTypeClass == InputType.TYPE_CLASS_NUMBER) &&
                SENSITIVE_NUMBER_VARIATIONS.contains(inputTypeVariation);

        boolean isSensitive = isSensitiveText || isSensitiveNumber || (inputTypeClass == InputType.TYPE_CLASS_PHONE);

        switch (textAndInputPrivacy) {
            case MASK_SENSITIVE_INPUTS:
                return isSensitive ? FIXED_INPUT_MASK : text;

            case MASK_ALL:
            case MASK_ALL_INPUTS:
                return FIXED_INPUT_MASK;

            default:
                return text;
        }
    }

    private String resolveCapturedHint(String hint, TextAndInputPrivacy textAndInputPrivacy) {
        return textAndInputPrivacy == TextAndInputPrivacy.MASK_ALL
                ? StringObfuscator.getStringObfuscator().obfuscate(hint)
                : hint;
    }

    private static final List<Integer> SENSITIVE_TEXT_VARIATIONS = Arrays.asList(
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
    );

    private static final List<Integer> SENSITIVE_NUMBER_VARIATIONS = Collections.singletonList(
            InputType.TYPE_NUMBER_VARIATION_PASSWORD
    );
}

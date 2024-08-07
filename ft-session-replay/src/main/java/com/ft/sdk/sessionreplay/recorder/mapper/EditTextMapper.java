package com.ft.sdk.sessionreplay.recorder.mapper;

import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.sessionreplay.internal.recorder.obfuscator.StringObfuscator;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.Arrays;

public class EditTextMapper extends TextViewMapper<EditText> {

    private static final int[] SENSITIVE_TEXT_VARIATIONS = {
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
    };

    private static final int[] SENSITIVE_NUMBER_VARIATIONS = {
            InputType.TYPE_NUMBER_VARIATION_PASSWORD
    };

    public EditTextMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @Override
    public String resolveCapturedText(EditText editText, SessionReplayPrivacy privacy, boolean isOption) {
        String text = editText.getText().toString();
        String hint = editText.getHint() != null ? editText.getHint().toString() : "";

        if (!text.isEmpty()) {
            return resolveCapturedText(editText, text, privacy);
        } else {
            return resolveCapturedHint(hint, privacy);
        }
    }

    private String resolveCapturedText(TextView textView, String text, SessionReplayPrivacy privacy) {
        int inputTypeVariation = textView.getInputType() & InputType.TYPE_MASK_VARIATION;
        int inputTypeClass = textView.getInputType() & InputType.TYPE_MASK_CLASS;

        boolean isSensitiveText = (inputTypeClass == InputType.TYPE_CLASS_TEXT) &&
                Arrays.stream(SENSITIVE_TEXT_VARIATIONS).anyMatch(variation -> variation == inputTypeVariation);

        boolean isSensitiveNumber = (inputTypeClass == InputType.TYPE_CLASS_NUMBER) &&
                Arrays.stream(SENSITIVE_NUMBER_VARIATIONS).anyMatch(variation -> variation == inputTypeVariation);

        boolean isSensitive = isSensitiveText || isSensitiveNumber || (inputTypeClass == InputType.TYPE_CLASS_PHONE);

        switch (privacy) {
            case ALLOW:
                return isSensitive ? FIXED_INPUT_MASK : text;
            case MASK:
            case MASK_USER_INPUT:
                return FIXED_INPUT_MASK;
            default:
                return text;
        }
    }

    private String resolveCapturedHint(String hint, SessionReplayPrivacy privacy) {
        if (privacy == SessionReplayPrivacy.MASK) {
            return StringObfuscator.getStringObfuscator().obfuscate(hint);
        } else {
            return hint;
        }
    }
}

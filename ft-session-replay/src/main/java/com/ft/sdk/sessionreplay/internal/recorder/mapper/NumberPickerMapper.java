package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import static com.ft.sdk.sessionreplay.ColorConstant.PARTIALLY_OPAQUE_ALPHA_VALUE;

import android.os.Build;
import android.widget.NumberPicker;

import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.Q)
public class NumberPickerMapper extends BasePickerMapper {

    public NumberPickerMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @Override
    @UiThread
    public List<Wireframe> map(
            NumberPicker view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        Long prevIndexLabelId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, PREV_INDEX_KEY_NAME);
        Long selectedIndexLabelId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, SELECTED_INDEX_KEY_NAME);
        Long topDividerId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, DIVIDER_TOP_KEY_NAME);
        Long bottomDividerId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, DIVIDER_BOTTOM_KEY_NAME);
        Long nextIndexLabelId = viewIdentifierResolver.resolveChildUniqueIdentifier(view, NEXT_INDEX_KEY_NAME);

        if (selectedIndexLabelId != null && topDividerId != null && bottomDividerId != null
                && prevIndexLabelId != null && nextIndexLabelId != null) {
            return map(
                    view,
                    mappingContext.getSystemInformation(),
                    mappingContext.getTextAndInputPrivacy(),
                    prevIndexLabelId,
                    topDividerId,
                    selectedIndexLabelId,
                    bottomDividerId,
                    nextIndexLabelId
            );
        }

        return new ArrayList<>();
    }

    @SuppressWarnings("LongMethod")
    private List<Wireframe> map(
            NumberPicker view,
            SystemInformation systemInformation,
            TextAndInputPrivacy privacy,
            Long prevIndexLabelId,
            Long topDividerId,
            Long selectedIndexLabelId,
            Long bottomDividerId,
            Long nextIndexLabelId
    ) {
        float screenDensity = systemInformation.getScreenDensity();
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(view, screenDensity);
        long textSize = resolveTextSize(view, screenDensity);
        long labelHeight = textSize * 2;
        long paddingStart = resolveDividerPaddingStart(view, screenDensity);
        long paddingEnd = resolveDividerPaddingEnd(view, screenDensity);
        String textColor = resolveSelectedTextColor(view);
        String nextPrevLabelTextColor = colorStringFormatter
                .formatColorAndAlphaAsHexString(view.getTextColor(), PARTIALLY_OPAQUE_ALPHA_VALUE);
        long padding = resolvePadding(screenDensity);
        long selectedLabelYPos = resolveSelectedLabelYPos(viewGlobalBounds, labelHeight);
        long dividerHeight = resolveDividerHeight(screenDensity);

        long topDividerYPos = selectedLabelYPos - dividerHeight - padding;
        long bottomDividerYPos = selectedLabelYPos + labelHeight + padding;
        long prevLabelYPos = topDividerYPos - labelHeight - padding;
        long nextLabelYPos = bottomDividerYPos + padding;
        long dividerWidth = viewGlobalBounds.getWidth() - paddingEnd - paddingStart;
        long dividerXPos = viewGlobalBounds.getX() + paddingStart;

        Wireframe prevValueLabelWireframe = provideLabelWireframe(
                prevIndexLabelId,
                viewGlobalBounds.getX(),
                prevLabelYPos,
                labelHeight,
                viewGlobalBounds.getWidth(),
                resolvePrevLabelValue(view),
                textSize,
                nextPrevLabelTextColor
        );

        Wireframe topDividerWireframe = provideDividerWireframe(
                topDividerId,
                dividerXPos,
                topDividerYPos,
                dividerWidth,
                dividerHeight,
                textColor
        );

        TextWireframe selectedValueLabelWireframe = provideLabelWireframe(
                selectedIndexLabelId,
                viewGlobalBounds.getX(),
                selectedLabelYPos,
                labelHeight,
                viewGlobalBounds.getWidth(),
                resolveSelectedLabelValue(view),
                textSize,
                textColor
        );

        Wireframe bottomDividerWireframe = provideDividerWireframe(
                bottomDividerId,
                dividerXPos,
                bottomDividerYPos,
                dividerWidth,
                dividerHeight,
                textColor
        );

        Wireframe nextValueLabelWireframe = provideLabelWireframe(
                nextIndexLabelId,
                viewGlobalBounds.getX(),
                nextLabelYPos,
                labelHeight,
                viewGlobalBounds.getWidth(),
                resolveNextLabelValue(view),
                textSize,
                nextPrevLabelTextColor
        );

        if (privacy == TextAndInputPrivacy.MASK_SENSITIVE_INPUTS) {
            return List.of(
                    prevValueLabelWireframe,
                    topDividerWireframe,
                    selectedValueLabelWireframe,
                    bottomDividerWireframe,
                    nextValueLabelWireframe
            );
        } else {
            return List.of(
                    topDividerWireframe,
                    selectedValueLabelWireframe.setText(DEFAULT_MASKED_TEXT_VALUE),
                    bottomDividerWireframe
            );
        }
    }

    private String resolvePrevLabelValue(NumberPicker view) {
        return resolveLabelValue(view, getPrevIndex(view));
    }

    private String resolveNextLabelValue(NumberPicker view) {
        return resolveLabelValue(view, getNextIndex(view));
    }

    private String resolveSelectedLabelValue(NumberPicker view) {
        return resolveLabelValue(view, view.getValue());
    }

    private int getPrevIndex(NumberPicker view) {
        return view.getValue() > view.getMinValue() ? view.getValue() - 1 : view.getMaxValue();
    }

    private int getNextIndex(NumberPicker view) {
        return view.getValue() < view.getMaxValue() ? view.getValue() + 1 : view.getMinValue();
    }

    private String resolveLabelValue(NumberPicker numberPicker, int index) {
        int normalizedIndex = index - numberPicker.getMinValue();
        if (numberPicker.getDisplayedValues() != null && numberPicker.getDisplayedValues().length > normalizedIndex) {
            return numberPicker.getDisplayedValues()[normalizedIndex];
        }
        return String.valueOf(index);
    }

    public static final String DEFAULT_MASKED_TEXT_VALUE = "xxx";
}

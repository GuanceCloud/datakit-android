package com.ft.sdk.sessionreplay.internal;

import android.app.Application;
import android.os.Build;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.ActionBarContainer;
import androidx.appcompat.widget.SwitchCompat;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.MapperTypeWrapper;
import com.ft.sdk.sessionreplay.SessionReplayInternalCallback;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.recorder.Recorder;
import com.ft.sdk.sessionreplay.internal.recorder.SessionReplayRecorder;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.ActionBarContainerMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.ButtonMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.CheckBoxMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.CheckedTextViewMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.ImageViewMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.NumberPickerMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.ProgressBarWireframeMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.RadioButtonMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.SeekBarWireframeMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.SwitchCompatMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.WebViewWireframeMapper;
import com.ft.sdk.sessionreplay.internal.resources.ResourceDataStoreManager;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.ResourcesWriter;
import com.ft.sdk.sessionreplay.internal.time.SessionReplayTimeProvider;
import com.ft.sdk.sessionreplay.internal.utils.ImageViewUtils;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.recorder.mapper.EditTextMapper;
import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;
import com.ft.sdk.sessionreplay.resources.DefaultDrawableCopier;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.DefaultViewIdentifierResolver;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapperFactory;
import com.ft.sdk.sessionreplay.utils.RumContextProvider;
import com.ft.sdk.sessionreplay.utils.TimeProvider;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.ArrayList;
import java.util.List;

public class DefaultRecorderProvider implements RecorderProvider {

    private final FeatureSdkCore sdkCore;
    private final TextAndInputPrivacy textAndInputPrivacy;
    private final ImagePrivacy imagePrivacy;
    private final TouchPrivacyManager touchPrivacyManager;
    private final List<MapperTypeWrapper<?>> customMappers;
    private final List<OptionSelectorDetector> customOptionSelectorDetectors;
    private final List<DrawableToColorMapper> customDrawableMappers;
    private final boolean dynamicOptimizationEnabled;
    private final SessionReplayInternalCallback internalCallback;
    private final boolean isDelayInit;

    public DefaultRecorderProvider(
            FeatureSdkCore sdkCore,
            TextAndInputPrivacy textAndInputPrivacy,
            ImagePrivacy imagePrivacy,
            TouchPrivacyManager touchPrivacyManager,
            List<MapperTypeWrapper<?>> customMappers,
            List<OptionSelectorDetector> customOptionSelectorDetectors,
            List<DrawableToColorMapper> customDrawableMappers,
            boolean dynamicOptimizationEnabled,
            SessionReplayInternalCallback internalCallback,
            boolean isDelayInit) {
        this.sdkCore = sdkCore;
        this.textAndInputPrivacy = textAndInputPrivacy;
        this.imagePrivacy = imagePrivacy;
        this.touchPrivacyManager = touchPrivacyManager;
        this.customMappers = customMappers;
        this.customOptionSelectorDetectors = customOptionSelectorDetectors;
        this.customDrawableMappers = customDrawableMappers;
        this.dynamicOptimizationEnabled = dynamicOptimizationEnabled;
        this.internalCallback = internalCallback;
        this.isDelayInit = isDelayInit;
    }

    @Override
    public Recorder provideSessionReplayRecorder(
            ResourceDataStoreManager resourceDataStoreManager,
            ResourcesWriter resourceWriter,
            RecordWriter recordWriter,
            Application application) {

        RumContextProvider rumContextProvider = new SessionReplayRumContextProvider(sdkCore);
        TimeProvider timeProvider = new SessionReplayTimeProvider(sdkCore);

        List<MapperTypeWrapper<?>> mappers = new ArrayList<>(customMappers);
        mappers.addAll(builtInMappers());

        return new SessionReplayRecorder(
                application,
                resourceWriter,
                rumContextProvider,
                textAndInputPrivacy,
                imagePrivacy,
                touchPrivacyManager,
                recordWriter,
                timeProvider,
                mappers,
                customOptionSelectorDetectors,
                customDrawableMappers,
                null,
                sdkCore,
                resourceDataStoreManager,
                internalCallback,
                dynamicOptimizationEnabled,
                isDelayInit
        );
    }

    private List<MapperTypeWrapper<?>> builtInMappers() {
        ViewIdentifierResolver viewIdentifierResolver = DefaultViewIdentifierResolver.get();
        ColorStringFormatter colorStringFormatter = DefaultColorStringFormatter.get();
        ViewBoundsResolver viewBoundsResolver = DefaultViewBoundsResolver.get();
        DrawableToColorMapper drawableToColorMapper = DrawableToColorMapperFactory.getDefault();
        ImageViewMapper imageViewMapper = new ImageViewMapper(
                ImageViewUtils.get(),
                new DefaultDrawableCopier(),
                viewIdentifierResolver,
                colorStringFormatter,
                viewBoundsResolver,
                drawableToColorMapper
        );
        TextViewMapper textViewMapper = new TextViewMapper<TextView>(
                viewIdentifierResolver,
                colorStringFormatter,
                viewBoundsResolver,
                drawableToColorMapper
        );

        List<MapperTypeWrapper<?>> mappersList = new ArrayList<>();
        mappersList.add(new MapperTypeWrapper<>(SwitchCompat.class,
                new SwitchCompatMapper(
                        (TextViewMapper<SwitchCompat>) textViewMapper,
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        ));
        mappersList.add(new MapperTypeWrapper<>(RadioButton.class,
                new RadioButtonMapper(
                        (TextViewMapper<RadioButton>) textViewMapper,
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        ));
        mappersList.add(new MapperTypeWrapper<>(CheckBox.class,
                new CheckBoxMapper(
                        (TextViewMapper<CheckBox>) textViewMapper,
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        ));
        mappersList.add(new MapperTypeWrapper<>(CheckedTextView.class,
                new CheckedTextViewMapper(
                        (TextViewMapper<CheckedTextView>) textViewMapper,
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        ));
        mappersList.add(new MapperTypeWrapper<>(EditText.class,
                new EditTextMapper(
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        ));
        mappersList.add(new MapperTypeWrapper<>(Button.class,
                new ButtonMapper(
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        ));
        mappersList.add(new MapperTypeWrapper<>(TextView.class, textViewMapper));
        mappersList.add(new MapperTypeWrapper<>(ImageView.class, imageViewMapper));
        mappersList.add(new MapperTypeWrapper<>(ActionBarContainer.class,
                new ActionBarContainerMapper(
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        ));
        mappersList.add(new MapperTypeWrapper<>(WebView.class,
                new WebViewWireframeMapper(
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        ));
        mappersList.add(new MapperTypeWrapper<>(SeekBar.class,
                new SeekBarWireframeMapper(
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper
                )
        ));
        mappersList.add(new MapperTypeWrapper<>(ProgressBar.class,
                new ProgressBarWireframeMapper(
                        viewIdentifierResolver,
                        colorStringFormatter,
                        viewBoundsResolver,
                        drawableToColorMapper,
                        true
                )
        ));

        WireframeMapper<NumberPicker> numberPickerMapper = getNumberPickerMapper(
                viewIdentifierResolver,
                colorStringFormatter,
                viewBoundsResolver,
                drawableToColorMapper
        );
        if (numberPickerMapper != null) {
            mappersList.add(0, new MapperTypeWrapper<>(NumberPicker.class, numberPickerMapper));
        }

        return mappersList;
    }

    private WireframeMapper<NumberPicker> getNumberPickerMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new NumberPickerMapper(
                    viewIdentifierResolver,
                    colorStringFormatter,
                    viewBoundsResolver,
                    drawableToColorMapper
            );
        } else {
            return null;
        }
    }
}

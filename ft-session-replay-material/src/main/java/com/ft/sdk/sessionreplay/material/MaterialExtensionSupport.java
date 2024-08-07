package com.ft.sdk.sessionreplay.material;

import com.ft.sdk.sessionreplay.ExtensionSupport;
import com.ft.sdk.sessionreplay.MapperTypeWrapper;
import com.ft.sdk.sessionreplay.material.internal.MaterialOptionSelectorDetector;
import com.ft.sdk.sessionreplay.material.internal.SliderWireframeMapper;
import com.ft.sdk.sessionreplay.material.internal.TabWireframeMapper;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.recorder.mapper.TextViewMapper;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.DefaultViewIdentifierResolver;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;
import java.util.List;

public class MaterialExtensionSupport implements ExtensionSupport {

    private final ViewIdentifierResolver viewIdentifierResolver = DefaultViewIdentifierResolver.get();
    private final ColorStringFormatter colorStringFormatter = DefaultColorStringFormatter.get();
    private final ViewBoundsResolver viewBoundsResolver = DefaultViewBoundsResolver.get();
    private final DrawableToColorMapper drawableToColorMapper = DrawableToColorMapper.getDefault();

    @Override
    public List<MapperTypeWrapper<?>> getCustomViewMappers() {
        SliderWireframeMapper sliderWireframeMapper = new SliderWireframeMapper(
            viewIdentifierResolver,
            colorStringFormatter,
            viewBoundsResolver
        );

        TabWireframeMapper tabWireframeMapper = new TabWireframeMapper(
            viewIdentifierResolver,
            viewBoundsResolver,
            new TextViewMapper(
                viewIdentifierResolver,
                colorStringFormatter,
                viewBoundsResolver,
                drawableToColorMapper
            )
        );

        return Arrays.asList(
            new MapperTypeWrapper<>(Slider.class, sliderWireframeMapper),
            new MapperTypeWrapper<>(TabLayout.TabView.class, tabWireframeMapper)
        );
    }

    @Override
    public List<OptionSelectorDetector> getOptionSelectorDetectors() {
        return Arrays.asList(new MaterialOptionSelectorDetector());
    }
}

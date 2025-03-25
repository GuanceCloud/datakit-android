package com.ft.sdk.sessionreplay.material;

import androidx.cardview.widget.CardView;

import com.ft.sdk.sessionreplay.ExtensionSupport;
import com.ft.sdk.sessionreplay.MapperTypeWrapper;
import com.ft.sdk.sessionreplay.material.internal.CardWireframeMapper;
import com.ft.sdk.sessionreplay.material.internal.ChipWireframeMapper;
import com.ft.sdk.sessionreplay.material.internal.MaterialDrawableToColorMapper;
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
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapperFactory;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;
import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MaterialExtensionSupport implements ExtensionSupport {

    private final ViewIdentifierResolver viewIdentifierResolver = DefaultViewIdentifierResolver.get();
    private final ColorStringFormatter colorStringFormatter = DefaultColorStringFormatter.get();
    private final ViewBoundsResolver viewBoundsResolver = DefaultViewBoundsResolver.get();
    private final MaterialDrawableToColorMapper materialDrawableToColorMapper = new MaterialDrawableToColorMapper();
    //fixme
    private final DrawableToColorMapper drawableToColorMapper = DrawableToColorMapperFactory.getDefault();

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


        CardWireframeMapper cardWireframeMapper = new CardWireframeMapper(
                viewIdentifierResolver,
                colorStringFormatter,
                viewBoundsResolver,
                drawableToColorMapper
        );
        ChipWireframeMapper chipWireframeMapper = new ChipWireframeMapper(
                viewIdentifierResolver,
                colorStringFormatter,
                viewBoundsResolver,
                drawableToColorMapper
        );

        return new ArrayList<>(Arrays.asList(
                new MapperTypeWrapper<>(Slider.class, sliderWireframeMapper),
                new MapperTypeWrapper<>(TabLayout.TabView.class, tabWireframeMapper),
                new MapperTypeWrapper<>(CardView.class, cardWireframeMapper),
                new MapperTypeWrapper<>(Chip.class, chipWireframeMapper)
        )
        );
    }

    @Override
    public List<OptionSelectorDetector> getOptionSelectorDetectors() {
        return new ArrayList<>(Collections.singletonList(new MaterialOptionSelectorDetector()));
    }

    @Override
    public List<DrawableToColorMapper> getCustomDrawableMapper() {
        return new ArrayList<>(Collections.singletonList(materialDrawableToColorMapper));
    }
}

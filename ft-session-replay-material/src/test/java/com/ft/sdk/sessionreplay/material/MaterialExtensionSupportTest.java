package com.ft.sdk.sessionreplay.material;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import com.ft.sdk.sessionreplay.MapperTypeWrapper;
import com.ft.sdk.sessionreplay.material.internal.CardWireframeMapper;
import com.ft.sdk.sessionreplay.material.internal.ChipWireframeMapper;
import com.ft.sdk.sessionreplay.material.internal.MaterialDrawableToColorMapper;
import com.ft.sdk.sessionreplay.material.internal.MaterialOptionSelectorDetector;
import com.ft.sdk.sessionreplay.material.internal.SliderWireframeMapper;
import com.ft.sdk.sessionreplay.material.internal.TabWireframeMapper;
import com.google.android.material.chip.Chip;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MaterialExtensionSupportTest {

    @Test
    public void getCustomViewMappers_shouldExposeAllSupportedMaterialMappings() {
        MaterialExtensionSupport support = new MaterialExtensionSupport();

        List<MapperTypeWrapper<?>> mappers = support.getCustomViewMappers();

        assertEquals(4, mappers.size());
        assertEquals(Slider.class, mappers.get(0).getType());
        assertTrue(mappers.get(0).getMapper() instanceof SliderWireframeMapper);
        assertEquals(TabLayout.TabView.class, mappers.get(1).getType());
        assertTrue(mappers.get(1).getMapper() instanceof TabWireframeMapper);
        assertEquals(androidx.cardview.widget.CardView.class, mappers.get(2).getType());
        assertTrue(mappers.get(2).getMapper() instanceof CardWireframeMapper);
        assertEquals(Chip.class, mappers.get(3).getType());
        assertTrue(mappers.get(3).getMapper() instanceof ChipWireframeMapper);
    }

    @Test
    public void getOptionSelectorDetectors_shouldDetectMaterialDatePickerHeader() {
        MaterialExtensionSupport support = new MaterialExtensionSupport();
        FrameLayout header = new FrameLayout(ApplicationProvider.getApplicationContext());
        header.setId(com.google.android.material.R.id.mtrl_picker_header);
        FrameLayout other = new FrameLayout(ApplicationProvider.getApplicationContext());
        other.setId(android.R.id.content);

        MaterialOptionSelectorDetector detector =
                (MaterialOptionSelectorDetector) support.getOptionSelectorDetectors().get(0);

        assertTrue(detector.isOptionSelector(header));
        assertFalse(detector.isOptionSelector(other));
    }

    @Test
    public void getCustomDrawableMapper_shouldResolveMaterialShapeFillColorOnly() {
        MaterialDrawableToColorMapper mapper =
                (MaterialDrawableToColorMapper) new MaterialExtensionSupport()
                        .getCustomDrawableMapper().get(0);

        MaterialShapeDrawable drawable = new MaterialShapeDrawable();
        drawable.setFillColor(ColorStateList.valueOf(Color.RED));

        assertEquals(Integer.valueOf(Color.RED), mapper.mapDrawableToColor(drawable, null));
        // Only MaterialShapeDrawable is supported; other drawable types should fall through.
        assertNull(mapper.mapDrawableToColor(new ColorDrawable(Color.BLUE), null));

        MaterialShapeDrawable withoutFill = new MaterialShapeDrawable();
        withoutFill.setFillColor(null);
        assertNull(mapper.mapDrawableToColor(withoutFill, null));
    }

    @Test
    public void supportFactories_shouldReturnFreshLists() {
        MaterialExtensionSupport support = new MaterialExtensionSupport();

        List<MapperTypeWrapper<?>> first = support.getCustomViewMappers();
        List<MapperTypeWrapper<?>> second = support.getCustomViewMappers();

        assertNotNull(first);
        assertNotNull(second);
        assertFalse(first == second);
    }
}

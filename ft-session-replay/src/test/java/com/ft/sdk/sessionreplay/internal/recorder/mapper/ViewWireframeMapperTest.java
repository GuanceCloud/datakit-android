package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.NoOpAsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class ViewWireframeMapperTest {
    private static final long VIEW_ID = 42L;
    private static final GlobalBounds VIEW_BOUNDS = new GlobalBounds(10L, 20L, 100L, 50L);

    @Test
    public void clickableViewWithoutBackgroundCreatesTransparentHeatmapBinding() {
        View view = new View(RuntimeEnvironment.application);
        view.setClickable(true);

        List<Wireframe> wireframes = createMapper().map(
                view,
                createMappingContext(),
                new NoOpAsyncJobStatusCallback(),
                null
        );

        Assert.assertEquals(1, wireframes.size());
        Assert.assertTrue(wireframes.get(0) instanceof ShapeWireframe);
        ShapeWireframe wireframe = (ShapeWireframe) wireframes.get(0);
        Assert.assertEquals(VIEW_ID, wireframe.getId().longValue());
        Assert.assertEquals(VIEW_BOUNDS.getX(), wireframe.getX());
        Assert.assertEquals(VIEW_BOUNDS.getY(), wireframe.getY());
        Assert.assertEquals(VIEW_BOUNDS.getWidth(), wireframe.getWidth());
        Assert.assertEquals(VIEW_BOUNDS.getHeight(), wireframe.getHeight());
        ShapeStyle shapeStyle = wireframe.getShapeStyle();
        Assert.assertNotNull(shapeStyle);
        Assert.assertEquals("#00000000", shapeStyle.getBackgroundColor());
        Assert.assertEquals(0f, shapeStyle.getOpacity().floatValue(), 0f);
    }

    @Test
    public void nonClickableViewWithoutBackgroundDoesNotCreateWireframe() {
        View view = new View(RuntimeEnvironment.application);

        List<Wireframe> wireframes = createMapper().map(
                view,
                createMappingContext(),
                new NoOpAsyncJobStatusCallback(),
                null
        );

        Assert.assertTrue(wireframes.isEmpty());
    }

    @Test
    public void adapterViewRowWithoutBackgroundCreatesTransparentHeatmapBinding() {
        ListView listView = new ListView(RuntimeEnvironment.application);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return "row";
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return new FrameLayout(parent.getContext());
            }
        });
        int exactSize = View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY);
        listView.measure(exactSize, exactSize);
        listView.layout(0, 0, 200, 200);
        View row = listView.getChildAt(0);
        Assert.assertNotNull(row);
        Assert.assertFalse(row.isClickable());

        List<Wireframe> wireframes = createMapper().map(
                row,
                createMappingContext(),
                new NoOpAsyncJobStatusCallback(),
                null
        );

        Assert.assertEquals(1, wireframes.size());
        ShapeWireframe wireframe = (ShapeWireframe) wireframes.get(0);
        Assert.assertEquals(0f, wireframe.getShapeStyle().getOpacity().floatValue(), 0f);
    }

    private ViewWireframeMapper createMapper() {
        ViewIdentifierResolver identifierResolver = new ViewIdentifierResolver() {
            @Override
            public long resolveViewId(View view) {
                return VIEW_ID;
            }

            @Override
            public Long resolveChildUniqueIdentifier(View parent, String childName) {
                return null;
            }
        };
        ViewBoundsResolver boundsResolver = new ViewBoundsResolver() {
            @Override
            public GlobalBounds resolveViewGlobalBounds(View view, float screenDensity) {
                return VIEW_BOUNDS;
            }

            @Override
            public GlobalBounds resolveViewPaddedBounds(View view, float screenDensity) {
                return VIEW_BOUNDS;
            }
        };
        ColorStringFormatter colorFormatter = new ColorStringFormatter() {
            @Override
            public String formatColorAsHexString(int color) {
                return "#000000ff";
            }

            @Override
            public String formatColorAndAlphaAsHexString(int color, int alpha) {
                return "#000000ff";
            }
        };
        DrawableToColorMapper drawableMapper = (drawable, internalLogger) -> null;
        return new ViewWireframeMapper(
                identifierResolver,
                colorFormatter,
                boundsResolver,
                drawableMapper
        );
    }

    private MappingContext createMappingContext() {
        SystemInformation systemInformation = new SystemInformation(
                new GlobalBounds(0L, 0L, 1080L, 1920L),
                0,
                1f,
                null
        );
        return new MappingContext(systemInformation, null, false, null, null, null, null);
    }
}

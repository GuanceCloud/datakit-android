package com.ft.sdk.sessionreplay.internal.recorder.mapper;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.NoOpAsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.List;
import java.util.Locale;

public class DecorViewMapper implements WireframeMapper<View> {

    private final ViewWireframeMapper viewWireframeMapper;
    private final ViewIdentifierResolver viewIdentifierResolver;

    private static final String POP_UP_DECOR_VIEW_CLASS_NAME_SUFFIX = "popupdecorview";
    private static final String WINDOW_WIREFRAME_COLOR = "#000000FF";
    private static final float WINDOW_WIREFRAME_OPACITY = 0.6f;
    private static final String WINDOW_KEY_NAME = "window";

    public DecorViewMapper(
            @NonNull ViewWireframeMapper viewWireframeMapper,
            @NonNull ViewIdentifierResolver viewIdentifierResolver
    ) {
        this.viewWireframeMapper = viewWireframeMapper;
        this.viewIdentifierResolver = viewIdentifierResolver;
    }

    @UiThread
    @Override
    public List<Wireframe> map(
            @NonNull View view,
            @NonNull MappingContext mappingContext,
            @NonNull AsyncJobStatusCallback asyncJobStatusCallback,
            @NonNull InternalLogger internalLogger
    ) {
        List<Wireframe> wireframes = viewWireframeMapper.map(view, mappingContext, new NoOpAsyncJobStatusCallback(), internalLogger);
        if (mappingContext.getSystemInformation().getThemeColor() != null) {
            addShapeStyleFromThemeIfNeeded(mappingContext.getSystemInformation().getThemeColor(), wireframes, view);
        }
        String decorClassName = view.getClass().getName();
        if (!decorClassName.toLowerCase(Locale.US).endsWith(POP_UP_DECOR_VIEW_CLASS_NAME_SUFFIX)) {
            Long windowIdentifier = viewIdentifierResolver.resolveChildUniqueIdentifier(view, WINDOW_KEY_NAME);
            if (windowIdentifier != null) {
                ShapeWireframe windowWireframe = new ShapeWireframe(
                        windowIdentifier,
                        0,
                        0,
                        mappingContext.getSystemInformation().getScreenBounds().getWidth(),
                        mappingContext.getSystemInformation().getScreenBounds().getHeight(),
                        null,
                        new ShapeStyle(WINDOW_WIREFRAME_COLOR, WINDOW_WIREFRAME_OPACITY, null),
                        null
                );
                wireframes.add(0, windowWireframe);
            }
        }
        return wireframes;
    }

    private void addShapeStyleFromThemeIfNeeded(
            String themeColor,
            List<Wireframe> wireframes,
            View view
    ) {
        ShapeWireframe rootNonEmptyWireframe = null;
        for (Wireframe wireframe : wireframes) {
            if (wireframe instanceof ShapeWireframe) {
                ShapeWireframe shapeWireframe = (ShapeWireframe) wireframe;
                if (shapeWireframe.getShapeStyle() != null) {
                    rootNonEmptyWireframe = shapeWireframe;
                    break;
                }
            }
        }

        if (rootNonEmptyWireframe == null) {
            ShapeStyle shapeStyle = new ShapeStyle(themeColor, view.getAlpha(), null);
            for (int i = 0; i < wireframes.size(); i++) {
                Wireframe oldWireframe = wireframes.get(i);
                if (oldWireframe instanceof ShapeWireframe) {
                    ShapeWireframe oldShapeWireframe = (ShapeWireframe) oldWireframe;
                    wireframes.set(i, new ShapeWireframe(
                            oldShapeWireframe.getId(),
                            oldShapeWireframe.getX(),
                            oldShapeWireframe.getY(),
                            oldShapeWireframe.getWidth(),
                            oldShapeWireframe.getHeight(),
                            null,
                            shapeStyle,
                            null
                    ));
                }
            }
        }
    }
}

package com.ft.sdk.sessionreplay.internal.recorder.mapper;


import com.ft.sdk.sessionreplay.model.WebviewWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.recorder.mapper.BaseWireframeMapper;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.DefaultViewIdentifierResolver;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapperFactory;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;
import com.tencent.smtt.sdk.WebView;

import java.util.Collections;
import java.util.List;

public class WebViewXWireframeMapper extends BaseWireframeMapper<WebView> {

    public WebViewXWireframeMapper() {
        this(DefaultViewIdentifierResolver.get(), DefaultColorStringFormatter.get(), DefaultViewBoundsResolver.get()
                , DrawableToColorMapperFactory.getDefault());


    }

    public WebViewXWireframeMapper(
            ViewIdentifierResolver viewIdentifierResolver,
            ColorStringFormatter colorStringFormatter,
            ViewBoundsResolver viewBoundsResolver,
            DrawableToColorMapper drawableToColorMapper
    ) {
        super(viewIdentifierResolver, colorStringFormatter, viewBoundsResolver, drawableToColorMapper);
    }

    @Override
    public List<Wireframe> map(
            WebView webView,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    ) {
        GlobalBounds viewGlobalBounds = viewBoundsResolver.resolveViewGlobalBounds(
                webView,
                mappingContext.getSystemInformation().getScreenDensity()
        );
        long webViewId = resolveViewId(webView);
        return Collections.singletonList(
                new WebviewWireframe(
                        webViewId,
                        viewGlobalBounds.getX(),
                        viewGlobalBounds.getY(),
                        viewGlobalBounds.getWidth(),
                        viewGlobalBounds.getHeight(),
                        null,
                        null,
                        null,
                        webViewId + "", // slotId as string
                        true // isVisible
                )
        );
    }
}

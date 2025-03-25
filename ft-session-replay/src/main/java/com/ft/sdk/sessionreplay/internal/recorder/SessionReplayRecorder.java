package com.ft.sdk.sessionreplay.internal.recorder;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.view.View;
import android.view.Window;

import androidx.annotation.MainThread;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.MapperTypeWrapper;
import com.ft.sdk.sessionreplay.SessionReplayInternalCallback;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.LifecycleCallback;
import com.ft.sdk.sessionreplay.internal.SessionReplayLifecycleCallback;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueHandler;
import com.ft.sdk.sessionreplay.internal.processor.MutationResolver;
import com.ft.sdk.sessionreplay.internal.processor.RecordedDataProcessor;
import com.ft.sdk.sessionreplay.internal.processor.RumContextDataHandler;
import com.ft.sdk.sessionreplay.internal.recorder.callback.OnWindowRefreshedCallback;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.DecorViewMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.HiddenViewMapper;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.ViewWireframeMapper;
import com.ft.sdk.sessionreplay.internal.recorder.resources.BitmapCachesManager;
import com.ft.sdk.sessionreplay.internal.recorder.resources.BitmapPool;
import com.ft.sdk.sessionreplay.internal.recorder.resources.DefaultImageWireframeHelper;
import com.ft.sdk.sessionreplay.internal.recorder.resources.ImageTypeResolver;
import com.ft.sdk.sessionreplay.internal.recorder.resources.MD5HashGenerator;
import com.ft.sdk.sessionreplay.internal.recorder.resources.ResourceResolver;
import com.ft.sdk.sessionreplay.internal.recorder.resources.ResourcesLRUCache;
import com.ft.sdk.sessionreplay.internal.recorder.resources.WebPImageCompression;
import com.ft.sdk.sessionreplay.internal.resources.ResourceDataStoreManager;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.ResourcesWriter;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.utils.ColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.DefaultViewIdentifierResolver;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapperFactory;
import com.ft.sdk.sessionreplay.utils.DrawableUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.PathUtils;
import com.ft.sdk.sessionreplay.utils.RumContextProvider;
import com.ft.sdk.sessionreplay.utils.TimeProvider;
import com.ft.sdk.sessionreplay.utils.ViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.ViewIdentifierResolver;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SessionReplayRecorder implements OnWindowRefreshedCallback, Recorder {

    private final Application appContext;
    private final RumContextProvider rumContextProvider;
    private final TextAndInputPrivacy textAndInputPrivacy;
    private final ImagePrivacy imagePrivacy;
    private final TouchPrivacyManager touchPrivacyManager;
    private final RecordWriter recordWriter;
    private final TimeProvider timeProvider;
    private final List<MapperTypeWrapper<?>> mappers;
    private final List<OptionSelectorDetector> customOptionSelectorDetectors;
    private final WindowInspector windowInspector;
    private final WindowCallbackInterceptor windowCallbackInterceptor;
    private final LifecycleCallback sessionReplayLifecycleCallback;
    private final RecordedDataQueueHandler recordedDataQueueHandler;
    private final ViewOnDrawInterceptor viewOnDrawInterceptor;
    private final SessionReplayInternalCallback internalCallback;
    private final InternalLogger internalLogger;
    private final Handler uiHandler;
    private boolean shouldRecord = false;
    private final FeatureSdkCore sdkCore;
    private boolean isDelayInit;

    public SessionReplayRecorder(
            Application appContext,
            ResourcesWriter resourcesWriter,
            RumContextProvider rumContextProvider,
            TextAndInputPrivacy textAndInputPrivacy,
            ImagePrivacy imagePrivacy,
            TouchPrivacyManager touchPrivacyManager,
            RecordWriter recordWriter,
            TimeProvider timeProvider,
            List<MapperTypeWrapper<?>> mappers,
            List<OptionSelectorDetector> customOptionSelectorDetectors,
            List<DrawableToColorMapper> customDrawableMappers,
            WindowInspector windowInspector,
            FeatureSdkCore sdkCore,
            ResourceDataStoreManager resourceDataStoreManager,
            SessionReplayInternalCallback internalCallback,
            boolean dynamicOptimizationEnabled,
            boolean isDelayInit
    ) {
        this.appContext = appContext;
        this.rumContextProvider = rumContextProvider;
        this.textAndInputPrivacy = textAndInputPrivacy;
        this.imagePrivacy = imagePrivacy;
        this.touchPrivacyManager = touchPrivacyManager;
        this.recordWriter = recordWriter;
        this.timeProvider = timeProvider;
        this.mappers = mappers;
        this.customOptionSelectorDetectors = customOptionSelectorDetectors;
        this.windowInspector = windowInspector == null ? new WindowInspector() : windowInspector;
        this.sdkCore = sdkCore;
        this.isDelayInit = isDelayInit;

        // Initialize components
        InternalLogger internalLogger = sdkCore.getInternalLogger();
        RumContextDataHandler rumContextDataHandler = new RumContextDataHandler(
                rumContextProvider,
                timeProvider,
                internalLogger
        );

        RecordedDataProcessor processor = new RecordedDataProcessor(
                resourceDataStoreManager,
                resourcesWriter,
                recordWriter,
                new MutationResolver(internalLogger)
        );

        String applicationId = rumContextProvider.getRumContext().getApplicationId();

        this.recordedDataQueueHandler = new RecordedDataQueueHandler(
                processor,
                rumContextDataHandler,
                internalLogger,
                new ThreadPoolExecutor(
                        CORE_DEFAULT_POOL_SIZE,
                        CORE_DEFAULT_POOL_SIZE,
                        THREAD_POOL_MAX_KEEP_ALIVE_MS,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingDeque<>()
                ),
                new ConcurrentLinkedQueue<>()
        );
        this.internalCallback = internalCallback;

        ViewIdentifierResolver viewIdentifierResolver = DefaultViewIdentifierResolver.get();
        ColorStringFormatter colorStringFormatter = DefaultColorStringFormatter.get();
        ViewBoundsResolver viewBoundsResolver = DefaultViewBoundsResolver.get();
        DrawableToColorMapper drawableToColorMapper = DrawableToColorMapperFactory.getDefault(customDrawableMappers);

        ViewWireframeMapper defaultVWM = new ViewWireframeMapper(
                viewIdentifierResolver,
                colorStringFormatter,
                viewBoundsResolver,
                drawableToColorMapper
        );

        BitmapCachesManager bitmapCachesManager = new BitmapCachesManager(
                new ResourcesLRUCache(),
                new BitmapPool(),
                internalLogger
        );

        ResourceResolver resourceResolver = new ResourceResolver(
                bitmapCachesManager,
                null,
                new PathUtils(internalLogger, bitmapCachesManager, null, null, null),
                new DrawableUtils(
                        internalLogger,
                        bitmapCachesManager,
                        sdkCore.createSingleThreadExecutorService("drawables")
                ),
                new WebPImageCompression(internalLogger),
                internalLogger,
                new MD5HashGenerator(internalLogger),
                recordedDataQueueHandler,
                applicationId

        );
        customOptionSelectorDetectors.add(new DefaultOptionSelectorDetector());
        this.viewOnDrawInterceptor = new ViewOnDrawInterceptor(
                internalLogger,
                new DefaultOnDrawListenerProducer(
                        new SnapshotProducer(
                                new DefaultImageWireframeHelper(
                                        internalLogger,
                                        resourceResolver,
                                        viewIdentifierResolver,
                                        new ViewUtilsInternal(),
                                        new ImageTypeResolver()
                                ),
                                new TreeViewTraversal(
                                        mappers,
                                        defaultVWM,
                                        new DecorViewMapper(defaultVWM, viewIdentifierResolver),
                                        new ViewUtilsInternal(),
                                        new HiddenViewMapper(viewIdentifierResolver, viewBoundsResolver),
                                        internalLogger
                                ),
                                new ComposedOptionSelectorDetector(
                                        customOptionSelectorDetectors
                                ),
                                touchPrivacyManager,
                                internalLogger
                        ),
                        recordedDataQueueHandler,
                        sdkCore, dynamicOptimizationEnabled
                ),
                touchPrivacyManager
        );

        this.windowCallbackInterceptor = new WindowCallbackInterceptor(
                recordedDataQueueHandler,
                viewOnDrawInterceptor,
                timeProvider,
                internalLogger,
                imagePrivacy, textAndInputPrivacy, touchPrivacyManager
        );

        this.sessionReplayLifecycleCallback = new SessionReplayLifecycleCallback(this);
        Activity activity = this.internalCallback.getCurrentActivity();
        if (activity != null) {
            ((SessionReplayLifecycleCallback) sessionReplayLifecycleCallback).setCurrentWindow(activity);
            ((SessionReplayLifecycleCallback) sessionReplayLifecycleCallback).registerFragmentLifecycleCallbacks(activity);
        }

        this.uiHandler = new Handler(Looper.getMainLooper());
        this.internalLogger = internalLogger;
    }

    @Override
    public void stopProcessingRecords() {
        recordedDataQueueHandler.clearAndStopProcessingQueue();
    }

    @Override
    public void registerCallbacks() {
        appContext.registerActivityLifecycleCallbacks(sessionReplayLifecycleCallback);
    }

    @Override
    public void unregisterCallbacks() {
        appContext.unregisterActivityLifecycleCallbacks(sessionReplayLifecycleCallback);
    }

    @Override
    public void resumeRecorders() {
        uiHandler.post(() -> {
            shouldRecord = true;
            List<Window> windows = sessionReplayLifecycleCallback.getCurrentWindows();

            if (isDelayInit && windows.isEmpty()) {
                isDelayInit = false;//初始化一次之后，不进行此逻辑判断
                Activity currentActivity = sdkCore.curentActivity();
                if (currentActivity != null) {
                    windows = List.of(currentActivity.getWindow());
                }
            }
            List<View> decorViews = windowInspector.getGlobalWindowViews(internalLogger);
            windowCallbackInterceptor.intercept(windows, appContext);
            viewOnDrawInterceptor.intercept(decorViews, textAndInputPrivacy, imagePrivacy);
        });
    }

    @Override
    public void stopRecorders() {
        uiHandler.post(() -> {
            viewOnDrawInterceptor.stopIntercepting();
            windowCallbackInterceptor.stopIntercepting();
            shouldRecord = false;
        });
    }

    @MainThread
    @Override
    public void onWindowsAdded(List<Window> windows) {
        if (shouldRecord) {
            List<View> decorViews = windowInspector.getGlobalWindowViews(internalLogger);
            windowCallbackInterceptor.intercept(windows, appContext);
            viewOnDrawInterceptor.intercept(decorViews, textAndInputPrivacy, imagePrivacy);
        }
    }

    @MainThread
    @Override
    public void onWindowsRemoved(List<Window> windows) {
        if (shouldRecord) {
            List<View> decorViews = windowInspector.getGlobalWindowViews(internalLogger);
            windowCallbackInterceptor.stopIntercepting(windows);
            viewOnDrawInterceptor.intercept(decorViews, textAndInputPrivacy, imagePrivacy);
        }
    }

    private static final long THREAD_POOL_MAX_KEEP_ALIVE_MS = DateUtils.SECOND_IN_MILLIS * 5; // 5000ms
    private static final int CORE_DEFAULT_POOL_SIZE = 1; // Only one thread will be kept alive
}

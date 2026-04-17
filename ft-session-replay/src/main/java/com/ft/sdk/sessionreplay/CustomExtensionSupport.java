package com.ft.sdk.sessionreplay;

import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenient ExtensionSupport implementation for quickly adding MapperTypeWrapper, OptionSelectorDetector and DrawableToColorMapper
 * 
 * Usage example:
 * <pre>
 * QuickExtensionSupport support = new QuickExtensionSupport()
 *     .addMapper(TextView.class, new CustomTextViewMapper())
 *     .addMapper(Button.class, new CustomButtonMapper())
 *     .addOptionSelectorDetector(new CustomOptionSelectorDetector())
 *     .addDrawableMapper(new CustomDrawableToColorMapper());
 * 
 * FTSdk.initSessionReplayConfig(new FTSessionReplayConfig()
 *     .setSampleRate(1f)
 *     .addExtensionSupport(support));
 * </pre>
 */
public class CustomExtensionSupport implements ExtensionSupport {
    
    private final List<MapperTypeWrapper<?>> customMappers = new ArrayList<>();
    private final List<OptionSelectorDetector> optionSelectorDetectors = new ArrayList<>();
    private final List<DrawableToColorMapper> drawableMappers = new ArrayList<>();
    
    /**
     * Add custom View Mapper
     * 
     * @param viewClass View type
     * @param mapper Corresponding WireframeMapper
     * @param <T> View type
     * @return Current instance for method chaining
     */
    public <T extends android.view.View> CustomExtensionSupport addMapper(Class<T> viewClass, WireframeMapper<T> mapper) {
        customMappers.add(new MapperTypeWrapper<>(viewClass, mapper));
        return this;
    }
    
    /**
     * Add MapperTypeWrapper
     * 
     * @param mapperWrapper MapperTypeWrapper instance
     * @return Current instance for method chaining
     */
    public CustomExtensionSupport addMapper(MapperTypeWrapper<?> mapperWrapper) {
        customMappers.add(mapperWrapper);
        return this;
    }
    
    /**
     * Add multiple MapperTypeWrapper instances
     * 
     * @param mapperWrappers List of MapperTypeWrapper instances
     * @return Current instance for method chaining
     */
    public CustomExtensionSupport addMappers(List<MapperTypeWrapper<?>> mapperWrappers) {
        customMappers.addAll(mapperWrappers);
        return this;
    }
    
    /**
     * Add OptionSelectorDetector
     * 
     * @param detector OptionSelectorDetector instance
     * @return Current instance for method chaining
     */
    public CustomExtensionSupport addOptionSelectorDetector(OptionSelectorDetector detector) {
        optionSelectorDetectors.add(detector);
        return this;
    }
    
    /**
     * Add multiple OptionSelectorDetector instances
     * 
     * @param detectors List of OptionSelectorDetector instances
     * @return Current instance for method chaining
     */
    public CustomExtensionSupport addOptionSelectorDetectors(List<OptionSelectorDetector> detectors) {
        optionSelectorDetectors.addAll(detectors);
        return this;
    }
    
    /**
     * Add DrawableToColorMapper
     * 
     * @param mapper DrawableToColorMapper instance
     * @return Current instance for method chaining
     */
    public CustomExtensionSupport addDrawableMapper(DrawableToColorMapper mapper) {
        drawableMappers.add(mapper);
        return this;
    }
    
    /**
     * Add multiple DrawableToColorMapper instances
     * 
     * @param mappers List of DrawableToColorMapper instances
     * @return Current instance for method chaining
     */
    public CustomExtensionSupport addDrawableMappers(List<DrawableToColorMapper> mappers) {
        drawableMappers.addAll(mappers);
        return this;
    }
    
    /**
     * Clear all added components
     * 
     * @return Current instance for method chaining
     */
    public CustomExtensionSupport clear() {
        customMappers.clear();
        optionSelectorDetectors.clear();
        drawableMappers.clear();
        return this;
    }
    
    /**
     * Get the count of added Mappers
     * 
     * @return Number of Mappers
     */
    public int getMapperCount() {
        return customMappers.size();
    }
    
    /**
     * Get the count of added OptionSelectorDetectors
     * 
     * @return Number of OptionSelectorDetectors
     */
    public int getOptionSelectorDetectorCount() {
        return optionSelectorDetectors.size();
    }
    
    /**
     * Get the count of added DrawableToColorMappers
     * 
     * @return Number of DrawableToColorMappers
     */
    public int getDrawableMapperCount() {
        return drawableMappers.size();
    }
    
    @Override
    public List<MapperTypeWrapper<?>> getCustomViewMappers() {
        return new ArrayList<>(customMappers);
    }
    
    @Override
    public List<OptionSelectorDetector> getOptionSelectorDetectors() {
        return new ArrayList<>(optionSelectorDetectors);
    }
    
    @Override
    public List<DrawableToColorMapper> getCustomDrawableMapper() {
        return new ArrayList<>(drawableMappers);
    }
    
    @Override
    public String toString() {
        return "QuickExtensionSupport{" +
                "mappers=" + customMappers.size() +
                ", optionSelectors=" + optionSelectorDetectors.size() +
                ", drawableMappers=" + drawableMappers.size() +
                '}';
    }
}

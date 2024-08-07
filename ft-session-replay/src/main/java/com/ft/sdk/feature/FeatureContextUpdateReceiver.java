package com.ft.sdk.feature;

import java.util.Map;

public interface FeatureContextUpdateReceiver {

     void onContextUpdate(String featureName, Map<String, Object> event);
}

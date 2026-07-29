package com.ft.sdk.internal.anr.historical;

import java.util.List;

interface ProcessExitSource {
    List<ProcessExitRecord> load() throws Exception;
}

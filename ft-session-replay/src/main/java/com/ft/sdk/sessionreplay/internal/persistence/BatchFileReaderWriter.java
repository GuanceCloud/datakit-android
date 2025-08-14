package com.ft.sdk.sessionreplay.internal.persistence;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

public interface BatchFileReaderWriter extends FileWriter<RawBatchEvent>, BatchFileReader {


}
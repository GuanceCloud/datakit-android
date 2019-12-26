package com.ft.plugin.garble.asm;

import java.io.IOException;
import java.io.InputStream;

/**
 * From https://github.com/Leaking/Hunter/blob/master/hunter-transform/src/main/java/com/quinn/hunter/transform/asm/IWeaver.java
 * DATE:2019-11-29 13:53
 * Description:
 */
public interface IWeaver {
    boolean isWeavableClass(String filePath) throws IOException;

    byte[] weaverSingleClassToByteArray(InputStream inputStream) throws IOException;
}

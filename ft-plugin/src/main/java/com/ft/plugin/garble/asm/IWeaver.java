package com.ft.plugin.garble.asm;

import java.io.IOException;
import java.io.InputStream;

/**
 * BY huangDianHua
 * DATE:2019-11-29 13:53
 * Description:
 */
public interface IWeaver {
    boolean isWeavableClass(String filePath) throws IOException;

    byte[] weaverSingleClassToByteArray(InputStream inputStream) throws IOException;
}

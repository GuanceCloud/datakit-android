package com.ft.plugin.garble.asm;

import java.io.IOException;
import java.io.InputStream;

/**
 *  <a href="https://github.com/Leaking/Hunter/blob/master/hunter-transform/src/main/java/com/quinn/hunter/transform/asm/IWeaver.java">Reference material</a>
 * DATE:2019-11-29 13:53
 * Description:
 */
public interface IWeaver {
    /**
     * Determine whether the class at the given class path can be modified
     * @param filePath
     * @return
     * @throws IOException
     */
    boolean isWeavableClass(String filePath) throws IOException;

    /**
     * Modify the stream of the input class and output the byte stream of the new class
     * @param inputStream
     * @return
     * @throws IOException
     */
    byte[] weaverSingleClassToByteArray(InputStream inputStream) throws IOException;
}

package com.ft.plugin.garble.asm;

import java.io.IOException;
import java.io.InputStream;

/**
 *  <a href="https://github.com/Leaking/Hunter/blob/master/hunter-transform/src/main/java/com/quinn/hunter/transform/asm/IWeaver.java">参考资料</a>
 * DATE:2019-11-29 13:53
 * Description:
 */
public interface IWeaver {
    /**
     * 判断传入的类路径的类是否可以被修改
     * @param filePath
     * @return
     * @throws IOException
     */
    boolean isWeavableClass(String filePath) throws IOException;

    /**
     * 修改传入的类的流，然后输出新的类的字节流
     * @param inputStream
     * @return
     * @throws IOException
     */
    byte[] weaverSingleClassToByteArray(InputStream inputStream) throws IOException;
}

package com.ft.plugin.garble;

/**
 * BY huangDianHua
 * DATE:2019-12-04 15:03
 * Description:
 *
 * opcode the opcode of the local variable instruction to be visited. This opcode is either
 *      ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE or RET.
 */
public enum FTMethodType {
    ALOAD,ILOAD, INVOKEVIRTUAL,INVOKESPECIAL,GETSTATIC,GETFIELD,INVOKESTATIC
}

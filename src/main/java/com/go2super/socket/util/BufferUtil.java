package com.go2super.socket.util;

public class BufferUtil {

    public static void printBytes(byte[] bytes) {
        String cache = "";
        for(int i = 0; i < bytes.length; i++) {
            cache += Byte.toUnsignedInt(bytes[i]) + ",";
        }
        System.out.println("[" + cache.substring(0, cache.length() - 1) + "]");
    }

}

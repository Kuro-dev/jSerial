package org.kurodev.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteConverter {
    public static byte[] write(int i) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(i).array();
    }

    public static byte[] write(char i) {
        return ByteBuffer.allocate(Character.BYTES).putChar(i).array();
    }

    public static byte[] write(long i) {
        return ByteBuffer.allocate(Long.BYTES).putLong(i).array();
    }

    public static byte[] write(float i) {
        return ByteBuffer.allocate(Float.BYTES).putFloat(i).array();
    }

    public static byte[] write(double i) {
        return ByteBuffer.allocate(Double.BYTES).putDouble(i).array();
    }

    public static byte[] write(short i) {
        return ByteBuffer.allocate(Short.BYTES).putShort(i).array();
    }

    public static byte[] write(String str) {
        byte[] string = str.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[string.length + Integer.BYTES];
        System.arraycopy(write(string.length), 0, out, 0, Integer.BYTES);
        System.arraycopy(string, 0, out, Integer.BYTES, string.length);
        return out;
    }

    public static byte[] combine(byte[]... bytes) {
        int size = 0;
        for (byte[] aByte : bytes) {
            size += aByte.length;
        }
        byte[] out = new byte[size];
        int x = 0;
        for (byte[] aByteArray : bytes) {
            System.arraycopy(aByteArray, 0, out, x, aByteArray.length);
            x += aByteArray.length;
        }
        return out;
    }
}

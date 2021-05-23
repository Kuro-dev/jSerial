package org.kurodev.util;

import org.junit.Assert;
import org.junit.Test;

public class ByteConverterTest {

    @Test
    public void writeIntTest() {
        int input = 0x0000ff00;
        byte[] expected = {0x0, 0x0, (byte) 0xff, 0x0};
        byte[] converted = ByteConverter.write(input);
        Assert.assertArrayEquals(expected, converted);
    }

    @Test
    public void writeLongTest() {
        long input = 0x0000ff000000ff00L;
        byte[] expected = {0x0, 0x0, (byte) 0xff, 0x0, 0x0, 0x0, (byte) 0xff, 0x0};
        byte[] converted = ByteConverter.write(input);
        Assert.assertArrayEquals(expected, converted);
    }

    @Test
    public void writeFloatTest() {
        float input = 37.986F;
        byte[] expected = {0x42, 0x17, (byte) 0xf1, (byte) 0xaa};
        byte[] converted = ByteConverter.write(input);
        Assert.assertArrayEquals(expected, converted);
    }

    @Test
    public void writeDoubleTest() {
        double input = 4235.543262D;
        byte[] expected = new byte[]{0x40, (byte) 0xb0, (byte) 0x8b, (byte) 0x8b, 0x13, 0x37, (byte) 0xeb, 0x29};
        byte[] converted = ByteConverter.write(input);
        Assert.assertArrayEquals(expected, converted);
    }

    @Test
    public void writeShortTest() {
        short input = (short) 0x00ff;
        byte[] expected = new byte[]{(byte) 0x00, (byte) 0xff};
        byte[] converted = ByteConverter.write(input);
        Assert.assertArrayEquals(expected, converted);
    }

    @Test
    public void writeStringTest() {
        String input = "this is a test string😊"; //length of string: 25 (0x0,0x0,0x0,0x19)
        byte[] expected = new byte[]{0x0, 0x0, 0x0, 0x19, 0x74, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20, 0x74, 0x65, 0x73, 0x74,
                0x20, 0x73, 0x74, 0x72, 0x69, 0x6e, 0x67, (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x8a};
        byte[] converted = ByteConverter.write(input);
        Assert.assertArrayEquals(expected, converted);
    }

    @Test
    public void combine() {
        byte[] input1 = {(byte) 0xff, (byte) 0xfa, 0x2b};
        byte[] input2 = {0x12, (byte) 0xa6, 0x2b};
        byte[] expected = {(byte) 0xff, (byte) 0xfa, 0x2b, 0x12, (byte) 0xa6, 0x2b};

        byte[] output = ByteConverter.combine(input1, input2);
        Assert.assertArrayEquals(expected, output);
    }
}
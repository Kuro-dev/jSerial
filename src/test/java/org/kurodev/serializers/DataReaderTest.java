package org.kurodev.serializers;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DataReaderTest {

    @Test
    public void readInt() throws IOException {
        byte[] input = {0xf, 0x12, 0x34, 0x0};
        int expected = 0x0f123400;
        var reader = new DataReader(new ByteArrayInputStream(input));
        Assert.assertEquals(expected, reader.readInt());
    }

    @Test
    public void readShort() throws IOException {
        byte[] input = {0xf, 0x12};
        short expected = 0x0f12;
        var reader = new DataReader(new ByteArrayInputStream(input));
        Assert.assertEquals(expected, reader.readShort());
    }

    @Test
    public void readLong() throws IOException {
        long expected = 0x0000ff000000ff00L;
        byte[] input = {0x0, 0x0, (byte) 0xff, 0x0, 0x0, 0x0, (byte) 0xff, 0x0};
        var reader = new DataReader(new ByteArrayInputStream(input));
        Assert.assertEquals(expected, reader.readLong());
    }

    @Test
    public void readBool() throws IOException {
        byte[] input = {1};
        var reader = new DataReader(new ByteArrayInputStream(input));
        Assert.assertTrue(reader.readBool());
    }

    @Test
    public void readFloat() throws IOException {
        float expected = 37.986F;
        byte[] input = {0x42, 0x17, (byte) 0xf1, (byte) 0xaa};
        var reader = new DataReader(new ByteArrayInputStream(input));
        Assert.assertEquals(expected, reader.readFloat(), 0);
    }

    @Test
    public void readDouble() throws IOException {
        byte[] input = new byte[]{0x40, (byte) 0xb0, (byte) 0x8b, (byte) 0x8b, 0x13, 0x37, (byte) 0xeb, 0x29};
        double expected = 4235.543262D;
        var reader = new DataReader(new ByteArrayInputStream(input));
        Assert.assertEquals(expected, reader.readDouble(), 0);
    }

    @Test
    public void readByte() throws IOException {
        byte[] input = new byte[]{0x40};
        double expected = 0x40;
        var reader = new DataReader(new ByteArrayInputStream(input));
        Assert.assertEquals(expected, reader.readByte(), 0);
    }

    @Test
    public void readChar() throws IOException {
        byte[] input = new byte[]{0x00, 0x30};
        char expected = 0x30;
        var reader = new DataReader(new ByteArrayInputStream(input));
        Assert.assertEquals(expected, reader.readChar(), 0);
    }

    @Test
    public void readString() throws IOException {
        String expected = "this is a test string😊"; //length of string: 25 (0x0,0x0,0x0,0x19)
        byte[] input = new byte[]{0x0, 0x0, 0x0, 0x19, 0x74, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20, 0x74, 0x65, 0x73, 0x74,
                0x20, 0x73, 0x74, 0x72, 0x69, 0x6e, 0x67, (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x8a};
        var reader = new DataReader(new ByteArrayInputStream(input));
        Assert.assertEquals(expected, reader.readString());
    }
}
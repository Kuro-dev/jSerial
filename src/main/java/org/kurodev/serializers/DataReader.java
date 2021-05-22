package org.kurodev.serializers;

import org.kurodev.DataType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataReader extends Serializer {
    private final InputStream in;

    public DataReader(InputStream in) {
        this.in = in;
    }

    private byte[] read(DataType type) throws IOException {
        if (type.getSize() == -1) {
            throw new IllegalArgumentException("Datatype must not be of unknown size");
        }
        return in.readNBytes(type.getSize());
    }

    public int readInt() throws IOException {
        return ByteBuffer.wrap(read(DataType.INTEGER)).getInt();
    }

    public short readShort() throws IOException {
        return ByteBuffer.wrap(read(DataType.SHORT)).getShort();
    }

    public long readLong() throws IOException {
        return ByteBuffer.wrap(read(DataType.LONG)).getLong();
    }

    public boolean readBool() throws IOException {
        return in.read() == 1;
    }

    public float readFloat() throws IOException {
        return ByteBuffer.wrap(read(DataType.FLOAT)).getFloat();
    }

    public double readDouble() throws IOException {
        return ByteBuffer.wrap(read(DataType.DOUBLE)).getDouble();
    }

    public byte readByte() throws IOException {
        return (byte) in.read();
    }

    public char readChar() throws IOException {
        return (char) in.read();
    }

    public String readString() throws IOException {
        int length = readInt();
        byte[] bytes = in.readNBytes(length);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

}

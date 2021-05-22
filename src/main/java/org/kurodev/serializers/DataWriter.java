package org.kurodev.serializers;

import org.kurodev.DataType;
import org.kurodev.util.ByteConverter;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DataWriter extends Serializer implements Flushable {
    protected final OutputStream out;

    /**
     * @param out The output stream to write to
     */
    DataWriter(OutputStream out) {
        this.out = out;
    }

    public void write(boolean bool) throws IOException {
        write(DataType.BOOLEAN, bool ? (byte) 1 : (byte) 0);
    }

    public void writeByte(int wByte) throws IOException {
        if (wByte > Byte.MAX_VALUE || wByte < Byte.MIN_VALUE) {
            //either do this, or just write only the first 8 bits.
            throw new IllegalArgumentException("Byte value must be between 127 and -128");
        }
        write(DataType.BYTE, (byte) wByte);
    }

    public void write(char val) throws IOException {
        write(DataType.CHAR, ByteConverter.write(val));
    }

    public void write(double val) throws IOException {
        write(DataType.DOUBLE, ByteConverter.write(val));
    }

    public void write(float val) throws IOException {
        write(DataType.FLOAT, ByteConverter.write(val));
    }

    /**
     * Writes integer value as 4 bytes
     *
     * @param val Integer to write
     * @apiNote to write only a byte value use {@link #writeByte(int)}
     */
    public void write(int val) throws IOException {
        write(DataType.INTEGER, ByteConverter.write(val));
    }

    public void write(long val) throws IOException {
        write(DataType.LONG, ByteConverter.write(val));
    }

    public void write(short val) throws IOException {
        write(DataType.SHORT, ByteConverter.write(val));
    }

    public void write(String val) throws IOException {
        write(DataType.STRING, val.getBytes(StandardCharsets.UTF_8));
    }

    public void write(DataType type, byte... bytes) throws IOException {
        byte[] metaData = new byte[0];
        if (type == DataType.STRING) {
            metaData = ByteConverter.write(bytes.length);
        }
        if (metaData.length > 0)
            out.write(metaData);
        out.write(bytes);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}

package org.kurodev.serializers;

import org.kurodev.DataType;
import org.kurodev.util.ByteConverter;

import java.io.IOException;
import java.io.OutputStream;

public class DataTypeIgnoringDataWriter extends DataWriter {

    public DataTypeIgnoringDataWriter(OutputStream out) {
        super(out);
    }

    @Override
    public void write(DataType type, byte... bytes) throws IOException {
        byte[] metaData = new byte[0];
        if (type == DataType.STRING) {
            metaData = ByteConverter.write(bytes.length);
        }
        if (metaData.length > 0)
            out.write(metaData);
        out.write(bytes);

    }
}

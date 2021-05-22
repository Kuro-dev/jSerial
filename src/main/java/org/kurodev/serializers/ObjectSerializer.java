package org.kurodev.serializers;

import org.kurodev.DataType;
import org.kurodev.serializers.exception.FailHandler;
import org.kurodev.serializers.exception.RecursiveDebthException;
import org.kurodev.util.ByteConverter;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class ObjectSerializer {
    public static final int DEFAULT_MAX_DEBTH = 15;
    private static final Logger logger = LoggerFactory.getLogger(ObjectSerializer.class);
    private final FailHandler<Exception> failHandler;
    private final int maxDebth;

    public ObjectSerializer() {
        this(DEFAULT_MAX_DEBTH, FailHandler.WRAP_IN_RUNTIME_EX);
    }

    public ObjectSerializer(int maxDebth) {
        this(maxDebth, FailHandler.WRAP_IN_RUNTIME_EX);
    }

    public ObjectSerializer(FailHandler<Exception> failHandler) {
        this(DEFAULT_MAX_DEBTH, failHandler);
    }

    public ObjectSerializer(int maxDebth, FailHandler<Exception> failHandler) {
        this.maxDebth = maxDebth;
        this.failHandler = failHandler;
    }

    public byte[] write(Object obj) {
        return write(obj, 0);
    }

    public byte[] write(Object obj, int debth) {
        if (debth > maxDebth) {
            throw new RecursiveDebthException(String.format("Maximum recursive debth has been surpassed. current:%d, maximum:%d", debth, maxDebth));
        }
        var bos = new ByteArrayOutputStream();
        var serializer = new DataTypeIgnoringDataWriter(bos);
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean wasAccessible = field.canAccess(obj);
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                writeValue(value, serializer);

            } catch (IllegalAccessException | IOException e) {
                failHandler.onException(e);
            } finally {
                field.setAccessible(wasAccessible);
            }
        }
        byte[] written = bos.toByteArray();
        byte[] size = ByteConverter.write(written.length);
        try {
            serializer.close();
        } catch (IOException e) {
            failHandler.onException(e);
        }
        return ByteConverter.combine(size, written);
    }

    private void writeValue(Object value, DataTypeIgnoringDataWriter serializer) throws IOException {
        //TODO replace with switch in java 17
        DataType type = DataType.identify(value);
        if (type != null) {
            switch (type) {
                case BOOLEAN -> serializer.write((boolean) value);
                case BYTE -> serializer.writeByte((byte) value);
                case CHAR -> serializer.write((char) value);
                case DOUBLE -> serializer.write((double) value);
                case FLOAT -> serializer.write((float) value);
                case INTEGER -> serializer.write((int) value);
                case LONG -> serializer.write((long) value);
                case SHORT -> serializer.write((short) value);
                case STRING -> serializer.write((String) value);
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
            }
        }
    }

    private <T> T readValue(Class<T> dataType, DataReader reader) throws IOException {
        //TODO replace with switch in java 17
        DataType type = DataType.identify(dataType);
        Object out;
        if (type != null) {
            switch (type) {
                case BOOLEAN -> out = reader.readBool();
                case BYTE -> out = reader.readByte();
                case CHAR -> out = reader.readChar();
                case DOUBLE -> out = reader.readDouble();
                case FLOAT -> out = reader.readFloat();
                case INTEGER -> out = reader.readInt();
                case LONG -> out = reader.readLong();
                case SHORT -> out = reader.readShort();
                case STRING -> out = reader.readString();
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: " + dataType);
        }
        return dataType.cast(out);
    }

    public <T> T read(byte[] bytes, Class<T> type) {
        var bis = new ByteArrayInputStream(bytes);
        var reader = new DataReader(bis);
        try {
            Objenesis objenesis = new ObjenesisStd();
            ObjectInstantiator<T> inst = objenesis.getInstantiatorOf(type);
            T obj = inst.newInstance();
            for (Field field : type.getFields()) {
                boolean access = field.canAccess(obj);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object value = readValue(fieldType, reader);
                field.set(obj, value);
                field.setAccessible(access);
            }
            Unsafe.getUnsafe().allocateInstance(type);
            reader.close();
            return obj;
        } catch (InstantiationException | IOException | IllegalAccessException e) {
            failHandler.onException(e);
        }
        return null;
    }
}

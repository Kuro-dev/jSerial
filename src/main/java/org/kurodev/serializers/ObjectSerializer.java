package org.kurodev.serializers;

import org.kurodev.DataType;
import org.kurodev.serializers.exception.Exclude;
import org.kurodev.serializers.exception.FailHandler;
import org.kurodev.serializers.exception.RecursiveDebthException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

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

    private Field[] getFieldsSorted(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        return fields;
    }

    public byte[] write(Object obj) {
        var bos = new ByteArrayOutputStream();
        write(obj, bos, 0);
        return bos.toByteArray();
    }

    public void write(Object obj, OutputStream out) {
        write(obj, out, 0);
    }

    public void write(Object obj, OutputStream out, int debth) {
        if (debth > maxDebth) {
            throw new RecursiveDebthException(String.format("Maximum recursive debth has been surpassed. current:%d, maximum:%d", debth, maxDebth));
        }
        var serializer = new DataTypeIgnoringDataWriter(out);
        Field[] fields = getFieldsSorted(obj.getClass());
        for (Field field : fields) {
            if (field.isAnnotationPresent(Exclude.class))
                continue;
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
        try {
            serializer.close();
            out.flush();
        } catch (IOException e) {
            failHandler.onException(e);
        }
    }

    public <T> T read(byte[] bytes, Class<T> type) {
        return read(new ByteArrayInputStream(bytes), type);
    }

    public <T> T read(InputStream in, Class<T> type) {
        Objenesis objenesis = new ObjenesisStd();
        var reader = new DataReader(in);
        try {
            ObjectInstantiator<T> inst = objenesis.getInstantiatorOf(type);
            T obj = inst.newInstance();
            for (Field field : getFieldsSorted(type)) {
                if (field.isAnnotationPresent(Exclude.class))
                    continue;
                boolean access = field.canAccess(obj);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object value = readValue(fieldType, reader);
                field.set(obj, value);
                field.setAccessible(access);
            }
            reader.close();
            return obj;
        } catch (IOException | IllegalAccessException e) {
            failHandler.onException(e);
        }
        return null;
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
        return (T) out;
    }
}

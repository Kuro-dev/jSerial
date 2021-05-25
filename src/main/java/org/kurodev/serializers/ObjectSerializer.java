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

    public int getMaxDebth() {
        return maxDebth;
    }

    private Field[] getFieldsSorted(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        return fields;
    }

    public byte[] write(Object obj) {
        var bos = new ByteArrayOutputStream();
        write(obj, bos);
        return bos.toByteArray();
    }

    public void write(Object obj, OutputStream out) {
        write(obj, new DataWriter(out), 0);
    }

    public void write(Object obj, DataWriter serializer, int debth) {
        if (debth > maxDebth) {
            throw new RecursiveDebthException(String.format("Maximum recursive debth has been surpassed. current:%d, maximum:%d", debth, maxDebth));
        }
        Field[] fields = getFieldsSorted(obj.getClass());
        for (Field field : fields) {
            if (field.isAnnotationPresent(Exclude.class))
                continue;
            boolean wasAccessible = field.canAccess(obj);
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                writeValue(value, serializer, debth);
            } catch (IllegalAccessException | IOException e) {
                failHandler.onException(e);
            } finally {
                field.setAccessible(wasAccessible);
            }
        }
        try {
            serializer.flush();
        } catch (IOException e) {
            failHandler.onException(e);
        }
    }

    public <T> T read(byte[] bytes, Class<T> type) {
        return read(new ByteArrayInputStream(bytes), type);
    }

    public <T> T read(InputStream in, Class<T> type) {
        return read(new DataReader(in), type, 0);
    }

    private <T> T read(DataReader reader, Class<T> type, int debth) {
        if (debth > maxDebth) {
            throw new RecursiveDebthException(String.format("Maximum recursive debth has been surpassed. current:%d, maximum:%d", debth, maxDebth));
        }
        Objenesis objenesis = new ObjenesisStd();
        try {
            ObjectInstantiator<T> inst = objenesis.getInstantiatorOf(type);
            T obj = inst.newInstance();
            for (Field field : getFieldsSorted(type)) {
                if (field.isAnnotationPresent(Exclude.class))
                    continue;
                boolean access = field.canAccess(obj);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object value = readValue(fieldType, reader, debth);
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

    private void writeValue(Object value, DataWriter serializer, int debth) throws IOException {
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
                case OBJECT -> this.write(value, serializer, ++debth);
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }
    }

    private <T> T readValue(Class<T> dataType, DataReader reader, int debth) throws IOException {
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
                case OBJECT -> out = read(reader, dataType, ++debth);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: " + dataType);
        }
        return (T) out;
    }
}

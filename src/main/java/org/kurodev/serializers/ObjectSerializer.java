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
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

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

    private static int size(Iterable<?> iterable) {
        if (iterable instanceof Collection) {
            return ((Collection<?>) iterable).size();
        }
        int counter = 0;
        for (Object i : iterable) {
            counter++;
        }
        return counter;
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

    void write(Object obj, DataWriter serializer, int debth) {
        if (debth > maxDebth) {
            throw new RecursiveDebthException(String.format("Maximum recursive debth has been surpassed. current:%d, maximum:%d", debth, maxDebth));
        }
        if (DataType.identify(obj) != DataType.OBJECT) {
            try {
                writeValue(obj, serializer, debth);
            } catch (IOException e) {
                failHandler.onException(e);
            }
        } else {
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
        if (DataType.identify(type) != DataType.OBJECT) {
            try {
                return readValue(type, reader, debth);
            } catch (IOException e) {
                failHandler.onException(e);
            }
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
        if (value.getClass().isArray()) {
            writeArray(convertToObjectArray(value), serializer, debth);
        } else if (value instanceof Iterable) {
            writeIterable((Iterable<?>) value, serializer, debth);
        } else if (type != null) {
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

    private void writeIterable(Iterable<?> iterable, DataWriter serializer, int debth) throws IOException {
        int length = size(iterable);
        writeValue(length, serializer, debth);
        for (Object o : iterable) {
            write(o, serializer, debth);
        }
    }

    private void writeArray(Object[] array, DataWriter serializer, int debth) throws IOException {
        int length = array.length;
        writeValue(length, serializer, debth);
        for (Object object : array) {
            write(object, serializer, debth);
        }
    }

    private Object[] convertToObjectArray(Object array) {
        Class<?> ofArray = array.getClass().getComponentType();
        if (ofArray.isPrimitive()) {
            List ar = new ArrayList();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                ar.add(Array.get(array, i));
            }
            return ar.toArray();
        } else {
            return (Object[]) array;
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
                case COLLECTION -> out = readCollection(dataType, reader, debth);
                case ARRAY -> out = readArray(dataType.getComponentType(), reader, debth);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: " + dataType);
        }
        return (T) out;
    }

    public Object readArray(Class<?> componentType, DataReader reader, int debth) throws IOException {
        int length = reader.readInt();
        Object array = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, read(reader, componentType, debth));
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    //Does not work yet
    public <T> Collection<T> readCollection(Class<?> type, DataReader reader, int debth) throws IOException {
        Class<T> componentType = (Class<T>) type.getComponentType();
        int length = reader.readInt();
        Objenesis objenesis = new ObjenesisStd();
        Collection<T> collection = (Collection<T>) objenesis.newInstance(type);
        for (int i = 0; i < length; i++) {
            collection.add(readValue(componentType, reader, debth));
        }
        return collection;
    }
}

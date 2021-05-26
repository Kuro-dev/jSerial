package org.kurodev;


import java.lang.reflect.Array;
import java.util.Collection;

public enum DataType {
    BOOLEAN(1, Boolean.class, boolean.class),
    BYTE(Byte.BYTES, Byte.class, byte.class),
    CHAR(Character.BYTES, Character.class, char.class),
    DOUBLE(Double.BYTES, Double.class, double.class),
    FLOAT(Float.BYTES, Float.class, float.class),
    INTEGER(Integer.BYTES, Integer.class, int.class),
    LONG(Long.BYTES, Long.class, long.class),
    OBJECT(-1, Object.class),
    SHORT(Short.BYTES, Short.class, short.class),
    STRING(-1, String.class),
    COLLECTION(-1, Collection.class),
    ARRAY(-1, Array.class);

    private final int size;
    private final Class<?>[] clazzes;

    DataType(int size, Class<?>... clazzes) {
        this.size = size;
        this.clazzes = clazzes;
    }

    public static DataType identify(int rByte) {
        for (DataType value : values()) {
            if (value.ordinal() == rByte) {
                return value;
            }
        }
        return null;
    }

    public static DataType identify(Class<?> clazz) {
        if (clazz.isArray()) {
            return ARRAY;
        }
        for (DataType value : values()) {
            if (value == OBJECT) {
                continue;
            }
            for (Class<?> aClass : value.clazzes) {
                if (clazz == aClass || aClass.isAssignableFrom(clazz)) {
                    return value;
                }

            }
        }
        if (clazz.isArray()) {
            return COLLECTION;
        }
        return OBJECT;
    }

    public static DataType identify(Object obj) {
        return identify(obj.getClass());
    }

    public int getSize() {
        return size;
    }
}

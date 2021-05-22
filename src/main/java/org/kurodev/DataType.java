package org.kurodev;


public enum DataType {
    BOOLEAN(1, Boolean.class),
    BYTE(Byte.BYTES, Byte.class),
    CHAR(Character.BYTES, Character.class),
    DOUBLE(Double.BYTES, Double.class),
    FLOAT(Float.BYTES, Float.class),
    INTEGER(Integer.BYTES, Integer.class),
    LONG(Long.BYTES, Long.class),
    SHORT(Short.BYTES, Short.class),
    STRING(-1, String.class);

    private final int size;
    private final Class<?> clazz;

    DataType(int size, Class<?> clazz) {
        this.size = size;
        this.clazz = clazz;
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
        for (DataType value : values()) {
            if (value.clazz == clazz) {
                return value;
            }
        }
        return null;
    }

    public static DataType identify(Object obj) {
        return identify(obj.getClass());
    }

    public int getSize() {
        return size;
    }
}

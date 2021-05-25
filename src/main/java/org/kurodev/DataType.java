package org.kurodev;


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
    STRING(-1, String.class);

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
        for (DataType value : values()) {
            for (Class<?> aClass : value.clazzes) {
                if (clazz == aClass) {
                    return value;
                }

            }
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

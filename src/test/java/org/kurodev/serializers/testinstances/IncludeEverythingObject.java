package org.kurodev.serializers.testinstances;

import java.util.Objects;

public class IncludeEverythingObject {
    double aDouble = Double.MAX_VALUE;
    float aFloat = Float.MAX_VALUE;
    int anInt = Integer.MAX_VALUE;
    int anotherInt = Integer.MIN_VALUE;
    short aShort = Short.MAX_VALUE;
    long aLong = Long.MAX_VALUE;
    boolean aBoolean = true;
    byte aByte = Byte.MAX_VALUE;
    char aChar = Character.MAX_VALUE;
    String string = "0000testString";

    @Override
    public String toString() {
        return "IncludeEverythingObject{" +
                "aDouble=" + aDouble +
                ", aFloat=" + aFloat +
                ", anInt=" + anInt +
                ", anotherInt=" + anotherInt +
                ", aShort=" + aShort +
                ", aLong=" + aLong +
                ", aBoolean=" + aBoolean +
                ", aByte=" + aByte +
                ", aChar=" + aChar +
                ", string='" + string + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncludeEverythingObject that = (IncludeEverythingObject) o;
        return Double.compare(that.aDouble, aDouble) == 0 &&
                Float.compare(that.aFloat, aFloat) == 0 &&
                anInt == that.anInt &&
                anotherInt == that.anotherInt &&
                aShort == that.aShort &&
                aLong == that.aLong &&
                aBoolean == that.aBoolean &&
                aByte == that.aByte &&
                aChar == that.aChar &&
                string.equals(that.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aDouble, aFloat, anInt, anotherInt, aShort, aLong, aBoolean, aByte, aChar, string);
    }

}

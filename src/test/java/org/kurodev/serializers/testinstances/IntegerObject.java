package org.kurodev.serializers.testinstances;

import java.util.Objects;

public class IntegerObject {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerObject that = (IntegerObject) o;
        return i == that.i;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i);
    }

    int i = 0xaabbccdd;
}

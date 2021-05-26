package org.kurodev.serializers.testinstances;

import java.util.Objects;

public class DoubleObject {
    double d = 4235.543262D;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleObject that = (DoubleObject) o;
        return Double.compare(that.d, d) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(d);
    }
}

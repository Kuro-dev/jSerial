package org.kurodev.serializers.testinstances;

import org.kurodev.serializers.exception.Exclude;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class IllegalTypeClass {
    @Exclude
    InputStream x = new ByteArrayInputStream(new byte[0]);
}

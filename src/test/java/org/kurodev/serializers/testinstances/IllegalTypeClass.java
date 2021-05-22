package org.kurodev.serializers.testinstances;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class IllegalTypeClass {
    InputStream x = new ByteArrayInputStream(new byte[0]);
}

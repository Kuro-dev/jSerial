package org.kurodev.serializers;

import org.junit.Test;
import org.kurodev.serializers.exception.Exclude;
import org.kurodev.serializers.testinstances.ExcludeObject;
import org.kurodev.serializers.testinstances.IncludeEverythingObject;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IntegrationTest {
    @Test
    public void createObjectInstanceTest() throws IOException {
        IncludeEverythingObject source = new IncludeEverythingObject();
        var serializer = new ObjectSerializer();
        byte[] encoded = serializer.write(source);
        IncludeEverythingObject decoded = serializer.read(encoded, IncludeEverythingObject.class);
        assertEquals(source, decoded);
    }
    @Test
    public void createObjectInstanceTestAndIgnoreExcludedFields() throws IOException {
        ExcludeObject source = new ExcludeObject();
        var serializer = new ObjectSerializer();
        byte[] encoded = serializer.write(source);
        ExcludeObject decoded = serializer.read(encoded, ExcludeObject.class);
        assertEquals(source, decoded);
    }
}

package org.kurodev.serializers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kurodev.serializers.exception.RecursiveDebthException;
import org.kurodev.serializers.testinstances.*;
import org.kurodev.serializers.testinstances.recursion.LegalRecursiveObject;
import org.kurodev.serializers.testinstances.recursion.RecursiveObject;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ObjectSerializerTest {
    private ObjectSerializer serializer;

    @Before
    public void prepare() {
        serializer = new ObjectSerializer();
    }

    @Test
    public void writeIntegerTest() {
        byte[] written = serializer.write(new IntegerObject());
        byte[] expected = {(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd};
        assertArrayEquals(expected, written);
    }

    @Test
    public void writeDoubleTest() {
        byte[] written = serializer.write(new DoubleObject());
        byte[] expected = new byte[]{0x40, (byte) 0xb0, (byte) 0x8b, (byte) 0x8b, 0x13, 0x37, (byte) 0xeb, 0x29};
        assertArrayEquals(expected, written);
    }

    @Test
    public void writeFloatTest() {
        byte[] written = serializer.write(new FloatObject());
        byte[] expected = new byte[]{0x42, 0x17, (byte) 0xf1, (byte) 0xaa};
        assertArrayEquals(expected, written);
    }

    @Test
    public void writeBooleanTest() {
        byte[] written = serializer.write(new BooleanObject());
        byte[] expected = new byte[]{0x1};
        assertArrayEquals(expected, written);
    }

    @Test
    public void writeByteTest() {
        byte[] written = serializer.write(new ByteObject());
        byte[] expected = new byte[]{0x1f};
        assertArrayEquals(expected, written);
    }

    @Test
    public void writeCharTest() {
        byte[] written = serializer.write(new CharObject());
        byte[] expected = new byte[]{0, 0x1f};
        assertArrayEquals(expected, written);
    }

    @Test
    public void writeLongTest() {
        byte[] written = serializer.write(new LongObject());
        byte[] expected = new byte[]{0x00, 0x00, (byte) 0xff, 0x00, 0x00, 0x00, (byte) 0xff, (byte) 0x00L};
        assertArrayEquals(expected, written);
    }

    @Test
    public void writeStringTest() {
        byte[] written = serializer.write(new StringObject());
        byte[] expected = new byte[]{
                0x0, 0x0, 0x0, 0x19,    //length of string
                0x74, 0x68, 0x69, 0x73, 0x20, 0x69, 0x73, 0x20, 0x61, 0x20, 0x74, 0x65, 0x73, 0x74,
                0x20, 0x73, 0x74, 0x72, 0x69, 0x6e, 0x67, (byte) 0xf0, (byte) 0x9f, (byte) 0x98, (byte) 0x8a};
        assertArrayEquals(expected, written);
    }

    @Test
    public void excludedIllegalTypeShouldNotThrowExceptionTest() {
        byte[] written = serializer.write(new IllegalTypeClass());
        Assert.assertArrayEquals(new byte[0], written);
    }

    @Test
    public void writeObjectInstanceTest() {
        byte[] written = serializer.write(new InstanceWithObjects());
        byte[] expected = new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, 0x0, 0x0, 0x0, (byte) 0xff};
        assertArrayEquals(expected, written);
    }

    @Test
    public void writePrimitiveTest() {
        int input = 0x00ff00ff;
        byte[] written = serializer.write(input);
        byte[] expected = new byte[]{0x00, (byte) 0xff, 0x00, (byte) 0xff};
        assertArrayEquals(expected, written);
    }

    @Test(expected = RecursiveDebthException.class)
    public void maxRecursionShouldThrowRecursiveDebthExceptionTest() {
        serializer = new ObjectSerializer(2);
        serializer.write(new RecursiveObject());
    }

    @Test()
    public void maxRecursionShouldNotThrowRecursiveDebthExceptionTest() {
        serializer = new ObjectSerializer(2);
        serializer.write(new LegalRecursiveObject());
    }

    @Test()
    public void supplyParametersForReadWriteObjectsTest() {
        readWriteTest(15);
        readWriteTest(15L);
        readWriteTest(15.05F);
        readWriteTest("testString!!!!!! :D");
        readWriteTest(125.563426346D);
        readWriteTest((byte) 16);
        readWriteTest(new IncludeEverythingObject());
    }

    public void readWriteTest(Object obj) {
        byte[] written = serializer.write(obj);
        Object read = serializer.read(written, obj.getClass());
        assertEquals(obj, read);
    }

    @Test
    public void testArrayWrite() {
        int[] integers = {1, 2, 3};
        byte[] bytes = serializer.write(integers);
        byte[] expected = {
                0, 0, 0, 3, //array length
                0, 0, 0, 1,//item 1
                0, 0, 0, 2,//item 2
                0, 0, 0, 3 //item 3
        };
        assertArrayEquals(expected, bytes);
    }

    @Test
    public void testListWrite() {
        List<Integer> integers = Arrays.asList(1, 2, 3);
        byte[] bytes = serializer.write(integers);
        byte[] expected = {
                0, 0, 0, 3, //array length
                0, 0, 0, 1,//item 1
                0, 0, 0, 2,//item 2
                0, 0, 0, 3 //item 3
        };
        assertArrayEquals(expected, bytes);
    }

    @Test
    public void testPrimitiveArrayRead() {
        int[] expected = {1, 2, 3};
        byte[] input = {
                0, 0, 0, 3, //array length
                0, 0, 0, 1,//item 1
                0, 0, 0, 2,//item 2
                0, 0, 0, 3 //item 3
        };
        int[] bytes = serializer.read(input, int[].class);
        assertArrayEquals(expected, bytes);
    }

    @Test
    public void testObjectArrayRead() {
        IntegerObject[] expected = {new IntegerObject(), new IntegerObject(), new IntegerObject()};
        byte[] input = {
                0, 0, 0, 3, //array length
                (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd,
                (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd,
                (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd,
        };
        IntegerObject[] parsed = serializer.read(input, IntegerObject[].class);
        assertArrayEquals(expected, parsed);
    }

    @Test
    @Ignore
    public void testListRead() {
        List<Integer> expected = Arrays.asList(1, 2, 3);
        byte[] input = {
                0, 0, 0, 3, //array length
                0, 0, 0, 1,//item 1
                0, 0, 0, 2,//item 2
                0, 0, 0, 3 //item 3
        };
        List<Integer> bytes = serializer.read(input, expected.getClass());
        assertEquals(expected, bytes);
    }

}
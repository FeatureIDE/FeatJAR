/*
 * Copyright (C) 2024 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR> for further information.
 */
package de.featjar;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;

/**
 * Tests formats.
 *
 * @author Sebastian Krieter
 * @author Andreas Gerasimow
 */
public class FormatTest {

    public static <T> void testParse(T expression1, String name, int count, IFormat<T> format) {
        assertEquals(format.getClass().getCanonicalName(), format.getIdentifier());
        assertTrue(format.supportsParse());
        for (final T expression2 : getFileList(name, count, format)) {
            assertEquals(expression1, expression2);
        }
    }

    public static <T> void testSerialize(T expression1, String name, IFormat<T> format) {
        assertEquals(format.getClass().getCanonicalName(), format.getIdentifier());
        assertTrue(format.supportsWrite());
        final byte[] serializeOutput = serialize(expression1, format);
        final byte[][] byteArrays = getByteArrays(name, 1, format);
        assertEquals(1, byteArrays.length);
        final byte[] parseInput = byteArrays[0];
        assertArrayEquals(serializeOutput, parseInput);
    }

    public static <T> void testSerializeAndParse(T expression1, IFormat<T> format) {
        assertEquals(format.getClass().getCanonicalName(), format.getIdentifier());
        assertTrue(format.supportsParse());
        assertTrue(format.supportsWrite());
        final T expression2 = saveAndLoad(expression1, format);
        assertEquals(expression1, expression2);
    }

    public static <T> void testParseAndSerialize(String name, IFormat<T> format) {
        assertEquals(format.getClass().getCanonicalName(), format.getIdentifier());
        assertTrue(format.supportsParse());
        assertTrue(format.supportsWrite());

        // parse
        final byte[][] byteArrays = getByteArrays(name, 1, format);
        assertEquals(1, byteArrays.length);
        final byte[] parseInput = byteArrays[0];
        final Result<T> result = IO.load(new ByteArrayInputStream(parseInput), format, StandardCharsets.UTF_8);
        assertNotNull(result);
        T obj = result.get();

        // serialize
        final byte[] serializeOutput = serialize(obj, format);

        assertArrayEquals(parseInput, serializeOutput);
    }

    private static <T> byte[] serialize(T object, IFormat<T> format) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            IO.save(object, out, format);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    private static <T> byte[][] getByteArrays(String name, int count, IFormat<T> format) {
        byte[][] result = new byte[count][];
        for (int i = 1; i <= count; i++) {
            URL systemResource = ClassLoader.getSystemResource(
                    String.format("formats/%s_%02d.%s", name, i, format.getFileExtension()));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream stream = systemResource.openStream()) {
                byte[] buffer = new byte[1024];
                int n;
                while ((n = stream.read(buffer)) != -1) {
                    baos.write(buffer, 0, n);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Assertions.fail();
            }
            result[i - 1] = baos.toByteArray();
        }
        return result;
    }

    private static <T> List<T> getFileList(String name, int count, IFormat<T> format) {
        ArrayList<T> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            URL systemResource = ClassLoader.getSystemResource(
                    String.format("formats/%s_%02d.%s", name, i, format.getFileExtension()));
            Result<T> result = IO.load(systemResource, format);
            assertTrue(result.isPresent(), result.printProblems());
            list.add(result.get());
        }
        assertFalse(list.isEmpty());
        return list;
    }

    private static <T> T saveAndLoad(T object, IFormat<T> format) {
        if (object == null) {
            return null;
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            IO.save(object, out, format);
        } catch (final IOException e) {
            e.printStackTrace();
            fail("Failed saving object: " + e.getMessage());
        }
        final byte[] memory = out.toByteArray();
        assertNotNull(memory);
        assertTrue(memory.length > 0, "Saved object is empty");

        final Result<T> result = IO.load(new ByteArrayInputStream(memory), format);
        assertTrue(result.isPresent(), "Failed loading saved object");
        return result.get();
    }
}

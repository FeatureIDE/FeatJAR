/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.output.AOutputMapper;
import de.featjar.base.tree.structure.LabeledTree;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class IOTest {
    static class IntegerFormat implements IFormat<Integer> {
        @Override
        public String getName() {
            return "Integer";
        }

        @Override
        public String getFileExtension() {
            return "dat";
        }

        @Override
        public boolean supportsParse() {
            return true;
        }

        @Override
        public boolean supportsWrite() {
            return true;
        }

        @Override
        public Result<Integer> parse(AInputMapper inputMapper) {
            return inputMapper.get().read().map(Integer::valueOf);
        }

        @Override
        public Result<String> serialize(Integer object) {
            return Result.of(object.toString());
        }
    }

    static class IntegerTreeFormat implements IFormat<LabeledTree<Integer>> {
        @Override
        public String getName() {
            return "IntegerTree";
        }

        @Override
        public String getFileExtension() {
            return "dat";
        }

        @Override
        public boolean supportsParse() {
            return true;
        }

        @Override
        public boolean supportsWrite() {
            return true;
        }

        @Override
        public IFormat<LabeledTree<Integer>> getInstance() {
            return new IntegerTreeFormat();
        }

        @Override
        public Result<LabeledTree<Integer>> parse(AInputMapper inputMapper) {
            List<Problem> problems = new ArrayList<>();
            List<String> lines = inputMapper.get().readLines();
            if (lines.isEmpty()) return Result.empty();
            LabeledTree<Integer> integerTree = new LabeledTree<>(Integer.valueOf(lines.remove(0)));
            for (String line : lines) {
                Result<LabeledTree<Integer>> result = inputMapper.withMainPath(
                        IIOObject.getPathWithNewExtension(line, getFileExtension()),
                        () -> getInstance().parse(inputMapper));
                if (result.isPresent()) integerTree.addChild(result.get());
                else problems.add(new Problem("could not parse subtree", Problem.Severity.WARNING));
            }
            return Result.of(integerTree, problems);
        }

        @Override
        public Result<String> serialize(LabeledTree<Integer> object) {
            return Result.of(object.getLabel().toString());
        }

        @Override
        public void write(LabeledTree<Integer> object, AOutputMapper outputMapper) throws IOException {
            outputMapper
                    .get()
                    .writeText(serialize(object).get() + "\n"
                            + object.getChildren().stream()
                                    .map(Object::hashCode)
                                    .map(Objects::toString)
                                    .collect(Collectors.joining("\n")));
            for (LabeledTree<Integer> child : object.getChildren()) {
                outputMapper.withMainPath(
                        IIOObject.getPathWithNewExtension(String.valueOf(child.hashCode()), getFileExtension()),
                        () -> getInstance().write(child, outputMapper));
            }
        }
    }

    static class NestedFormat implements IFormat<List<Integer>> {
        @Override
        public String getName() {
            return "Nested";
        }

        @Override
        public String getFileExtension() {
            return "dat";
        }

        @Override
        public boolean supportsWrite() {
            return true;
        }

        @Override
        public IFormat<List<Integer>> getInstance() {
            return new NestedFormat();
        }

        @Override
        public void write(List<Integer> object, AOutputMapper outputMapper) throws IOException {
            if (!object.isEmpty()) {
                outputMapper.withMainPath(
                        outputMapper
                                .getPath(outputMapper.get())
                                .get()
                                .resolveSibling(IIOObject.getPathWithNewExtension(
                                                String.valueOf(object.remove(0)), getFileExtension())
                                        .resolve("index")),
                        () -> getInstance().write(object, outputMapper));
            }
        }
    }

    public void testInteger(Path testPath) throws IOException {
        try {
            Result<Integer> result = IO.load("42x", new IntegerFormat());
            assertFalse(result.isPresent());

            result = IO.load("42", new IntegerFormat());
            assertTrue(result.isPresent());
            assertEquals(42, result.get());

            assertDoesNotThrow(() -> IO.save(42, testPath, new IntegerFormat()));
            result = IO.load(testPath, new IntegerFormat());
            assertTrue(result.isPresent());

            result = IO.load(testPath, new IntegerFormat());
            assertTrue(result.isPresent());
            assertEquals(42, result.get());
            assertTrue(Files.isRegularFile(testPath));

            String str = IO.print(42, new IntegerFormat());
            result = IO.load(str, new IntegerFormat());
            assertTrue(result.isPresent());
            assertEquals(42, result.get());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IO.save(42, outputStream, new IntegerFormat());
            result = IO.load(new ByteArrayInputStream(outputStream.toByteArray()), new IntegerFormat());
            assertTrue(result.isPresent());
            assertEquals(42, result.get());
        } finally {
            Files.delete(testPath);
        }
    }

    @Test
    public void integer() throws IOException {
        testInteger(Paths.get("ioTest.dat"));
        testInteger(Paths.get("./ioTest.dat"));
        try {
            testInteger(Paths.get("tempInteger/ioTest.dat"));
        } finally {
            try {
                Files.delete(Paths.get("tempInteger"));
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressWarnings("resource")
    public void testIntegerTree(Path testPath) throws IOException {
        try {
            LabeledTree<Integer> integerTree = new LabeledTree<>(Integer.valueOf(1));
            integerTree.addChild(new LabeledTree<>(Integer.valueOf(2)));
            LabeledTree<Integer> child = new LabeledTree<>(Integer.valueOf(3));
            integerTree.addChild(child);
            child.addChild(new LabeledTree<>(Integer.valueOf(4)));
            assertDoesNotThrow(() -> IO.save(integerTree, testPath, new IntegerTreeFormat()));

            Result<LabeledTree<Integer>> result = IO.load(testPath, new IntegerTreeFormat());
            assertTrue(result.isPresent());
            assertEquals(1, result.get().getLabel());
            assertEquals(0, result.get().getChildrenCount());
            assertTrue(result.hasProblems());
            result = IO.load(testPath, new IntegerTreeFormat(), IOMapperOptions.INPUT_FILE_HIERARCHY);
            assertTrue(result.isPresent());
            assertEquals(1, result.get().getLabel());
            assertEquals(2, result.get().getChildrenCount());
            assertEquals(2, result.get().getFirstChild().get().getLabel());
            assertEquals(3, result.get().getLastChild().get().getLabel());
            assertEquals(
                    4, result.get().getLastChild().get().getFirstChild().get().getLabel());

            Map<Path, String> stringMap = IO.printHierarchy(result.get(), new IntegerTreeFormat());
            assertTrue(stringMap.get(Paths.get("__main__")).startsWith("1"));

            assertDoesNotThrow(
                    () -> IO.save(integerTree, testPath, new IntegerTreeFormat(), IOMapperOptions.ZIP_COMPRESSION));
            assertDoesNotThrow(
                    () -> IO.save(integerTree, testPath, new IntegerTreeFormat(), IOMapperOptions.OUTPUT_FILE_JAR));
        } finally {
            Files.walk(testPath.getParent() == null ? Paths.get("") : testPath.getParent(), 1)
                    .forEach(path -> {
                        if (path.getFileName().toString().endsWith(".dat")
                                || path.getFileName().toString().endsWith(".jar")
                                || path.getFileName().toString().endsWith(".zip")) {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        }
    }

    @Test
    public void integerTree() throws IOException {
        testIntegerTree(Paths.get("ioTest.dat"));
        testIntegerTree(Paths.get("./ioTest.dat"));
        try {
            testIntegerTree(Paths.get("tempIntegerTree/ioTest.dat"));
        } finally {
            try {
                Files.delete(Paths.get("tempIntegerTree"));
            } catch (Exception ignored) {
            }
        }
    }

    public void testNested(Path testPath) throws IOException {
        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);
        assertDoesNotThrow(() -> IO.save(integers, testPath, new NestedFormat()));
        assertTrue(Files.isRegularFile(testPath));
        assertTrue(Files.isDirectory(testPath.resolveSibling("1.dat")));
        assertTrue(Files.isRegularFile(testPath.resolveSibling("1.dat").resolve("index")));
        assertTrue(Files.isDirectory(testPath.resolveSibling("1.dat").resolve("2.dat")));
        assertTrue(Files.isRegularFile(
                testPath.resolveSibling("1.dat").resolve("2.dat").resolve("index")));
        Files.delete(testPath.resolveSibling("1.dat").resolve("2.dat").resolve("index"));
        Files.delete(testPath.resolveSibling("1.dat").resolve("2.dat"));
        Files.delete(testPath.resolveSibling("1.dat").resolve("index"));
        Files.delete(testPath.resolveSibling("1.dat"));
        Files.delete(testPath);
    }

    @Test
    public void nested() throws IOException {
        testNested(Paths.get("ioTest.dat"));
        testNested(Paths.get("./ioTest.dat"));
        try {
            testNested(Paths.get("tempNested/ioTest.dat"));
        } finally {
            try {
                Files.delete(Paths.get("tempNested"));
            } catch (Exception ignored) {
            }
        }
    }

    // TODO: absolute paths

}

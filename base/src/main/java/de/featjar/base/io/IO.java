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

import de.featjar.base.data.IFactory;
import de.featjar.base.data.IFactorySupplier;
import de.featjar.base.data.Maps;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.IFormatSupplier;
import de.featjar.base.io.input.AInput;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.input.StreamInputMapper;
import de.featjar.base.io.input.StringInputMapper;
import de.featjar.base.io.output.AOutput;
import de.featjar.base.io.output.AOutputMapper;
import de.featjar.base.io.output.StreamOutputMapper;
import de.featjar.base.io.output.StringOutputMapper;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Loads inputs and saves outputs of a {@link IFormat}.
 * Loading an {@link AInput} amounts to reading from its source and parsing it using a {@link IFormat}.
 * Saving an {@link AOutput} amounts to and serializing it using a {@link IFormat} and writing to its target.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class IO {
    /**
     * Default {@link Charset} for loading inputs.
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * {@return the file extension of the given path.}
     * @param file the file path
     */
    public static String getFileExtension(Path file) {
        Path fileName = file.getFileName();
        return fileName != null ? getFileExtension(fileName.toString()) : "";
    }

    /**
     * {@return the file extension of the given file name.}
     * @param fileName the file name
     */
    public static String getFileExtension(String fileName) {
        final int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex > 0 ? fileName.substring(extensionIndex + 1) : "";
    }

    /**
     * {@return the file name of the given path as a String, omitting the file extension.}
     * @param file the file path
     */
    public static String getFileNameWithoutExtension(Path file) {
        Path fileName = file.getFileName();
        return fileName != null ? getFileNameWithoutExtension(fileName.toString()) : "";
    }

    /**
     * {@return the given file name omitting its file extension.}
     * @param fileName the file name
     */
    public static String getFileNameWithoutExtension(String fileName) {
        final int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName;
    }

    /**
     * Loads an input.
     *
     * @param inputStream the input stream
     * @param format      the format
     * @param <T>         the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(InputStream inputStream, IFormat<T> format) {
        return load(inputStream, format, DEFAULT_CHARSET);
    }

    /**
     * Loads an input.
     *
     * @param inputStream the input stream
     * @param format      the format
     * @param charset     the charset
     * @param <T>         the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(InputStream inputStream, IFormat<T> format, Charset charset) {
        try (AInputMapper inputMapper = new StreamInputMapper(inputStream, charset, null)) {
            return parse(inputMapper, format);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param url    the URL
     * @param format the format
     * @param <T>    the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(URL url, IFormat<T> format) {
        return load(url, format, DEFAULT_CHARSET);
    }

    /**
     * Loads an input.
     *
     * @param url     the URL
     * @param format  the format
     * @param factory the factory
     * @param <T>     the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(URL url, IFormat<T> format, Supplier<T> factory) {
        return load(url, format, factory, DEFAULT_CHARSET);
    }

    /**
     * Loads an input.
     *
     * @param url            the URL
     * @param formatSupplier the format supplier
     * @param <T>            the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(URL url, IFormatSupplier<T> formatSupplier) {
        return load(url, formatSupplier, DEFAULT_CHARSET);
    }

    /**
     * Loads an input.
     *
     * @param url            the URL
     * @param formatSupplier the format supplier
     * @param factory        the factory
     * @param <T>            the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(URL url, IFormatSupplier<T> formatSupplier, Supplier<T> factory) {
        return load(url, formatSupplier, factory, DEFAULT_CHARSET);
    }

    /**
     * Loads an input.
     *
     * @param url     the URL
     * @param format  the format
     * @param charset the charset
     * @param <T>     the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(URL url, IFormat<T> format, Charset charset) {
        try (AInputMapper inputMapper = new StreamInputMapper(
                url.openStream(),
                charset,
                IIOObject.getFileExtension(url.getFile()).orElse(null))) {
            return parse(inputMapper, format);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param url     the URL
     * @param format  the format
     * @param factory the factory
     * @param charset the charset
     * @param <T>     the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(URL url, IFormat<T> format, Supplier<T> factory, Charset charset) {
        try (AInputMapper inputMapper = new StreamInputMapper(
                url.openStream(),
                charset,
                IIOObject.getFileExtension(url.getFile()).orElse(null))) {
            return parse(inputMapper, format, factory);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param url            the URL
     * @param formatSupplier the format supplier
     * @param factory        the factory
     * @param charset        the charset
     * @param <T>            the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(URL url, IFormatSupplier<T> formatSupplier, Supplier<T> factory, Charset charset) {
        try (AInputMapper inputMapper = new StreamInputMapper(
                url.openStream(),
                charset,
                IIOObject.getFileExtension(url.getFile()).orElse(null))) {
            return parse(inputMapper, formatSupplier, factory);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param url            the URL
     * @param formatSupplier the format supplier
     * @param charset        the charset
     * @param <T>            the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(URL url, IFormatSupplier<T> formatSupplier, Charset charset) {
        try (AInputMapper inputMapper = new StreamInputMapper(
                url.openStream(),
                charset,
                IIOObject.getFileExtension(url.getFile()).orElse(null))) {
            return parse(inputMapper, formatSupplier);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param format          the format
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(Path path, IFormat<T> format, IOMapperOptions... ioMapperOptions) {
        return load(path, format, DEFAULT_CHARSET, ioMapperOptions);
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param format          the format
     * @param factory         the factory
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(
            Path path, IFormat<T> format, Supplier<T> factory, IOMapperOptions... ioMapperOptions) {
        return load(path, format, factory, DEFAULT_CHARSET, ioMapperOptions);
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param formatSupplier  the format supplier
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(Path path, IFormatSupplier<T> formatSupplier, IOMapperOptions... ioMapperOptions) {
        return load(path, formatSupplier, DEFAULT_CHARSET, ioMapperOptions);
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param formatSupplier  the format supplier
     * @param factory         the factory
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(
            Path path, IFormatSupplier<T> formatSupplier, Supplier<T> factory, IOMapperOptions... ioMapperOptions) {
        return load(path, formatSupplier, factory, DEFAULT_CHARSET, ioMapperOptions);
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param formatSupplier  the format supplier
     * @param factorySupplier the factory supplier
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(
            Path path,
            IFormatSupplier<T> formatSupplier,
            IFactorySupplier<T> factorySupplier,
            IOMapperOptions... ioMapperOptions) {
        return load(path, formatSupplier, factorySupplier, DEFAULT_CHARSET, ioMapperOptions);
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param format          the format
     * @param charset         the charset
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(
            Path path, IFormat<T> format, Charset charset, IOMapperOptions... ioMapperOptions) {
        try (AInputMapper inputMapper = AInputMapper.of(path, charset, ioMapperOptions)) {
            return parse(inputMapper, format);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param format          the format
     * @param factory         the factory
     * @param charset         the charset
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(
            Path path, IFormat<T> format, Supplier<T> factory, Charset charset, IOMapperOptions... ioMapperOptions) {
        try (AInputMapper inputMapper = AInputMapper.of(path, charset, ioMapperOptions)) {
            return parse(inputMapper, format, factory);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param formatSupplier  the format supplier
     * @param factory         the factory
     * @param charset         the charset
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(
            Path path,
            IFormatSupplier<T> formatSupplier,
            Supplier<T> factory,
            Charset charset,
            IOMapperOptions... ioMapperOptions) {
        try (AInputMapper inputMapper = AInputMapper.of(path, charset, ioMapperOptions)) {
            return parse(inputMapper, formatSupplier, factory);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param formatSupplier  the format supplier
     * @param charset         the charset
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(
            Path path, IFormatSupplier<T> formatSupplier, Charset charset, IOMapperOptions... ioMapperOptions) {
        try (AInputMapper inputMapper = AInputMapper.of(path, charset, ioMapperOptions)) {
            return parse(inputMapper, formatSupplier);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param path            the path
     * @param formatSupplier  the format supplier
     * @param factorySupplier the factory supplier
     * @param charset         the charset
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(
            Path path,
            IFormatSupplier<T> formatSupplier,
            IFactorySupplier<T> factorySupplier,
            Charset charset,
            IOMapperOptions... ioMapperOptions) {
        try (AInputMapper inputMapper = AInputMapper.of(path, charset, ioMapperOptions)) {
            return parse(path, inputMapper, formatSupplier, factorySupplier);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param string the string
     * @param format the format
     * @param <T>    the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(String string, IFormat<T> format) {
        try (AInputMapper inputMapper = new StringInputMapper(string, DEFAULT_CHARSET, null)) {
            return parse(inputMapper, format);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param string  the string
     * @param format  the format
     * @param factory the factory
     * @param <T>     the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(String string, IFormat<T> format, IFactory<T> factory) {
        try (AInputMapper inputMapper = new StringInputMapper(string, DEFAULT_CHARSET, null)) {
            return parse(inputMapper, format, factory);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param string         the string
     * @param path           the path
     * @param formatSupplier the format supplier
     * @param factory        the factory
     * @param <T>            the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(String string, Path path, IFormatSupplier<T> formatSupplier, IFactory<T> factory) {
        try (AInputMapper inputMapper = new StringInputMapper(
                string, DEFAULT_CHARSET, IIOObject.getFileExtension(path).orElse(null))) {
            return parse(inputMapper, formatSupplier, factory);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param string         the string
     * @param path           the path
     * @param formatSupplier the format supplier
     * @param <T>            the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(String string, Path path, IFormatSupplier<T> formatSupplier) {
        try (AInputMapper inputMapper = new StringInputMapper(
                string, DEFAULT_CHARSET, IIOObject.getFileExtension(path).orElse(null))) {
            return parse(inputMapper, formatSupplier);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Loads an input.
     *
     * @param string          the string
     * @param path            the path
     * @param formatSupplier  the format supplier
     * @param factorySupplier the factory supplier
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    public static <T> Result<T> load(
            String string, Path path, IFormatSupplier<T> formatSupplier, IFactorySupplier<T> factorySupplier) {
        try (AInputMapper inputMapper = new StringInputMapper(
                string, DEFAULT_CHARSET, IIOObject.getFileExtension(path).orElse(null))) {
            return parse(path, inputMapper, formatSupplier, factorySupplier);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    /**
     * Parses an input.
     *
     * @param inputMapper the input mapper
     * @param format      the format
     * @param factory     the factory
     * @param <T>         the type of the parsed result
     * @return the parsed result
     */
    private static <T> Result<T> parse(AInputMapper inputMapper, IFormat<T> format, Supplier<T> factory) {
        return format.supportsParse()
                ? format.getInstance().parse(inputMapper, factory)
                : Result.empty(new UnsupportedOperationException(format.toString()));
    }

    /**
     * Parses an input.
     *
     * @param inputMapper the input mapper
     * @param format      the format
     * @param <T>         the type of the parsed result
     * @return the parsed result
     */
    private static <T> Result<T> parse(AInputMapper inputMapper, IFormat<T> format) {
        return format.supportsParse()
                ? format.getInstance().parse(inputMapper)
                : Result.empty(new UnsupportedOperationException(format.toString()));
    }

    /**
     * Parses an input.
     *
     * @param inputMapper    the input mapper
     * @param formatSupplier the format supplier
     * @param factory        the factory
     * @param <T>            the type of the parsed result
     * @return the parsed result
     */
    private static <T> Result<T> parse(
            AInputMapper inputMapper, IFormatSupplier<T> formatSupplier, Supplier<T> factory) {
        return inputMapper
                .get()
                .getInputHeader()
                .mapResult(formatSupplier::getFormat)
                .mapResult(format -> parse(inputMapper, format, factory));
    }

    /**
     * Parses an input.
     *
     * @param inputMapper    the input mapper
     * @param formatSupplier the format supplier
     * @param <T>            the type of the parsed result
     * @return the parsed result
     */
    private static <T> Result<T> parse(AInputMapper inputMapper, IFormatSupplier<T> formatSupplier) {
        return inputMapper
                .get()
                .getInputHeader()
                .mapResult(formatSupplier::getFormat)
                .mapResult(format -> parse(inputMapper, format));
    }

    /**
     * Parses an input.
     *
     * @param inputMapper     the input mapper
     * @param path            the path
     * @param formatSupplier  the format supplier
     * @param factorySupplier the factory supplier
     * @param <T>             the type of the parsed result
     * @return the parsed result
     */
    private static <T> Result<T> parse(
            Path path,
            AInputMapper inputMapper,
            IFormatSupplier<T> formatSupplier,
            IFactorySupplier<T> factorySupplier) {
        return inputMapper
                .get()
                .getInputHeader()
                .mapResult(formatSupplier::getFormat)
                .mapResult(format -> factorySupplier
                        .getFactory(path, format)
                        .mapResult(factory -> parse(inputMapper, format, factory)));
    }

    /**
     * Saves an output.
     *
     * @param object          the object
     * @param path            the path
     * @param format          the format
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the object
     * @throws IOException if an I/O error has occurred
     */
    public static <T> void save(T object, Path path, IFormat<T> format, IOMapperOptions... ioMapperOptions)
            throws IOException {
        save(object, path, format, DEFAULT_CHARSET, ioMapperOptions);
    }

    /**
     * Saves an output.
     *
     * @param object          the object
     * @param path            the path
     * @param format          the format
     * @param charset         the charset
     * @param ioMapperOptions the {@link AIOMapper} options
     * @param <T>             the type of the object
     * @throws IOException if an I/O error has occurred
     */
    public static <T> void save(
            T object, Path path, IFormat<T> format, Charset charset, IOMapperOptions... ioMapperOptions)
            throws IOException {
        if (format.supportsWrite()) {
            try (AOutputMapper outputMapper = AOutputMapper.of(path, charset, ioMapperOptions)) {
                format.getInstance().write(object, outputMapper);
            }
        }
    }

    /**
     * Saves an output.
     *
     * @param object    the object
     * @param format    the format
     * @param outStream the output stream
     * @param <T>       the type of the object
     * @throws IOException if an I/O error has occurred
     */
    public static <T> void save(T object, OutputStream outStream, IFormat<T> format) throws IOException {
        save(object, outStream, format, DEFAULT_CHARSET);
    }

    /**
     * Saves an output.
     *
     * @param object    the object
     * @param format    the format
     * @param outStream the output stream
     * @param charset   the charset
     * @param <T>       the type of the object
     * @throws IOException if an I/O error has occurred
     */
    public static <T> void save(T object, OutputStream outStream, IFormat<T> format, Charset charset)
            throws IOException {
        if (format.supportsWrite()) {
            try (AOutputMapper outputMapper = new StreamOutputMapper(outStream, charset)) {
                format.getInstance().write(object, outputMapper);
            }
        }
        outStream.flush();
    }

    /**
     * {@return the object printed as a string}
     *
     * @param object the object
     * @param format the format
     * @param <T>    the type of the object
     * @throws IOException if an I/O error has occurred
     */
    public static <T> String print(T object, IFormat<T> format) throws IOException {
        if (format.supportsWrite()) {
            try (AOutputMapper outputMapper = new StringOutputMapper(DEFAULT_CHARSET)) {
                format.getInstance().write(object, outputMapper);
                return outputMapper.get().getOutputStream().toString();
            }
        }
        return "";
    }

    /**
     * {@return the object hierarchy printed as a collection of strings}
     *
     * @param object the object
     * @param format the format
     * @param <T>    the type of the object
     * @throws IOException if an I/O error has occurred
     */
    public static <T> LinkedHashMap<Path, String> printHierarchy(T object, IFormat<T> format) throws IOException {
        if (format.supportsWrite()) {
            try (StringOutputMapper outputMapper = new StringOutputMapper(DEFAULT_CHARSET)) {
                format.getInstance().write(object, outputMapper);
                return outputMapper.getOutputStrings();
            }
        }
        return Maps.empty();
    }

    /**
     * Writes a string into a physical file.
     *
     * @param string  the string
     * @param path    the path
     * @param charset the charset
     * @throws IOException if an I/O error occurs
     */
    public static void write(String string, Path path, Charset charset) throws IOException {
        Files.write(
                path,
                string.getBytes(charset),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);
    }

    /**
     * Writes a string into a physical file.
     *
     * @param string the string
     * @param path   the path
     * @throws IOException if an I/O error occurs
     */
    public static void write(String string, Path path) throws IOException {
        write(string, path, DEFAULT_CHARSET);
    }

    /**
     * Reads a string from a physical file.
     *
     * @param path   the path
     * @throws IOException if an I/O error occurs
     *
     * @return the content of the entire file as a string.
     */
    public static String read(Path path) throws IOException {
        return read(Files.newInputStream(path));
    }

    /**
     * Reads a string from an input stream.
     *
     * @param in   the stream
     * @throws IOException if an I/O error occurs
     *
     * @return the content of the entire stream as a string.
     */
    public static String read(InputStream in) throws IOException {
        ByteArrayOutputStream byteContent = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        for (int length; (length = in.read(buffer)) != -1; ) {
            byteContent.write(buffer, 0, length);
        }
        return byteContent.toString(DEFAULT_CHARSET);
    }

    /**
     * Reads a list of text lines from an input stream.
     *
     * @param in   the stream
     * @throws IOException if an I/O error occurs
     *
     * @return the content of the entire stream as a list of lines.
     */
    public static List<String> readLines(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET))) {
            return reader.lines().collect(Collectors.toList());
        }
    }
}

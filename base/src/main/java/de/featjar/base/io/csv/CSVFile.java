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
package de.featjar.base.io.csv;

import de.featjar.base.FeatJAR;
import de.featjar.base.io.IO;
import de.featjar.base.io.output.AOutput;
import de.featjar.base.io.output.FileOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Writes CSV files. Allows customized separators, number formats, and header
 * fields. Writes to a given output whenever {@link #flush()} is called.
 * Implements a fluent API for writing values to a new line, for example with
 * {@code csvFile.add(...).add(...).newLine().flush();}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class CSVFile {

    public static Stream<List<String>> readAllLines(Path csvFile) throws IOException {
        return readAllLines(csvFile, DEFAULT_SEPARATOR);
    }

    public static Stream<List<String>> readAllLines(Path csvFile, String separator) throws IOException {
        return Files.lines(csvFile).map(line -> Arrays.asList(line.split(separator)));
    }

    public static final void writeCSV(CSVFile writer, Consumer<CSVFile> writing) {
        writer.newLine();
        try {
            writing.accept(writer);
            writer.flush();
        } catch (Exception e) {
            FeatJAR.log().error(e);
            writer.removeLastLine();
        }
    }

    protected static final String NEW_LINE = System.lineSeparator();
    protected static final String DEFAULT_SEPARATOR = ",";
    protected NumberFormat numberFormat = DecimalFormat.getInstance(Locale.ENGLISH);

    {
        numberFormat.setGroupingUsed(false);
    }

    protected String separator = DEFAULT_SEPARATOR;
    protected List<String> headerFields = null;
    protected boolean headerFieldsFlushed;
    protected final LinkedList<List<String>> values = new LinkedList<>();
    protected final AOutput output;

    /**
     * Creates CSV file written to the given output.
     *
     * @param output the output
     */
    public CSVFile(AOutput output) {
        this.output = output;
    }

    public CSVFile(Path path) throws IOException {
        this(new FileOutput(path, IO.DEFAULT_CHARSET));
    }

    public CSVFile(Path path, boolean append) throws IOException {
        this(new FileOutput(path, IO.DEFAULT_CHARSET, !append, true));
    }

    /**
     * {@return this CSV file's field separator} Usually "," or ";".
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Sets this CSV file's field separator.
     *
     * @param separator the separator
     * @return this CSV file
     */
    public CSVFile setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    /**
     * {@return this CSV file's number format}
     */
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    /**
     * Sets this CSV file's number format.
     *
     * @param numberFormat the number format
     * @return this CSV file
     */
    public CSVFile setNumberFormat(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        return this;
    }

    /**
     * {@return this CSV file's header fields}
     */
    public List<String> getHeaderFields() {
        return headerFields;
    }

    /**
     * Sets this CSV file's header fields.
     *
     * @param headerFields the header fields
     * @return this CSV file
     */
    public CSVFile setHeaderFields(String... headerFields) {
        setHeaderFields(Arrays.asList(headerFields));
        return this;
    }

    /**
     * Sets this CSV file's header fields.
     *
     * @param headerFields the header fields
     * @return this CSV file
     */
    public CSVFile setHeaderFields(List<String> headerFields) {
        this.headerFields = new ArrayList<>(headerFields);
        return this;
    }

    /**
     * Adds a header field to this CSV file.
     *
     * @param headerField the header field
     * @return this CSV file
     */
    public CSVFile addHeaderField(String headerField) {
        headerFields.add(headerField);
        return this;
    }

    /**
     * Adds a line of given values to this CSV file.
     *
     * @param line the line
     * @return this CSV file
     */
    public CSVFile addLine(List<String> line) {
        values.add(line);
        return this;
    }

    /**
     * Begin a new line of values in this CSV file. Add values with
     * {@link #add(Object)}.
     *
     * @return this CSV file
     */
    public CSVFile newLine() {
        if (values.isEmpty() || values.get(values.size() - 1).size() > 0) {
            values.add(headerFields != null ? new ArrayList<>(headerFields.size()) : new ArrayList<>());
        }
        return this;
    }

    /**
     * Removes the last line of this CSV file. Useful to clean up an incomplete
     * line.
     *
     * @return this CSV file
     */
    public CSVFile removeLastLine() {
        if (!values.isEmpty()) {
            values.remove(values.size() - 1);
        }
        return this;
    }

    /**
     * Adds a value to the current line of this CSV file.
     *
     * @param value the value
     * @return this CSV file
     */
    public CSVFile add(Object value) {
        values.get(values.size() - 1).add(String.valueOf(value));
        return this;
    }

    /**
     * Adds a value to the current line of this CSV file. Avoid scientific notation
     * and non-English punctuation.
     *
     * @param value the value
     * @return this CSV file
     */
    public CSVFile add(float value) {
        add(numberFormat.format(value));
        return this;
    }

    /**
     * Adds a value to the current line of this CSV file. Avoid scientific notation
     * and non-English punctuation.
     *
     * @param value the value
     * @return this CSV file
     */
    public CSVFile add(double value) {
        add(numberFormat.format(value));
        return this;
    }

    /**
     * Adds a value to the current line of this CSV file. Avoid scientific notation
     * and non-English punctuation.
     *
     * @param value the value
     * @return this CSV file
     */
    public CSVFile add(Float value) {
        add((float) value);
        return this;
    }

    /**
     * Adds a value to the current line of this CSV file. Avoid scientific notation
     * and non-English punctuation.
     *
     * @param value the value
     * @return this CSV file
     */
    public CSVFile add(Double value) {
        add((double) value);
        return this;
    }

    /**
     * Adds a value to the current line of this CSV file. Avoid scientific notation
     * and non-English punctuation.
     *
     * @param value the value
     * @return this CSV file
     */
    public CSVFile add(BigDecimal value) {
        add(value.doubleValue());
        return this;
    }

    /**
     * Adds a value to the current line of this CSV file.
     *
     * @param value the value
     * @return this CSV file
     */
    public CSVFile add(BigInteger value) {
        add(value.toString());
        return this;
    }

    /**
     * Flushes all new lines to this CSV file's output.
     *
     * @return this CSV file
     */
    public CSVFile flush() {
        if (output != null) {
            try {
                if (headerFields != null && !headerFieldsFlushed) {
                    output.writeText(printLine(headerFields));
                    headerFieldsFlushed = true;
                }
                for (List<String> line : values) {
                    output.writeText(printLine(line));
                }
                output.flush();
                values.clear();
            } catch (final IOException e) {
                FeatJAR.log().error(e);
            }
        }
        return this;
    }

    private String printLine(List<String> line) {
        final StringBuilder sb = new StringBuilder();
        for (final String value : line) {
            if (value != null) {
                sb.append(value);
            }
            sb.append(separator);
        }
        if (line.isEmpty()) {
            sb.append(NEW_LINE);
        } else {
            final int length = sb.length() - 1;
            sb.replace(length, length + separator.length(), NEW_LINE);
        }
        return sb.toString();
    }
}

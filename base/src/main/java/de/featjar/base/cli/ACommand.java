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
package de.featjar.base.cli;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.IOMapperOptions;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.IFormatSupplier;
import de.featjar.base.log.Log.Verbosity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * The abstract class for any command.
 *
 * @author Sebastian Krieter
 */
public abstract class ACommand implements ICommand {

    /**
     * Input option for loading files.
     */
    public static final AOption<Path> INPUT_OPTION = Options.newOption("input", Options.PathParser)
            .setDescription("Path to input file(s)")
            .setValidator(Options.PathValidator);

    /**
     * Output option for saving files.
     */
    public static final AOption<Path> OUTPUT_OPTION =
            Options.newOption("output", Options.PathParser).setDescription("Path to output file(s)");

    /**
     * Flag to allow for overwriting existing files.
     */
    public static final AOption<Boolean> OUTPUT_OVERWRITE_OPTION =
            Options.newFlag("overwrite").setDescription("Overwrite existing file at output path.");

    /**
     * ZIP compression option for saving files.
     */
    public static final AOption<Boolean> OUTPUT_COMPRESSION_OPTION = Options.newFlag("zip-output")
            .setDescription("Stores output as zip file. (Requires to set an output path.)");

    /**
     * ZIP compression option for reading files.
     */
    public static final AOption<Boolean> INTPUT_COMPRESSION_OPTION =
            Options.newFlag("zip-input").setDescription("Reads input as zip file.");

    /**
     * {@return all options registered for the calling class}
     */
    public final List<AOption<?>> getOptions() {
        return Options.getAllOptions(getClass());
    }

    protected final <T> Result<T> readFromInput(OptionList optionParser, IFormatSupplier<T> formatSupplier) {
        return IO.load(
                optionParser.getResult(INPUT_OPTION).orElseThrow(),
                formatSupplier,
                optionParser.getResult(INTPUT_COMPRESSION_OPTION).get()
                        ? new IOMapperOptions[] {IOMapperOptions.ZIP_COMPRESSION}
                        : new IOMapperOptions[0]);
    }

    protected final <T> int writeResult(OptionList optionParser, Result<T> result, IFormat<T> ouputFormat) {
        if (result.isEmpty()) {
            FeatJAR.log().problems(result, Verbosity.ERROR);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
        return writeObject(optionParser, result.get(), ouputFormat);
    }

    protected <T> int writeObject(OptionList optionParser, T output, IFormat<T> ouputFormat) {
        try {
            write(optionParser, output, ouputFormat);
            return FeatJAR.EXIT_SUCCESS;
        } catch (IOException e) {
            FeatJAR.log().error(e);
            return FeatJAR.ERROR_WRITING_RESULT;
        }
    }

    /**
     * Write result to output path or console.
     * @param <T> type of the result
     * @param ouputFormat format to store the result in
     * @param optionParser the option list
     */
    private <T> void write(OptionList optionParser, T output, IFormat<T> outputFormat) throws IOException {
        Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElse(null);

        if (outputPath == null) {
            if (outputFormat == null || !outputFormat.isTextual()) {
                FeatJAR.log().plainMessage(String.valueOf(output));
            } else {
                outputFormat.serialize(output).ifEmpty(FeatJAR.log()::problems).ifPresent(FeatJAR.log()::plainMessage);
            }
        } else {
            if (Files.isDirectory(outputPath)) {
                throw new IOException(outputPath.toString() + " is a directory");
            } else if (outputFormat == null) {
                FeatJAR.log().warning(new IOException(outputPath.toString() + " no output format specified"));
                OpenOption[] openOptions =
                        optionParser.getResult(OUTPUT_OVERWRITE_OPTION).get()
                                ? new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING}
                                : new OpenOption[] {StandardOpenOption.CREATE_NEW};
                Files.write(outputPath, String.valueOf(output).getBytes(StandardCharsets.UTF_8), openOptions);
            } else {
                if (Files.exists(outputPath)) {
                    if (optionParser.getResult(OUTPUT_OVERWRITE_OPTION).get()) {
                        FeatJAR.log().warning("Overwriting existing file " + outputPath.toString());
                    } else {
                        FeatJAR.log()
                                .warning(outputPath.toString() + " already exists. Use --"
                                        + OUTPUT_OVERWRITE_OPTION.getName() + " to overwrite existing files.");
                        throw new IOException(outputPath.toString() + " already exists");
                    }
                }
                IOMapperOptions[] ioOutputOptions =
                        optionParser.getResult(OUTPUT_COMPRESSION_OPTION).get()
                                ? new IOMapperOptions[] {IOMapperOptions.ZIP_COMPRESSION}
                                : new IOMapperOptions[0];
                IO.save(output, outputPath, outputFormat, ioOutputOptions);
            }
        }
    }
}

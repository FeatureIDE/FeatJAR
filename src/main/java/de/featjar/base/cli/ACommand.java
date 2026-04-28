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
    public static final Option<Path> INPUT_OPTION = Option.newOption("input", Option.PathParser)
            .setDescription("Path to input file(s)")
            .setValidator(Option.PathValidator);

    /**
     * Output option for saving files.
     */
    public static final Option<Path> OUTPUT_OPTION =
            Option.newOption("output", Option.PathParser).setDescription("Path to output file(s)");

    public static final Option<Boolean> OUTPUT_OVERWRITE_OPTION =
            Option.newFlag("overwrite").setDescription("Overwrite existing file at output path.");

    /**
     * ZIP compression option for saving files.
     */
    public static final Option<Boolean> OUTPUT_COMPRESSION_OPTION =
            Option.newFlag("zip-output").setDescription("Stores output as zip file. (Requires to set an output path.)");

    /**
     * ZIP compression option for reading files.
     */
    public static final Option<Boolean> INTPUT_COMPRESSION_OPTION =
            Option.newFlag("zip-input").setDescription("Reads input as zip file.");

    /**
     * {@return all options registered for the calling class}
     */
    public final List<Option<?>> getOptions() {
        return Option.getAllOptions(getClass());
    }

    protected final <T> Result<T> readFromInput(OptionList optionParser, IFormat<T> format) {
        Path inputPath = getInputPath(optionParser);
        IOMapperOptions[] ioInputOptions = getInputOptions(optionParser);
        return IO.load(inputPath, format, ioInputOptions);
    }

    protected final <T> Result<T> readFromInput(OptionList optionParser, IFormatSupplier<T> format) {
        Path inputPath = getInputPath(optionParser);
        IOMapperOptions[] ioInputOptions = getInputOptions(optionParser);
        return IO.load(inputPath, format, ioInputOptions);
    }

    private Path getInputPath(OptionList optionParser) {
        return optionParser.getResult(INPUT_OPTION).orElseThrow();
    }

    private IOMapperOptions[] getInputOptions(OptionList optionParser) {
        return optionParser.getResult(INTPUT_COMPRESSION_OPTION).get()
                ? new IOMapperOptions[] {IOMapperOptions.ZIP_COMPRESSION}
                : new IOMapperOptions[0];
    }

    /**
     * Write result to output path or console.
     * @param <T> type of the result
     * @param outputResult the result of the command execution
     * @param ouputFormat format to store the result in
     * @param optionParser the option list
     * @return an exit code
     */
    protected final <T> void writeToOutput(T output, IFormat<T> ouputFormat, OptionList optionParser)
            throws IOException {
        Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElse(null);

        if (outputPath == null) {
            if (ouputFormat == null || !ouputFormat.isTextual()) {
                FeatJAR.log().plainMessage(String.valueOf(output));
            } else {
                ouputFormat.serialize(output).ifEmpty(FeatJAR.log()::problems).ifPresent(FeatJAR.log()::plainMessage);
            }
        } else {
            if (Files.isDirectory(outputPath)) {
                throw new IOException(outputPath.toString() + " is a directory");
            } else if (ouputFormat == null) {
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
                IO.save(output, outputPath, ouputFormat, ioOutputOptions);
            }
        }
    }
}

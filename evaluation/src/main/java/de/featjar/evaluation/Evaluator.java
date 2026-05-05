/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-evaluation.
 *
 * evaluation is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with evaluation. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-evaluation> for further information.
 */
package de.featjar.evaluation;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.ListOption;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.RangeOption;
import de.featjar.base.io.csv.CSVFile;
import de.featjar.evaluation.util.OptionCombiner;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TODO documentation
 *
 * @author Sebastian Krieter
 */
public abstract class Evaluator extends ACommand {

    public static String getTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Timestamp(System.currentTimeMillis()));
    }

    public static final Option<Path> modelsPathOption = Option.newOption("models", Option.PathParser)
            .setDefaultValue(Path.of("models"))
            .setDescription("Path to feature model files.")
            .setValidator(Option.PathValidator);
    public static final Option<Path> resourcesPathOption = Option.newOption("resources", Option.PathParser)
            .setDefaultValue(Path.of("resources"))
            .setDescription("Path to other resources necessary for the evaluation.")
            .setValidator(Option.PathValidator);

    public static final Option<Long> timeout = Option.newOption("timeout", Option.LongParser, Long.MAX_VALUE)
            .setDescription("The timeout value for individual runs in milliseconds.");
    public static final Option<Integer> memory = Option.newOption("memory", Option.IntegerParser, -1)
            .setDescription(
                    "The max memory used by started Java processes in gigabytes. Sets the JVM -Xmx parameter of started java process. A negative value defaults to the standard value for the JVM. (Does not affect the memory of this process!)");
    public static final Option<Long> randomSeed =
            Option.newOption("seed", Option.LongParser).setDescription("The seed used by some random operations.");

    public static final Option<Boolean> overwrite = Option.newOption("overwrite", Option.BooleanParser, Boolean.FALSE);

    public static final ListOption<String> systemsOption =
            (ListOption<String>) Option.newListOption("systems", Option.StringParser)
                    .setDescription("The systems considered in the evaluation.");
    public static final RangeOption systemIterationsOption = Option.newRangeOption("systemIterations");
    public static final RangeOption algorithmIterationsOption = Option.newRangeOption("algorithmIterations");

    public OptionList optionParser;
    public OptionCombiner optionCombiner;

    public Path outputPath;
    public Path outputRootPath;
    public Path modelPath;
    public Path resourcePath;
    public Path csvPath;
    public Path dataPath;
    public Path genPath;
    public Path tempPath;
    public List<String> systemNames;

    public OptionList getOptionParser() {
        return optionParser;
    }

    public int getSystemId(final String modelName) {
        return systemNames.indexOf(modelName);
    }

    public <T> T getOption(Option<T> option) {
        return optionParser.getResult(option).orElseThrow();
    }

    public String readCurrentOutputMarker() {
        final Path currentOutputMarkerFile = outputRootPath.resolve(".current");
        String currentOutputMarker = null;
        if (Files.isReadable(currentOutputMarkerFile)) {
            List<String> lines;
            try {
                lines = Files.readAllLines(currentOutputMarkerFile);

                if (!lines.isEmpty()) {
                    final String firstLine = lines.get(0);
                    currentOutputMarker = firstLine.trim();
                }
            } catch (final Exception e) {
                FeatJAR.log().error(e);
            }
        }

        try {
            Files.createDirectories(outputRootPath);
        } catch (final IOException e) {
            FeatJAR.log().error(e);
        }

        if (currentOutputMarker == null) {
            currentOutputMarker = getTimeStamp();
            try {
                Files.write(currentOutputMarkerFile, currentOutputMarker.getBytes(StandardCharsets.UTF_8));
            } catch (final IOException e) {
                FeatJAR.log().error(e);
            }
        }
        return currentOutputMarker;
    }

    protected abstract void runEvaluation() throws Exception;

    @Override
    public int run(OptionList optionParser) {
        this.optionParser = optionParser;
        this.optionCombiner = new OptionCombiner(optionParser);
        try {
            init();

            updateSubPaths();

            FeatJAR.log().info("Running " + getIdentifier());
            Properties properties = new Properties();
            for (final Option<?> opt : getOptions()) {
                String name = opt.getName();
                String value = String.valueOf(optionParser.getResult(opt).orElse(null));
                String isDefaultValue = optionParser.has(opt) ? "" : " (default)";
                properties.put(name, value);
                FeatJAR.log().info("\t%-20s: %s%s", name, value, isDefaultValue);
            }
            try (OutputStream newOutputStream = Files.newOutputStream(csvPath.resolve("config.properties"))) {
                properties.store(newOutputStream, null);
            }

            runEvaluation();
            return 0;
        } catch (final Exception e) {
            FeatJAR.log().error(e);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        } finally {
            FeatJAR.log().dispose();
            dispose();
        }
    }

    public void init() throws Exception {
        outputRootPath = optionParser.getResult(OUTPUT_OPTION).get();
        resourcePath = optionParser.getResult(resourcesPathOption).get();
        modelPath = optionParser.getResult(modelsPathOption).get();
        systemNames = Files.list(modelPath)
                .map(p -> p.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
        FeatJAR.log().info("Running " + this.getClass().getSimpleName());
    }

    private void updateSubPaths() throws IOException {
        initSubPaths();
        try {
            setupDirectories();
        } catch (final IOException e) {
            FeatJAR.log().error("Fail -> Could not create output directory.");
            FeatJAR.log().error(e);
            throw e;
        }
    }

    protected void initRootPaths() {}

    protected void initSubPaths() {
        outputPath = outputRootPath.resolve(readCurrentOutputMarker());
        dataPath = outputPath.resolve("data");
        csvPath = dataPath.resolve("data-" + getTimeStamp());
        tempPath = outputPath.resolve("temp");
        genPath = outputPath.resolve("gen");
    }

    protected void setupDirectories() throws IOException {
        try {
            createDir(outputPath);
            createDir(dataPath);
            createDir(csvPath);
            createDir(genPath);
            createDir(tempPath);
        } catch (final IOException e) {
            FeatJAR.log().error("Could not create output directory.");
            FeatJAR.log().error(e);
            throw e;
        }
    }

    private void createDir(final Path path) throws IOException {
        if (path != null) {
            Files.createDirectories(path);
        }
    }

    public void dispose() {
        deleteTempFolder();
    }

    private void deleteTempFolder() {
        if (tempPath != null) {
            try {
                Files.walkFileTree(tempPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public CSVFile addCSVWriter(String fileName, String... csvHeader) throws IOException {
        long count = Files.walk(csvPath)
                .filter(p -> p.getFileName().toString().matches(Pattern.quote(fileName) + "(-\\d+)?[.]csv"))
                .count();
        final Path csvFilePath = csvPath.resolve(fileName + "-" + count + ".csv");
        final CSVFile csvWriter = new CSVFile(csvFilePath);
        csvWriter.setHeaderFields(csvHeader);
        csvWriter.flush();
        return csvWriter;
    }

    public static int readMaxCSVId(final Path csvFile) {
        try {
            return CSVFile.readAllLines(csvFile)
                    .skip(1)
                    .mapToInt(l -> Integer.parseInt(l.get(0)))
                    .max()
                    .orElse(-1);
        } catch (IOException e) {
            return -1;
        }
    }
}

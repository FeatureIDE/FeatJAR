package de.featjar.gui;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.data.Result;
import de.featjar.base.env.HostEnvironment;
import de.featjar.base.env.HostEnvironment.OperatingSystem;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.IFormatSupplier;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;
import de.featjar.gui.bin.GuiWebBinary;
import de.featjar.gui.io.EMFFeatureModelFormat;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Starts a GUI for modifying a feature model.
 *
 * Converts the input model into the EMF format the client, starts the
 * language server as a separate process, and opens the diagram in a browser. The
 * server runs detached so that a crash does not cause FeatJar to crash.
 * Output signals issued by the client are written to the server's standard output.
 *
 * @author Niclas Kleinert
 * @author Sebastian Krieter
 */
public class FeatureModelGuiCommand extends ACommand {

    public static final Option<IFormatSupplier<IFeatureModel>> INPUT_FORMAT =
            Options.newInputFormatOption(FeatureModelFormats.class);

    public static final Option<IFormat<IFeatureModel>> OUTPUT_FORMAT =
            Options.newOutputFormatOption(FeatureModelFormats.class, new XMLFeatureModelFormat().getName());

    public static final Option<Integer> PORT_OPTION = Options.newOption("port", Options.IntegerParser, "8081")
            .setDescription("Port of the local graphical language server.");

    private static final Path BINARY_PATH =
            FeatJAR.extension(GuiWebBinary.class).getDirectory();

    private static final Path CLIENT_PATH = BINARY_PATH.resolve("app");
    private static final Path CLIENT_HTML_PATH = CLIENT_PATH.resolve("diagram.html");
    private static final Path CLIENT_ABSOLUTE_EMF_FILE_PATH = CLIENT_PATH.resolve("gui_model.featuremodel");

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Opens a GUI for feature modeling.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("gui");
    }

    @Override
    public int run(OptionList optionList) {
        Result<IFeatureModel> inputFM = readFromInput(optionList, optionList.get(INPUT_FORMAT));
        if (inputFM.isEmpty()) {
            FeatJAR.log().problems(inputFM);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
        try {
            IO.save(inputFM.get(), CLIENT_ABSOLUTE_EMF_FILE_PATH, new EMFFeatureModelFormat());
        } catch (IOException e) {
            FeatJAR.log().error(e);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }

        final GuiServer guiServer = new GuiServer();
        guiServer.setStartHook(this::openBrowser);
        guiServer.setStopHook(() -> writeOutput(optionList));
        guiServer.setSignalHook(signal -> {
            switch (signal) {
                case FeatureModelWebsocketLauncher.SIGNAL_SAVE -> writeOutput(optionList);
                default -> {}
            }
        });
        return guiServer.run(optionList.get(PORT_OPTION)) ? FeatJAR.EXIT_SUCCESS : FeatJAR.ERROR_COMPUTING_RESULT;
    }

    private int writeOutput(OptionList optionList) {
        return writeResult(
                optionList,
                IO.load(CLIENT_ABSOLUTE_EMF_FILE_PATH, new EMFFeatureModelFormat()),
                optionList.get(OUTPUT_FORMAT));
    }

    private void openBrowser() {
        FeatJAR.log().message("URL: " + CLIENT_HTML_PATH.toAbsolutePath().toUri());
        String openCommand =
                switch (HostEnvironment.OPERATING_SYSTEM) {
                    case OperatingSystem.LINUX -> "xdg-open";
                    case OperatingSystem.MAC_OS -> "open";
                    case OperatingSystem.WINDOWS -> "start";
                    case OperatingSystem.UNKNOWN -> null;
                    default ->
                        throw new IllegalArgumentException("Unexpected value: " + HostEnvironment.OPERATING_SYSTEM);
                };
        if (openCommand != null) {
            try {
                new ProcessBuilder(Arrays.asList(
                                openCommand,
                                CLIENT_HTML_PATH.toAbsolutePath().toUri().toString()))
                        .start();
            } catch (IOException e) {
                FeatJAR.log().error(e);
            }
        }
    }
}

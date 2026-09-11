package de.featjar.gui.bin;

import de.featjar.base.env.ABinary;
import de.featjar.base.env.HostEnvironment.OperatingSystem;

public class GuiWebBinary extends ABinary {

    @Override
    public String getCategory() {
        return "gui";
    }

    @Override
    protected String getName() {
        return "web";
    }

    protected String getOSResourceDirectory(OperatingSystem os) {
        return switch (os) {
            case WINDOWS, MAC_OS, LINUX -> "";
            case UNKNOWN -> "unkown";
            default -> throw new IllegalStateException("Unexpected value: " + os);
        };
    }
}

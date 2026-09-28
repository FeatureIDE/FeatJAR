package de.featjar.gui.operation;

import org.eclipse.glsp.server.operations.Operation;

/**
 * This Class is AI generated
 * Carries the "Set Color..." request from the client to the server —
 * which feature was selected, and which color was entered for it.
 */
public class SetFeatureColorOperation extends Operation {

    public static final String KIND = "setFeatureColor";
    private String elementId;
    private String color;

    public SetFeatureColorOperation() {
        super(KIND);
    }

    public String getElementId() {
        return elementId;
    }

    public String getColor() {
        return color;
    }
}

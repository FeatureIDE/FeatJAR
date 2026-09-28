package de.featjar.gui.operation;

import org.eclipse.glsp.server.operations.Operation;

/**
 * This Class is AI generated
 * Carries the "Show attributes" checkbox state from the client to the server
 * whenever it's switched on or off.
 */
public class ToggleShowAttributesOperation extends Operation {

    public static final String KIND = "toggleShowAttributes";
    private boolean enabled;

    public ToggleShowAttributesOperation() {
        super(KIND);
    }

    public boolean isEnabled() {
        return enabled;
    }
}

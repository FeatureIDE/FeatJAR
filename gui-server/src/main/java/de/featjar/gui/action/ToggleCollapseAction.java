package de.featjar.gui.action;

import org.eclipse.glsp.server.actions.Action;

public class ToggleCollapseAction extends Action {

    public static final String KIND = "toggleCollapse";

    private String elementId;

    public ToggleCollapseAction() {
        super(KIND);
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(final String elementId) {
        this.elementId = elementId;
    }
}

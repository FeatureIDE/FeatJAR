package de.featjar.gui.operation.handler;

import de.featjar.gui.operation.ToggleShowAttributesOperation;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.operations.GModelOperationHandler;

/**
 * This Class is AI generated
 * Remembers whether attributes should currently be shown,
 * so the diagram can include or leave out the attribute labels
 * the next time it's rebuilt.
 */
public class ToggleShowAttributesHandler extends GModelOperationHandler<ToggleShowAttributesOperation> {

    public static final String SHOW_ATTRIBUTES_PROPERTY = "showAttributes";

    @Override
    public Optional<Command> createCommand(ToggleShowAttributesOperation operation) {
        return commandOf(() -> modelState.setProperty(SHOW_ATTRIBUTES_PROPERTY, operation.isEnabled()));
    }
}

package de.featjar.gui.operation.handler;

import com.google.inject.Inject;
import de.featjar.gui.operation.SetFeatureColorOperation;
import de.featjar.gui.utils.AttributeKeysUtils;
import de.featjar.gui.utils.IdentifiableResolver;
import featJAR.Feature;
import featJAR.Identifiable;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.operations.GModelOperationHandler;

/**
 * this Class is AI generated
 * Handles the "Set Color..." action from the diagram.
 *
 * Looks up the feature the user clicked, and if a color was actually
 * given, sets it as a "color" attribute on that feature in memory.
 * The diagram picks this up automatically afterwards and repaints
 * the node.
 */
public class SetFeatureColorHandler extends GModelOperationHandler<SetFeatureColorOperation> {

    @Inject
    protected IdentifiableResolver resolver;

    @Override
    public Optional<Command> createCommand(SetFeatureColorOperation operation) {
        Identifiable element = resolver.findById((operation.getElementId())).orElseThrow();
        if (!(element instanceof Feature feature)) {
            return Optional.empty();
        }

        String color = operation.getColor();
        if (color == null || color.isBlank()) {
            return Optional.empty();
        }

        return commandOf(() -> AttributeKeysUtils.setColor(feature, color));
    }
}

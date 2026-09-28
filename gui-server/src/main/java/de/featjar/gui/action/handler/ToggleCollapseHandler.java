// Generated with Claude Opus 5.5 (Anthropic, via claude.ai)
package de.featjar.gui.action.handler;

import com.google.inject.Inject;
import de.featjar.gui.action.ToggleCollapseAction;
import de.featjar.gui.utils.CollapseUtils;
import de.featjar.gui.utils.IdentifiableResolver;
import featJAR.Feature;
import featJAR.Identifiable;
import java.util.List;
import java.util.Optional;
import org.eclipse.glsp.server.actions.AbstractActionHandler;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.emf.notation.EMFNotationModelState;
import org.eclipse.glsp.server.features.core.model.ModelSubmissionHandler;

/**
 * Handles the {@link ToggleCollapseAction}: collapses or expands the subtree of a feature
 * and sends the updated diagram to the client.
 * If the action contains no elementId, the currently selected element is used.
 */
public class ToggleCollapseHandler extends AbstractActionHandler<ToggleCollapseAction> {

    @Inject
    protected EMFNotationModelState modelState;

    @Inject
    protected IdentifiableResolver resolver;

    @Inject
    protected ModelSubmissionHandler modelSubmissionHandler;

    @Override
    protected List<Action> executeAction(final ToggleCollapseAction action) {
        Optional<Identifiable> element = findElement(action.getElementId());
        if (element.isEmpty() || !(element.get() instanceof Feature feature)) {
            return none();
        }
        CollapseUtils.toggle(modelState, feature.getId());
        return modelSubmissionHandler.submitModel();
    }

    private Optional<Identifiable> findElement(final String elementId) {
        if (elementId == null || elementId.isEmpty()) {
            return modelState.getProperty("currentSelection", Identifiable.class);
        }
        return resolver.findById(elementId).toOptional();
    }
}

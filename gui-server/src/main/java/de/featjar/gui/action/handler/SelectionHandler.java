/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-gui-server.
 *
 * gui-server is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * gui-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gui-server. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE> for further information.
 */
package de.featjar.gui.action.handler;

import com.google.inject.Inject;
import de.featjar.gui.utils.IdentifiableResolver;
import java.util.List;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.actions.SelectAction;
import org.eclipse.glsp.server.emf.EMFModelState;

/**
 * The handler set the current selection received from the client by setting the "currentSelection"
 * property in {@link SelectionHandler#modelState}.
 */
public class SelectionHandler implements ActionHandler {

    @Inject
    protected IdentifiableResolver resolver;

    @Inject
    protected EMFModelState modelState;

    @Override
    public boolean handles(final Action action) {
        return action instanceof SelectAction;
    }

    @Override
    public List<Class<? extends Action>> getHandledActionTypes() {
        return List.of(SelectAction.class);
    }

    @Override
    public List<Action> execute(final Action action) {
        List<String> selectedIds = ((SelectAction) action).getSelectedElementsIDs();

        if (selectedIds.isEmpty()) {
            return none();
        }

        resolver.findById(selectedIds.get(0)).ifPresent(element -> modelState.setProperty("currentSelection", element));

        return none();
    }
}

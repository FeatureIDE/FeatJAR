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

import de.featjar.gui.FeatureModelWebsocketLauncher;
import de.featjar.gui.action.SaveAction;
import java.util.List;
import org.eclipse.glsp.server.actions.AbstractActionHandler;
import org.eclipse.glsp.server.actions.Action;

/**
 * The handler initiates the saving of the current state.
 * It is triggered by the underlying action {@link SaveAction}.
 */
public class SaveHandler extends AbstractActionHandler<SaveAction> {

    @Override
    protected List<Action> executeAction(final SaveAction action) {
        FeatureModelWebsocketLauncher.out.println(FeatureModelWebsocketLauncher.SIGNAL_SAVE);
        return none();
    }
}

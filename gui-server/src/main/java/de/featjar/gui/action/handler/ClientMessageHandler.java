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

import de.featjar.base.FeatJAR;
import de.featjar.gui.action.ClientMessageAction;
import java.util.List;
import org.eclipse.glsp.server.actions.AbstractActionHandler;
import org.eclipse.glsp.server.actions.Action;

/**
 * The handler logs a message send by the client.
 * It is triggered by the underlying action {@link ClientMessageAction}.
 */
public class ClientMessageHandler extends AbstractActionHandler<ClientMessageAction> {

    @Override
    protected List<Action> executeAction(final ClientMessageAction action) {
        FeatJAR.log().message("Client Message: " + action.getClientMessage());
        return none();
    }
}

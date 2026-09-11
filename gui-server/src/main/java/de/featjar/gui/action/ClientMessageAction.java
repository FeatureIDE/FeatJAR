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
package de.featjar.gui.action;

import org.eclipse.glsp.server.actions.Action;

/**
 * The Action is initiated via the the client's action dispatcher and sends a message to the server.
 * This triggers the {@link ClientMessageAction}.
 * Currently, the Action is only used for keep alive pings that are send to the server.
 *
 * The client has the same implemented class.
 * The KIND constant has to exactly match the KIND constant from client implementation.
 * Otherwise the server never registers a handler for it and the action is silently dropped.
 * The fields in the client class have to match them in the server class.
 */
public class ClientMessageAction extends Action {

    public static final String KIND = "clientMessage";

    private String message;

    public ClientMessageAction() {
        super(KIND);
    }

    public ClientMessageAction(final String message) {
        super(KIND);
        this.message = message;
    }

    public String getClientMessage() {
        return message;
    }

    public void setClientMessage(String message) {
        this.message = message;
    }
}

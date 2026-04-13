/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.data.operation;

import java.util.ArrayDeque;

/**
 * A history of {@link IOperation operations}. Stores a limited number of executed and undone operations in order of their application. Allows to undo and redo operations.
 *
 * @author Sebastian Krieter
 */
public class History {

    private ArrayDeque<IOperation> appliedOperations = new ArrayDeque<>();
    private ArrayDeque<IOperation> undoneOperations = new ArrayDeque<>();

    private int limit = Integer.MAX_VALUE;

    public boolean apply(IOperation operation) {
        if (operation.firstDo()) {
            undoneOperations.clear();
            if (appliedOperations.size() >= limit) {
                appliedOperations.removeFirst();
            }
            appliedOperations.add(operation);
            return true;
        }
        return false;
    }

    public boolean undo() {
        if (!appliedOperations.isEmpty()) {
            IOperation operation = appliedOperations.removeLast();
            if (operation.undo()) {
                undoneOperations.add(operation);
                return true;
            } else {
                appliedOperations.add(operation);
            }
        }
        return false;
    }

    public boolean redo() {
        if (!undoneOperations.isEmpty()) {
            IOperation operation = undoneOperations.removeLast();
            if (operation.redo()) {
                appliedOperations.add(operation);
                return true;
            } else {
                undoneOperations.add(operation);
            }
        }
        return false;
    }
}

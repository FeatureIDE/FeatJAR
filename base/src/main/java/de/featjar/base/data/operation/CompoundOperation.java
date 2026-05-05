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

import de.featjar.base.FeatJAR;
import java.util.List;
import java.util.ListIterator;

/**
 * An {@link IOperation operation} that consists of multiple other operations.
 *
 * @author Sebastian Krieter
 */
public abstract class CompoundOperation implements IOperation {

    private List<IOperation> subOperations;

    protected abstract List<IOperation> createSubOperations();

    @Override
    public boolean firstDo() {
        subOperations = createSubOperations();
        ListIterator<IOperation> listIterator = subOperations.listIterator();
        while (listIterator.hasNext()) {
            IOperation operation = listIterator.next();
            try {
                if (operation.firstDo()) {
                    continue;
                }
            } catch (Exception e) {
                FeatJAR.log().error(e);
            }
            // only reached in case of a problem
            partialUndo(listIterator);
            return false;
        }
        return true;
    }

    @Override
    public boolean undo() {
        ListIterator<IOperation> listIterator = subOperations.listIterator(subOperations.size());
        while (listIterator.hasPrevious()) {
            IOperation operation = listIterator.previous();
            try {
                if (operation.undo()) {
                    continue;
                }
            } catch (Exception e) {
                FeatJAR.log().error(e);
            }
            // only reached in case of a problem
            partialRedo(listIterator);
            return false;
        }
        return true;
    }

    @Override
    public boolean redo() {
        ListIterator<IOperation> listIterator = subOperations.listIterator();
        while (listIterator.hasNext()) {
            IOperation operation = listIterator.next();
            try {
                if (operation.redo()) {
                    continue;
                }
            } catch (Exception e) {
                FeatJAR.log().error(e);
            }
            // only reached in case of a problem
            partialUndo(listIterator);
            return false;
        }
        return true;
    }

    private void partialUndo(ListIterator<IOperation> listIterator) {
        listIterator.previous();
        while (listIterator.hasPrevious()) {
            IOperation operation = listIterator.previous();
            if (!operation.undo()) {
                throw new IllegalStateException(String.format(
                        "Could not undo sub-operation %s of operation %s", operation.getName(), getName()));
            }
        }
    }

    private void partialRedo(ListIterator<IOperation> listIterator) {
        listIterator.next();
        while (listIterator.hasNext()) {
            IOperation operation = listIterator.next();
            if (!operation.redo()) {
                throw new IllegalStateException(String.format(
                        "Could not redo sub-operation %s of operation %s", operation.getName(), getName()));
            }
        }
    }
}

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
package de.featjar.base.log;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import java.util.List;

/**
 * Provides an interface to track the progress of a {@link IComputation computation}.
 * For instance, print the relative progress to the {@link ProgressThread console}.
 * Uses {@link IMessage messages} to customize the output.
 *
 * @see FeatJAR#progress()
 *
 * @author Sebastian Krieter
 */
public interface IProgressBar {

    /**
     * Tracks the given progress object with a set of default messages.
     * @param progress the progress object
     *
     * @see ActivityMessage
     * @see RelativeProgressMessage
     * @see PassedTimeMessage
     */
    default void track(Progress progress) {
        track(progress, new ActivityMessage(), new RelativeProgressMessage(progress), new PassedTimeMessage());
    }

    /**
     * Tracks the given progress object with the given list of messages.
     * @param progress the progress object
     * @param messageSuppliers the list of messages
     */
    default void track(Progress progress, IMessage... messageSuppliers) {
        track(progress, List.of(messageSuppliers));
    }

    /**
     * Tracks the given progress object with the given list of messages.
     * @param progress the progress object
     * @param messageSuppliers the list of messages
     */
    void track(Progress progress, List<IMessage> messageSuppliers);

    /**
     * Removes the tracking of the last tracked progress object.
     */
    void untrack();
}

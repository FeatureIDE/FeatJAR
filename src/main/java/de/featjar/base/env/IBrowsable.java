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
package de.featjar.base.env;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * An object that can be displayed in a web browser.
 * Can be passed an argument to influence what is displayed.
 *
 * @param <T> the type of the argument
 * @author Elias Kuiter
 */
public interface IBrowsable<T> {
    /**
     * Opens a URL given by a string in the default web browser.
     *
     * @param urlString the URL string
     */
    static void browse(String urlString) {
        browse(URI.create(urlString));
    }

    /**
     * Opens the given URI in the default web browser.
     *
     * @param uri the URI
     */
    static void browse(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@return the URI to open in a web browser to display this object}
     *
     * @param argument the argument
     */
    Result<URI> getBrowseURI(T argument);

    /**
     * Displays this object in a web browser.
     *
     * @param argument the argument
     */
    default void browse(T argument) {
        Result<URI> browseURI = getBrowseURI(argument);
        if (browseURI.isEmpty()) {
            FeatJAR.log().error("cannot display " + this + " in browser");
            return;
        }
        FeatJAR.log().info("displaying " + this + " in browser");
        browse(browseURI.get());
    }

    /**
     * Displays this object in a web browser, blocking execution until the user interacts.
     *
     * @param argument the argument
     */
    @SuppressWarnings("resource")
    default void debugBrowse(T argument) {
        browse(argument);
        FeatJAR.log().info("press return to continue");
        new Scanner(System.in, StandardCharsets.UTF_8).nextLine();
    }
}

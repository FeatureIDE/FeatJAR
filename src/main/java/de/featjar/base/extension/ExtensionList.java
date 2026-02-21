/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
package de.featjar.base.extension;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Maps;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import de.featjar.base.log.IndentFormatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Synchronized list containing {@link IExtension extensions}.
 * This is supposed to be used by extension points.
 *
 * @param <T> the type of the extensions
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class ExtensionList<T extends IExtension> implements Iterable<T> {
    private final LinkedHashMap<String, Integer> indexMap = Maps.empty();
    private final List<T> extensions = new CopyOnWriteArrayList<>();

    /**
     * Adds a new extension to this list.
     *
     * @param extension the extension to add
     * @return whether the extension was not in the list before
     */
    public synchronized boolean addExtension(T extension) {
        if ((extension != null) && !indexMap.containsKey(extension.getIdentifier())) {
            indexMap.put(extension.getIdentifier(), extensions.size());
            extensions.add(extension);
            return true;
        }
        return false;
    }

    /**
     * Removes an extension from this list.
     *
     * @param extension the extension to remove
     * @return whether the extension was in the list before
     */
    public synchronized boolean removeExtension(T extension) {
        FeatJAR.log().debug("uninstalling extension " + extension.getClass().getName());
        if (indexMap.containsKey(extension.getIdentifier())) {
            indexMap.remove(extension.getIdentifier());
            extensions.remove(extension);
            extension.close();
            return true;
        }
        return false;
    }

    /**
     * Removes all extensions from this list.
     */
    public synchronized void removeAll() {
        extensions.forEach(this::removeExtension);
    }

    /**
     * {@return all extensions installed at this extension point}
     * The list is in the same order as the extensions were installed with {@link #installExtension(IExtension)}.
     */
    public List<T> getExtensions() {
        return extensions;
    }

    /**
     * {@return the extension in this list with a given identifier, if any}
     *
     * @param identifier the identifier
     */
    public synchronized Result<T> getExtension(String identifier) {
        Objects.requireNonNull(identifier, "identifier must not be null!");
        final Integer index = indexMap.get(identifier);
        return index != null
                ? Result.of(extensions.get(index))
                : Result.empty(new Problem("no extension found for identifier " + identifier, Problem.Severity.ERROR));
    }

    /**
     * {@return the extension in this list matching the given identifier}
     * The matching is case-insensitive.
     * If no extensions match or the match is ambiguous, an empty result is returned.
     *
     * @param partOfIdentifier the part of the extension's identifier
     */
    public synchronized Result<T> getMatchingExtension(String partOfIdentifier) {
        final String identifierPart = partOfIdentifier.toLowerCase();
        LinkedHashSet<String> matchingIdentifiers = indexMap.keySet().stream()
                .filter(identifier -> identifier.toLowerCase().contains(identifierPart))
                .collect(Sets.toSet());
        if (matchingIdentifiers.isEmpty())
            return Result.empty(
                    new Problem("found no extensions matching " + partOfIdentifier, Problem.Severity.ERROR));
        if (matchingIdentifiers.size() > 1)
            return Result.empty(new Problem(
                    "found more than one extensions matching " + partOfIdentifier + ": \n"
                            + IndentFormatter.formatList(matchingIdentifiers),
                    Problem.Severity.ERROR));
        return getExtension(matchingIdentifiers.iterator().next());
    }

    /**
     * {@return all extensions in this list matching the given regular expression}
     * If no extensions match, an empty list is returned.
     *
     * @param regex the regular expression
     */
    public synchronized List<T> getMatchingExtensions(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return indexMap.entrySet().stream()
                .filter(e -> pattern.matcher(e.getKey()).matches())
                .map(e -> extensions.get(e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<T> iterator() {
        return extensions.iterator();
    }
}

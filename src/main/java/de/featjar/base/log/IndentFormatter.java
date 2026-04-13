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

import de.featjar.base.log.Log.Verbosity;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Prepends a log message with an appropriate indent.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class IndentFormatter implements IFormatter {
    private int level = 0;
    private String symbol = "\t";

    public void addIndent() {
        level++;
    }

    public void removeIndent() {
        if (level > 0) level--;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(level, 0);
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = Objects.requireNonNull(symbol);
    }

    @Override
    public String getPrefix(String message, Verbosity verbosity) {
        return String.valueOf(symbol).repeat(Math.max(0, level));
    }

    public static String formatList(Collection<?> collection) {
        return collection.stream().map(o -> "\t" + o + "\n").collect(Collectors.joining());
    }

    public static String formatList(String prefix, Collection<?> collection) {
        return String.format("%s[%n%s]", prefix, formatList(collection));
    }
}

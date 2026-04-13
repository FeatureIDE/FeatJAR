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

import java.util.Collection;

/**
 * Builds multiline strings that are indented.
 *
 * @author Elias Kuiter
 */
public class IndentStringBuilder {
    protected final StringBuilder stringBuilder;
    protected final IndentFormatter indentFormatter;

    public IndentStringBuilder() {
        this(new StringBuilder(), new IndentFormatter());
    }

    public IndentStringBuilder(StringBuilder stringBuilder) {
        this(stringBuilder, new IndentFormatter());
    }

    public IndentStringBuilder(IndentFormatter indentFormatter) {
        this(new StringBuilder(), indentFormatter);
    }

    public IndentStringBuilder(StringBuilder stringBuilder, IndentFormatter indentFormatter) {
        this.stringBuilder = stringBuilder;
        this.indentFormatter = indentFormatter;
    }

    public IndentStringBuilder append(String string) {
        stringBuilder.append(indentFormatter.getPrefix(string, null)).append(string);
        return this;
    }

    public IndentStringBuilder appendLine(Collection<?> collection) {
        collection.stream().map(Object::toString).forEach(this::appendLine);
        return this;
    }

    public IndentStringBuilder appendLine(String string) {
        return append(string + "\n");
    }

    public IndentStringBuilder appendLine() {
        return appendLine("");
    }

    public IndentStringBuilder addIndent() {
        indentFormatter.addIndent();
        return this;
    }

    public IndentStringBuilder removeIndent() {
        indentFormatter.removeIndent();
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}

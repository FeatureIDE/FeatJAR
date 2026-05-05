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
package de.featjar.base.data.identifier;

/**
 * Identifies an object with a given number.
 *
 * @author Elias Kuiter
 */
public class CounterIdentifier extends AIdentifier {

    public static CounterIdentifier newInstance() {
        return new Factory().get();
    }

    protected final long counter;

    public CounterIdentifier(long counter, Factory factory) {
        super(factory);
        this.counter = counter;
    }

    public long getCounter() {
        return counter;
    }

    @Override
    public String toString() {
        return String.valueOf(counter);
    }

    /**
     * Creates counter identifiers by incrementing a number.
     */
    public static class Factory implements IIdentifierFactory {
        long counter = 0;

        @Override
        public CounterIdentifier get() {
            return new CounterIdentifier(++counter, this);
        }

        @Override
        public CounterIdentifier parse(String identifierString) {
            return new CounterIdentifier(Long.parseLong(identifierString), this);
        }
    }
}

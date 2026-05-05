/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula;

/**
 * Holds statistics regarding coverage of a configuration sample.
 *
 * @author Sebastian Krieter
 */
public class CoverageStatistic {
    private long numberOfInvalidElements;
    private long numberOfCoveredElements;
    private long numberOfUncoveredElements;
    private long numberOfIgnoredElements;

    public void setNumberOfInvalidElements(long numberOfInvalidElements) {
        this.numberOfInvalidElements = numberOfInvalidElements;
    }

    public void setNumberOfCoveredElements(long numberOfCoveredElements) {
        this.numberOfCoveredElements = numberOfCoveredElements;
    }

    public void setNumberOfUncoveredElements(long numberOfUncoveredElements) {
        this.numberOfUncoveredElements = numberOfUncoveredElements;
    }

    public void setNumberOfIgnoredElements(long numberOfIgnoredElements) {
        this.numberOfIgnoredElements = numberOfIgnoredElements;
    }

    public void incNumberOfInvalidElements() {
        numberOfInvalidElements++;
    }

    public void incNumberOfCoveredElements() {
        numberOfCoveredElements++;
    }

    public void incNumberOfUncoveredElements() {
        numberOfUncoveredElements++;
    }

    public void incNumberOfIgnoredElements() {
        numberOfIgnoredElements++;
    }

    public long total() {
        return numberOfInvalidElements + numberOfCoveredElements + numberOfUncoveredElements + numberOfIgnoredElements;
    }

    public long valid() {
        return numberOfCoveredElements + numberOfUncoveredElements;
    }

    public long invalid() {
        return numberOfInvalidElements;
    }

    public long covered() {
        return numberOfCoveredElements;
    }

    public long uncovered() {
        return numberOfUncoveredElements;
    }

    public long ignored() {
        return numberOfIgnoredElements;
    }

    public double coverage() {
        return (numberOfCoveredElements + numberOfUncoveredElements != 0)
                ? (double) numberOfCoveredElements / (numberOfCoveredElements + numberOfUncoveredElements)
                : 1.0;
    }

    public CoverageStatistic merge(CoverageStatistic other) {
        numberOfInvalidElements += other.numberOfInvalidElements;
        numberOfCoveredElements += other.numberOfCoveredElements;
        numberOfUncoveredElements += other.numberOfUncoveredElements;
        numberOfIgnoredElements += other.numberOfIgnoredElements;
        return this;
    }

    public String print() {
        long total = total();
        int digits = total == 0 ? 1 : (int) (Math.log10(total()) + 1);
        String format = "%" + digits + "d";

        StringBuilder sb = new StringBuilder();
        sb.append("Interaction Coverage Statistics");
        sb.append("\nCoverage:     ");
        sb.append(coverage());
        sb.append("\nInteractions: ");
        sb.append(total);
        sb.append("\n ");
        sb.append(Character.toChars(0x251c));
        sb.append(Character.toChars(0x2500));
        sb.append("Covered:   ");
        sb.append(String.format(format, numberOfCoveredElements));
        sb.append("\n ");
        sb.append(Character.toChars(0x251c));
        sb.append(Character.toChars(0x2500));
        sb.append("Uncovered: ");
        sb.append(String.format(format, numberOfUncoveredElements));
        sb.append("\n ");
        sb.append(Character.toChars(0x251c));
        sb.append(Character.toChars(0x2500));
        sb.append("Ignored:   ");
        sb.append(String.format(format, numberOfInvalidElements));
        sb.append("\n ");
        sb.append(Character.toChars(0x2514));
        sb.append(Character.toChars(0x2500));
        sb.append("Invalid:   ");
        sb.append(String.format(format, numberOfIgnoredElements));
        return sb.toString();
    }
}

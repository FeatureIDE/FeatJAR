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
package de.featjar.formula.io.dimacs;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Pair;
import de.featjar.base.io.NonEmptyLineIterator;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.VariableMap;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic parser for DIMACS format.
 *
 * @author Sebastian Krieter
 */
public class DimacsParser {

    private static final Pattern commentPattern = Pattern.compile("\\A" + DimacsSerializer.COMMENT + "\\s*(.*)\\Z");
    private static final Pattern variablePattern =
            Pattern.compile("\\A" + DimacsSerializer.COMMENT + "\\s+(\\d+)\\s+(\\S+)\\s*\\Z");
    private static final Pattern groupPattern =
            Pattern.compile("\\A" + DimacsSerializer.COMMENT + "\\s+" + DimacsSerializer.GROUP + "\\s+(\\d+)\\s*\\Z");
    private static final Pattern clauseLinePattern =
            Pattern.compile("\\A((-?\\d+\\s+)*)" + DimacsSerializer.CLAUSE_END + "\\s*\\Z");
    private static final Pattern problemPattern = Pattern.compile(
            "\\A\\s*" + DimacsSerializer.PROBLEM + "\\s+" + DimacsSerializer.TYPE + "\\s+(\\d+)\\s+(\\d+)");

    /** Maps indexes to variables. */
    protected final VariableMap indexVariables = new VariableMap();

    /**
     * The amount of variables as declared in the problem definition. May differ
     * from the actual amount of found variables.
     */
    private int variableCount;
    /** The amount of clauses in the problem. */
    private int clauseCount;

    /** The line iterator. */
    protected NonEmptyLineIterator nonEmptyLineIterator;

    /** True to read the variable directory for naming variables. */
    private boolean readVariableDirectory = false;
    /**
     * <p>
     * Sets the reading variable directory flag. If true, the reader will look for a
     * variable directory in the comments. This contains names for the variables
     * which would otherwise just be numbers.
     * </p>
     *
     * <p>
     * Defaults to false.
     * </p>
     *
     * @param readVariableDirectory whether to read the variable directory
     */
    public void setReadingVariableDirectory(boolean readVariableDirectory) {
        this.readVariableDirectory = readVariableDirectory;
    }

    /**
     * Parses the input.
     *
     * @param inputMapper The source to read from.
     * @return a pair containing the variable map and a list of clauses.
     * @throws IOException    if the reader encounters a problem.
     * @throws ParseException if the input does not conform to the DIMACS CNF file format.
     */
    public Pair<VariableMap, List<List<int[]>>> parse(AInputMapper inputMapper) throws ParseException, IOException {
        nonEmptyLineIterator = inputMapper.get().getNonEmptyLineIterator();
        nonEmptyLineIterator.get();
        indexVariables.clear();

        readHeader();

        final List<List<int[]>> clauses = readBody();

        final int actualClauseCount = clauses.stream().mapToInt(List::size).sum();
        if (clauseCount != actualClauseCount) {
            throw new ParseException(
                    String.format("Found %d instead of %d clauses", actualClauseCount, clauseCount), 1);
        }
        return new Pair<>(indexVariables, clauses);
    }

    protected void init(AInputMapper inputMapper) {
        nonEmptyLineIterator = inputMapper.get().getNonEmptyLineIterator();
        nonEmptyLineIterator.get();
        indexVariables.clear();
    }

    private void readHeader() throws ParseException {
        readComments();
        readProblem();

        if (readVariableDirectory) {
            for (int i = 1; i <= variableCount; i++) {
                if (!indexVariables.has(i)) {
                    indexVariables.add(i, getUniqueName(i));
                }
            }
        }

        final int actualVariableCount = indexVariables.size();
        if (variableCount != actualVariableCount) {
            throw new ParseException(
                    String.format("Found %d instead of %d variables", actualVariableCount, variableCount), 1);
        }
    }

    private String getUniqueName(int i) {
        String indexName = Integer.toString(i);
        String name = indexName;
        int suffix = 2;
        while (indexVariables.has(name)) {
            name = indexName + "_" + suffix;
            suffix++;
        }
        return name;
    }

    protected void readComments() {
        for (String line = nonEmptyLineIterator.currentLine(); line != null; line = nonEmptyLineIterator.get()) {
            if (matchVariable(line)) {
                continue;
            }
            if (matchComment(line)) {
                continue;
            }
            break;
        }
    }

    /**
     * Reads the problem definition.
     *
     * @throws ParseException if the input does not conform to the DIMACS CNF file
     *                        format
     */
    private void readProblem() throws ParseException {
        final String line = nonEmptyLineIterator.currentLine();
        if (line == null) {
            throw new ParseException("Invalid problem format", nonEmptyLineIterator.getLineCount());
        }
        final Matcher matcher = problemPattern.matcher(line);
        if (!matcher.find()) {
            throw new ParseException("Invalid problem format", nonEmptyLineIterator.getLineCount());
        }
        final String trail = line.substring(matcher.end());
        if (trail.trim().isEmpty()) {
            nonEmptyLineIterator.get();
        } else {
            nonEmptyLineIterator.setCurrentLine(trail);
        }

        try {
            variableCount = Integer.parseInt(matcher.group(1));
        } catch (final NumberFormatException e) {
            throw new ParseException("Variable count is not an integer", nonEmptyLineIterator.getLineCount());
        }
        if (variableCount < 0) {
            throw new ParseException("Variable count is not positive", nonEmptyLineIterator.getLineCount());
        }

        try {
            clauseCount = Integer.parseInt(matcher.group(2));
        } catch (final NumberFormatException e) {
            throw new ParseException("Clause count is not an integer", nonEmptyLineIterator.getLineCount());
        }
        if (clauseCount < 0) {
            throw new ParseException("Clause count is not positive", nonEmptyLineIterator.getLineCount());
        }
    }

    /**
     * Reads all clauses.
     *
     * @return all clauses; not null
     * @throws ParseException if the input does not conform to the DIMACS CNF file
     *                        format
     */
    private List<List<int[]>> readBody() throws ParseException {
        final LinkedList<List<int[]>> groups = new LinkedList<>();
        groups.add(new ArrayList<>(clauseCount));
        for (String line = nonEmptyLineIterator.currentLine(); line != null; line = nonEmptyLineIterator.get()) {
            if (matchVariable(line)) {
                continue;
            }
            if (matchGroup(line, groups)) {
                continue;
            }
            if (matchComment(line)) {
                continue;
            }
            if (matchClause(line, groups.getLast())) {
                continue;
            }
            throw new ParseException(String.format("Invalid line %s", line), 1);
        }
        if (groups.size() > 1 && groups.get(0).isEmpty()) {
            groups.removeFirst();
        }
        return groups;
    }

    private boolean matchGroup(String line, List<List<int[]>> groups) {
        final Matcher matcher = groupPattern.matcher(line);
        if (matcher.matches()) {
            String group = matcher.group(1);
            try {
                groups.add(new ArrayList<>(Integer.parseInt(group)));
            } catch (final NumberFormatException e) {
                FeatJAR.log()
                        .warning("Line " + nonEmptyLineIterator.getLineCount()
                                + ": Unable to parse number in group comment: " + group);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean matchVariable(String line) {
        final Matcher matcher = variablePattern.matcher(line);
        if (matcher.matches()) {
            String indexString = matcher.group(1);
            try {
                final int index = Integer.parseInt(indexString);
                final String variable = matcher.group(2);
                if (readVariableDirectory && !indexVariables.has(index)) {
                    indexVariables.add(index, variable);
                }
            } catch (final NumberFormatException e) {
                FeatJAR.log()
                        .warning("Line " + nonEmptyLineIterator.getLineCount()
                                + ": Unable to parse number in variable comment: " + indexString);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean matchComment(String line) {
        final Matcher matcher = commentPattern.matcher(line);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean matchClause(String line, List<int[]> clauses) throws ParseException {
        Matcher matcher = clauseLinePattern.matcher(line);
        if (matcher.matches()) {
            String[] literalStrings = matcher.group(1).trim().split("\\s+");
            clauses.add(
                    (literalStrings.length == 1 && literalStrings[0].isEmpty())
                            ? new int[0]
                            : parseClause(literalStrings));
            if (clauses.size() > clauseCount) {
                throw new ParseException(String.format("Found more than %d clauses", clauseCount), 1);
            }
            return true;
        } else {
            return false;
        }
    }

    private int[] parseClause(String[] literalStrings) throws ParseException {
        final int[] literals = new int[literalStrings.length];
        for (int i = 0; i < literals.length; i++) {
            final String token = literalStrings[i];
            final int index;
            try {
                index = Integer.parseInt(token);
            } catch (final NumberFormatException e) {
                throw new ParseException("Illegal literal", nonEmptyLineIterator.getLineCount());
            }
            if (index == 0) {
                throw new ParseException("Illegal literal", nonEmptyLineIterator.getLineCount());
            }
            final int key = Math.abs(index);
            if (!indexVariables.has(key)) {
                indexVariables.add(key, getUniqueName(key));
            }
            literals[i] = index;
        }
        return literals;
    }
}

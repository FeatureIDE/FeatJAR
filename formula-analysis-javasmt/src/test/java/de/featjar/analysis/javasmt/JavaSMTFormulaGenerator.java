/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt> for further information.
 */
package de.featjar.analysis.javasmt;

import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.GreaterEqual;
import de.featjar.formula.structure.predicate.GreaterThan;
import de.featjar.formula.structure.predicate.LessEqual;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JavaSMTFormulaGenerator {

    private final Random random;

    public JavaSMTFormulaGenerator(long seed) {
        random = new Random(seed);
    }

    public IFormula generate(int numVariables, int numConstraints, int numAtomicSets) {
        List<Variable> variables = new ArrayList<>();
        for (int i = 0; i < numVariables; i++) {
            variables.add(new Variable("var_" + i, Double.class));
        }

        List<IFormula> constraints = new ArrayList<>();
        for (int i = 0; i < numConstraints; i++) {
            constraints.add(generateRandomConstraint(variables));
        }

        for (int i = 0; i < numAtomicSets; i++) {
            constraints.add(generateAtomicSet(variables));
        }

        return new And(constraints);
    }

    private IFormula generateRandomConstraint(List<Variable> variables) {
        List<IFormula> literals = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            int varIndex = this.random.nextInt(variables.size());
            Variable variable = variables.get(varIndex);

            Constant constant = new Constant(Double.valueOf(this.random.nextInt(20)));

            int opType = this.random.nextInt(5);
            IFormula integerToBoolean = null;

            switch (opType) {
                case 0:
                    integerToBoolean = new GreaterThan(variable, constant);
                    break;
                case 1:
                    integerToBoolean = new LessThan(variable, constant);
                    break;
                case 2:
                    integerToBoolean = new GreaterEqual(variable, constant);
                    break;
                case 3:
                    integerToBoolean = new LessEqual(variable, constant);
                    break;
                case 4:
                    integerToBoolean = new Equals(variable, constant);
                    break;
            }

            literals.add(integerToBoolean);
        }

        return new Or(literals);
    }

    private IFormula generateAtomicSet(List<Variable> variables) {
        return new Equals(
                variables.get(random.nextInt(variables.size())), variables.get(random.nextInt(variables.size())));
    }
}

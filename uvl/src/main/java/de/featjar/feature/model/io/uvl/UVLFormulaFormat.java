/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-uvl.
 *
 * uvl is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * uvl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uvl. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-uvl> for further information.
 */
package de.featjar.feature.model.io.uvl;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.uvl.visitor.FeatureTreeToFormulaVisitor;
import de.featjar.feature.model.io.uvl.visitor.FormulaToUVLConstraintVisitor;
import de.featjar.formula.io.IFormulaFormat;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.True;
import de.vill.model.Attribute;
import de.vill.model.Feature;
import de.vill.model.FeatureType;
import de.vill.model.Group;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses and writes formulas from and to UVL files.
 *
 * @author Sebastian Krieter
 * @author Andreas Gerasimow
 */
public class UVLFormulaFormat extends AUVLFormat<IFormula> implements IFormulaFormat {

    /**
     * Name of the root element.
     */
    public static final String ROOT_FEATURE_NAME = "Formula";

    public static final String ID = UVLFormulaFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public UVLFormulaFormat getInstance() {
        return this;
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<IFormula> parse(AInputMapper inputMapper) {
        List<Problem> problems = new ArrayList<>();
        de.vill.model.FeatureModel uvlModel = parseUVLModel(inputMapper);
        try {
            IFeatureModel featureModel = UVLFeatureModelToFeatureTree.createFeatureModel(uvlModel);

            List<? extends IFeatureTree> roots = featureModel.getRoots();
            if (roots.isEmpty()) {
                problems.add(new Problem("No root features exist.", Problem.Severity.ERROR));
                return Result.empty(problems);
            }

            List<IFormula> formulas = new ArrayList<>();
            boolean fail = false;
            for (IFeatureTree rootFeature : roots) {
                Result<IFormula> result = Trees.traverse(rootFeature, new FeatureTreeToFormulaVisitor());
                if (result.isEmpty()) {
                    problems.addAll(result.getProblems());
                    fail = true;
                } else {
                    IFormula treeFormula = result.get();
                    if (!(treeFormula instanceof True)) {
                        formulas.add(treeFormula);
                    }
                }
            }
            if (fail) {
                return Result.empty(problems);
            }

            List<IFormula> constraintFormulas =
                    UVLFeatureModelToFeatureTree.uvlConstraintToFormula(uvlModel.getConstraints());
            formulas.addAll(constraintFormulas);

            IFormula formula = new Reference(formulas.size() == 1 ? formulas.get(0) : new And(formulas));

            return Result.of(formula, problems);
        } catch (Exception e) {
            return Result.empty(e);
        }
    }

    @Override
    public Result<String> serialize(IFormula formula) {
        de.vill.model.FeatureModel uvlModel = new de.vill.model.FeatureModel();
        de.vill.model.Feature uvlRootFeature = new Feature(ROOT_FEATURE_NAME);
        uvlRootFeature.setFeatureType(FeatureType.BOOL);
        uvlRootFeature.getAttributes().put("name", new Attribute<>("name", ROOT_FEATURE_NAME));
        uvlRootFeature.getAttributes().put("abstract", new Attribute<>("abstract", true));
        uvlModel.setRootFeature(uvlRootFeature);
        uvlModel.getFeatureMap().put(ROOT_FEATURE_NAME, uvlRootFeature);

        de.vill.model.Group uvlRootGroup = new Group(Group.GroupType.OPTIONAL);
        uvlRootFeature.addChildren(uvlRootGroup);

        formula.getVariableNames().forEach((variableName) -> {
            de.vill.model.Feature uvlFeature = new Feature(variableName);
            uvlFeature.setFeatureType(FeatureType.BOOL);
            uvlFeature.getAttributes().put("name", new Attribute<>("name", variableName));
            uvlFeature.getAttributes().put("abstract", new Attribute<>("abstract", false));
            uvlModel.getFeatureMap().put(variableName, uvlFeature);
            uvlRootGroup.getFeatures().add(uvlFeature);
        });

        Result<de.vill.model.constraint.Constraint> uvlConstraint =
                Trees.traverse(formula, new FormulaToUVLConstraintVisitor());
        List<Problem> problems = new ArrayList<>(uvlConstraint.getProblems());
        if (uvlConstraint.isEmpty()) {
            return Result.empty(problems);
        }

        uvlModel.getOwnConstraints().add(uvlConstraint.get());
        return Result.of(uvlModel.toString(), problems);
    }
}

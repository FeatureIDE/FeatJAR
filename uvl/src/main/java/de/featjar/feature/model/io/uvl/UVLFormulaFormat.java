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

import de.featjar.base.data.Attribute;
import de.featjar.base.data.Attributes;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.io.uvl.visitor.FeatureTreeToUVLFeatureModelVisitor;
import de.featjar.feature.model.io.uvl.visitor.FormulaToUVLConstraintVisitor;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.io.IFormulaFormat;
import de.featjar.formula.structure.IFormula;
import de.vill.model.Feature;
import de.vill.model.Group;
import de.vill.model.constraint.Constraint;
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
     * Name of the artificial root element.
     */
    public static final String PSEUDO_ROOT_NAME = "__formula__";

    /**
     * Attribute for marking an artificial root.
     */
    public static final Attribute<Boolean> PSEUDO_ROOT_ATTRIBUTE = Attributes.get("pseudo_root", Boolean.class);

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
        try {
            return UVLFeatureModelToFeatureTree.toFeatureModel(parseUVLModel(inputMapper))
                    .toComputation()
                    .map(ComputeFormula::new)
                    .computeResult();
        } catch (Exception e) {
            return Result.empty(e);
        }
    }

    @Override
    public Result<String> serialize(IFormula formula) {
        de.vill.model.FeatureModel uvlModel = new de.vill.model.FeatureModel();
        Feature uvlRootFeature = FeatureTreeToUVLFeatureModelVisitor.newFeature(uvlModel, PSEUDO_ROOT_NAME);
        uvlModel.setRootFeature(uvlRootFeature);
        FeatureTreeToUVLFeatureModelVisitor.setAttribute(uvlRootFeature, PSEUDO_ROOT_ATTRIBUTE.getName(), Boolean.TRUE);

        Group uvlRootGroup = new Group(Group.GroupType.OPTIONAL);
        uvlRootFeature.addChildren(uvlRootGroup);

        for (String variableName : formula.getVariableNames()) {
            uvlRootGroup.getFeatures().add(FeatureTreeToUVLFeatureModelVisitor.newFeature(uvlModel, variableName));
        }

        Result<Constraint> uvlConstraint = Trees.traverse(formula, new FormulaToUVLConstraintVisitor());
        List<Problem> problems = new ArrayList<>(uvlConstraint.getProblems());
        if (uvlConstraint.isEmpty()) {
            return Result.empty(problems);
        }
        uvlModel.getOwnConstraints().add(uvlConstraint.get());

        return Result.of(uvlModel.toString(), problems);
    }
}

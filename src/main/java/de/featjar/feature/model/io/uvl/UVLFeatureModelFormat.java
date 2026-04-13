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
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.*;
import de.featjar.feature.model.io.uvl.visitor.FeatureTreeToUVLFeatureModelVisitor;
import de.featjar.feature.model.io.uvl.visitor.FormulaToUVLConstraintVisitor;
import de.featjar.formula.structure.IFormula;
import de.vill.main.UVLModelFactory;
import java.util.*;

/**
 * Parses and writes feature models from and to UVL files.
 *
 * @author Sebastian Krieter
 * @author Andreas Gerasimow
 */
public class UVLFeatureModelFormat implements IFormat<IFeatureModel> {

    @Override
    public Result<IFeatureModel> parse(AInputMapper inputMapper) {
        try {
            String content = inputMapper.get().text();
            UVLModelFactory uvlModelFactory = new UVLModelFactory();
            de.vill.model.FeatureModel uvlModel = uvlModelFactory.parse(content);

            IFeatureModel featureModel = UVLFeatureModelToFeatureTree.createFeatureModel(uvlModel);

            List<IFormula> formulas = UVLFeatureModelToFeatureTree.uvlConstraintToFormula(uvlModel.getConstraints());
            formulas.forEach((formula) -> featureModel.mutate().addConstraint(formula));

            return Result.of(featureModel);
        } catch (Exception e) {
            return Result.empty(e);
        }
    }

    @Override
    public Result<String> serialize(IFeatureModel fm) {
        List<Problem> problems = new ArrayList<>();
        try {
            if (fm.getRootFeatures().isEmpty()) {
                problems.add(new Problem("No root features exists.", Problem.Severity.ERROR));
                return Result.empty(problems);
            }

            IFeature rootFeature = fm.getRootFeatures().get(0);
            problems.add(new Problem(
                    "UVL supports only one root feature. If there are more than one root features in the model, the first one will be used.",
                    Problem.Severity.WARNING));

            Result<IFeatureTree> featureTree = fm.getFeatureTree(rootFeature);
            problems.addAll(featureTree.getProblems());
            if (featureTree.isEmpty()) {
                return Result.empty(problems);
            }

            Result<de.vill.model.FeatureModel> uvlModel =
                    Trees.traverse(featureTree.get(), new FeatureTreeToUVLFeatureModelVisitor());
            problems.addAll(uvlModel.getProblems());
            if (uvlModel.isEmpty()) {
                return Result.empty(problems);
            }

            for (IConstraint constraint : fm.getConstraints()) {
                Result<de.vill.model.constraint.Constraint> uvlConstraint =
                        Trees.traverse(constraint.getFormula(), new FormulaToUVLConstraintVisitor());
                problems.addAll(uvlConstraint.getProblems());
                if (uvlConstraint.isEmpty()) {
                    return Result.empty(problems);
                }
                uvlModel.get().getOwnConstraints().add(uvlConstraint.get());
            }

            return Result.of(uvlModel.get().toString(), problems);
        } catch (Exception e) {
            return Result.empty(e);
        }
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
    public String getFileExtension() {
        return "uvl";
    }

    @Override
    public String getName() {
        return "Universal Variability Language";
    }
}

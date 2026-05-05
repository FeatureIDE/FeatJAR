/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.io.tikz;

import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.IFeatureModelFormat;
import java.io.IOException;

/**
 * Format for serializing a feature model as Tikz picture within a stand-alone Latex document.
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class LatexFeatureModelFormat implements IFeatureModelFormat {

    @Override
    public Result<String> serialize(IFeatureModel featureModel) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (String line : IO.readLines(
                    ClassLoader.getSystemResourceAsStream("de/featjar/feature/model/io/tikz/tikz_standalone.tex"))) {
                if ("%%<tikz_settings>".equals(line)) {
                    for (String settingsLine : IO.readLines(ClassLoader.getSystemResourceAsStream(
                            "de/featjar/feature/model/io/tikz/tikz_fm_settings.tex"))) {
                        stringBuilder.append(settingsLine);
                        stringBuilder.append(System.lineSeparator());
                    }
                } else if ("%%<feature_diagram>".equals(line)) {
                    stringBuilder.append(new TikzFeatureModelSerializer().serialize(featureModel));
                } else {
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                }
            }
            return Result.of(stringBuilder.toString());
        } catch (IOException e) {
            return Result.empty(e);
        }
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public String getFileExtension() {
        return ".tex";
    }

    @Override
    public String getName() {
        return "LaTeX-Document with TikZ";
    }
}

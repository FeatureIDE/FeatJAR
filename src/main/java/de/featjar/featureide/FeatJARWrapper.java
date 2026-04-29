/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model-assistance.
 *
 * feature-model-assistance is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model-assistance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model-assistance. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model-assistance> for further information.
 */
package de.featjar.featureide;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.IFormatSupplier;
import de.featjar.feature.configuration.Configuration;
import de.featjar.feature.configuration.computation.ComputeAssignmentFromConfiguration;
import de.featjar.feature.configuration.computation.ComputeConfigurationFromAssignment;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import de.featjar.formula.io.csv.BooleanAssignmentListCSVFormat;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * FeatJAR wrapper for conveniently using basic functions for feature models.
 *
 * @author Sebastian Krieter
 */
public class FeatJARWrapper {

    public FeatJARWrapper() {
        if (!FeatJAR.isInitialized()) {
            FeatJAR.initialize();
        }
    }

    /**
     * Stores any object for which a {@link IFormat format} exists to a file.
     * This is a convenience method, equivalent to calling {@link IO#save(Object, Path, IFormat, de.featjar.base.io.IOMapperOptions...)}.
     * For more information see the {@link IO} class.
     *
     * @param <T> the type of the object
     * @param anything the object to store
     * @param path the path to the file
     * @param format the format
     * @throws IOException if there is a problem with writing the file to the file system
     *
     * @see IO#save(Object, java.io.OutputStream, IFormat)
     * @see IO#save(Object, java.io.OutputStream, IFormat, java.nio.charset.Charset)
     * @see IO#save(Object, Path, IFormat, de.featjar.base.io.IOMapperOptions...)
     * @see IO#save(Object, Path, IFormat, java.nio.charset.Charset, de.featjar.base.io.IOMapperOptions...)
     */
    public <T> void storeAnything(T anything, Path path, IFormat<T> format) throws IOException {
        IO.save(anything, path, format);
    }

    /**
     * Loads any object for which a {@link IFormat format} exists from a file.
     * This is a convenience method, equivalent to calling {@link IO#load(Path, IFormatSupplier, de.featjar.base.io.IOMapperOptions...)}.
     * For more information see the {@link IO} class.
     *
     * @param <T> the type of the object
     * @param path the path to the file
     * @param formatSupplier a supplier for a suitable format
     * @return the loaded object wrapped in a {@link Result} or an empty Result with further problem information (see {@link Result#getProblems()})
     *
     * @see IO#load(java.io.InputStream, IFormat, java.nio.charset.Charset)
     * @see IO#load(Path, IFormat, de.featjar.base.io.IOMapperOptions...)
     */
    public <T> Result<T> loadAnything(Path path, IFormatSupplier<T> formatSupplier) {
        return IO.load(path, formatSupplier);
    }

    /**
     * Loads a feature model from a file.
     *
     * @param path the path to the feature model file
     * @return the loaded object wrapped in a {@link Result} or an empty Result with further problem information (see {@link Result#getProblems()})
     *
     * @see IO#load(Path, IFormatSupplier, de.featjar.base.io.IOMapperOptions...)
     */
    public Result<IFeatureModel> loadFeatureModel(Path path) {
        return IO.load(path, FeatureModelFormats.getInstance());
    }

    /**
     * Stores a feature model to a file.
     *
     * @param featureModel the feature model to store
     * @param path the path to the file
     * @throws IOException if there is a problem with writing the file to the file system
     *
     * @see IO#save(Object, Path, IFormat, de.featjar.base.io.IOMapperOptions...)
     */
    public void storeFeatureModel(IFeatureModel featureModel, Path path) throws IOException {
        IO.save(
                featureModel,
                path,
                FeatureModelFormats.getInstance().getFormatByName("UVL").orElseThrow());
    }

    /**
     * Loads a list of configurations from a file.
     *
     * @param path the path to the configurations file
     * @return the loaded object wrapped in a {@link Result} or an empty Result with further problem information (see {@link Result#getProblems()})
     *
     * @see IO#load(Path, IFormatSupplier, de.featjar.base.io.IOMapperOptions...)
     */
    public Result<List<Configuration>> loadConfigurations(Path path) {
        return IO.load(path, BooleanAssignmentListFormats.getInstance())
                .toComputation()
                .map(ComputeConfigurationFromAssignment::new)
                .computeResult();
    }

    /**
     * Stores a list of configurations to a file.
     *
     * @param configurations the configurations to store
     * @param path the path to the file
     * @throws IOException if there is a problem with writing the file to the file system
     *
     * @see IO#save(Object, Path, IFormat, de.featjar.base.io.IOMapperOptions...)
     */
    public void storeConfigurations(List<Configuration> configurations, Path path) throws IOException {
        IO.save(
                Computations.of(configurations)
                        .map(ComputeAssignmentFromConfiguration::new)
                        .compute(),
                path,
                new BooleanAssignmentListCSVFormat());
    }

    /**
     * {@return a new FeatureModelAnalyzer for a given feature model}
     *
     * @param featureModel the feature model to analyze
     */
    public FeatureModelAnalyzer featureModelAnalyzer(IFeatureModel featureModel) {
        return new FeatureModelAnalyzer(featureModel);
    }

    /**
     * {@return a new FeatureModelBuilder for a new feature model}
     */
    public FeatureModelBuilder featureModelBuilder() {
        return new FeatureModelBuilder();
    }
}

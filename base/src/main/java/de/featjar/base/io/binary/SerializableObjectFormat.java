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
package de.featjar.base.io.binary;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.output.AOutputMapper;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Parses and writes serializable objects.
 *
 * @param <T> the type of the object
 * @author Sebastian Krieter
 */
public class SerializableObjectFormat<T extends Serializable> implements IFormat<T> {
    @Override
    public String getName() {
        return "Serializable Object";
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
    public Result<T> parse(AInputMapper inputMapper) {
        try (ObjectInputStream in = new ObjectInputStream(inputMapper.get().getInputStream())) {
            @SuppressWarnings("unchecked")
            final T readObject = (T) in.readObject();
            return Result.of(readObject);
        } catch (final Exception e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }

    @Override
    public void write(T object, AOutputMapper outputMapper) {
        final OutputStream outputStream = outputMapper.get().getOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
            oos.writeObject(object);
            oos.flush();
        } catch (final Exception e) {
            FeatJAR.log().error(e);
        }
    }
}

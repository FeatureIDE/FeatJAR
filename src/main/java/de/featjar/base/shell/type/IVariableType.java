package de.featjar.base.shell.type;

import de.featjar.base.data.Result;
import de.featjar.base.extension.IExtension;
import java.nio.file.Path;

public interface IVariableType<T> extends IExtension {

    /**
     * {@return the name of the type}
     */
    String getName();

    /**
     * {@return the class type}
     */
    Class<T> getClassType();

    /**
     * Loads a value of this type from the given path.
     *
     * @param path the path to a file
     *
     * @return a result containing the loaded value
     */
    Result<T> load(Path path);
}

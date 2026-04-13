package de.featjar.shell.type;

import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.shell.type.IVariableType;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import java.nio.file.Path;

public class ConfigType implements IVariableType<BooleanAssignment> {

    @Override
    public Class<BooleanAssignment> getClassType() {
        return BooleanAssignment.class;
    }

    @Override
    public Result<BooleanAssignment> load(Path path) {
        return IO.load(path, BooleanAssignmentGroupsFormats.getInstance())
                .map(g -> g.getMergedGroups().getFirst());
    }

    @Override
    public String getName() {
        return "config";
    }
}

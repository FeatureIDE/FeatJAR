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
package de.featjar.base.env;

import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.structure.ATree;
import de.featjar.base.tree.structure.ITree;
import de.featjar.base.tree.visitor.ITreeVisitor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;

/**
 * A dependency of a computation. Describes the dependency without storing its
 * actual value, which is passed in a dependency list to
 * {@link IComputation#compute(List, Progress)}.
 *
 * @param <U> the type of the dependency's computation result
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public class ParameterList<P extends Parameter> {

    private class ParameterizedType extends ATree<ParameterizedType> {

        private final Class<?> type;
        private final List<P> parameters = new ArrayList<>();

        public ParameterizedType(Class<?> type) {
            this.type = type;
        }

        @Override
        public ITree<ParameterizedType> cloneNode() {
            ParameterizedType parameterIntroducer = new ParameterizedType(type);
            parameterIntroducer.parameters.addAll(parameters);
            return parameterIntroducer;
        }

        @Override
        public boolean equalsNode(ParameterizedType other) {
            return this == other;
        }

        @Override
        public int hashCodeNode() {
            return type.hashCode();
        }
    }

    private final ParameterizedType parameterPerClassList = new ParameterizedType(Object.class);

    private final Class<?> parameterizedSuperType;

    private int lastID = -1;

    public ParameterList(Class<?> parameterizedSuperType) {
        this.parameterizedSuperType = parameterizedSuperType;
    }

    public void addParameter(P parameter) {
        addParameter(parameter, null, getCallingClass());
    }

    public void addParameter(P parameter, P parentParameter) {
        addParameter(parameter, parentParameter, getCallingClass());
    }

    public synchronized void addParameter(P parameter, P parentParameter, Class<?> callingClass) {
        if (parentParameter != null) {
            parameter.setID(parameter.getID());
        } else {
            parameter.setID(++lastID);
        }
        ParameterizedType introducer = Trees.preOrderStream(parameterPerClassList)
                .filter(i -> i.type == callingClass)
                .findFirst()
                .orElse(null);
        if (introducer == null) {
            introducer = new ParameterizedType(callingClass);
            List<ParameterizedType> list = Trees.traverse(parameterPerClassList, new ParentCollector(callingClass))
                    .get();
            for (ParameterizedType parameterIntroducer : list) {
                parameterIntroducer.addChild(introducer);
            }
        }
        introducer.parameters.add(parameter);
    }

    public synchronized void clear() {
        parameterPerClassList.clearChildren();
    }

    public SequencedCollection<? extends Parameter> getParameterList() {
        return getParameterList(getCallingClass());
    }

    public synchronized SequencedCollection<P> getParameterList(Class<?> callingClass) {
        return Trees.traverse(parameterPerClassList, new ParameterCollector(callingClass))
                .get();
    }

    private Class<?> getCallingClass() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 3; i < stackTrace.length; i++) {
                Class<?> callingClass = Class.forName(stackTrace[i].getClassName());
                if (parameterizedSuperType.isAssignableFrom(callingClass)) {
                    return callingClass;
                }
            }
            throw new IllegalStateException();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private class ParameterCollector implements ITreeVisitor<ParameterizedType, SequencedCollection<P>> {
        private final Class<?> callingClass;
        private final LinkedHashMap<Integer, P> parameters = new LinkedHashMap<>();

        public ParameterCollector(Class<?> callingClass) {
            this.callingClass = callingClass;
        }

        @Override
        public TraversalAction firstVisit(List<ParameterizedType> path) {
            ParameterizedType currentNode = ITreeVisitor.getCurrentNode(path);
            if (currentNode.type.isAssignableFrom(callingClass)) {
                for (P parameter : currentNode.parameters) {
                    parameters.put(parameter.getID(), parameter);
                }
                return ITreeVisitor.TraversalAction.CONTINUE;
            } else {
                return ITreeVisitor.TraversalAction.SKIP_CHILDREN;
            }
        }

        @Override
        public Result<SequencedCollection<P>> getResult() {
            return Result.of(parameters.sequencedValues());
        }
    }

    private class ParentCollector implements ITreeVisitor<ParameterizedType, List<ParameterizedType>> {
        private final Class<?> callingClass;
        private final List<ParameterizedType> parents = new ArrayList<>();

        private ParameterizedType lastParent;

        public ParentCollector(Class<?> callingClass) {
            this.callingClass = callingClass;
        }

        @Override
        public TraversalAction firstVisit(List<ParameterizedType> path) {
            ParameterizedType currentNode = ITreeVisitor.getCurrentNode(path);
            if (currentNode.type.isAssignableFrom(callingClass)) {
                lastParent = currentNode;
                return ITreeVisitor.TraversalAction.CONTINUE;
            } else {
                return ITreeVisitor.TraversalAction.SKIP_CHILDREN;
            }
        }

        @Override
        public TraversalAction lastVisit(List<ParameterizedType> path) {
            if (lastParent == ITreeVisitor.getCurrentNode(path)) {
                parents.add(lastParent);
                lastParent = null;
            }
            return ITreeVisitor.TraversalAction.CONTINUE;
        }

        @Override
        public Result<List<ParameterizedType>> getResult() {
            return Result.of(parents);
        }
    }
}

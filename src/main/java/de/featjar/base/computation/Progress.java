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
package de.featjar.base.computation;

import java.util.function.Supplier;

/**
 * Tracks progress of an asynchronous computation.
 * Attached to a {@link FutureResult}.
 *
 * @author Elias Kuiter
 */
public class Progress implements Supplier<Double>, AutoCloseable {

    /**
     * An instance of a progress object that does nothing.
     */
    public static final Progress NULL = new Null();

    private static class Null extends Progress {

        @Override
        public void setCurrentStep(long currentStep) {}

        @Override
        public void incrementCurrentStep() {}

        @Override
        public void addCurrentSteps(long steps) {}

        @Override
        public void setTotalSteps(long totalSteps) {}
    }

    private String name;
    private long totalSteps, currentSteps;
    private boolean finished;

    /**
     * Constructs a new progress instance with one step.
     */
    public Progress() {
        this(1);
    }
    /**
     * Constructs a new progress instance.
     * @param totalSteps the number of necessary steps
     */
    protected Progress(long totalSteps) {
        if (totalSteps < 1) {
            throw new IndexOutOfBoundsException(String.valueOf(totalSteps));
        }
        this.totalSteps = totalSteps;
    }

    public static Progress completed(long steps) {
        return new Progress(steps == 0 ? 1 : steps);
    }

    /**
     * {@return the name of this progress}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this progress}
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@return the progress's current step}
     */
    public long getCurrentStep() {
        return currentSteps;
    }

    /**
     * Sets the progress's current step.
     *
     * @param currentStep the current step
     */
    public void setCurrentStep(long currentStep) {
        this.currentSteps = (totalSteps < currentStep) ? totalSteps : currentStep;
    }

    /**
     * Increases the progress's current step by one and checks for cancellation.
     */
    public void incrementCurrentStep() {
        addCurrentSteps(1);
    }

    /**
     * Increases the progress's current step by one and checks for cancellation.
     */
    public void incrementCurrentStepSynchronized() {
        synchronized (this) {
            addCurrentSteps(1);
        }
    }

    public void finish() {
        finished = true;
        currentSteps = totalSteps;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Increases the progress's current step by an amount and checks for cancellation.
     *
     * @param steps the steps
     */
    public void addCurrentSteps(long steps) {
        setCurrentStep(currentSteps + steps);
    }

    /**
     * {@return the progress's total number of steps}
     */
    public long getTotalSteps() {
        return totalSteps;
    }

    /**
     * Sets the progress's total number of steps.
     * Can be accurate or a preliminary estimate.
     *
     * @param totalSteps the total steps
     */
    public void setTotalSteps(long totalSteps) {
        this.totalSteps = (totalSteps < currentSteps) ? currentSteps : totalSteps;
    }

    /**
     * {@return this progress' percentage (i.e., the current step divided by the total number of steps)}
     */
    public Double get() {
        return currentSteps < 0 ? 0 : (double) currentSteps / totalSteps;
    }

    @Override
    public String toString() {
        return String.format("%d / %d", currentSteps, totalSteps);
    }

    @Override
    public void close() {
        finish();
    }
}

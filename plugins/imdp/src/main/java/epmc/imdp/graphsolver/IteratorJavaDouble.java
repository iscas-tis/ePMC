package epmc.imdp.graphsolver;

import java.util.Arrays;
import java.util.Collections;

import epmc.imdp.IntervalPlayer;
import epmc.imdp.options.OptionsIMDP;
import epmc.options.Options;

/**
 * Class to maximise or minimise over the intervals of a given IMDP.
 * This class is intended to be used with a sparse matrix representation
 * of IMDPs. 
 * 
 * @author Ernst Moritz Hahn
 */
public final class IteratorJavaDouble {
    /**
     * Builder for the iterator class.
     * 
     * @author Ernst Moritz Hahn
     */
    public final static class Builder {
        /** Number of states of the IMDP. */
        private int numStates;
        /** Bounds assigning nondet choices to states. */
        private int[] stateBounds;
        /** Bounds assigning intervals to nondeterministic choices. */
        private int[] nondetBounds;
        /** Successor states of intervals. */
        private int[] successors;
        /** Interval weights. */
        private double[] weights;
        private double[] values;
        private boolean min;

        public Builder setNumStates(int numStates) {
            this.numStates = numStates;
            return this;
        }

        private int getNumStates() {
            return numStates;
        }

        public Builder setStateBounds(int[] stateBounds) {
            this.stateBounds = stateBounds;
            return this;
        }

        private int[] getStateBounds() {
            return stateBounds;
        }

        public Builder setSuccessors(int[] successors) {
            this.successors = successors;
            return this;
        }

        private int[] getSuccessors() {
            return successors;
        }

        public Builder setWeights(double[] weights) {
            this.weights = weights;
            return this;
        }

        private double[] getWeights() {
            return weights;
        }

        public Builder setValues(double[] values) {
            this.values = values;
            return this;
        }

        private double[] getValues() {
            return values;
        }

        public Builder setMin(boolean min) {
            this.min = min;
            return this;
        }

        private boolean isMin() {
            return min;
        }

        public Builder setNondetBounds(int[] nondetBounds) {
            this.nondetBounds = nondetBounds;
            return this;
        }

        private int[] getNondetBounds() {
            return nondetBounds;
        }

        public IteratorJavaDouble build() {
            return new IteratorJavaDouble(this);
        }
    }

    private final int numStates;
    private final int[] stateBounds;
    private final int[] nondetBounds;
    private final int[] successors;
    private final double[] weights;
    private double[] values;
    private final boolean min;
    private SortEntryJavaDouble[] sortEntries;
    private long numOptSteps;
    private long timesSorted;

    private IteratorJavaDouble(Builder builder) {
        assert assertBuilder(builder);
        numStates = builder.getNumStates();
        stateBounds = builder.getStateBounds();
        nondetBounds = builder.getNondetBounds();
        successors = builder.getSuccessors();
        weights = builder.getWeights();
        values = builder.getValues();
        min = computeIsMin(builder);
        int maxSortSize = 0;
        for (int state = 0; state < numStates; state++) {
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                int nondetFrom = nondetBounds[nondet];
                int nondetTo = nondetBounds[nondet + 1];
                maxSortSize = Math.max(maxSortSize, nondetTo - nondetFrom);
            }
        }
        sortEntries = new SortEntryJavaDouble[maxSortSize];
        for (int entryNr = 0; entryNr < maxSortSize; entryNr++) {
            sortEntries[entryNr] = new SortEntryJavaDouble();
        }
    }

    private boolean assertBuilder(Builder builder) {
        assert builder != null;
        assert builder.getNumStates() >= 0;
        assert builder.getStateBounds() != null;
        assert builder.getNondetBounds() != null;
        assert builder.getSuccessors() != null;
        assert builder.getWeights() != null;
        //		assert builder.getValues() != null;
        return true;
    }

    private boolean computeIsMin(Builder builder) {
        boolean min;
        IntervalPlayer player = Options.get().get(OptionsIMDP.IMDP_INTERVAL_PLAYER);
        if (player == IntervalPlayer.ANTAGONISTIC) {
            min = !builder.isMin();
        } else {
            min = builder.isMin();
        }
        return min;
    }

    public double nondetStep(int nondet) {
        int nondetFrom = nondetBounds[nondet];
        int nondetTo = nondetBounds[nondet + 1];

        numOptSteps++;
        double sumLower = 0.0;
        boolean sorted = true;
        double lastValue = min ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (int entryNr = nondetFrom; entryNr < nondetTo; entryNr++) {
            sumLower += weights[entryNr * 2];
            int successor = successors[entryNr];
            double successorValue = values[successor];
            if (min && successorValue < lastValue
                    || !min && successorValue > lastValue) {
                sorted = false;
            }
            lastValue = successorValue;
        }
        if (!sorted) {
            timesSorted++;
            sort(nondetFrom, nondetTo);
        }

        return stepOnSorted(sumLower, nondetFrom, nondetTo);
    }

    private double stepOnSorted(double sumLower, int nondetFrom, int nondetTo) {
        double result = 0;
        for (int entryNr = nondetFrom; entryNr < nondetTo; entryNr++) {
            int successor = successors[entryNr];
            double successorValue = values[successor];
            if (1.0 + weights[entryNr * 2] < weights[entryNr * 2 + 1] + sumLower) {
                result += (1.0 + weights[entryNr * 2] - sumLower)
                        * successorValue;
                sumLower = 1.0;
            } else {
                sumLower += weights[entryNr * 2 + 1] - weights[entryNr * 2];
                result += weights[entryNr * 2 + 1] * successorValue;
            }
        }

        return result;
    }

    private void sort(int nondetFrom, int nondetTo) {
        for (int entryNr = nondetFrom; entryNr < nondetTo; entryNr++) {
            int successor = successors[entryNr];
            double successorValue = values[successor];
            SortEntryJavaDouble entry = sortEntries[entryNr - nondetFrom];
            entry.setLower(weights[entryNr * 2]);
            entry.setUpper(weights[entryNr * 2 + 1]);
            entry.setSuccessor(successors[entryNr]);
            entry.setValue(successorValue);
        }
        if (min) {
            Arrays.sort(this.sortEntries, 0, nondetTo - nondetFrom);
        } else {
            Arrays.sort(this.sortEntries, 0, nondetTo - nondetFrom, Collections.reverseOrder());
        }
        for (int entryNr = nondetFrom; entryNr < nondetTo; entryNr++) {
            SortEntryJavaDouble entry = sortEntries[entryNr - nondetFrom];
            weights[entryNr * 2] = entry.getLower();
            weights[entryNr * 2 + 1] = entry.getUpper();
            successors[entryNr] = entry.getSuccessor();
        }
    }

    public void setValues(double[] values) {
        assert values != null;
        this.values = values;
    }

    long getNumOptSteps() {
        return numOptSteps;
    }

    long getTimesSorted() {
        return timesSorted;
    }
}

package epmc.imdp.lump.antagonistic;

import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.imdp.lump.ClassToNumber;
import epmc.imdp.lump.ClassToNumberIntArray;
import epmc.imdp.lump.ClassToNumberTIntIntMap;
import epmc.imdp.lump.Partition;
import epmc.imdp.lump.Violator;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.value.ValueInterval;

public final class ViolatorAntagonistic implements Violator {
    public final static class Builder implements Violator.Builder {
        private GraphExplicit original;
        private Partition partition;

        @Override
        public Builder setOriginal(GraphExplicit original) {
            this.original = original;
            return this;
        }

        private GraphExplicit getOriginal() {
            return original;
        }

        @Override
        public Builder setPartition(Partition partition) {
            this.partition = partition;
            return this;
        }

        private Partition getPartition() {
            return partition;
        }

        @Override
        public ViolatorAntagonistic build() {
            assert original != null;
            assert partition != null;
            return new ViolatorAntagonistic(this);
        }
    }

    private final boolean useClassToNumberHash = true;
    private final boolean noSelfCompare;
    private final GraphExplicit original;
    private final Partition partition;
    private PolytopeSetSolver solver;
    private final PolytopeSet statePolytopeSet;
    private final PolytopeSet comparePolytopeSet;
    private final PolytopeSet minStatePolytopeSet;
    private final PolytopeSet minComparePolytopeSet;
    private final ClassToNumber classToNumber;
    private int numClasses;

    private ViolatorAntagonistic(Builder builder) {
        assert builder != null;
        GraphExplicit original = builder.getOriginal();
        Partition partition = builder.getPartition();
        assert original != null;
        assert partition != null;
        noSelfCompare = Options.get().getBoolean(OptionsIMDPLump.IMDP_NO_SELF_COMPARE);
        assert original != null;
        assert partition != null;
        this.original = original;
        this.partition = partition;
        ClassToNumber.Builder classToNumberBuilder;
        if (useClassToNumberHash) {
            classToNumberBuilder = new ClassToNumberTIntIntMap.Builder();
        } else {
            classToNumberBuilder = new ClassToNumberIntArray.Builder();
        }
        classToNumberBuilder.setSize(original.getNumNodes());
        classToNumber = classToNumberBuilder.build();
        statePolytopeSet = new PolytopeSet();
        comparePolytopeSet = new PolytopeSet();
        minStatePolytopeSet = new PolytopeSet();
        minComparePolytopeSet = new PolytopeSet();
        solver = new PolytopeSetSolver();
    }

    @Override
    public boolean violate(int state, int compareState) {
        assert state >= 0;
        assert compareState >= 0;
        if (noSelfCompare && state == compareState) {
            return false;
        }
        computeClassMap(state, compareState);
        computePolytopeSet(statePolytopeSet, state);
        computePolytopeSet(comparePolytopeSet, compareState);
        solver.findMinimal(minStatePolytopeSet, statePolytopeSet);
        solver.findMinimal(minComparePolytopeSet, comparePolytopeSet);
        boolean result = !minStatePolytopeSet.equals(minComparePolytopeSet);
        statePolytopeSet.reset();
        comparePolytopeSet.reset();
        minStatePolytopeSet.reset();
        minComparePolytopeSet.reset();
        return result;
    }

    private void computePolytopeSet(PolytopeSet result, int state) {
        assert result != null;
        assert state >= 0;
        int numActions = original.getNumSuccessors(state);
        result.reset();
        result.setDimensions(numClasses, numActions);
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            int action = original.getSuccessorNode(state, actionNr);
            computePolytope(result, actionNr, action);
        }
    }

    private void computePolytope(PolytopeSet result, int actionNr, int action) {
        assert result != null;
        assert actionNr >= 0;
        assert action >= 0;
        int numSuccessors = original.getNumSuccessors(action);
        EdgeProperty weights = original.getEdgeProperty(CommonProperties.WEIGHT);
        for (int succNr = 0; succNr < numSuccessors; succNr++) {
            int succState = original.getSuccessorNode(action, succNr);
            int succClass = partition.getBlockNumberFromState(succState);
            int classNumber = getClassToNumber(succClass);
            ValueInterval weight = ValueInterval.as(weights.get(action, succNr));
            result.add(actionNr, classNumber, weight);
        }
    }

    private void computeClassMap(int state, int compareState) {
        resetClasses();
        int numSuccessors = original.getNumSuccessors(state);
        for (int succNr = 0; succNr < numSuccessors; succNr++) {
            int distr = original.getSuccessorNode(state, succNr);
            computeClassMapDistr(distr);
        }
        numSuccessors = original.getNumSuccessors(compareState);
        for (int succNr = 0; succNr < numSuccessors; succNr++) {
            int distr = original.getSuccessorNode(compareState, succNr);
            computeClassMapDistr(distr);
        }
    }

    private void resetClasses() {
        classToNumber.reset();
        numClasses = 0;
    }

    private void computeClassMapDistr(int distribution) {
        assert distribution >= 0;
        int numSuccessors = original.getNumSuccessors(distribution);
        for (int succ = 0; succ < numSuccessors; succ++) {
            int succState = original.getSuccessorNode(distribution, succ);
            int succClass = partition.getBlockNumberFromState(succState);
            int classNumber = getClassToNumber(succClass);
            if (classNumber == -1) {
                setClassToNumber(succClass, numClasses);
                numClasses++;
            }
        }
    }

    private int getClassToNumber(int succClass) {
        return classToNumber.get(succClass);
    }

    private void setClassToNumber(int succClass, int numClasses) {
        classToNumber.set(succClass, numClasses);
    }

    @Override
    public void sendStatistics(Log log) {
        assert log != null;
        // TODO Auto-generated method stub

    }
}

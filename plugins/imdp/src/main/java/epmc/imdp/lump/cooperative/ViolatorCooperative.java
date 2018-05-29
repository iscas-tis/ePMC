package epmc.imdp.lump.cooperative;

import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.imdp.lump.ClassToNumber;
import epmc.imdp.lump.ClassToNumberIntArray;
import epmc.imdp.lump.ClassToNumberTIntIntMap;
import epmc.imdp.lump.Partition;
import epmc.imdp.lump.Violator;
import epmc.imdp.messages.MessagesIMDPLump;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.modelchecker.Log;
import epmc.operator.OperatorEq;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;

public final class ViolatorCooperative implements Violator {
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
        public ViolatorCooperative build() {
            assert original != null;
            assert partition != null;
            return new ViolatorCooperative(this);
        }

    }

    private final boolean noSelfCompare;
    private final GraphExplicit original;
    private final Partition partition;
    private int numChallengerSuccessors;
    private final ProblemSet checkProblem;
    private final ProblemSetSolver solver;
    private int numClasses;
    private int numActions;
    private final ValueInterval entry1;
    private final ValueInterval entry2;
    private final boolean useZeroActionShortcut;
    private final boolean useActionShortcut;
    // TODO add option
    private boolean useClassToNumberHash = false;
    private final ClassToNumber classToNumber;

    private long numZeroActionShortcutUsed;
    private long numActionShortCutUsed;
    private final OperatorEvaluator eqReal;
    private final OperatorEvaluator eqInterval;
    private final ValueBoolean cmp;

    private ViolatorCooperative(Builder builder) {
        assert builder != null;
        GraphExplicit original = builder.getOriginal();
        Partition partition = builder.getPartition();
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

        checkProblem = new ProblemSet();
        solver = new ProblemSetSolver();
        entry1 = newValueWeightTransition();
        entry2 = newValueWeightTransition();
        useZeroActionShortcut = Options.get().getBoolean(OptionsIMDPLump.IMDP_SHORTCUT_ZERO_ACTIONS);
        useActionShortcut = Options.get().getBoolean(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION);
        eqReal = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeReal.get(), TypeReal.get());
        eqInterval = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeInterval.get(), TypeInterval.get());
        cmp = TypeBoolean.get().newValue();
    }

    @Override
    public boolean violate(int state, int compareState) {
        if (noSelfCompare && state == compareState) {
            return false;
        }
        int numSuccs = original.getNumSuccessors(state);
        boolean violated = false;
        for (int succ = 0; succ < numSuccs; succ++) {
            int distribution = original.getSuccessorNode(state, succ);
            if (violate(state, distribution, compareState)) {
                violated = true;
                break;
            }
        }
        return violated;
    }

    private boolean violate(int state, int distribution, int compareState) {
        assert state >= 0;
        assert distribution >= 0;
        assert compareState >= 0;

        computeClassMap(distribution);
        computeNumActions(compareState);
        if (useZeroActionShortcut && numActions == 0) {
            resetInterval(distribution);
            numZeroActionShortcutUsed++;
            return true;
        }
        checkProblem.setDimensions(numClasses, numActions);
        computeChallengerIntervals(distribution);
        boolean canShortcut = computeDefenderIntervals(compareState);
        if (canShortcut) {
            resetInterval(distribution);
            checkProblem.reset();
            return false;
        }
        boolean result = solver.check(checkProblem);
        resetInterval(distribution);
        checkProblem.reset();
        return result;
    }

    private long getNumProblemSetsSolvedByLP() {
        return this.solver.getNumProblemSetsSolvedByLP();
    }

    private long getNumLPsSolved() {
        return this.solver.getNumLPsSolved();
    }

    private long getLPTime() {
        return this.solver.getLPTime();
    }

    private long getNumZeroActionShortcutUsed() {
        return numZeroActionShortcutUsed;
    }

    private long getNumActionShortCutUsed() {
        return numActionShortCutUsed;
    }

    private long getNumUnsimulableClass() {
        return solver.getNumUnsimulableClass();
    }

    private long getNumActionExact() {
        return solver.getNumActionExact();
    }

    private int getNumCachedEntries() {
        return solver.getNumCachedEntries();
    }

    private long getNumNonNormalisedSuccessfullLookups() {
        return solver.getNumNonNormalisedSuccessfullLookups();
    }

    private long getNumNormalisedSuccessfullLookups() {
        return solver.getNumNormalisedSuccessfullLookups();
    }

    private void computeClassMap(int distribution) {
        numChallengerSuccessors = original.getNumSuccessors(distribution);

        this.numClasses = 0;
        for (int succ = 0; succ < numChallengerSuccessors; succ++) {
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

    private void computeNumActions(int compareState) {
        EdgeProperty weights = original.getEdgeProperty(CommonProperties.WEIGHT);
        int numStateSuccessors = original.getNumSuccessors(compareState);
        this.numActions = 0;
        Value realZero = UtilValue.newValue(getTypeReal(), 0);
        for (int succ = 0; succ < numStateSuccessors; succ++) {
            int succDistr = original.getSuccessorNode(compareState, succ);
            int numDistrSucc = original.getNumSuccessors(succDistr);
            boolean ok = true;
            for (int distrSucc = 0; distrSucc < numDistrSucc; distrSucc++) {
                int succState = original.getSuccessorNode(succDistr, distrSucc);
                int succClass = partition.getBlockNumberFromState(succState);
                int classNumber = getClassToNumber(succClass);
                Value succLower = ValueInterval.as(weights.get(succDistr, distrSucc)).getIntervalLower();
                eqReal.apply(cmp, succLower, realZero);
                if (classNumber == -1 && !cmp.getBoolean()) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                numActions++;
            }
        }
    }

    private void resetInterval(int distribution) {
        classToNumber.reset();
    }

    private void computeChallengerIntervals(int distribution) {
        numChallengerSuccessors = original.getNumSuccessors(distribution);
        EdgeProperty weights = original.getEdgeProperty(CommonProperties.WEIGHT);
        for (int succ = 0; succ < numChallengerSuccessors; succ++) {
            int succState = original.getSuccessorNode(distribution, succ);
            int succClass = partition.getBlockNumberFromState(succState);
            int classNumber = getClassToNumber(succClass);
            Value weight = weights.get(distribution, succ);
            checkProblem.addChallenger(classNumber, weight);
        }
    }

    private boolean computeDefenderIntervals(int defenderState) {
        EdgeProperty weights = original.getEdgeProperty(CommonProperties.WEIGHT);
        int numStateSuccessors = original.getNumSuccessors(defenderState);
        Value realZero = UtilValue.newValue(getTypeReal(), 0);
        int actionNr = 0;
        for (int succ = 0; succ < numStateSuccessors; succ++) {
            int succDistr = original.getSuccessorNode(defenderState, succ);
            int numDistrSucc = original.getNumSuccessors(succDistr);
            boolean ok = true;
            for (int distrSucc = 0; distrSucc < numDistrSucc; distrSucc++) {
                int succState = original.getSuccessorNode(succDistr, distrSucc);
                int succClass = partition.getBlockNumberFromState(succState);
                int classNumber = getClassToNumber(succClass);
                ValueInterval succWeight = ValueInterval.as(weights.get(succDistr, distrSucc));
                ValueReal succLower = succWeight.getIntervalLower();
                eqReal.apply(cmp, succLower, realZero);
                if (classNumber == -1 && !cmp.getBoolean()) {
                    ok = false;
                    break;
                }
            }
            if (!ok) {
                continue;
            }
            for (int distrSucc = 0; distrSucc < numDistrSucc; distrSucc++) {
                int succState = original.getSuccessorNode(succDistr, distrSucc);
                int succClass = partition.getBlockNumberFromState(succState);
                int classNumber = getClassToNumber(succClass);
                Value succWeight = weights.get(succDistr, distrSucc);
                checkProblem.addDefender(actionNr, classNumber, succWeight);
            }
            if (useActionShortcut && shortCut(actionNr)) {
                numActionShortCutUsed++;
                return true;
            }

            actionNr++;
        }
        return false;
    }


    private boolean shortCut(int actionNr) {
        int numClasses = checkProblem.getNumClasses();
        for (int classNr = 0; classNr < numClasses; classNr++) {
            checkProblem.getChallenger(entry1, classNr);
            checkProblem.getDefender(entry2, actionNr, classNr);
            eqInterval.apply(cmp, entry1, entry2);
            if (!cmp.getBoolean()) {
                return false;
            }
        }
        return true;
    }

    private TypeReal getTypeReal() {
        return TypeReal.get();
    }

    private ValueInterval newValueWeightTransition() {
        return TypeInterval.get().newValue();
    }

    @Override
    public void sendStatistics(Log log) {
        assert log != null;
        log.send(MessagesIMDPLump.IMDP_LUMP_LP_STATISTICS,
                getNumProblemSetsSolvedByLP(),
                getNumLPsSolved(),
                getLPTime(),
                getNumZeroActionShortcutUsed(),
                getNumActionShortCutUsed(),
                getNumUnsimulableClass(),
                getNumActionExact(),
                getNumCachedEntries(),
                getNumNonNormalisedSuccessfullLookups(),
                getNumNormalisedSuccessfullLookups());		
    }
}

package epmc.imdp.lump.cooperative;

import java.util.Map;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.ConstraintType;
import epmc.constraintsolver.Direction;
import epmc.constraintsolver.Feature;
import epmc.imdp.lump.CacheTypeProvider;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.util.StopWatch;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;

/**
 * Solves a given problem set.
 * 
 * @author Ernst Moritz Hahn
 */
final class ProblemSetSolver {
    private final static String VAR_PREFIX = "F";
    private final static String UNDERSCORE = "_";

    private final Map<ProblemSet,Boolean> problemSetCache;
    private final ExtremePointsEnumerator extremePointsEnumerator;
    private final ConstraintSolverConfiguration contextConstraintSolver;
    private final ProblemSetNormaliser normaliser;
    private final ValueInterval entry1;
    private final ValueInterval entry2;
    private final ValueReal entry3;
    private final boolean shortcutUnsimulableClass;
    private final boolean shortcutActionExact;
    private final boolean cacheBefore;
    private final boolean cacheAfter;

    /* fields for statistics */
    private long numProblemSetsSolvedByLP;
    private long numLPsSolved;
    private final StopWatch lpTime = new StopWatch(false);
    private long numUnsimulableClass;
    private long numActionExact;
    private long numNonNormalisedSuccessfullLookups;
    private long numNormalisedSuccessfullLookups;
    private final OperatorEvaluator addInverseReal;
    private final OperatorEvaluator eqReal;
    private final OperatorEvaluator eqInterval;
    private final OperatorEvaluator gtReal;
    private final OperatorEvaluator addReal;
    private final OperatorEvaluator setInterval;
    private final OperatorEvaluator setReal;
    private final ValueBoolean cmpEq;
    private final ValueBoolean cmpGt;

    ProblemSetSolver() {
        extremePointsEnumerator = new ExtremePointsEnumerator();
        contextConstraintSolver = new ConstraintSolverConfiguration();
        contextConstraintSolver.requireFeature(Feature.LP);
        normaliser = new ProblemSetNormaliser();
        Class<? extends CacheTypeProvider> clazz = Options.get().get(OptionsIMDPLump.IMDP_LP_CACHE_TYPE);
        try {
            problemSetCache = clazz.newInstance().newMap();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        entry1 = newValueWeightTransition();
        entry2 = newValueWeightTransition();
        entry3 = TypeReal.get().newValue();
        shortcutUnsimulableClass = Options.get().getBoolean(OptionsIMDPLump.IMDP_SHORTCUT_UNSIMULABLE_BLOCK);
        shortcutActionExact = Options.get().getBoolean(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_AFTER_NORMALISATION);
        cacheBefore = Options.get().getBoolean(OptionsIMDPLump.IMDP_PROBLEM_SET_CACHE_BEFORE_NORMALISATION);
        cacheAfter = Options.get().getBoolean(OptionsIMDPLump.IMDP_PROBLEM_SET_CACHE_AFTER_NORMALISATION);
        addInverseReal = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeReal.get());
        eqReal = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeReal.get(), TypeReal.get());
        eqInterval = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeInterval.get(), TypeInterval.get());
        gtReal = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
        addReal = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeReal.get(), TypeReal.get());
        setInterval = ContextValue.get().getEvaluator(OperatorSet.SET, TypeInterval.get(), TypeInterval.get());
        setReal = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        cmpEq = TypeBoolean.get().newValue();
        cmpGt = TypeBoolean.get().newValue();
    }

    boolean check(ProblemSet problemSet) {
        assert problemSet != null;

        if (shortcutUnsimulableClass && eqInvClassShortCut(problemSet)) {
            numUnsimulableClass++;
            return true;
        }
        ProblemSet beforeNormalisation = null;
        if (cacheBefore) {
            Boolean cachedResult = problemSetCache.get(problemSet);
            if (cachedResult != null) {
                numNonNormalisedSuccessfullLookups++;
                return cachedResult;
            }
            beforeNormalisation = problemSet.clone();
        }
        normaliser.normalise(problemSet);
        if (shortcutActionExact && eqPShortCut(problemSet)) {
            numActionExact++;
            return false;
        }
        if (cacheAfter) {
            Boolean cachedResult = problemSetCache.get(problemSet);
            if (cachedResult != null) {
                numNormalisedSuccessfullLookups++;
                return cachedResult;
            }
        }

        boolean result = solveByLP(problemSet);		

        if (cacheBefore) {
            problemSetCache.put(beforeNormalisation, result);			
        }
        if (cacheAfter) {
            problemSetCache.put(problemSet.clone(), result);
        }
        return result;
    }

    long getNumProblemSetsSolvedByLP() {
        return numProblemSetsSolvedByLP;
    }

    long getNumLPsSolved() {
        return numLPsSolved;
    }

    long getLPTime() {
        return lpTime.getTimeSeconds();
    }

    private boolean solveByLP(ProblemSet problemSet) {
        lpTime.start();
        numProblemSetsSolvedByLP++;
        ValueArrayAlgebra challengerIntervals = problemSet.getIntervals();
        int numClasses = problemSet.getNumClasses();
        boolean result = extremePointsEnumerator.enumerate(
                v -> {
                    boolean lpResult = solveLP(problemSet, v);
                    numLPsSolved++;
                    return lpResult;
                },
                challengerIntervals, numClasses);
        lpTime.stop();
        return result;
    }

    private boolean eqPShortCut(ProblemSet problemSet) {
        int numActions = problemSet.getNumActions();
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            if (eqPShortCut(problemSet, actionNr)) {
                return true;
            }
        }
        return false;
    }

    private boolean eqInvClassShortCut(ProblemSet problemSet) {
        assert problemSet != null;
        int numClasses = problemSet.getNumClasses();
        for (int classNr = 0; classNr < numClasses; classNr++) {
            if (eqInvClassShortCut(problemSet, classNr)) {
                return true;
            }
        }
        return false;
    }

    private boolean eqPShortCut(ProblemSet checkProblem, int actionNr) {
        int numClasses = checkProblem.getNumClasses();
        for (int classNr = 0; classNr < numClasses; classNr++) {
            checkProblem.getChallenger(entry1, classNr);
            checkProblem.getDefender(entry2, actionNr, classNr);
            eqInterval.apply(cmpEq, entry1, entry2);
            if (!cmpEq.getBoolean()) {
                return false;
            }
        }
        return true;
    }

    private boolean eqInvClassShortCut(ProblemSet problemSet, int classNr) {
        assert problemSet != null;
        assert classNr >= 0;
        int numActions = problemSet.getNumActions();
        problemSet.getChallenger(entry1, classNr);
        setInterval.apply(entry2, UtilValue.newValue(entry2.getType(), 0));
        setReal.apply(entry3, UtilValue.newValue(entry3.getType(), 0));
        OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, entry3.getType(), entry2.getIntervalUpper().getType());
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            problemSet.getDefender(entry2, actionNr, classNr);
            max.apply(entry3, entry3, entry2.getIntervalUpper());
        }
        ValueReal classLo = entry1.getIntervalLower();
        eqReal.apply(cmpEq, classLo, entry3);
        gtReal.apply(cmpGt, classLo, entry3);
        if (!cmpEq.getBoolean() && cmpGt.getBoolean()) {
            return true;
        }		
        return false;
    }

    private boolean solveLP(ProblemSet problemSet, ValueArrayAlgebra classValue) {
        try (ConstraintSolver problem
                = contextConstraintSolver.newProblem()) {
            problem.setDirection(Direction.MIN);
            int numClasses = problemSet.getNumClasses();
            int numActions = problemSet.getNumActions();
            prepareVariables(problem, numClasses, numActions);
            prepareClassSums(problem, problemSet, classValue);
            prepareBoundValues(problem, problemSet, classValue);
            return problem.solve() != ConstraintSolverResult.SAT;
        }
    }

    private void prepareVariables(ConstraintSolver problem, int numClasses, int numActions) {
        Value zero = UtilValue.newValue(getTypeReal(), 0);
        Value one = UtilValue.newValue(getTypeReal(), 1);
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            for (int classNr = 0; classNr < numClasses; classNr++) {
                problem.addVariable(VAR_PREFIX + UNDERSCORE + actionNr + UNDERSCORE + classNr,
                        getTypeReal(), zero, one);
            }
        }
    }

    private void prepareClassSums(ConstraintSolver problem, ProblemSet problemSet, ValueArrayAlgebra classValue) {
        assert problem != null;
        assert problemSet != null;
        assert classValue != null;
        int numClasses = problemSet.getNumClasses();
        int numActions = problemSet.getNumActions();
        Value entry = getTypeReal().newValue();
        ValueArray actionOnes = UtilValue.newArray(getTypeRealArray(), numActions);
        Value one = UtilValue.newValue(getTypeReal(), 1);
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            actionOnes.set(one, actionNr);
        }
        int[] actionVariables = new int[numActions];
        for (int classNr = 0; classNr < numClasses; classNr++) {
            classValue.get(entry, classNr);
            for (int actionNr = 0; actionNr < numActions; actionNr++) {
                int index = getVariableIndex(actionNr, classNr, numClasses);
                actionVariables[actionNr] = index;
            }
            problem.addConstraint(actionOnes, actionVariables, ConstraintType.EQ, entry);
        }
    }

    private void prepareBoundValues(ConstraintSolver problem, ProblemSet problemSet, Value classValue) {
        int numClasses = problemSet.getNumClasses();
        int numActions = problemSet.getNumActions();
        for (int classNr = 0; classNr < numClasses; classNr++) {
            for (int actionNr = 0; actionNr < numActions; actionNr++) {
                prepareBoundValues(problem, problemSet, classNr, actionNr, false);
                prepareBoundValues(problem, problemSet, classNr, actionNr, true);
            }
        }
    }

    private void prepareBoundValues(ConstraintSolver problem,
            ProblemSet problemSet, int classNr,
            int actionNr, boolean min) {
        int numClasses = problemSet.getNumClasses();
        ValueArray boundValues = UtilValue.newArray(getTypeRealArray(), numClasses);
        ValueReal entry = getTypeReal().newValue();
        ValueReal finalEntry = getTypeReal().newValue();
        ValueInterval iEntry = getTypeInterval().newValue();
        ValueReal one = UtilValue.newValue(getTypeReal(), 1);
        int[] variables = new int[numClasses];
        problemSet.getDefender(iEntry, actionNr, classNr);
        setReal.apply(entry, min ? iEntry.getIntervalLower() : iEntry.getIntervalUpper());
        for (int otherClass = 0; otherClass < numClasses; otherClass++) {
            variables[otherClass] = getVariableIndex(actionNr, otherClass, numClasses);
            addInverseReal.apply(finalEntry, entry);
            if (classNr == otherClass) {
                addReal.apply(finalEntry, finalEntry, one);
            }
            boundValues.set(finalEntry, otherClass);
        }
        Value zero = UtilValue.newValue(getTypeReal(), 0);
        ConstraintType constraintType = min ? ConstraintType.GE : ConstraintType.LE;
        problem.addConstraint(boundValues, variables, constraintType, zero);
    }

    private int getVariableIndex(int actionNr, int classNr, int numClasses) {
        return actionNr * numClasses + classNr;
    }

    private TypeArray getTypeRealArray() {
        return getTypeReal().getTypeArray();
    }

    private TypeReal getTypeReal() {
        return TypeReal.get();
    }

    private TypeInterval getTypeInterval() {
        return TypeInterval.get();
    }

    long getNumUnsimulableClass() {
        return numUnsimulableClass;
    }

    long getNumActionExact() {
        return numActionExact;
    }

    int getNumCachedEntries() {
        return problemSetCache.size();
    }

    long getNumNonNormalisedSuccessfullLookups() {
        return numNonNormalisedSuccessfullLookups;
    }

    long getNumNormalisedSuccessfullLookups() {
        return numNormalisedSuccessfullLookups;
    }

    ValueInterval newValueWeightTransition() {
        return ValueInterval.as(TypeWeightTransition.get().newValue());
    }
}

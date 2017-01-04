package epmc.imdp.lump;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.ConstraintType;
import epmc.constraintsolver.Direction;
import epmc.constraintsolver.Feature;
import epmc.error.EPMCException;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.util.StopWatch;
import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;
import gnu.trove.map.hash.THashMap;

/**
 * Solves a given problem set.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemSetSolver {
	private final static String VAR_PREFIX = "F";
	private final static String UNDERSCORE = "_";

	/** Value context used. */
	private final ContextValue contextValue;
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
	
	ProblemSetSolver(ContextValue contextValue) {
		assert contextValue != null;
		this.contextValue = contextValue;
		extremePointsEnumerator = new ExtremePointsEnumerator(contextValue);
		contextConstraintSolver = new ConstraintSolverConfiguration(contextValue);
		contextConstraintSolver.requireFeature(Feature.LP);
		normaliser = new ProblemSetNormaliser(contextValue);
		Method method = contextValue.getOptions().get(OptionsIMDPLump.IMDP_LP_CACHE_TYPE);
		try {
			@SuppressWarnings("unchecked")
			Map<ProblemSet, Boolean> invoke = (Map<ProblemSet, Boolean>) method.invoke(this);
			this.problemSetCache = invoke;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
		entry1 = newValueWeightTransition();
		entry2 = newValueWeightTransition();
		entry3 = TypeReal.get(contextValue).newValue();
		shortcutUnsimulableClass = contextValue.getOptions().getBoolean(OptionsIMDPLump.IMDP_SHORTCUT_UNSIMULABLE_BLOCK);
		shortcutActionExact = contextValue.getOptions().getBoolean(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_AFTER_NORMALISATION);
		cacheBefore = contextValue.getOptions().getBoolean(OptionsIMDPLump.IMDP_PROBLEM_SET_CACHE_BEFORE_NORMALISATION);
		cacheAfter = contextValue.getOptions().getBoolean(OptionsIMDPLump.IMDP_PROBLEM_SET_CACHE_AFTER_NORMALISATION);
	}
	
	boolean check(ProblemSet problemSet) throws EPMCException {
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
	
	public Map<ProblemSet,Boolean> newTroveHash() {
		return new THashMap<>();
	}
	
	public Map<ProblemSet,Boolean> newJavaUtilTree() {
		return new TreeMap<>();
	}

	public Map<ProblemSet,Boolean> newJavaUtilHash() {
		return new HashMap<>();
	}

	
	private boolean solveByLP(ProblemSet problemSet) throws EPMCException {
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

	private boolean eqPShortCut(ProblemSet problemSet) throws EPMCException {
		int numActions = problemSet.getNumActions();
		for (int actionNr = 0; actionNr < numActions; actionNr++) {
			if (eqPShortCut(problemSet, actionNr)) {
				return true;
			}
		}
		return false;
	}

	private boolean eqInvClassShortCut(ProblemSet problemSet) throws EPMCException {
		int numClasses = problemSet.getNumClasses();
		for (int classNr = 0; classNr < numClasses; classNr++) {
			if (eqInvClassShortCut(problemSet, classNr)) {
				return true;
			}
		}
		return false;
	}

	private boolean eqPShortCut(ProblemSet checkProblem, int actionNr) throws EPMCException {
		int numClasses = checkProblem.getNumClasses();
		for (int classNr = 0; classNr < numClasses; classNr++) {
			checkProblem.getChallenger(entry1, classNr);
			checkProblem.getDefender(entry2, actionNr, classNr);
			if (!entry1.isEq(entry2)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean eqInvClassShortCut(ProblemSet problemSet, int classNr) throws EPMCException {
		int numActions = problemSet.getNumActions();
		problemSet.getChallenger(entry1, classNr);
		entry2.set(entry2.getType().getZero());
		entry3.set(entry3.getType().getZero());
		for (int actionNr = 0; actionNr < numActions; actionNr++) {
			problemSet.getDefender(entry2, actionNr, classNr);
			entry3.max(entry3, entry2.getIntervalUpper());
		}
		Value classLo = entry1.getIntervalLower();
		if (!classLo.isEq(entry3) && ValueAlgebra.asAlgebra(classLo).isGt(entry3)) {
			return true;
		}		
		return false;
	}

	private boolean solveLP(ProblemSet problemSet, ValueArrayAlgebra classValue) throws EPMCException {
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
		Value zero = getTypeReal().getZero();
		Value one = getTypeReal().getOne();
		for (int actionNr = 0; actionNr < numActions; actionNr++) {
			for (int classNr = 0; classNr < numClasses; classNr++) {
				problem.addVariable(VAR_PREFIX + UNDERSCORE + actionNr + UNDERSCORE + classNr,
						getTypeReal(), zero, one);
			}
		}
	}

	private void prepareClassSums(ConstraintSolver problem, ProblemSet problemSet, ValueArrayAlgebra classValue) throws EPMCException {
		int numClasses = problemSet.getNumClasses();
		int numActions = problemSet.getNumActions();
		Value entry = getTypeReal().newValue();
		ValueArray actionOnes = UtilValue.newArray(getTypeRealArray(), numActions);
		Value one = getTypeReal().getOne();
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
	
	private void prepareBoundValues(ConstraintSolver problem, ProblemSet problemSet, Value classValue) throws EPMCException {
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
			int actionNr, boolean min) throws EPMCException {
		int numClasses = problemSet.getNumClasses();
		ValueArray boundValues = UtilValue.newArray(getTypeRealArray(), numClasses);
		Value entry = getTypeReal().newValue();
		ValueReal finalEntry = getTypeReal().newValue();
		ValueInterval iEntry = getTypeInterval().newValue();
		Value one = getTypeReal().getOne();
		int[] variables = new int[numClasses];
		problemSet.getDefender(iEntry, actionNr, classNr);
		entry.set(min ? iEntry.getIntervalLower() : iEntry.getIntervalUpper());
		for (int otherClass = 0; otherClass < numClasses; otherClass++) {
			variables[otherClass] = getVariableIndex(actionNr, otherClass, numClasses);
			finalEntry.addInverse(entry);
			if (classNr == otherClass) {
				finalEntry.add(finalEntry, one);
			}
			boundValues.set(finalEntry, otherClass);
		}
		Value zero = getTypeReal().getZero();
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
		return TypeReal.get(getContextValue());
	}
	
	private TypeInterval getTypeInterval() {
		return TypeInterval.get(getContextValue());
	}
	
	private ContextValue getContextValue() {
		return contextValue;
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
		return ValueInterval.asInterval(TypeWeightTransition.get(getContextValue()).newValue());
	}
}

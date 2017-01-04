package epmc.imdp.lump;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.value.ContextValue;
import epmc.value.TypeReal;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;
import epmc.value.ValueInterval;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

final class Violator {
	private final GraphExplicit original;
	private final Partition partition;
	private final int[] classToNumberArray;
	private final TIntIntMap classToNumberHash;
	private int numChallengerSuccessors;
	private final ProblemSet checkProblem;
	private final ProblemSetSolver checker;
	private int numClasses;
	private int numActions;
	private final Value entry1;
	private final Value entry2;
	private final boolean useZeroActionShortcut;
	private final boolean useActionShortcut;
	// TODO add option
	private boolean useClassToNumberHash = false;
	
	private long numZeroActionShortcutUsed;
	private long numActionShortCutUsed;

	Violator(GraphExplicit original, Partition partition) throws EPMCException {
		assert original != null;
		assert partition != null;
		this.original = original;
		this.partition = partition;
		if (useClassToNumberHash) {
			classToNumberArray = null;
			classToNumberHash = new TIntIntHashMap(100, 0.5f, -1, -1);
		} else {
			classToNumberArray = new int[original.getNumNodes()];
			Arrays.fill(classToNumberArray, -1);
			classToNumberHash = null;
		}
		checkProblem = new ProblemSet(original.getContextValue());
		checker = new ProblemSetSolver(original.getContextValue());
		entry1 = newValueWeightTransition();
		entry2 = newValueWeightTransition();
		useZeroActionShortcut = original.getOptions().getBoolean(OptionsIMDPLump.IMDP_SHORTCUT_ZERO_ACTIONS);
		useActionShortcut = original.getOptions().getBoolean(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION);
	}
	
	boolean violate(int distribution, int compareState) throws EPMCException {
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
		boolean result = checker.check(checkProblem);
		resetInterval(distribution);
		checkProblem.reset();
		return result;
	}

	private void computeClassMap(int distribution) throws EPMCException {
		original.queryNode(distribution);
		numChallengerSuccessors = original.getNumSuccessors();

		this.numClasses = 0;
		for (int succ = 0; succ < numChallengerSuccessors; succ++) {
			int succState = original.getSuccessorNode(succ);
			int succClass = partition.getBlockNumberFromState(succState);
			int classNumber = getClassToNumber(succClass);
			if (classNumber == -1) {
				setClassToNumber(succClass, numClasses);
				numClasses++;
			}
		}
	}

	private int getClassToNumber(int succClass) {
		if (useClassToNumberHash) {
			return classToNumberHash.get(succClass);
		} else {
			return classToNumberArray[succClass];
		}
	}

	private void setClassToNumber(int succClass, int numClasses) {
		if (useClassToNumberHash) {
			classToNumberHash.put(succClass, numClasses);			
		} else {
			classToNumberArray[succClass] = numClasses;
		}
	}

	private void computeNumActions(int compareState) throws EPMCException {
		EdgeProperty weights = original.getEdgeProperty(CommonProperties.WEIGHT);
		original.queryNode(compareState);
		int numStateSuccessors = original.getNumSuccessors();
		this.numActions = 0;
		Value realZero = getTypeReal().getZero();
		for (int succ = 0; succ < numStateSuccessors; succ++) {
			int succDistr = original.getSuccessorNode(succ);
			original.queryNode(succDistr);
			int numDistrSucc = original.getNumSuccessors();
			boolean ok = true;
			for (int distrSucc = 0; distrSucc < numDistrSucc; distrSucc++) {
				int succState = original.getSuccessorNode(distrSucc);
				int succClass = partition.getBlockNumberFromState(succState);
				int classNumber = getClassToNumber(succClass);
				Value succLower = ValueInterval.asInterval(weights.get(distrSucc)).getIntervalLower();
				if (classNumber == -1 && !succLower.isEq(realZero)) {
					ok = false;
					break;
				}
			}
			if (ok) {
				numActions++;
			}
			original.queryNode(compareState);
		}
	}

	private void resetInterval(int distribution) throws EPMCException {
		original.queryNode(distribution);
		if (useClassToNumberHash) {
			classToNumberHash.clear();
		} else {
			for (int succ = 0; succ < numChallengerSuccessors; succ++) {
				int succState = original.getSuccessorNode(succ);
				int succClass = partition.getBlockNumberFromState(succState);
				classToNumberArray[succClass] = -1;
			}
		}
	}

	private void computeChallengerIntervals(int distribution) throws EPMCException {
		original.queryNode(distribution);
		numChallengerSuccessors = original.getNumSuccessors();
		EdgeProperty weights = original.getEdgeProperty(CommonProperties.WEIGHT);
		for (int succ = 0; succ < numChallengerSuccessors; succ++) {
			int succState = original.getSuccessorNode(succ);
			int succClass = partition.getBlockNumberFromState(succState);
			int classNumber = getClassToNumber(succClass);
			Value weight = weights.get(succ);
			checkProblem.addChallenger(classNumber, weight);
		}
	}
	
	private boolean computeDefenderIntervals(int defenderState) throws EPMCException {
		EdgeProperty weights = original.getEdgeProperty(CommonProperties.WEIGHT);
		original.queryNode(defenderState);
		int numStateSuccessors = original.getNumSuccessors();
		Value realZero = getTypeReal().getZero();
		int actionNr = 0;
		for (int succ = 0; succ < numStateSuccessors; succ++) {
			int succDistr = original.getSuccessorNode(succ);
			original.queryNode(succDistr);
			int numDistrSucc = original.getNumSuccessors();
			boolean ok = true;
			for (int distrSucc = 0; distrSucc < numDistrSucc; distrSucc++) {
				int succState = original.getSuccessorNode(distrSucc);
				int succClass = partition.getBlockNumberFromState(succState);
				int classNumber = getClassToNumber(succClass);
				ValueInterval succWeight = ValueInterval.asInterval(weights.get(distrSucc));
				Value succLower = succWeight.getIntervalLower();
				if (classNumber == -1 && !succLower.isEq(realZero)) {
					ok = false;
					break;
				}
			}
			if (!ok) {
				original.queryNode(defenderState);
				continue;
			}
			for (int distrSucc = 0; distrSucc < numDistrSucc; distrSucc++) {
				int succState = original.getSuccessorNode(distrSucc);
				int succClass = partition.getBlockNumberFromState(succState);
				int classNumber = getClassToNumber(succClass);
				Value succWeight = weights.get(distrSucc);
				checkProblem.addDefender(actionNr, classNumber, succWeight);
			}
			if (useActionShortcut && shortCut(actionNr)) {
				numActionShortCutUsed++;
				return true;
			}
			
			actionNr++;
			original.queryNode(defenderState);
		}
		return false;
	}


	private boolean shortCut(int actionNr) throws EPMCException {
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

	private ContextValue getContextValue() {
		return original.getContextValue();
	}
	
	private TypeReal getTypeReal() {
		return TypeReal.get(getContextValue());
	}
	
	long getNumProblemSetsSolvedByLP() {
		return this.checker.getNumProblemSetsSolvedByLP();
	}
	
	long getNumLPsSolved() {
		return this.checker.getNumLPsSolved();
	}
	
	long getLPTime() {
		return this.checker.getLPTime();
	}

	long getNumZeroActionShortcutUsed() {
		return numZeroActionShortcutUsed;
	}
	
	long getNumActionShortCutUsed() {
		return numActionShortCutUsed;
	}
	
	long getNumUnsimulableClass() {
		return checker.getNumUnsimulableClass();
	}

	long getNumActionExact() {
		return checker.getNumActionExact();
	}

	int getNumCachedEntries() {
		return checker.getNumCachedEntries();
	}

	long getNumNonNormalisedSuccessfullLookups() {
		return checker.getNumNonNormalisedSuccessfullLookups();
	}
	
	long getNumNormalisedSuccessfullLookups() {
		return checker.getNumNormalisedSuccessfullLookups();
	}

	private Value newValueWeightTransition() {
		return TypeWeightTransition.get(getContextValue()).newValue();
	}
}

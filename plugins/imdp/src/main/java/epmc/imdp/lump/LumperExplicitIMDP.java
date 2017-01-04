package epmc.imdp.lump;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.lumping.LumperExplicit;
import epmc.graphsolver.lumping.UtilLump;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitLump;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.imdp.IntervalPlayer;
import epmc.imdp.lump.signature.SignatureManager;
import epmc.imdp.messages.MessagesIMDPLump;
import epmc.imdp.options.OptionsIMDP;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;
import epmc.util.BitSetUnboundedLongArray;
import epmc.util.IntDeque;
import epmc.util.StopWatch;
import epmc.value.ContextValue;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class LumperExplicitIMDP implements LumperExplicit {
	public final static String IDENTIFIER = "imdp";
	private GraphExplicit original;
	private GraphSolverObjectiveExplicit quotient;
	private Violator violator;
	private Partition partition;
	private boolean noSelfCompare;
	private SignatureManager signatureManager;
	private Method splitBlockMethod;
	private BitSet signatureMarkers = new BitSetUnboundedLongArray();
	private final StopWatch signatureTime = new StopWatch(false);
	private GraphSolverObjectiveExplicit objective;
	private int targetBlock = -1;
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setOriginal(GraphSolverObjectiveExplicit objective) {
		this.objective = objective;
		this.original = objective.getGraph();
	}

	@Override
	public boolean canLump() {
		if (!(this.objective instanceof GraphSolverObjectiveExplicitLump)
				&& !(this.objective instanceof GraphSolverObjectiveExplicitUnboundedReachability)) {
			return false;
		}
		Semantics semantics = objective.getGraph().getGraphPropertyObject(CommonProperties.SEMANTICS);
		if (!SemanticsMDP.isMDP(semantics)) {
			return false;
		}
		Options options = objective.getGraph().getOptions();
		IntervalPlayer player = options.get(OptionsIMDP.IMDP_INTERVAL_PLAYER);
		if (player == IntervalPlayer.ANTAGONISTIC) {
			return false;
		}
		return true;
	}

	@Override
	public void lump() throws EPMCException {
		noSelfCompare = getOptions().getBoolean(OptionsIMDPLump.IMDP_NO_SELF_COMPARE);
		StopWatch totalTime = new StopWatch(true);
		Statistics origStatistics = new Statistics(original);
		getLog().send(MessagesIMDPLump.IMDP_LUMP_LUMP_START,
				origStatistics.getNumStates(),
				origStatistics.getNumNondet(),
				origStatistics.getNumFanout());
		StopWatch initTime = new StopWatch(true);
		getLog().send(MessagesIMDPLump.IMDP_LUMP_COMPUTE_INITIAL_START);
		this.partition = new Partition(computeInitialPartition());
		signatureManager = new SignatureManager(original, partition.getOrigToQuotientState());
		getLog().send(MessagesIMDPLump.IMDP_LUMP_COMPUTE_INITIAL_DONE, initTime.getTimeSeconds(), partition.size());
		StopWatch refineTime = new StopWatch(true);
		getLog().send(MessagesIMDPLump.IMDP_LUMP_REFINEMENT_START);
		Method method = getOptions().get(OptionsIMDPLump.IMDP_LUMP_METHOD);
		splitBlockMethod = getOptions().get(OptionsIMDPLump.IMDP_SPLIT_BLOCK_METHOD);
		assert splitBlockMethod != null;
		try {
			method.invoke(this);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
		getLog().send(MessagesIMDPLump.IMDP_LUMP_REFINEMENT_DONE, refineTime.getTimeSeconds(), partition.size());
		getLog().send(MessagesIMDPLump.IMDP_LUMP_LP_STATISTICS,
				violator.getNumProblemSetsSolvedByLP(),
				violator.getNumLPsSolved(),
				violator.getLPTime(),
				violator.getNumZeroActionShortcutUsed(),
				violator.getNumActionShortCutUsed(),
				violator.getNumUnsimulableClass(),
				violator.getNumActionExact(),
				violator.getNumCachedEntries(),
				violator.getNumNonNormalisedSuccessfullLookups(),
				violator.getNumNormalisedSuccessfullLookups());
		getLog().send(MessagesIMDPLump.IMDP_LUMP_SIGNATURE_STATISTICS,
				signatureTime.getTimeSeconds());
		getLog().send(MessagesIMDPLump.IMDP_LUMP_BUILD_QUOTIENT_START);
		StopWatch buildQuotientTime = new StopWatch(true);
		computeQuotient();
		Statistics quotientStatistics = new Statistics(quotient.getGraph());
		getLog().send(MessagesIMDPLump.IMDP_LUMP_BUILD_QUOTIENT_DONE,
				buildQuotientTime.getTimeSeconds(),
				quotientStatistics.getNumStates(),
				quotientStatistics.getNumNondet(),
				quotientStatistics.getNumFanout());
		getLog().send(MessagesIMDPLump.IMDP_LUMP_LUMP_DONE, totalTime.getTimeSeconds());
	}

	private void computeQuotient() throws EPMCException {
		QuotientBuilder builder = new QuotientBuilder(objective, partition);
		builder.build();
		quotient = builder.getQuotient();
	}

	public void lumpPerState() throws EPMCException {
		violator = new Violator(original, partition);
		assert partition.getNumBlocks() >= 1;
		boolean changed = true;
		int numStates = original.computeNumStates();
		while (changed) {
			changed = false;
			for (int state = 0; state < numStates; state++) {
				int blockNumber = partition.getBlockNumberFromState(state);
				if (blockNumber == targetBlock) {
					continue;
				}
				changed |= splitBlock(blockNumber, state);
			}
		}			
	}

	public void lumpPerBlock() throws EPMCException {
		original.computePredecessors();
		violator = new Violator(original, partition);
		assert partition.getNumBlocks() >= 1;
		IntDeque todo = new IntDeque();
		BitSet todoMarks = new BitSetBoundedLongArray(original.computeNumStates());
		todoMarks.set(0, partition.getNumBlocks());
		for (int i = 0; i < todoMarks.cardinality(); i++) {
			todo.addLast(i);
		}
		while (!todoMarks.isEmpty()) {
			int blockNumber = todo.removeFirst();
			if (blockNumber == targetBlock) {
				todoMarks.clear(blockNumber);
				continue;
			}
			todoMarks.clear(blockNumber);
			int oldFrom = partition.getBlockFrom(blockNumber);
			int oldTo = partition.getBlockTo(blockNumber);
			int oldSize = partition.size();
			boolean changed = splitBlock(blockNumber);
			
			if (changed) {
				if (!todoMarks.get(blockNumber)) {
					todoMarks.set(blockNumber);
					todo.addLast(blockNumber);
				}
				int newSize = partition.size();
				for (int entry = oldSize; entry < newSize; entry++) {
					if (!todoMarks.get(entry)) {
						todoMarks.set(entry);
						todo.addLast(entry);
					}
				}
				markPredecessorBlocks(oldFrom, oldTo, todoMarks, partition, todo);
			}
		}
	}
	
	private void markPredecessorBlocks(int oldFrom, int oldTo, BitSet todoMarks, Partition partition, IntDeque todo) throws EPMCException {
		for (int stateNr = oldFrom; stateNr < oldTo; stateNr++) {
			int state = partition.getBlockState(stateNr);
			original.queryNode(state);
			int numStatePredecessors =  original.getNumPredecessors();
			for (int statePredNr = 0; statePredNr < numStatePredecessors; statePredNr++) {
				int predDistr = original.getPredecessorNode(statePredNr);
				original.queryNode(predDistr);
				int numDistrPredecessors = original.getNumPredecessors();
				for (int distrPredNr = 0; distrPredNr < numDistrPredecessors; distrPredNr++) {
					int predState = original.getPredecessorNode(distrPredNr);
					int predBlock = partition.getBlockNumberFromState(predState);
					if (!todoMarks.get(predBlock)) {
						todo.addLast(predBlock);
						todoMarks.set(predBlock);
					}
				}
				original.queryNode(state);							
			}
		}
	}

	public boolean splitBlock(int blockNumber) throws EPMCException {
		try {
			return (Boolean) splitBlockMethod.invoke(this, blockNumber);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean splitBlockSimple(int blockNumber) throws EPMCException {
		int from = partition.getBlockFrom(blockNumber);
		int to = partition.getBlockTo(blockNumber);
		boolean changed = false;
		for (int stateNr = from; stateNr < to; stateNr++) {
			int state = partition.getBlockState(stateNr);
			changed |= splitBlock(blockNumber, state);
			if (changed) {
				break;
			}
		}
		return changed;
	}

	public boolean splitBlockPseudoSignature(int blockNumber) throws EPMCException {
		int from = partition.getBlockFrom(blockNumber);
		int to = partition.getBlockTo(blockNumber);
		boolean changed = false;
		signatureTime.start();
		signatureManager.restartQuerying();
		for (int stateNr = from; stateNr < to; stateNr++) {
			int state = partition.getBlockState(stateNr);
			signatureManager.addState(state);
		}
		signatureTime.stop();
		int numSignatures = signatureManager.getNumSignatures();
		for (int signature = 0; signature < numSignatures; signature++) {
			partition.markBlockForSplit(blockNumber);
			int state = signatureManager.getState(signature, 0);
			for (int otherSignature = 0; otherSignature < numSignatures; otherSignature++) {
				if (noSelfCompare && signature == otherSignature) {
					continue;
				}
				int otherState = signatureManager.getState(otherSignature, 0);
				int otherPartitionSize = signatureManager.getSignatureSize(otherSignature);
				if (checkSplitOff(state, otherState)) {
					for (int i = 0; i < otherPartitionSize; i++) {
						partition.markStateForSplit(signatureManager.getState(otherSignature, i), 1);
					}
					changed = true;
				} else {
					for (int i = 0; i < otherPartitionSize; i++) {
						partition.markStateForSplit(signatureManager.getState(otherSignature, i), 0);
					}
				}
			}
			partition.split();
			if (changed) {
				break;
			}
		}
		return changed;
	}

	public boolean splitBlockSignature(int blockNumber) throws EPMCException {
		partition.markBlockForSplit(blockNumber);
		int from = partition.getBlockFrom(blockNumber);
		int to = partition.getBlockTo(blockNumber);
		signatureTime.start();
		signatureManager.restartQuerying();
		for (int stateNr = from; stateNr < to; stateNr++) {
			int state = partition.getBlockState(stateNr);
			signatureManager.addState(state);
		}
		signatureTime.stop();
		int numSignatures = signatureManager.getNumSignatures();
		int sigNr = 0;
		for (int signature = 0; signature < numSignatures; signature++) {
			if (signatureMarkers.get(signature)) {
				continue;
			}
			int state = signatureManager.getState(signature, 0);
			for (int otherSignature = signature; otherSignature < numSignatures; otherSignature++) {
				int otherState = signatureManager.getState(otherSignature, 0);
				if ((noSelfCompare && signature == otherSignature)
						|| equals(state, otherState)) {
					signatureMarkers.set(otherSignature);
					int sigSize = signatureManager.getSignatureSize(otherSignature);
					for (int stateNr = 0; stateNr < sigSize; stateNr++) {
						int sigState = signatureManager.getState(otherSignature, stateNr);
						partition.markStateForSplit(sigState, sigNr);
					}
				}
			}
			sigNr++;
		}
		partition.split();
		for (int signature = 0; signature < numSignatures; signature++) {
			signatureMarkers.clear(signature);
		}
		return sigNr > 1;
	}

	private boolean splitBlock(int blockNumber, int state) throws EPMCException {
		int from = partition.getBlockFrom(blockNumber);
		int to = partition.getBlockTo(blockNumber);
		boolean changed = false;
		partition.markBlockForSplit(blockNumber);
		for (int cmpStateNr = from; cmpStateNr < to; cmpStateNr++) {
			int compareState = partition.getBlockState(cmpStateNr);
			if (checkSplitOff(state, compareState)) {
				partition.markStateForSplit(compareState, 1);
				changed = true;
			} else {
				partition.markStateForSplit(compareState, 0);
			}
		}
		partition.split();
		return changed;
	}

	private boolean equals(int state, int otherState) throws EPMCException {
		return !checkSplitOff(state, otherState) && !checkSplitOff(otherState, state);
	}
	
	private boolean checkSplitOff(int state, int compareState) throws EPMCException {
		if (noSelfCompare && state == compareState) {
			return false;
		}
		original.queryNode(state);
		int numSuccs = original.getNumSuccessors();
		boolean violated = false;
		for (int succ = 0; succ < numSuccs; succ++) {
			int distribution = original.getSuccessorNode(succ);
			if (violate(distribution, compareState)) {
				violated = true;
				original.queryNode(state);
				break;
			}
			original.queryNode(state);
		}
		return violated;
	}

	private boolean violate(int distribution, int compareState) throws EPMCException {
		assert distribution >= 0;
		assert compareState >= 0;
		return violator.violate(distribution, compareState);
	}

    private int[] computeInitialPartition() throws EPMCException {
    	if (this.objective instanceof GraphSolverObjectiveExplicitLump) {
    		GraphSolverObjectiveExplicitLump objectiveLump = (GraphSolverObjectiveExplicitLump) objective;
    		return objectiveLump.getPartition();
    	} else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
    		GraphSolverObjectiveExplicitUnboundedReachability objectiveReach = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
    		int numStates = objectiveReach.getGraph().computeNumStates();
    		int[] result = new int[numStates];
    		BitSet targets = objectiveReach.getTarget();
    		for (int i = 0; i < numStates; i++) {
    			result[i] = targets.get(i) ? 1 : 0;
    		}
    		UtilLump.fillGaps(result);
    		int targetState = targets.nextSetBit(0);
    		// TODO make optional
    		if (targetState != -1) {
    			targetBlock = result[targetState];
    		}
    		return result;
    	} else {
    		return null;
    	}
    }

	/**
	 * Get the log used.
	 * 
	 * @return log used
	 */
	private Log getLog() {
		return getOptions().get(OptionsMessages.LOG);
	}
	
	/**
	 * Get the options set used.
	 * 
	 * @return options set used
	 */
	private Options getOptions() {
		return getContextValue().getOptions();
	}
	
	/**
	 * Get the value context used.
	 * 
	 * @return value context used
	 */
    private ContextValue getContextValue() {
    	return original.getContextValue();
    }

	@Override
	public GraphSolverObjectiveExplicit getQuotient() {
		return quotient;
	}

	@Override
	public void quotientToOriginal() throws EPMCException {
		if (objective instanceof GraphSolverObjectiveExplicitLump) {
			
		} else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
			GraphSolverObjectiveExplicitUnboundedReachability unboundedOrig = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
			GraphSolverObjectiveExplicitUnboundedReachability unboundedQuot = (GraphSolverObjectiveExplicitUnboundedReachability) quotient;
			int numStatesOrig = original.computeNumStates();
			ValueArray quotResult = unboundedQuot.getResult();
			ValueArray origResult = UtilValue.newArray(quotResult.getType(), numStatesOrig);
			Value entry = unboundedQuot.getResult().getType().getEntryType().newValue();
			for (int origState = 0; origState < numStatesOrig; origState++) {
				int quotState = partition.getBlockNumberFromState(origState);
				quotResult.get(entry, quotState);
				origResult.set(entry, origState);
			}
			unboundedOrig.setResult(origResult);
		} else {
			assert false;
		}
	}
}

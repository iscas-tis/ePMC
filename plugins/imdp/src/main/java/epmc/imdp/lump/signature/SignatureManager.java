package epmc.imdp.lump.signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;
import epmc.value.TypeAlgebra;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInterval;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public final class SignatureManager {
	private final static ActionSignatureIdentityComparator ACTION_SIGNATURE_IDENTITY_COMPARATOR = new ActionSignatureIdentityComparator();
	private final boolean normalise;
	private final GraphExplicit original;
	private final int numStates;
	private final int numActions;
	private final int[] partition;
	private final EdgeProperty weights;
	private final ValueArrayAlgebra blockWeights;

	private final Set<ActionSignature> stateActionSignaturesUsed;
	private final ActionSignature[] stateActionSignatures;
	private int stateActionSignaturesSize;
	private final StateSignature prepareStateSignature;
	private final Map<StateSignature, StateSignature> stateSignatures;
	
	private final Map<ActionSignature, ActionSignature> actionSignatures;
	private final BitSet blockUsed;
	private final int[] blockList;
	private int blockListSize;
	private final ActionSignature prepareActionSignature;
	private final ValueAlgebra blockValue;

	private final Map<StateSignature,TIntList> signatureToStates = new HashMap<>();
	private final List<TIntList> signatureBlocks = new ArrayList<>();
	private int numSignatureBlocks;
	
	private final ValueInterval entry;
	private final ValueInterval entry2;
	private final ValueInterval entry3;

	public SignatureManager(GraphExplicit original, int[] partition) throws EPMCException {
		assert original != null;
		assert partition != null;
		this.original = original;
		this.partition = partition;
		numStates = original.computeNumStates();
		numActions = original.getNumNodes() - numStates;
		stateSignatures = new HashMap<>();
		actionSignatures = new HashMap<>();
		weights = original.getEdgeProperty(CommonProperties.WEIGHT);
		prepareStateSignature = new StateSignature();
		prepareActionSignature = new ActionSignature(original.getContextValue());
		blockWeights = UtilValue.newArray(TypeInterval.get(original.getContextValue()).getTypeArray(), numStates);
		blockUsed = new BitSetBoundedLongArray(numStates);
		blockList = new int[numStates];
		blockValue = UtilValue.newValue(TypeInterval.get(original.getContextValue()), 0);
		stateActionSignaturesUsed = new HashSet<>();
		stateActionSignatures = new ActionSignature[numStates];
		entry = ValueInterval.asInterval(TypeWeightTransition.get(original.getContextValue()).newValue());
		entry2 = ValueInterval.asInterval(TypeWeightTransition.get(original.getContextValue()).newValue());
		entry3 = ValueInterval.asInterval(TypeWeightTransition.get(original.getContextValue()).newValue());
		normalise = original.getOptions().getBoolean(OptionsIMDPLump.IMDP_SIGNATURE_NORMALISE);
	}
	
	StateSignature computeStateSignature(int state)  throws EPMCException {
		assert state >= 0;
		assert state < numStates;
		
		original.queryNode(state);
		int numSucc = original.getNumSuccessors();
		prepareStateSignature.setSize(numSucc);
		stateActionSignaturesSize = 0;
		for (int succNr = 0; succNr < numSucc; succNr++) {
			int succAction = original.getSuccessorNode(succNr);
			ActionSignature actionSignature = computeActionSignature(succAction);
			original.queryNode(state);
			if (!stateActionSignaturesUsed.contains(actionSignature)) {
				stateActionSignaturesUsed.add(actionSignature);
				stateActionSignatures[stateActionSignaturesSize] = actionSignature;
				stateActionSignaturesSize++;
			}
		}
		stateActionSignaturesUsed.clear();
		Arrays.sort(stateActionSignatures, 0, stateActionSignaturesSize, ACTION_SIGNATURE_IDENTITY_COMPARATOR);
		prepareStateSignature.setSize(stateActionSignaturesSize);
		for (int number = 0; number < stateActionSignaturesSize; number++) {
			prepareStateSignature.setActionSignature(number, stateActionSignatures[number]);
		}
		StateSignature stateSignature = stateSignatures.get(prepareStateSignature);
		if (stateSignature == null) {
			stateSignature = prepareStateSignature.clone();
			stateSignatures.put(stateSignature, stateSignature);
		}
		return stateSignature;
	}

	ActionSignature computeActionSignature(int action) throws EPMCException {
		assert action >= numStates;
		assert action < numStates + numActions;
		
		computeBlockWeights(action);
		setActionSignature();
		if (normalise) {
			normaliseActionSignature();
		}
		resetActionStructures();
		ActionSignature result = actionSignatures.get(prepareActionSignature);
		if (result == null) {
			result = prepareActionSignature.clone();
			actionSignatures.put(result, result);
		}
		return result;
	}

	private void computeBlockWeights(int action) throws EPMCException {
		original.queryNode(action);
		int numSucc = original.getNumSuccessors();
		for (int succNr = 0; succNr < numSucc; succNr++) {
			int succState = original.getSuccessorNode(succNr);
			Value weight = weights.get(succNr);
			int block = partition[succState];
			if (blockUsed.get(block)) {
				blockWeights.get(blockValue, block);
				blockValue.add(blockValue, weight);
				blockWeights.set(blockValue, block);
			} else {
				blockWeights.set(weight, block);
				blockUsed.set(block);
				blockList[blockListSize] = block;
				blockListSize++;
			}
		}
	}

	private void setActionSignature() {
		Arrays.sort(blockList, 0, blockListSize);
		prepareActionSignature.setSize(blockListSize);
		for (int number = 0; number < blockListSize; number++) {
			prepareActionSignature.setBlock(number, blockList[number]);
			int block = blockList[number];
			blockWeights.get(blockValue, block);
			prepareActionSignature.setBlock(number, block);
			prepareActionSignature.setValue(number, blockValue);
		}
	}

	private void normaliseActionSignature() throws EPMCException {
		int size = prepareActionSignature.getSize();
		Value one = getTypeWeightTransition().getOne();
		entry.set(getTypeWeightTransition().getZero());
		for (int index = 0; index < size; index++) {
			prepareActionSignature.getValue(index);
			entry.add(entry, prepareActionSignature.getValue(index));
		}
		entry.subtract(one, entry);
		for (int index = 0; index < size; index++) {
			entry2.set(prepareActionSignature.getValue(index));
			entry3.add(entry, entry2);
			entry2.getIntervalLower().max(entry2.getIntervalLower(), entry3.getIntervalUpper());
			entry2.getIntervalUpper().min(entry2.getIntervalUpper(), entry3.getIntervalLower());
			prepareActionSignature.setValue(index, entry2);
		}
	}
	
	private void resetActionStructures() {
		for (int number = 0; number < blockListSize; number++) {
			blockWeights.set(0, number);
			int block = blockList[number];
			blockUsed.set(block, false);
			blockList[number] = 0;
		}
		blockListSize = 0;
	}

	public void restartQuerying() {
		signatureToStates.clear();
		for (int i = 0; i < numSignatureBlocks; i++) {
			signatureBlocks.get(i).clear();
		}
		numSignatureBlocks = 0;
	}
	
	public void addState(int state) throws EPMCException {
		StateSignature signature = computeStateSignature(state);
		TIntList stateList = signatureToStates.get(signature);
		if (stateList == null) {
			if (numSignatureBlocks < signatureBlocks.size()) {
				stateList = signatureBlocks.get(numSignatureBlocks);
			} else {
				stateList = new TIntArrayList();
				signatureBlocks.add(stateList);
			}
			numSignatureBlocks++;
			signatureToStates.put(signature, stateList);
		}
		stateList.add(state);
	}
	
	public int getNumSignatures() {
		return numSignatureBlocks;
	}
	
	public int getSignatureSize(int signature) {
		return signatureBlocks.get(signature).size();
	}
	
	public int getState(int signature, int number) {
		return signatureBlocks.get(signature).get(number);
	}
	
	private TypeAlgebra getTypeWeightTransition() {
		return TypeWeightTransition.get(original.getContextValue());
	}
}

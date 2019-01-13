package epmc.imdp.lump.signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.imdp.lump.Partition;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInterval;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class SignatureManager {
    private final static ActionSignatureIdentityComparator ACTION_SIGNATURE_IDENTITY_COMPARATOR = new ActionSignatureIdentityComparator();
    private final boolean normalise;
    private final GraphExplicit original;
    private final int numStates;
    private final int numActions;
    private final Partition partition;
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
    private final ValueInterval blockValue;

    private final Map<StateSignature,IntArrayList> signatureToStates = new HashMap<>();
    private final List<IntArrayList> signatureBlocks = new ArrayList<>();
    private int numSignatureBlocks;

    private final ValueInterval entry;
    private final ValueInterval entry2;
    private final ValueInterval entry3;
    
    private final OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeInterval.get(), TypeInterval.get());
    private final OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeInterval.get(), TypeInterval.get());

    public SignatureManager(GraphExplicit original, Partition partition) {
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
        prepareActionSignature = new ActionSignature();
        blockWeights = UtilValue.newArray(TypeInterval.get().getTypeArray(), numStates);
        blockUsed = new BitSetBoundedLongArray(numStates);
        blockList = new int[numStates];
        blockValue = UtilValue.newValue(TypeInterval.get(), 0);
        stateActionSignaturesUsed = new HashSet<>();
        stateActionSignatures = new ActionSignature[numStates];
        entry = ValueInterval.as(TypeWeightTransition.get().newValue());
        entry2 = ValueInterval.as(TypeWeightTransition.get().newValue());
        entry3 = ValueInterval.as(TypeWeightTransition.get().newValue());
        normalise = Options.get().getBoolean(OptionsIMDPLump.IMDP_SIGNATURE_NORMALISE);
    }

    StateSignature computeStateSignature(int state)  {
        assert state >= 0;
        assert state < numStates;

        int numSucc = original.getNumSuccessors(state);
        prepareStateSignature.setSize(numSucc);
        stateActionSignaturesSize = 0;
        for (int succNr = 0; succNr < numSucc; succNr++) {
            int succAction = original.getSuccessorNode(state, succNr);
            ActionSignature actionSignature = computeActionSignature(succAction);
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

    ActionSignature computeActionSignature(int action) {
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

    private void computeBlockWeights(int action) {
        int numSucc = original.getNumSuccessors(action);
        for (int succNr = 0; succNr < numSucc; succNr++) {
            int succState = original.getSuccessorNode(action, succNr);
            Value weight = weights.get(action, succNr);
            int block = partition.getBlockNumberFromState(succState);
            if (blockUsed.get(block)) {
                blockWeights.get(blockValue, block);
                add.apply(blockValue, blockValue, weight);
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

    private void normaliseActionSignature() {
        int size = prepareActionSignature.getSize();
        Value one = UtilValue.newValue(getTypeWeightTransition(), 1);
        Value zero = UtilValue.newValue(getTypeWeightTransition(), 0);
        set.apply(entry, zero);
        for (int index = 0; index < size; index++) {
            prepareActionSignature.getValue(index);
            add.apply(entry, entry, prepareActionSignature.getValue(index));
        }
        OperatorEvaluator min = ContextValue.get().getEvaluator(OperatorMin.MIN, entry2.getIntervalUpper().getType(), entry3.getIntervalLower().getType());
        OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, entry2.getIntervalUpper().getType(), entry3.getIntervalLower().getType());
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeWeightTransition.get(), TypeWeightTransition.get());
        subtract.apply(entry, one, entry);
        for (int index = 0; index < size; index++) {
            set.apply(entry2, prepareActionSignature.getValue(index));
            add.apply(entry3, entry, entry2);
            max.apply(entry2.getIntervalLower(), entry2.getIntervalLower(), entry3.getIntervalUpper());
            min.apply(entry2.getIntervalUpper(), entry2.getIntervalUpper(), entry3.getIntervalLower());
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

    public void addState(int state) {
        StateSignature signature = computeStateSignature(state);
        IntArrayList stateList = signatureToStates.get(signature);
        if (stateList == null) {
            if (numSignatureBlocks < signatureBlocks.size()) {
                stateList = signatureBlocks.get(numSignatureBlocks);
            } else {
                stateList = new IntArrayList();
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
        return signatureBlocks.get(signature).getInt(number);
    }

    private TypeAlgebra getTypeWeightTransition() {
        return TypeWeightTransition.get();
    }
}

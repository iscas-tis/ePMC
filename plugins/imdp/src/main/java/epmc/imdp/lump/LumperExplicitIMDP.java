package epmc.imdp.lump;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsIMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.lumping.LumperExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitLump;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.imdp.IntervalPlayer;
import epmc.imdp.lump.antagonistic.ViolatorAntagonistic;
import epmc.imdp.lump.cooperative.ViolatorCooperative;
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
        if (!SemanticsIMDP.isIMDP(semantics)) {
            return false;
        }
        return true;
    }

    @Override
    public void lump() {
        noSelfCompare = Options.get().getBoolean(OptionsIMDPLump.IMDP_NO_SELF_COMPARE);
        StopWatch totalTime = new StopWatch(true);
        Statistics origStatistics = new Statistics(original);
        getLog().send(MessagesIMDPLump.IMDP_LUMP_LUMP_START,
                origStatistics.getNumStates(),
                origStatistics.getNumNondet(),
                origStatistics.getNumFanout());
        StopWatch initTime = new StopWatch(true);
        getLog().send(MessagesIMDPLump.IMDP_LUMP_COMPUTE_INITIAL_START);
        this.partition = computeInitialPartition();
        signatureManager = new SignatureManager(original, partition);
        getLog().send(MessagesIMDPLump.IMDP_LUMP_COMPUTE_INITIAL_DONE, initTime.getTimeSeconds(), partition.getNumBlocks());
        StopWatch refineTime = new StopWatch(true);
        getLog().send(MessagesIMDPLump.IMDP_LUMP_REFINEMENT_START);
        Method method = Options.get().get(OptionsIMDPLump.IMDP_LUMP_METHOD);
        splitBlockMethod = Options.get().get(OptionsIMDPLump.IMDP_SPLIT_BLOCK_METHOD);
        assert splitBlockMethod != null;
        try {
            method.invoke(this);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
            throw new RuntimeException(e);
        }
        getLog().send(MessagesIMDPLump.IMDP_LUMP_REFINEMENT_DONE, refineTime.getTimeSeconds(), partition.getNumBlocks());
        violator.sendStatistics(getLog());
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

    private void computeQuotient() {
        QuotientBuilder builder = new QuotientBuilder(objective, partition);
        builder.build();
        quotient = builder.getQuotient();
    }

    public void lumpPerState() {
        violator = newViolator();
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

    public void lumpPerBlock() {
        original.computePredecessors();
        violator = newViolator();
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
            int oldSize = partition.getNumBlocks();
            boolean changed = splitBlock(blockNumber);

            if (changed) {
                if (!todoMarks.get(blockNumber)) {
                    todoMarks.set(blockNumber);
                    todo.addLast(blockNumber);
                }
                int newSize = partition.getNumBlocks();
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

    private void markPredecessorBlocks(int oldFrom, int oldTo, BitSet todoMarks, Partition partition, IntDeque todo) {
        for (int stateNr = oldFrom; stateNr < oldTo; stateNr++) {
            int state = partition.getBlockState(stateNr);
            int numStatePredecessors =  original.getProperties().getNumPredecessors(state);
            for (int statePredNr = 0; statePredNr < numStatePredecessors; statePredNr++) {
                int predDistr = original.getProperties().getPredecessorNode(state, statePredNr);
                int numDistrPredecessors = original.getProperties().getNumPredecessors(predDistr);
                for (int distrPredNr = 0; distrPredNr < numDistrPredecessors; distrPredNr++) {
                    int predState = original.getProperties().getPredecessorNode(predDistr, distrPredNr);
                    int predBlock = partition.getBlockNumberFromState(predState);
                    if (!todoMarks.get(predBlock)) {
                        todo.addLast(predBlock);
                        todoMarks.set(predBlock);
                    }
                }
            }
        }
    }

    public boolean splitBlock(int blockNumber) {
        try {
            return (Boolean) splitBlockMethod.invoke(this, blockNumber);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean splitBlockSimple(int blockNumber) {
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

    public boolean splitBlockPseudoSignature(int blockNumber) {
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
                if (violate(state, otherState)) {
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

    public boolean splitBlockSignature(int blockNumber) {
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

    private boolean splitBlock(int blockNumber, int state) {
        int from = partition.getBlockFrom(blockNumber);
        int to = partition.getBlockTo(blockNumber);
        boolean changed = false;
        partition.markBlockForSplit(blockNumber);
        for (int cmpStateNr = from; cmpStateNr < to; cmpStateNr++) {
            int compareState = partition.getBlockState(cmpStateNr);
            if (violate(state, compareState)) {
                partition.markStateForSplit(compareState, 1);
                changed = true;
            } else {
                partition.markStateForSplit(compareState, 0);
            }
        }
        partition.split();
        return changed;
    }

    private boolean equals(int state, int otherState) {
        return !violate(state, otherState) && !violate(otherState, state);
    }

    private boolean violate(int state, int compareState) {
        return violator.violate(state, compareState);
    }

    private Partition computeInitialPartition() {
        int numStates = original.computeNumStates();
        Partition partition = new PartitionSimple(numStates);
        if (this.objective instanceof GraphSolverObjectiveExplicitLump) {
            GraphSolverObjectiveExplicitLump objectiveLump = (GraphSolverObjectiveExplicitLump) objective;
            partition.markBlockForSplit(0);
            for (int state = 0; state < numStates; state++) {
                partition.markStateForSplit(state, objectiveLump.getBlock(state));
            }
            partition.split();
        } else if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            GraphSolverObjectiveExplicitUnboundedReachability objectiveReach = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
            BitSet targets = objectiveReach.getTarget();
            boolean allTarget = true;
            boolean allNonTarget = true;
            for (int state = 0; state < numStates; state++) {
                if (targets.get(state)) {
                    allNonTarget = false;
                } else {
                    allTarget = false;
                }
                if (!allNonTarget && !allTarget) {
                    break;
                }
            }
            if (!allTarget && !allNonTarget) {
                partition.markBlockForSplit(0);
                for (int state = 0; state < numStates; state++) {
                    partition.markStateForSplit(state, targets.get(state) ? 1 : 0);
                }
                partition.split();
            }
            // TODO make optional
            targetBlock = !allNonTarget ? -1 :
                allTarget ? 0 : 1;
        } else {
            throw new RuntimeException();
        }
        return partition;
    }

    /**
     * Get the log used.
     * 
     * @return log used
     */
    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }

    @Override
    public GraphSolverObjectiveExplicit getQuotient() {
        return quotient;
    }

    @Override
    public void quotientToOriginal() {
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

    private Violator newViolator() {
        Violator.Builder builder;
        IntervalPlayer player = Options.get().get(OptionsIMDP.IMDP_INTERVAL_PLAYER);
        switch (player) {
        case ANTAGONISTIC:
            builder = new ViolatorAntagonistic.Builder();
            break;
        case COOPERATIVE:
            builder = new ViolatorCooperative.Builder();
            break;
        default:
            throw new RuntimeException(player.toString());
        }
        return builder.setOriginal(original)
                .setPartition(partition)
                .build();
    }
}

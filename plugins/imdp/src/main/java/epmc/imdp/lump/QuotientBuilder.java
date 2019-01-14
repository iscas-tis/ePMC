package epmc.imdp.lump;

import java.util.HashSet;
import java.util.Set;

import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.NodeProperty;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitLump;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.operator.OperatorAdd;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

final class QuotientBuilder {
    private GraphSolverObjectiveExplicit original;
    private final Partition partition;
    private GraphSolverObjectiveExplicit quotient;

    QuotientBuilder(GraphSolverObjectiveExplicit original, Partition partition) {
        assert original != null;
        assert partition != null;
        this.original = original;
        this.partition = partition;
    }

    /**
     * Prepare the quotient graph.
     * This is done by counting the number of states, fan out, etc. and in
     * order to allocate the memory for the graph.
     * 
     * @param partition partition to compute quotient of
     */
    private GraphExplicitSparseAlternate prepareQuotientGraph() {
        assert partition != null;
        GraphExplicit originalGraph = original.getGraph();
        EdgeProperty origWeight = originalGraph.getEdgeProperty(CommonProperties.WEIGHT);		
        int numQuotientStates = partition.getNumBlocks();
        int numNonDet = 0;
        int numProb = 0;
        int partitionSize = partition.getNumBlocks();
        Set<Int2ObjectOpenHashMap<ValueAlgebra>> quotSuccessors = new HashSet<>();
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeightTransition.get(), TypeWeightTransition.get());
        for (int blockNr = 0; blockNr < partitionSize; blockNr++) {
            int from = partition.getBlockFrom(blockNr);
            int to = partition.getBlockTo(blockNr);
            int representatant = getRepresentant(partition, from, to);
            int numRepSucc = originalGraph.getNumSuccessors(representatant);
            quotSuccessors.clear();
            for (int repSuccNr = 0; repSuccNr < numRepSucc; repSuccNr++) {
                Int2ObjectOpenHashMap<ValueAlgebra> quotSuccessor = new Int2ObjectOpenHashMap<>();
                int succDistr = originalGraph.getSuccessorNode(representatant, repSuccNr);
                int numDistrSucc = originalGraph.getNumSuccessors(succDistr);
                for (int distrSuccNr = 0; distrSuccNr < numDistrSucc; distrSuccNr++) {
                    int succState = originalGraph.getSuccessorNode(succDistr, distrSuccNr);
                    int succPartition = partition.getBlockNumberFromState(succState);
                    ValueAlgebra quotSuccValue = quotSuccessor.get(succPartition);
                    if (quotSuccValue == null) {
                        quotSuccValue = UtilValue.newValue(TypeWeightTransition.get(), 0);
                        quotSuccessor.put(succPartition, quotSuccValue);
                    }
                    add.apply(quotSuccValue, quotSuccValue, origWeight.get(succDistr, distrSuccNr));
                    numProb++;
                }
                quotSuccessors.add(quotSuccessor);
            }
            numNonDet += quotSuccessors.size();
        }
        return new GraphExplicitSparseAlternate(numQuotientStates, numNonDet, numProb);
    }

    /**
     * Get representant of given block.
     * Returns representant with lowest number, so as to return the same
     * representant independently of the lumping order used. This is necessary,
     * because for IMDP lumping the exact quotient used depends on the
     * representant (though all possible quotients are equivalent).
     * The block parameter must be a valid signature block.
     * 
     * @param block block to get representant of
     * @return representant of block.
     */
    private int getRepresentant(Partition partition, int from, int to) {
        int minState = Integer.MAX_VALUE;
        for (int stateNr = from; stateNr < to; stateNr++) {
            int state = partition.getBlockState(stateNr);
            minState = Math.min(minState, state);
        }
        return minState;
    }

    void build() {
        GraphExplicitSparseAlternate quotientGraph = buildGraph();
        if (original instanceof GraphSolverObjectiveExplicitLump) {
            GraphSolverObjectiveExplicitLump quotientLump = new GraphSolverObjectiveExplicitLump();
            quotientLump.setGraph(quotientGraph);
            quotient = quotientLump;
        } else if (original instanceof GraphSolverObjectiveExplicitUnboundedReachability) {
            GraphSolverObjectiveExplicitUnboundedReachability originalUnbounded = (GraphSolverObjectiveExplicitUnboundedReachability) original;
            GraphSolverObjectiveExplicitUnboundedReachability quotientUnbounded = new GraphSolverObjectiveExplicitUnboundedReachability();
            quotientUnbounded.setComputeScheduler(originalUnbounded.isComputeScheduler());
            quotientUnbounded.setGraph(quotientGraph);
            quotientUnbounded.setMin(originalUnbounded.isMin());
            int numQuotientStates = quotientGraph.computeNumStates();
            BitSet origTarget = originalUnbounded.getTarget();
            BitSet origZero = originalUnbounded.getZeroSet();
            BitSet quotTarget = new BitSetBoundedLongArray(numQuotientStates);
            BitSet quotZero = new BitSetBoundedLongArray(numQuotientStates);
            int numOrigStates = original.getGraph().computeNumStates();
            for (int origState = 0; origState < numOrigStates; origState++) {
                int quotState = partition.getBlockNumberFromState(origState);
                quotTarget.set(quotState, origTarget.get(origState));
                quotZero.set(quotState, origZero.get(origState));
            }
            quotientUnbounded.setTarget(quotTarget);
            quotientUnbounded.setZeroSink(quotZero);
            quotient = quotientUnbounded;
        } else {
            assert false;
        }
    }

    private GraphExplicitSparseAlternate buildGraph() {
        GraphExplicit originalGraph = original.getGraph();
        EdgeProperty origWeight = originalGraph.getEdgeProperty(CommonProperties.WEIGHT);
        NodeProperty origState = originalGraph.getNodeProperty(CommonProperties.STATE);
        GraphExplicitSparseAlternate quotientGraph = prepareQuotientGraph();
        EdgeProperty quotientWeight = quotientGraph.addSettableEdgeProperty(CommonProperties.WEIGHT, origWeight.getType());
        NodeProperty quotientState = quotientGraph.addSettableNodeProperty(CommonProperties.STATE,  origState.getType());
        quotientGraph.addSettableGraphProperty(CommonProperties.SEMANTICS, originalGraph.getGraphPropertyType(CommonProperties.SEMANTICS));
        quotientGraph.setGraphProperty(CommonProperties.SEMANTICS, originalGraph.getGraphProperty(CommonProperties.SEMANTICS));
        int[] quotDistr = new int[1];
        quotDistr[0] = partition.getNumBlocks();
        Value[] toBlockValues = new Value[partition.getNumBlocks()];
        for (int blockNr = 0; blockNr < partition.getNumBlocks(); blockNr++) {
            toBlockValues[blockNr] = UtilValue.newValue(getTypeWeightTransition(), 0);
        }

        int partitionSize = partition.getNumBlocks();
        Set<Int2ObjectOpenHashMap<ValueAlgebra>> quotSuccessors = new HashSet<>();
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeightTransition.get(), TypeWeightTransition.get());
        for (int blockNr = 0; blockNr < partitionSize; blockNr++) {
            quotSuccessors.clear();
            int from = partition.getBlockFrom(blockNr);
            int to = partition.getBlockTo(blockNr);
            int representatant = getRepresentant(partition, from, to);
            int numRepSucc = originalGraph.getNumSuccessors(representatant);

            for (int repSuccNr = 0; repSuccNr < numRepSucc; repSuccNr++) {
                int origDistr = originalGraph.getSuccessorNode(representatant, repSuccNr);
                int numDistrSucc = originalGraph.getNumSuccessors(origDistr);
                Int2ObjectOpenHashMap<ValueAlgebra> quotSuccessor = new Int2ObjectOpenHashMap<>();
                for (int distrSuccNr = 0; distrSuccNr < numDistrSucc; distrSuccNr++) {
                    int succState = originalGraph.getSuccessorNode(origDistr, distrSuccNr);
                    int succBlock = partition.getBlockNumberFromState(succState);
                    ValueAlgebra quotSuccValue = quotSuccessor.get(succBlock);
                    if (quotSuccValue == null) {
                        quotSuccValue = UtilValue.newValue(getTypeWeightTransition(), 0);
                        quotSuccessor.put(succBlock, quotSuccValue);
                    }
                    add.apply(quotSuccValue, quotSuccValue, origWeight.get(origDistr, distrSuccNr));
                }
                quotSuccessors.add(quotSuccessor);
            }
            quotientGraph.prepareNode(blockNr, quotSuccessors.size());
            quotientState.set(blockNr, origState.get(representatant));
            for (Int2ObjectOpenHashMap<ValueAlgebra> quotAction : quotSuccessors) {
                quotientGraph.prepareNode(quotDistr[0], quotAction.size());
                int[] succNr = new int[1];
                quotAction.forEach((toBlock,value) -> {
                    quotientGraph.setSuccessorNode(quotDistr[0], succNr[0], toBlock);
                    quotientWeight.set(quotDistr[0], succNr[0], value);
                    succNr[0]++;
                });
                quotDistr[0]++;
            }
        }
        return quotientGraph;
    }

    GraphSolverObjectiveExplicit getQuotient() {
        return quotient;
    }

    private TypeAlgebra getTypeWeightTransition() {
        return TypeWeightTransition.get();
    }
}

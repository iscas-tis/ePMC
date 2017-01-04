package epmc.imdp.lump;

import java.util.HashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.NodeProperty;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitLump;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;
import epmc.value.ContextValue;
import epmc.value.TypeAlgebra;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
	 * @throws EPMCException thrown in case of problems
	 */
	private GraphExplicitSparseAlternate prepareQuotientGraph() throws EPMCException {
		assert partition != null;
		GraphExplicit originalGraph = original.getGraph();
		EdgeProperty origWeight = originalGraph.getEdgeProperty(CommonProperties.WEIGHT);		
		int numQuotientStates = partition.size();
		int numNonDet = 0;
		int numProb = 0;
		int partitionSize = partition.size();
		Set<TIntObjectMap<ValueAlgebra>> quotSuccessors = new HashSet<>();
		for (int blockNr = 0; blockNr < partitionSize; blockNr++) {
			int from = partition.getBlockFrom(blockNr);
			int to = partition.getBlockTo(blockNr);
			int representatant = getRepresentant(partition, from, to);
			originalGraph.queryNode(representatant);
			int numRepSucc = originalGraph.getNumSuccessors();
			quotSuccessors.clear();
			for (int repSuccNr = 0; repSuccNr < numRepSucc; repSuccNr++) {
				TIntObjectMap<ValueAlgebra> quotSuccessor = new TIntObjectHashMap<>(100, 0.5f, -1);
				int succDistr = originalGraph.getSuccessorNode(repSuccNr);
				originalGraph.queryNode(succDistr);
				int numDistrSucc = originalGraph.getNumSuccessors();
				for (int distrSuccNr = 0; distrSuccNr < numDistrSucc; distrSuccNr++) {
					int succState = originalGraph.getSuccessorNode(distrSuccNr);
					int succPartition = partition.getBlockNumberFromState(succState);
					ValueAlgebra quotSuccValue = quotSuccessor.get(succPartition);
					if (quotSuccValue == null) {
						quotSuccValue = UtilValue.newValue(TypeWeightTransition.get(getContextValue()), 0);
						quotSuccessor.put(succPartition, quotSuccValue);
					}
					quotSuccValue.add(quotSuccValue, origWeight.get(distrSuccNr));
					numProb++;
				}
				quotSuccessors.add(quotSuccessor);
				originalGraph.queryNode(representatant);
			}
			numNonDet += quotSuccessors.size();
		}
		return new GraphExplicitSparseAlternate(getContextValue(), false, numQuotientStates, numNonDet, numProb);
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

	void build() throws EPMCException {
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

	private GraphExplicitSparseAlternate buildGraph() throws EPMCException {
		GraphExplicit originalGraph = original.getGraph();
		EdgeProperty origWeight = originalGraph.getEdgeProperty(CommonProperties.WEIGHT);
		NodeProperty origState = originalGraph.getNodeProperty(CommonProperties.STATE);
		GraphExplicitSparseAlternate quotientGraph = prepareQuotientGraph();
		EdgeProperty quotientWeight = quotientGraph.addSettableEdgeProperty(CommonProperties.WEIGHT, origWeight.getType());
		NodeProperty quotientState = quotientGraph.addSettableNodeProperty(CommonProperties.STATE,  origState.getType());
		quotientGraph.addSettableGraphProperty(CommonProperties.SEMANTICS, originalGraph.getGraphPropertyType(CommonProperties.SEMANTICS));
		quotientGraph.setGraphProperty(CommonProperties.SEMANTICS, originalGraph.getGraphProperty(CommonProperties.SEMANTICS));
		int quotDistr = partition.size();
		Value[] toBlockValues = new Value[partition.size()];
		for (int blockNr = 0; blockNr < partition.size(); blockNr++) {
			toBlockValues[blockNr] = UtilValue.newValue(getTypeWeightTransition(), 0);
		}
		
		int partitionSize = partition.size();
		Set<TIntObjectMap<ValueAlgebra>> quotSuccessors = new HashSet<>();
		for (int blockNr = 0; blockNr < partitionSize; blockNr++) {
			quotSuccessors.clear();
			int from = partition.getBlockFrom(blockNr);
			int to = partition.getBlockTo(blockNr);
			int representatant = getRepresentant(partition, from, to);
			originalGraph.queryNode(representatant);
			int numRepSucc = originalGraph.getNumSuccessors();

			for (int repSuccNr = 0; repSuccNr < numRepSucc; repSuccNr++) {
				int origDistr = originalGraph.getSuccessorNode(repSuccNr);
				originalGraph.queryNode(origDistr);
				int numDistrSucc = originalGraph.getNumSuccessors();
				TIntObjectMap<ValueAlgebra> quotSuccessor = new TIntObjectHashMap<>(100, 0.5f, -1);
				for (int distrSuccNr = 0; distrSuccNr < numDistrSucc; distrSuccNr++) {
					int succState = originalGraph.getSuccessorNode(distrSuccNr);
					int succBlock = partition.getBlockNumberFromState(succState);
					ValueAlgebra quotSuccValue = quotSuccessor.get(succBlock);
					if (quotSuccValue == null) {
						quotSuccValue = UtilValue.newValue(getTypeWeightTransition(), 0);
						quotSuccessor.put(succBlock, quotSuccValue);
					}
					quotSuccValue.add(quotSuccValue, origWeight.get(distrSuccNr));
				}
				originalGraph.queryNode(representatant);
				quotSuccessors.add(quotSuccessor);
			}
			quotientGraph.queryNode(blockNr);
			quotientGraph.prepareNode(quotSuccessors.size());
			quotientState.set(origState.get());
			for (TIntObjectMap<ValueAlgebra> quotAction : quotSuccessors) {
				quotientGraph.queryNode(quotDistr);
				quotientGraph.prepareNode(quotAction.size());
				TIntObjectIterator<ValueAlgebra> iterator = quotAction.iterator();
				int succNr = 0;
				while (iterator.hasNext()) {
					iterator.advance();
					int toBlock = iterator.key();
					Value value = iterator.value();
					quotientGraph.setSuccessorNode(succNr, toBlock);
					quotientWeight.set(value, succNr);
					succNr++;
				}
				quotDistr++;
			}
			originalGraph.queryNode(representatant);
		}
		return quotientGraph;
	}

	GraphSolverObjectiveExplicit getQuotient() {
		return quotient;
	}

	private TypeAlgebra getTypeWeightTransition() {
		return TypeWeightTransition.get(getContextValue());
	}

	/**
	 * Get the value context used.
	 * 
	 * @return value context used
	 */
    private ContextValue getContextValue() {
    	return original.getGraph().getContextValue();
    }
}

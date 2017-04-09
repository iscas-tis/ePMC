package epmc.multiobjective.graphsolver;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.Scheduler;
import epmc.graph.explicit.SchedulerSimple;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;

public final class SchedulerSimpleMultiobjectiveJava implements SchedulerSimple {
	private final GraphExplicitSparseAlternate graph;
	private final int[] stateBounds;
	private final int numStates;
	private final ValueInteger value;
	private final int[] decisions;

	public SchedulerSimpleMultiobjectiveJava(GraphExplicitSparseAlternate graph) {
		assert graph != null;
		this.graph = graph;
		stateBounds = graph.getStateBoundsJava();
		numStates = graph.computeNumStates();
		value = TypeInteger.get(graph.getContextValue()).newValue();
		decisions = new int[numStates];
		Arrays.fill(decisions, Scheduler.UNSET);
	}
	
	@Override
	public GraphExplicit getGraph() {
		return graph;
	}

	@Override
	public Value get(int node) throws EPMCException {
		value.set(getDecision(node));
		return value;
	}

	@Override
	public void set(int node, Value value) throws EPMCException {
		assert false;
	}

	@Override
	public int size() {
		return numStates;
	}

	@Override
	public void set(int node, int decision) {
		assert false;
	}

	@Override
	public int getDecision(int node) {
		return decisions[node] - stateBounds[node];
	}

	public int[] getDecisions() {
		return decisions;
	}	
}

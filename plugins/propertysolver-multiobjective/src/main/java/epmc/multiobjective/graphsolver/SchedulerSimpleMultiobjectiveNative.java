package epmc.multiobjective.graphsolver;

import java.nio.ByteBuffer;

import com.sun.jna.Memory;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.Scheduler;
import epmc.graph.explicit.SchedulerSimple;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;

public final class SchedulerSimpleMultiobjectiveNative implements SchedulerSimple {
	private final GraphExplicitSparseAlternate graph;
	private final ByteBuffer stateBounds;
	private final int numStates;
	private final ValueInteger value;
	private final Memory decisions;

	SchedulerSimpleMultiobjectiveNative(GraphExplicitSparseAlternate graph) {
		assert graph != null;
		this.graph = graph;
		stateBounds = graph.getStateBoundsNative();
		numStates = graph.computeNumStates();
		value = TypeInteger.get(graph.getContextValue()).newValue();
		decisions = new Memory(Integer.BYTES * numStates);
		for (int state = 0; state < numStates; state++) {
			decisions.setInt(state, Scheduler.UNSET);
		}
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
		return decisions.getInt(Integer.BYTES * node) - stateBounds.getInt(Integer.BYTES * node);
	}

	public ByteBuffer getDecisions() {
		return decisions.getByteBuffer(0, Integer.BYTES * numStates);
	}	
}

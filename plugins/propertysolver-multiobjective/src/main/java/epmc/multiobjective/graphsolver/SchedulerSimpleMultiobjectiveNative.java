package epmc.multiobjective.graphsolver;

import java.nio.ByteBuffer;

import com.sun.jna.Memory;

import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.Scheduler;
import epmc.graph.explicit.SchedulerSimple;

public final class SchedulerSimpleMultiobjectiveNative implements SchedulerSimple {
	private final ByteBuffer stateBounds;
	private final int numStates;
	private final Memory decisions;

	SchedulerSimpleMultiobjectiveNative(GraphExplicitSparseAlternate graph) {
		assert graph != null;
		stateBounds = graph.getStateBoundsNative();
		numStates = graph.computeNumStates();
		decisions = new Memory(Integer.BYTES * numStates);
		for (int state = 0; state < numStates; state++) {
			decisions.setInt(state, Scheduler.UNSET);
		}
	}
	
	@Override
	public int size() {
		return numStates;
	}

	@Override
	public int getDecision(int node) {
		return decisions.getInt(Integer.BYTES * node) - stateBounds.getInt(Integer.BYTES * node);
	}

	public ByteBuffer getDecisions() {
		return decisions.getByteBuffer(0, Integer.BYTES * numStates);
	}	
}

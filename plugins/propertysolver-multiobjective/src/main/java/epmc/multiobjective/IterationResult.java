package epmc.multiobjective;

import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueArrayInteger;

final class IterationResult {
	private final ValueArrayAlgebra q;
	private final ValueArrayInteger scheduler;
	
	IterationResult(ValueArrayAlgebra q, ValueArrayInteger scheduler) {
		assert q != null;
		assert scheduler != null;
		this.q = q;
		this.scheduler = scheduler;
	}
	
	ValueArrayAlgebra getQ() {
		return q;
	}
	
	ValueArrayInteger getScheduler() {
		return scheduler;
	}
	
	@Override
	public String toString() {
		return q.toString();
	}
}

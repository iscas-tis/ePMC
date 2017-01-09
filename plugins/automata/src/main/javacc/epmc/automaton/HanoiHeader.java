package epmc.automaton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epmc.expression.Expression;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import epmc.value.ContextValue;

final class HanoiHeader {
	private final ContextValue context;
	private final Map<String,Expression> ap2expr;
	private int numStates;
	private final BitSet startStates = new BitSetUnboundedLongArray();
	private final List<Expression> aps = new ArrayList<>();
	private int numAcc;
	
	HanoiHeader(ContextValue context, Map<String,Expression> ap2expr) {
		assert context != null;
		assert ap2expr != null;
		this.context = context;
		this.ap2expr = ap2expr;
	}
	
	ContextValue getContext() {
		return context;
	}
	
	void setNumStates(int numStates) {
		this.numStates = numStates;
	}
	
	int getNumStates() {
		return numStates;
	}
	
	void setStartState(int startState) {
		startStates.set(startState);
	}

	BitSet getStartStates() {
		return startStates;
	}
	
	void addAP(String name) {
		assert name != null;
		assert ap2expr.containsKey(name);
		aps.add(ap2expr.get(name));
	}
	
	Expression numberToIdentifier(int number) {
		assert number >= 0;
		assert number < aps.size();
		return aps.get(number);
	}
	
	void setNumAcc(int numAcc) {
		this.numAcc = numAcc;
	}
	
	public int getNumAcc() {
		return numAcc;
	}
}

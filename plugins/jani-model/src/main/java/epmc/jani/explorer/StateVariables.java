package epmc.jani.explorer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.value.Type;
import epmc.value.TypeNumBitsKnown;

public final class StateVariables {
	private final List<Expression> variables = new ArrayList<>();
	private final List<Boolean> permanentVariables = new ArrayList<>();
	private final Map<Expression,Integer> varMap = new LinkedHashMap<>();
	private final Map<Expression,Type> typeMap = new LinkedHashMap<>();
	private int numBits;
	
	public int addVariable(Expression identifier, Type type, boolean permanent) throws EPMCException {
		assert identifier != null;
		assert type != null;
		variables.add(identifier);
		if (permanent) {
			if (TypeNumBitsKnown.getNumBits(type) == TypeNumBitsKnown.UNKNOWN
					|| numBits == Integer.MAX_VALUE) {
				numBits = Integer.MAX_VALUE;
			} else {
				numBits += TypeNumBitsKnown.getNumBits(type);
			}
		}
		permanentVariables.add(permanent);
		varMap.put(identifier, variables.size() - 1);
		typeMap.put(identifier, type);
		return variables.size() - 1;
	}
	
	public int getVariableNumber(Expression identifier) {
		assert identifier != null;
		assert varMap != null;
		assert varMap.containsKey(identifier) : identifier;
		return varMap.get(identifier);
	}
	
	public boolean isStoreVariable(int variable) {
		return permanentVariables.get(variable);
	}
	
	public Expression[] getIdentifiersArray() {
		return variables.toArray(new Expression[0]);
	}
	
	public List<Expression> getVariables() {
		return variables;
	}

	public int getNumBits() {
		return numBits;
	}

	public boolean contains(Expression identifier) {
		return varMap.containsKey(identifier);
	}
	
	public Type getType(Expression expression) {
		return typeMap.get(expression);
	}
}

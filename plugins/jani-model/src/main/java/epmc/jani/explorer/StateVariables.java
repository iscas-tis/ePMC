/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.jani.explorer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeNumBitsKnown;
import epmc.value.Value;

public final class StateVariables {
	private final static String SPACE = " ";
	
	private final List<Expression> variables = new ArrayList<>();
	private final List<Boolean> permanentVariables = new ArrayList<>();
	private final Map<Expression,Integer> varMap = new LinkedHashMap<>();
	private final Map<Expression,Type> typeMap = new LinkedHashMap<>();
	private final Map<Expression,Value> initialValues = new LinkedHashMap<>();
	private int numBits;
	
	public int addVariable(Expression identifier, Type type, boolean permanent, Expression initialValue) throws EPMCException {
		assert identifier != null;
		assert type != null;
//		assert permanent || initialValue != null : identifier + SPACE + type;
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
		if (initialValue != null) {
			initialValues.put(identifier, UtilEvaluatorExplicit.evaluate(initialValue, new ExpressionToType() {
				@Override
				public Type getType(Expression expression) throws EPMCException {
					return null;
				}
			
				@Override
				public ContextValue getContextValue() {
					return type.getContext();
				}
			}));
		}
		return variables.size() - 1;
	}

	
	public int addVariable(Expression identifier, Type type, boolean permanent) throws EPMCException {
		assert identifier != null;
		assert type != null;
		return addVariable(identifier, type, permanent, null);
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
	
	public Value getInitial(Expression expression) {
		assert expression != null;
		return initialValues.get(expression);
	}
}

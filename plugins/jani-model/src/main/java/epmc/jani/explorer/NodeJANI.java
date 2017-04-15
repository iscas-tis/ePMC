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

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.explorer.ExplorerNode;
import epmc.util.BitStream;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBitStoreable;
import epmc.value.ValueBoolean;
import epmc.value.ValueNumBitsKnown;
import epmc.value.ValueObject;
import epmc.value.ValueRange;

public final class NodeJANI implements ExplorerNode {
	private final ExplorerJANI explorer;
	private final Value[] values;
	private final boolean[] variablesSetMarks;
	private int numSet;
	private final int[] variablesSet;
	private final boolean[] storeVariables;
	private final int numBits;
	private final StateVariables stateVariables;
	private Value[] initialValues;

	public NodeJANI(ExplorerJANI explorer, StateVariables stateVariables) throws EPMCException {
		assert explorer != null;
		assert stateVariables != null;
		this.explorer = explorer;
		this.stateVariables = stateVariables;
		List<Expression> variables = new ArrayList<>(stateVariables.getVariables());
		values = new Value[variables.size()];
		int numBits = 0;
		storeVariables = new boolean[variables.size()];
		initialValues = new Value[variables.size()];
		for (int varNr = 0; varNr < variables.size(); varNr++) {
			assert variables.get(varNr) != null : varNr;
			values[varNr] = stateVariables.get(variables.get(varNr)).getType().newValue();
			boolean storeVariable = stateVariables.get(varNr).isPermanent();
			storeVariables[varNr] = storeVariable;
			if (storeVariable) {
				if (ValueNumBitsKnown.getNumBits(values[varNr]) == Integer.MAX_VALUE
						|| numBits == Integer.MAX_VALUE) {
					numBits = Integer.MAX_VALUE;
				} else {
					numBits += ValueNumBitsKnown.getNumBits(values[varNr]);
				}
			}
			if (!storeVariable) {
				Value initial = stateVariables.get(variables.get(varNr)).getInitialValue();
//				assert initial != null : variables.get(varNr);
				initialValues[varNr] = initial;
			}
		}
		this.numBits = numBits;
		this.variablesSetMarks = new boolean[variables.size()];
		this.variablesSet = new int[variables.size()];
	}

	@Override
	public NodeJANI clone() {
		try {
			NodeJANI clone = new NodeJANI(explorer, stateVariables);
			for (int varNr = 0; varNr < values.length; varNr++) {
				clone.values[varNr].set(values[varNr]);
			}
			return clone;
		} catch (EPMCException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void read(BitStream reader) {
		assert reader != null;
		for (int varNr = 0; varNr < values.length; varNr++) {
			if (storeVariables[varNr]) {
				ValueBitStoreable.asBitStoreable(values[varNr]).read(reader);
			}
		}
	}

	@Override
	public void write(BitStream writer) {
		for (int varNr = 0; varNr < values.length; varNr++) {
			if (storeVariables[varNr]) {
				ValueBitStoreable.asBitStoreable(values[varNr]).write(writer);
			}
		}
	}

	@Override
	public void set(ExplorerNode node) {
		assert node != null;
		assert node instanceof NodeJANI;
		NodeJANI other = (NodeJANI) node;
		assert this.values.length == other.values.length :
			this.values.length + " " + other.values.length;
		for (int varNr = 0; varNr < values.length; varNr++) {
			values[varNr].set(other.values[varNr]);
		}
	}

	@Override
	public int getNumBits() {
		return numBits;
	}

	public void unmark() {
		for (int markNr = 0; markNr < numSet; markNr++) {
			int varNr = variablesSet[markNr];
			variablesSetMarks[varNr] = false;
		}
		for (int varNr = 0; varNr < storeVariables.length; varNr++) {
			if (!storeVariables[varNr] && initialValues[varNr] != null) {
				this.values[varNr].set(initialValues[varNr]);
			}
		}
		numSet = 0;
	}
	
	public boolean setSet(NodeJANI other) {
		assert other != null;
		assert this.values.length == other.values.length;
		for (int varNr = 0; varNr < other.numSet; varNr++) {
			int variable = other.variablesSet[varNr];
			if (variablesSetMarks[variable]) {
				return false;
			}
			values[variable].set(other.values[variable]);
			variablesSet[numSet] = variable;
			numSet++;
			variablesSetMarks[variable] = true;
		}
		return true;
	}
	
	public boolean setAndMark(NodeJANI other) {
		assert other != null;
		assert this.values.length == other.values.length;
		for (int variable = 0; variable < other.values.length; variable++) {
			values[variable].set(other.values[variable]);
			if (other.variablesSetMarks[variable]) {
				mark(variable);
			}
		}
		return true;
	}	
	
	public Value[] getValues() {
		return values;
	}
	
	public Value getValue(int varNr) {
		return values[varNr];
	}
	
	public void getValue(Value result, int varNr) {
		result.set(values[varNr]);
	}
	
	public boolean getBoolean(int variable) {
		return ValueBoolean.asBoolean(values[variable]).getBoolean();
	}
	
	public boolean setVariable(int variable, Value value) throws EPMCException {
		assert variable >= 0;
		assert variable < values.length;
		assert value != null;
		if (variablesSetMarks[variable]) {
			return false;
		}
		values[variable].set(value);
		ensure(ValueRange.checkRange(values[variable]),
				ProblemsJANIExplorer.JANI_EXPLORER_TRANSITION_INVALID_ASSIGNMENT);
		variablesSet[numSet] = variable;
		variablesSetMarks[variable] = true;
		numSet++;
		return true;
	}

	public boolean setVariable(int variable, int value) {
		assert variable >= 0;
		assert variable < values.length;
		if (variablesSetMarks[variable]) {
			return false;
		}
		ValueAlgebra.asAlgebra(values[variable]).set(value);
		variablesSet[numSet] = variable;
		variablesSetMarks[variable] = true;
		numSet++;
		return true;
	}
	
	public boolean setVariable(int variable, Object value) {
		assert variable >= 0;
		assert variable < values.length;
		assert value != null;
		if (variablesSetMarks[variable]) {
			return false;
		}
		ValueObject.asObject(values[variable]).set(value);
		variablesSet[numSet] = variable;
		variablesSetMarks[variable] = true;
		numSet++;
		return true;
	}

	public boolean setVariable(int variable, boolean value) {
		assert variable >= 0;
		assert variable < values.length;
		if (variablesSetMarks[variable]) {
			return false;
		}
		ValueBoolean.asBoolean(values[variable]).set(value);
		variablesSet[numSet] = variable;
		variablesSetMarks[variable] = true;
		numSet++;
		return true;
	}

	public boolean mark(int variable) {
		if (variablesSetMarks[variable]) {
			return false;
		}
		variablesSet[numSet] = variable;
		numSet++;
		variablesSetMarks[variable] = true;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("-----\n");
		builder.append(this.stateVariables.getVariables());
		builder.append("\n");
		builder.append(Arrays.toString(storeVariables));
		builder.append("\n");
		builder.append(Arrays.toString(this.values));
		builder.append("\n");
		builder.append(Arrays.toString(variablesSetMarks));
		builder.append("\n");
		builder.append(numSet + "\n");
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NodeJANI)) {
			return false;
		}
		NodeJANI other = (NodeJANI) obj;
		if (!Arrays.equals(this.values, other.values)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	public void setNotSet(NodeJANI nodeAutomaton) {
		for (int varNr = 0; varNr < values.length; varNr++) {
			if (!variablesSetMarks[varNr]) {
				values[varNr].set(nodeAutomaton.values[varNr]);
			}
		}
	}
}

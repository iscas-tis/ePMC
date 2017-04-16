package epmc.jani.explorer;

import java.util.ArrayList;
import java.util.List;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeNumBitsKnown;

public final class TypeJANIDecision implements Type {
	private final ExplorerJANI explorer;
	private final int[] variables;
	private final int[] numBits;
	private final Type[] types;
	private final int totalNumBits;
	private int requiredEntries;

	TypeJANIDecision(ExplorerJANI explorer) {
		assert explorer != null;
		this.explorer = explorer;
		StateVariables stateVariables = explorer.getStateVariables();
		List<Integer> usedVariableNumbers = new ArrayList<>();
		List<Integer> numBitsList = new ArrayList<>();
		List<Type> typesList = new ArrayList<>();
		for (StateVariable variable : stateVariables.getVariables()) {
			if (!variable.isDecision()) {
				continue;
			}
			numBitsList.add(TypeNumBitsKnown.getNumBits(variable.getType()));
			usedVariableNumbers.add(stateVariables.getVariableNumber(variable.getIdentifier()));
			typesList.add(variable.getType());
		}
		variables = new int[usedVariableNumbers.size()];
		numBits = new int[numBitsList.size()];
		for (int index = 0; index < usedVariableNumbers.size(); index++) {
			variables[index] = usedVariableNumbers.get(index);
			numBits[index] = numBitsList.get(index);
		}
		types = typesList.toArray(new Type[0]);
		int totalNumBits = 0;
		for (int index = 0; index < numBits.length; index++) {
			int nBits = numBits[index];
			if (nBits == TypeNumBitsKnown.UNKNOWN) {
				totalNumBits = TypeNumBitsKnown.UNKNOWN;
			} else {
				totalNumBits += nBits;
			}
		}
		this.totalNumBits = totalNumBits;
		this.requiredEntries = totalNumBits / Long.SIZE + (totalNumBits % Long.SIZE == 0 ? 0 : 1);
	}
	
	@Override
	public ContextValue getContext() {
		return explorer.getContextValue();
	}

	@Override
	public ValueJANIDecision newValue() {
		return new ValueJANIDecision(this);
	}

	@Override
	public boolean canImport(Type type) {
		assert type != null;
		if (!(type instanceof TypeJANIDecision)) {
			return false;
		}
		TypeJANIDecision other = (TypeJANIDecision) type;
		if (explorer != other.explorer) {
			return false;
		}
		return true;
	}

	@Override
	public TypeArray getTypeArray() {
		return new TypeArrayJANIDecisionType(this);
	}

	int[] getVariables() {
		return variables;
	}
	
	int[] getNumBits() {
		return numBits;
	}
	
	int getTotalNumBits() {
		return totalNumBits;
	}
	
	int getRequiredEntries() {
		return requiredEntries;
	}
	
	Type[] getTypes() {
		return types;
	}
}

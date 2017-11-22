package epmc.jani.explorer;

import java.util.ArrayList;
import java.util.List;

import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeNumBitsKnown;

public final class TypeDecision implements Type {
    private final int[] variables;
    private final int[] numBits;
    private final Type[] types;
    private final int totalNumBits;

    public final static boolean is(Type type) {
        return type instanceof TypeDecision;
    }
    
    public final static TypeDecision as(Type type) {
        if (is(type)) {
            return (TypeDecision) type;
        } else {
            return null;
        }
    }
    
    TypeDecision(ExplorerJANI explorer) {
        assert explorer != null;
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
    }

    @Override
    public ValueDecision newValue() {
        return new ValueDecision(this);
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

    Type[] getTypes() {
        return types;
    }
}

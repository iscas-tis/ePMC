package epmc.param.value;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueReal;

public final class ContextValuePARAM {
    private final ContextValue contextValue;
    private final Map<Type,Type> types = new HashMap<>();
    private final TypeFractionBigInteger typeFractionBigInteger;
    private final TypeReal typeReal;
    private final TypeFunctionPolynomial typeFunctionPolynomial;
    private final TypeFunction typeFunction;
    private final TObjectIntMap<Object> parameterToNumber = new TObjectIntHashMap<>();
	private final List<Object> parameters = new ArrayList<>();
	private final List<Object> parametersExternal = Collections.unmodifiableList(parameters);
	private final Map<String, Operator> originalOperators = new HashMap<>();
	private final Map<String, Operator> originalOperatorsExternal = Collections.unmodifiableMap(originalOperators);
	private Type oldTypeInteger;

    public ContextValuePARAM(ContextValue contextValue) {
        assert contextValue != null;
        this.contextValue = contextValue;
		this.typeFractionBigInteger = makeUnique(new TypeFractionBigInteger(this));
		this.typeReal = makeUnique(this.typeFractionBigInteger);
        this.typeFunctionPolynomial = makeUnique(new TypeFunctionPolynomial(this));
        this.typeFunction = makeUnique(new TypeFunctionPolynomialFraction(this.typeFunctionPolynomial));
    }

    public Value newParameterFunction(String parameter) {
    	assert parameter != null;
    	addParameter(parameter);
    	ValueFunction result = typeFunction.newValue();
    	result.setParameter(parameter);
    	return result;
    }
    
    public ContextValue getContextValue() {
        return this.contextValue;
    }
    
    public TypeReal getTypeReal() {
    	return this.typeReal;
    }
    
    public TypeFunction getTypeFunction() {
		return this.typeFunction;
	}

    public TypeFunctionPolynomial getTypeFunctionPolynomial() {
    	return this.typeFunctionPolynomial;
    }
    
    public List<Object> getParameters() {
		return parametersExternal;
	}
    
    public ValueFunction newFunction() {
    	return typeFunction.newValue();
    }
    
    public ValueReal newValueReal() {
    	return typeReal.newValue();
    }

	public void addParameter(Object parameter) {
		assert parameter != null;
    	if (!this.parameterToNumber.containsKey(parameter)) {
    		this.parameters.add(parameter);
    		this.parameterToNumber.put(parameter, parameterToNumber.size());
    		this.typeFunction.addParameter(parameter);
    	}
	}

	public int getNumParameters() {
		return this.parameters.size();
	}

	public int getParameterNumber(Object parameter) {
		assert parameter != null;
		if (!parameterToNumber.containsKey(parameter)) {
			addParameter(parameter);
		}
		return this.parameterToNumber.get(parameter);
	}

	public Object getParameter(int parameterNumber) {
		assert parameterNumber >= 0;
		assert parameterNumber < parameters.size();
		return parameters.get(parameterNumber);
	}

	public boolean isParameter(Object parameter) {
		assert parameter != null;
		return this.parameterToNumber.containsKey(parameter);
	}
	
	public TypeArray getTypeArray(Type entryType) {
		assert entryType != null;
		if (entryType instanceof TypeFractionBigInteger) {
			return makeUnique(new TypeArrayFractionBigInteger((TypeFractionBigInteger) entryType));
		} else {
			return entryType.getTypeArray();
		}
	}

    public <T extends Type> T makeUnique(T type) {
        assert type != null;
        // TODO HACK for deserialisation
        if (types == null) {
            return type;
        }
        @SuppressWarnings("unchecked")
        T result = (T) types.get(type);
        if (result == null) {
            types.put(type, type);
            result = type;
        }
        return result;
    }

	public void setOriginalOperators(Map<String, Operator> originalOperators) {
		this.originalOperators.clear();
		this.originalOperators.putAll(originalOperators);
	}
	
	public Map<String, Operator> getOriginalOperators() {
		return this.originalOperatorsExternal;
	}

	public void setOldTypeInteger(Type oldTypeInteger) {
		assert oldTypeInteger != null;
		this.oldTypeInteger = oldTypeInteger;
	}
	
	public Type getOldTypeInteger() {
		return oldTypeInteger;
	}
}

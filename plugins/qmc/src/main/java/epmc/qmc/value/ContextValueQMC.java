package epmc.qmc.value;

import java.util.HashMap;
import java.util.Map;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueReal;

public final class ContextValueQMC {
    private final ContextValue contextValue;
    private final Map<Type,Type> types = new HashMap<>();
    private final TypeSuperOperator typeSuperOperator;
    private final TypeComplex typeComplex;

    public ContextValueQMC(ContextValue contextValue) {
        this.contextValue = contextValue;
        this.typeComplex = makeUnique(new TypeComplex(contextValue));
        this.typeSuperOperator = makeUnique(new TypeSuperOperator(this));
        assert typeComplex != null;
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
    
    public TypeSuperOperator getTypeSuperOperator(int dimensions) {
        assert dimensions > 0 || dimensions == -1 : dimensions;
        return makeUnique(new TypeSuperOperator(this, dimensions));
    }
    
    public TypeSuperOperator getTypeSuperOperator() {
        return typeSuperOperator;
    }
    
    public TypeComplex getTypeComplex() {
        assert typeComplex != null;
        return typeComplex;
    }
    
    public Value newComplex(int real, int imaginary) {
        return getTypeComplex().newValue(UtilValue.newValue(TypeReal.get(contextValue), real),
                UtilValue.newValue(TypeReal.get(contextValue), imaginary));
    }
    
    public Value newComplex(ValueReal real, ValueReal imaginary) {
        assert real != null;
        assert imaginary != null;
        return getTypeComplex().newValue(real, imaginary);
    }
    
    public ContextValue getContextValue() {
        return contextValue;
    }
}

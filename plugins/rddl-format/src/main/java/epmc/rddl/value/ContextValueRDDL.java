package epmc.rddl.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import epmc.value.ContextValue;
import epmc.value.Type;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class ContextValueRDDL {
    private final ContextValue contextValue;
    private final Map<Type,Type> uniqueMap = new THashMap<>();
    private final TObjectIntMap<String> enumConstantsToNumber = new TObjectIntHashMap<>();
    private final List<String> numberToEnumConstant = new ArrayList<>();
    private final List<String> numberToEnumConstantExternal = Collections.unmodifiableList(numberToEnumConstant);

    public ContextValueRDDL(ContextValue context) {
        this.contextValue = context;
    }

    public Type getTypeDistributionFinite(int i, Type typeWeight) {
        return makeUnique(new TypeDistributionFiniteExplicit(contextValue, i, typeWeight));
    }
    
    public Type getTypeNormal() {
        return makeUnique(new TypeDistributionNormal(contextValue));
    }

    private Type makeUnique(Type type) {
        assert type != null;
        Type result = uniqueMap.get(type);
        if (result == null) {
            result = type;
            uniqueMap.put(result, result);
        }
        return result;
    }

    public ContextValue getContextValue() {
        return contextValue;
    }
    
    public void addEnumConstant(String constant) {
        assert constant != null;
        if (enumConstantsToNumber.containsKey(constant)) {
            return;
        }
        numberToEnumConstant.add(constant);
        enumConstantsToNumber.put(constant, enumConstantsToNumber.size());
    }
    
    public int getEnumConstantNumber(String constant) {
        assert constant != null;
        assert enumConstantsToNumber.containsKey(constant);
        return enumConstantsToNumber.get(constant);
    }

    public TypeRDDLEnum newTypeEnum(String name, List<String> values) {
        return new TypeRDDLEnum(this, name, values);
    }
    
    public Type newTypeObject(String name) {
        return new TypeRDDLObject(this, name);
    }

    public List<String> getNumberToEnumConstant() {
        return numberToEnumConstantExternal;
    }

}

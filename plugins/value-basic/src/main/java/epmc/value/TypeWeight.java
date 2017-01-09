package epmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;

public interface TypeWeight extends TypeAlgebra {
    static TypeWeight get(ContextValue context) {
        assert context != null;
        return context.getType(TypeWeight.class);
    }
    
    static void set(TypeWeight type) {
        assert type != null;
        ContextValue context = type.getContext();
        context.setType(TypeWeight.class, context.makeUnique(type));
    }
    
    static boolean isWeight(Type type) {
        return type instanceof TypeWeight;
    }
    
    static TypeWeight asWeight(Type type) {
    	if (isWeight(type)) {
    		return (TypeWeight) type;
    	} else {
    		return null;
    	}
    }
    
    ValueAlgebra getPosInf();

    ValueAlgebra getNegInf();
    
    ValueAlgebra getZero();
    
    ValueAlgebra getOne();
    
    @Override
    ValueAlgebra newValue();
}

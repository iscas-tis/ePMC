package epmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;

public interface TypeReal extends TypeNumber, TypeWeight, TypeWeightTransition {
    static TypeReal get(ContextValue context) {
        assert context != null;
        return context.getType(TypeReal.class);
    }
    
    static void set(TypeReal type) {
        assert type != null;
        ContextValue context = type.getContext();
        context.setType(TypeReal.class, type);
    }
    
    static boolean isReal(Type type) {
    	return type instanceof TypeReal;
    }
    
    static TypeReal asReal(Type type) {
    	if (isReal(type)) {
    		return (TypeReal) type;
    	} else {
    		return null;
    	}
    }
    
    @Override
    TypeArrayReal getTypeArray();

    @Override
    ValueReal newValue();

    ValueReal getUnderflow();

    ValueReal getOverflow();
        
    ValueReal getPosInf();

    ValueReal getNegInf();
    
    ValueReal getZero();
    
    ValueReal getOne();
    
}

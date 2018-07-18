package epmc.param.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;

public interface TypeFunction extends TypeWeightTransition, TypeWeight {
    interface Builder {
        Builder setParameters(ParameterSet parameters);
        
        TypeFunction build();
    }
    
    static TypeFunction get() {
        return ContextValue.get().getType(TypeFunction.class);
    }

    static void set(TypeFunction type) {
        assert type != null;
        ContextValue.get().setType(TypeFunction.class, type);
    }

    static boolean is(Type type) {
        return type instanceof TypeFunction;
    }

    static TypeFunction as(Type type) {
        if (is(type)) {
            return (TypeFunction) type;
        } else {
            return null;
        }
    }

    @Override
    ValueFunction newValue();

	ParameterSet getParameterSet();
}

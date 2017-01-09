package epmc.value;

import epmc.value.ContextValue;

public interface TypeWeightTransition extends TypeAlgebra {
    static TypeWeightTransition get(ContextValue context) {
        assert context != null;
        return context.getType(TypeWeightTransition.class);
    }
    
    static void set(TypeWeightTransition type) {
        assert type != null;
        ContextValue context = type.getContext();
        context.setType(TypeWeightTransition.class, context.makeUnique(type));
    }
}

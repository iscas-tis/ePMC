package epmc.param.value.rational;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeReal;

public interface TypeRational extends TypeReal {
    static boolean is(Type type) {
        return type instanceof TypeRational;
    }

    static TypeRational as(Type type) {
        if (type instanceof TypeRational) {
            return (TypeRational) type;
        } else {
            return null;
        }
    }

    static TypeRational get() {
        return ContextValue.get().getType(TypeRational.class);
    }

    static void set(TypeRational type) {
        assert type != null;
        ContextValue.get().setType(TypeRational.class,
                ContextValue.get().makeUnique(type));
    }

    @Override
    ValueRational newValue();
    
    @Override
    TypeArrayRational getTypeArray();
}

package epmc.param.plugin;

import epmc.param.value.rational.TypeRational;
import epmc.value.ContextValue;
import epmc.value.TypeAlgebra;

public final class TypeProviderRational implements TypeProvider {
    public final static String IDENTIFIER = "rational";

    @Override
    public TypeAlgebra provide() {
        return ContextValue.get().makeUnique(TypeRational.get());
    }
}

package epmc.param.plugin;

import epmc.param.value.rational.TypeRational;
import epmc.value.ContextValue;
import epmc.value.TypeAlgebra;
import epmc.value.TypeInterval;

public final class TypeProviderIntervalRational implements TypeProvider {
    public final static String IDENTIFIER = "interval-rational";

    @Override
    public TypeAlgebra provide() {
        return ContextValue.get().makeUnique(new TypeInterval(TypeRational.get()));
    }
}

package epmc.param.plugin;

import epmc.value.ContextValue;
import epmc.value.TypeAlgebra;
import epmc.value.TypeDouble;
import epmc.value.TypeInterval;

public final class TypeProviderIntervalDouble implements TypeProvider {
    public final static String IDENTIFIER = "interval-double";

    @Override
    public TypeAlgebra provide() {
        return ContextValue.get().makeUnique(new TypeInterval(TypeDouble.get()));
    }
}

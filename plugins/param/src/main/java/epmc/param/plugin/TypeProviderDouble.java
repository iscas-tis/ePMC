package epmc.param.plugin;

import epmc.value.TypeAlgebra;
import epmc.value.TypeDouble;

public final class TypeProviderDouble implements TypeProvider {
    public final static String IDENTIFIER = "double";

    @Override
    public TypeAlgebra provide() {
        return TypeDouble.get();
    }
}

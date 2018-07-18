package epmc.param.plugin;

import epmc.value.TypeAlgebra;

@FunctionalInterface
public interface TypeProvider {
    TypeAlgebra provide();
}

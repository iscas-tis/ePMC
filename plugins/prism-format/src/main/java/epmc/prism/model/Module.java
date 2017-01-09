package epmc.prism.model;

import java.util.Map;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.jani.model.type.JANIType;
import epmc.value.Type;

public interface Module {
    Positional getPositional();

    default Map<Expression,JANIType> getVariables() {
        assert false;
        return null;
    }
    
    default Map<Expression, Expression> getInitValues() {
        assert false;
        return null;
    }

    default Set<Expression> getAlphabet() {
        assert false;
        return null;
    }

    default String getName() {
        assert false;
        return null;
    }

    default void checkExpressionConsistency(
            Map<Expression, JANIType> globalVariables,
            Map<Expression, Type> types) throws EPMCException {
        assert false;
    }

    default Module replaceFormulas(Map<Expression, Expression> specifiedConsts) {
        assert false;
        return null;
    }
    
    default boolean isCommands() {
        return this instanceof ModuleCommands;
    }
    
    default ModuleCommands asCommands() {
        return (ModuleCommands) this;
    }    
}

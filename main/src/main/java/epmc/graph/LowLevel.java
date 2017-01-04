package epmc.graph;

import java.io.Closeable;

import epmc.error.EPMCException;
import epmc.expression.ExpressionToType;
import epmc.value.ContextValue;

/**
 * Low-level representation of a model.
 * 
 * @author Ernst Moritz Hahn
 *
 */
public interface LowLevel extends Closeable, ExpressionToType {
    /**
     * Create new set of initial states of this low-level model.
     * 
     * @return new set of initial states
     * @throws EPMCException thrown in case of problems during creation
     */
    StateSet newInitialStateSet() throws EPMCException;

    ContextValue getContextValue();
    
    @Override
    void close();
}

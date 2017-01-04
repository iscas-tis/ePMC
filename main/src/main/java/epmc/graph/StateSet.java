package epmc.graph;

import java.io.Closeable;

import epmc.error.EPMCException;
import epmc.value.ContextValue;

//TODO complete documentation

public interface StateSet extends Closeable, Cloneable {

    ContextValue getContextValue();

    int size();
 
    @Override
    void close();

    boolean isSubsetOf(StateSet states) throws EPMCException;
    
    StateSet clone();

    default boolean isEmpty() {
        return size() == 0;
    }
}

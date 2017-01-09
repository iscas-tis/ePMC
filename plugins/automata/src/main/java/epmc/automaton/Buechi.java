package epmc.automaton;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.explicit.GraphExplicit;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Value;

// TODO complete documentation
// TODO change interface to the one of Automata, because
// 1.) the current interface is irritating
// 2.) the change would allow composing Buechi automata with models, e.g. for
//     non-probabilistic model checking
// TODO move implementation (by SPOT) to separate plugin
// TODO if we switch to GPL, use SPOT library rather than SPOT command line tool

public interface Buechi {
    /* methods to be implemented by implementing classes */
    String getIdentifier();    

    boolean isDeterministic();
    
    Expression[] getExpressions();

    int getNumLabels();
    
    int getTrueState();

    GraphExplicit getGraph();
    
    void query(Value[] get) throws EPMCException;
    
    
    /* default methods */
    
    default ContextValue getContextValue() {
        return getGraph().getContextValue();
    }
    
    default int getNumStates() {
        return getGraph().getNumNodes();
    }
    
    default Options getOptions() {
        return getGraph().getOptions();
    }
}

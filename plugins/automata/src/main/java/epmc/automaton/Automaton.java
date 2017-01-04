package epmc.automaton;

import java.io.Closeable;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

// TODO transform this EPMC part into plugin, as it is not needed for many use cases

// TODO complete documentation

/**
 * Interface to be implemented by an automaton.
 * The interface is provided to have a general means to compose automata to
 * models so thus to obtain a product. For stochastic models, for this to work
 * in general the automaton should be deterministic. As there are some
 * exceptions for the case of unambiguous automata with Markov chains,
 * nondeterministic automata are also supported. The interface also serves to
 * allow an {@link Expression} to be transformed into an automaton by an
 * according plugin, in order to export the automaton from the tool.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Automaton extends Closeable {
    interface Builder {
        default String getIdentifier() {
        	return null;
        }
        
        default Builder setBuechi(Buechi buechi) {
        	return this;
        }
        
        default Builder setExpression(ContextValue contextValue, Expression expression, Expression[] expressions) throws EPMCException {
            ValueBoolean negate = TypeBoolean.get(contextValue).newValue(false);
            Buechi buechi = UtilAutomaton.newBuechi(expression, expressions, true, negate);
            setBuechi(buechi);
            return this;
        }
        
        default Builder setExpression(ContextValue contextValue, Expression expression) throws EPMCException {
            Expression[] expressions = UtilAutomaton.collectLTLInner(expression).toArray(new Expression[0]);
            setExpression(contextValue, expression, expressions);
            return this;
        }

        Automaton build() throws EPMCException;
    }

    int getInitState();
   
    int getNumStates();

    Object numberToState(int number);

    Object numberToLabel(int number);
    
    Expression[] getExpressions();
    
    void queryState(Value[] modelState, int automatonState)
            throws EPMCException;
    
    default String getIdentifier() {
        return null;
    }
    
    default Buechi getBuechi() {
        assert false;
        return null;
    }
        
    default int getSuccessorState() {
        assert false;
        return -1;
    }
    
    default int getSuccessorLabel() {
        assert false;
        return -1;
    }
    
    default int getNumberSuccessors() {
        assert isDeterministic();
        return 1;
    }

    default int getSuccessorState(int successorNumber) {
        assert successorNumber >= 0;
        assert successorNumber < 1;
        return getSuccessorState();
    }

    default int getSuccessorLabel(int successorNumber) {
        assert successorNumber >= 0;
        assert successorNumber < 1;
        return getSuccessorLabel();
    }
    
    ContextValue getContextValue();
    
    default Options getOptions() {
    	return getContextValue().getOptions();
    }
    
    @Override
    default void close() {
    }
    
    default boolean isDeterministic() {
        return true;
    }
}

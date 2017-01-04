package epmc.propertysolverltlfg.automaton;

import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.util.BitSet;
/**
 * This is an abstract class for Finite Deterministic Automata
 * can handle transition-based acceptances
 * */
public class AutomatonDDDTA extends AutomatonDDDFA { 
    /* constructors */
    public AutomatonDDDTA(
    		ExpressionToDD expressionToDD
    		, AutomatonRabin rabin
    		, DD states
    		) throws EPMCException {
    	super(expressionToDD, rabin, states);
    }
    
    @Override
	void putStateLabels(VariableDD states, BitSet labeling, int state) 
    		throws EPMCException {   }
    
    @Override
	void putEdgeLabels(BitSet labeling, DD guard) throws EPMCException {
        for (int labelNr = labeling.nextSetBit(0)
        		; labelNr >= 0
        		; labelNr = labeling.nextSetBit(labelNr + 1)) {
            DD label = labelDDs.get(labelNr);
            label = label.orWith(guard.clone());
            labelDDs.set(labelNr, label);
        }
    }
}

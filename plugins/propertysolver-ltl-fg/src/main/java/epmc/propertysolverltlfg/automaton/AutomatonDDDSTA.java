package epmc.propertysolverltlfg.automaton;

import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.util.BitSet;

/**
 * Automaton class for deterministic state-based and transition-based 
 * automaton. */

public class AutomatonDDDSTA extends AutomatonDDDFA {

    /* constructors */
    public AutomatonDDDSTA(
    		ExpressionToDD expressionToDD
    		, AutomatonRabin rabin
    		, DD states)
            throws EPMCException {
    	super(expressionToDD, rabin, states);
    }

 
    
    public DD getFinStates(int labelID) throws EPMCException {
    	DD result = this.labelStates.get(labelID);
    	if(result == null) {
    		this.labelStates.put(labelID, contextDD.newConstant(false));
    		result = this.labelStates.get(labelID);
    	}
    	return result.clone();
    }
  
	@Override
	void putStateLabels(VariableDD states, BitSet labeling, int state)
			throws EPMCException {
        for(int labelNr = labeling.nextSetBit(0)
        		; labelNr >= 0 
        		; labelNr = labeling.nextSetBit(labelNr + 1)) {
        	DD label = labelStates.get(labelNr);
        	if(label == null) {
        		label = contextDD.newConstant(false);
        	}
        	label = label.or(states.newIntValue(0, state));
        	labelStates.put(labelNr, label);
        }
	}

	@Override
	void putEdgeLabels(BitSet labeling, DD guard) throws EPMCException {
        for(int labelNr = labeling.nextSetBit(0)
        		; labelNr >= 0 
        		; labelNr = labeling.nextSetBit(labelNr + 1)) {
        	DD label = labelDDs.get(labelNr);
        	label = label.orWith(guard.clone());
        	labelDDs.set(labelNr, label);
        }
	}
    
}

package epmc.propertysolverltlfg.automaton;

import epmc.error.EPMCException;
import epmc.util.BitSet;
import epmc.value.ContextValue;

/**
 * The automaton class for deterministic Transition-based Automaton 
 * use edges as accepting conditions 
 * */
public class AutomatonDTA extends AutomatonDFA {

	public final static String IDENTIFIER = "dta";
   
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
    
    
    public AutomatonDTA(AutomatonRabin buechi) throws EPMCException {
    	super(buechi);
    }
	
	@Override
	public boolean isDeterministic() {
        return false;
    }

	@Override
	void putStateLabels(AutomatonDFAStateImpl state) throws EPMCException {	}


	@Override
	void putEdgeLabels(BitSet labeling) {
		AutomatonDFALabelImpl lab = new AutomatonDFALabelImpl(labeling);
		succLabel = makeUnique(lab);		
	}


	@Override
	public ContextValue getContextValue() {
		return this.automaton.getContextValue();
	}

}

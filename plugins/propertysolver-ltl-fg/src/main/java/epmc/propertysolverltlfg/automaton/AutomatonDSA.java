package epmc.propertysolverltlfg.automaton;
import epmc.automaton.AutomatonLabelUtil;
import epmc.error.EPMCException;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import epmc.value.ContextValue;

/**
 * The automaton class for formula-based deterministic automaton
 * and state-based Rabin automaton
 * use states or formulas as accepting conditions 
 * */
public class AutomatonDSA extends AutomatonDFA {

	public final static String IDENTIFIER = "dsa";
   
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
    
    public AutomatonDSA(AutomatonRabin buechi) throws EPMCException {
    	super(buechi);
    	succLabel = new AutomatonDFALabelImpl(
    			new BitSetUnboundedLongArray(automaton.getNumNodes())
    			);
    }
    
    @Override
    public AutomatonLabelUtil numberToLabel(int number) {
        return succLabel;
    }

	@Override
	void putStateLabels(AutomatonDFAStateImpl state) throws EPMCException {	}
	@Override
	void putEdgeLabels(BitSet labeling) {}

	@Override
	public ContextValue getContextValue() {
		return this.automaton.getContextValue();
	}

}

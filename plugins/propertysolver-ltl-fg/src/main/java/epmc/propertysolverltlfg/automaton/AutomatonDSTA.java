package epmc.propertysolverltlfg.automaton;

import java.util.HashMap;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.NodeProperty;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

/**
 * The automaton class for Transition-based Generalized Rabin Automaton 
 * use edges as accepting conditions 
 * */
public class AutomatonDSTA extends AutomatonDTA {

	public final static String IDENTIFIER = "dstgra";
    private final Map<Integer, BitSet> labelStates = new HashMap<>();
    private final BitSet visited ;
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
    
    
    public AutomatonDSTA(AutomatonRabin buechi) throws EPMCException {
    	super(buechi);
        this.visited = new BitSetUnboundedLongArray(automaton.getNumNodes());
    }
    
	@Override
	void putStateLabels(AutomatonDFAStateImpl state) throws EPMCException {	
        if(! visited.get(state.getNumber())) {
        	visited.set(state.getNumber());
            NodeProperty nodeLabel = automaton.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
            RabinStateUtil stateUtil = nodeLabel.getObject();
            BitSet stateLabel = stateUtil.getLabeling();
            for(int labelNr = stateLabel.nextSetBit(0); 
            		labelNr >= 0 ; 
            		labelNr = stateLabel.nextSetBit(labelNr + 1)) {
            	BitSet lab = getFinStates(labelNr);
            	lab.set(state.getNumber());
            	labelStates.put(labelNr,  lab);
            }
        }
	}
    
    public BitSet getFinStates(int labelID) throws EPMCException {
    	BitSet result = this.labelStates.get(labelID);
    	if(result == null) {
    		this.labelStates.put(labelID
    				, new BitSetUnboundedLongArray(automaton.getNumNodes())
    		);
    		result = this.labelStates.get(labelID);
    	}
    	return result;
    }

}

package epmc.propertysolverltlfg;

import java.util.List;

import epmc.algorithms.dd.ComponentsDD;
import epmc.automaton.AutomatonDD;
import epmc.automaton.ProductGraphDD;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.propertysolverltlfg.automaton.AcceptanceCondition;
import epmc.propertysolverltlfg.automaton.AcceptanceLabel;
import epmc.propertysolverltlfg.automaton.AutomatonDDDSA;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
/**
 * input formula will be LTL, but automaton should be GRA
 * and support HOA format
 * @author Yong Li
 */
public final class PropertySolverDDLTLGRA extends PropertySolverDDLTL {
	
    public final static String IDENTIFIER = "ltl-gra-dd";
    
	@Override
	AutomatonType getAutoType() {
		return AutomatonType.GRA;
	}    
    
    @Override
	AutomatonDD constructRabinDD(AutomatonRabin rabin, DD modelStates) throws EPMCException {
    	return new AutomatonDDDSA(expressionToDD, rabin, modelStates);
    }
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }    
    
    
    private boolean decideComponentMDPLeaf(AutomatonDDDSA automaton, DD leafSCC
    		, List<AcceptanceLabel> infs) throws EPMCException {
    	boolean accepted = true;

    	for(AcceptanceLabel inf : infs) {
    		DD infStates = getLableDD(inf, automaton);
    		infStates = infStates.andWith(leafSCC.clone());
    		if(infStates.isFalseWith()) {
    			accepted = false;
    			break;
    		}
    	}

    	return accepted;
    }
    
    
    private DD getLableDD(AcceptanceLabel label, AutomatonDDDSA automatonDD) 
    		throws EPMCException {
        ContextDD contextDD = this.expressionToDD.getContextDD();
    	if(label == null || label.isFalse()) return contextDD.newConstant(false);
        
    	if(label.isTrue()) return contextDD.newConstant(true);
    	DD labelDD = automatonDD.getLabelVar(label.getStateSet()).clone();
    	if(label.isNegated()) labelDD = labelDD.notWith();
    	
    	return labelDD;
    }
    
    @Override
	DD computeAcceptedMECs(ProductGraphDD product, AutomatonDD automatonDD, DD nodeSpace) 
    		throws EPMCException {
    	
    	AutomatonDDDSA automaton = (AutomatonDDDSA) automatonDD;
        List<AcceptanceCondition> labels = automaton.getBuechi().getAcceptances();
        DD oneNodes = getContextDD().newConstant(false);
    	int numECCs = 0;
        for(AcceptanceCondition lab : labels) {
        	AcceptanceLabel fin = lab.getFiniteStates();
        	DD limit = getLableDD(fin, automaton).notWith();
        	limit = limit.andWith(nodeSpace.clone());
        	ComponentsDD sccs = new ComponentsDD(product, limit, skipTransient);
            for (DD component = sccs.next(); component != null; component = sccs.next()) {
            	if(decideComponentMDPLeaf(automaton, component, lab.getInfiniteStates())) {
            		oneNodes = oneNodes.orWith(component);
            		++ numECCs;
            	}
            }
            limit.dispose();
            sccs.close();
        }
        log.send(MessagesLTLTGRA.LTL_TGRA_COMPUTING_END_COMPONENTS_DONE, numECCs);
		return oneNodes;
    }


}

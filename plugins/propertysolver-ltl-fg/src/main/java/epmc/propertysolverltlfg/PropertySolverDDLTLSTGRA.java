package epmc.propertysolverltlfg;

import java.util.List;

import epmc.automaton.AutomatonDD;
import epmc.automaton.ProductGraphDD;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.error.EPMCException;
import epmc.propertysolverltlfg.algorithm.ComponentsDD;
import epmc.propertysolverltlfg.automaton.AcceptanceCondition;
import epmc.propertysolverltlfg.automaton.AcceptanceLabel;
import epmc.propertysolverltlfg.automaton.AutomatonDDDSTA;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
/**
 * input formula will be LTL, but automaton should be tGRA
 * and support HOA format
 * @author Yong Li
 */
public final class PropertySolverDDLTLSTGRA extends PropertySolverDDLTL {
    public final static String IDENTIFIER = "ltl-stgra-dd";
    
	@Override
	AutomatonType getAutoType() {
		return AutomatonType.STGRA;
	} 
    
    @Override
	AutomatonDD constructRabinDD(AutomatonRabin rabin, DD modelStates) 
    		throws EPMCException {
    	return new AutomatonDDDSTA(expressionToDD, rabin, modelStates);
    }
    
    private DD restrictTrans(Permutation nextToPres, DD edges, DD nodes)
            throws EPMCException {
        edges = edges.and(nodes);
        DD permNodes = nodes.permute(nextToPres);
        edges = edges.andWith(permNodes);
        return edges;
    }
    
    private boolean decideComponentMDPLeaf(AutomatonDDDSTA automaton, DD leafSCC
    		, DD trans, Permutation nextToPres, List<AcceptanceLabel> infs) throws EPMCException {
    	boolean accepted = true;
    	// first, we need to restrict all transitions in leafSCC
    	DD transInLeaf = restrictTrans(nextToPres, trans.clone(), leafSCC);
    	for(AcceptanceLabel inf : infs) {
    		DD infTrans = getLableDD(inf, automaton);
    		infTrans = infTrans.andWith(transInLeaf.clone());
    		if(infTrans.isFalseWith()) {
    			accepted = false;
    			break;
    		}
    	}
    	transInLeaf.dispose();
    	return accepted;
    }

    private DD getLableDD(AcceptanceLabel label, AutomatonDDDSTA automatonDD) 
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
    	AutomatonDDDSTA automaton = (AutomatonDDDSTA)automatonDD;
        List<AcceptanceCondition> labels = automaton.getBuechi().getAcceptances();
        DD oneNodes = getContextDD().newConstant(false);
        DD trans = product.getTransitions();
        int numECCs = 0;
        ContextDD contextDD = this.expressionToDD.getContextDD();
        if(this.mecComputation) {
        	log.send(MessagesLTLTGRA.LTL_TGRA_FIRST_MEC_COMPONENTS);        	
        	ComponentsDD sccs = new ComponentsDD(product, nodeSpace, contextDD.newConstant(true), false , skipTransient);
            for (DD component = sccs.next(); component != null; component = sccs.next()) {
            	for(AcceptanceCondition lab : labels) {
            		AcceptanceLabel fin = lab.getFiniteStates();
                	DD limit = getLableDD(fin, automaton).notWith();
                	if(fin != null && ! fin.isFalse() && ! fin.isTrue())
                		limit = limit.andWith(automaton.getFinStates(fin.getStateSet()));
                	if(limit.isFalse()) {
                		limit.dispose();
                		continue;
                	}
                	ComponentsDD sccInMEC = new ComponentsDD(product, component, limit, skipTransient);
                    for (DD scc = sccInMEC.next(); scc != null; scc = sccInMEC.next()) {
                    	if(decideComponentMDPLeaf(automaton, scc, trans
                    			, product.getSwapPresNext(), lab.getInfiniteStates())) {
                    		oneNodes = oneNodes.orWith(scc);
                    		++ numECCs;
                    	}
                    }
                    limit.dispose();
                    sccInMEC.close();
            	}
            }
            sccs.close();
        }else {
            for(AcceptanceCondition lab : labels) {
        		AcceptanceLabel fin = lab.getFiniteStates();
            	DD limit = getLableDD(fin, automaton).notWith();
            	if(fin != null && ! fin.isFalse() && ! fin.isTrue())
            		limit = limit.andWith(automaton.getFinStates(fin.getStateSet()));
            	ComponentsDD sccs = new ComponentsDD(product, nodeSpace, limit, skipTransient);
                for (DD component = sccs.next(); component != null; component = sccs.next()) {
                	if(decideComponentMDPLeaf(automaton, component, trans
                			, product.getSwapPresNext(), lab.getInfiniteStates())) {
                		oneNodes = oneNodes.orWith(component);
                		++ numECCs;
                	}
                }
                limit.dispose();
                sccs.close();
            }
        }
        
        log.send(MessagesLTLTGRA.LTL_TGRA_COMPUTING_END_COMPONENTS_DONE, numECCs);
		return oneNodes;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }    

}

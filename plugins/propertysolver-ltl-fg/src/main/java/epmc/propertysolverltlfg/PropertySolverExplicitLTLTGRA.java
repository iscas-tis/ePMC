package epmc.propertysolverltlfg;

import java.util.List;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.algorithms.explicit.EndComponents;
import epmc.automaton.Automaton;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.propertysolverltlfg.algorithm.ComponentsExplicitImpl;
import epmc.propertysolverltlfg.automaton.AcceptanceCondition;
import epmc.propertysolverltlfg.automaton.AcceptanceLabel;
import epmc.propertysolverltlfg.automaton.AutomatonDTA;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
import epmc.propertysolverltlfg.automaton.RabinTransitionUtil;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import epmc.value.ValueObject;


/**
 * input formula will be LTL, but automaton should be tGRA
 * and support HOA format
 * @author Yong Li
 */
//TODO: product construction, need to distinguish states and actions
public final class PropertySolverExplicitLTLTGRA extends PropertySolverExplicitLTL {
	public final static String IDENTIFIER = "ltl-tgra-explicit";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	AutomatonType getAutoType() {
		return AutomatonType.TGRA;
	} 

	@Override
	Automaton constructRabinExplicit(AutomatonRabin rabin)
			throws EPMCException {
		return new AutomatonDTA(rabin);
	}
	
	@Override
	protected boolean isTransitionBased() {
		return true;
	}
	
	@Override
	protected boolean isStateBased() {
		return false;
	}

	@Override
	BitSet computeAcceptedMECs(GraphExplicit graph, Automaton automatonExplicit)
			throws EPMCException {
		AutomatonDTA automaton = (AutomatonDTA)automatonExplicit;
//		System.out.println(automaton.toString());
//		System.out.println("product: " + graph.toString());
        BitSet acc = new BitSetUnboundedLongArray();        
        int numComponents = 0;
        if(! this.mecComputation) {

			List<AcceptanceCondition> labels = automaton.getAccConditions();
			for (AcceptanceCondition label : labels) {
//				System.out.println("label: " + label);
				int finEdge;
				AcceptanceLabel fin = label.getFiniteStates();
				if (fin == null || fin.isFalse())
					finEdge = -1;
				else if (fin.isTrue()) // 
					finEdge = -2;
				else {
					finEdge = fin.getStateSet();
				}
                
				if (finEdge == -2)
					continue;
				BitSet existing = new BitSetUnboundedLongArray(graph.getNumNodes());
				existing.set(0, graph.getNumNodes(), true);
//				ComponentsEX sccs = new ComponentsEX(graph, existing, finEdge, true);
//				sccs.computeSCCs();
				EndComponents sccs = new ComponentsExplicitImpl(graph, existing, finEdge, true);
				for(BitSet leafSCC = sccs.next(); leafSCC != null ; leafSCC = sccs.next()) {
					if(decideComponentRabinMDPLeaf(graph, leafSCC, automaton,
							finEdge, label.getInfiniteStates())) {
						acc.or(leafSCC);
						++ numComponents;
					}
				}
			}
		} else {
			ComponentsExplicit components = UtilAlgorithms.newComponentsExplicit();
			EndComponents sccs = components.stronglyConnectedComponents(graph);
			
			for(BitSet scc = sccs.next(); scc != null ; scc = sccs.next()) {
				
				List<AcceptanceCondition> labels = automaton.getAccConditions();
				
				for (AcceptanceCondition label : labels) {
//					System.out.println("label: " + label);
					int finEdge;
					AcceptanceLabel fin = label.getFiniteStates();
					if (fin == null || fin.isFalse())
						finEdge = -1;
					else if (fin.isTrue()) // 
						finEdge = -2;
					else {
						finEdge = fin.getStateSet();
					}
	                
					if (finEdge == -2)
						continue;
					EndComponents mecs = new ComponentsExplicitImpl(graph, scc, finEdge, true);
					for(BitSet leafSCC = mecs.next(); leafSCC != null ; leafSCC = mecs.next()) {
						if(decideComponentRabinMDPLeaf(graph, leafSCC, automaton,
								finEdge, label.getInfiniteStates())) {
							acc.or(leafSCC);
							++ numComponents;
						}
					}
				}
				
			}
		}
        log.send(MessagesLTLTGRA.LTL_TGRA_COMPUTING_END_COMPONENTS_DONE, numComponents);
		return acc;
	}
    // may just consider edge from state to action
	private boolean decideComponentRabinMDPLeaf(GraphExplicit graph,
			BitSet leafSCC, AutomatonDTA automaton, int finEdge,
			List<AcceptanceLabel> infiniteStates) throws EPMCException {
		if(infiniteStates.size() == 0) return true;
		BitSet labels = new BitSetUnboundedLongArray(infiniteStates.size());
		labels.set(0, infiniteStates.size(), true);
		EdgeProperty edgeProp = graph.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
		for(int node = leafSCC.nextSetBit(0); node >= 0 ; node = leafSCC.nextSetBit(node + 1)) {
			graph.queryNode(node);   
			for(int succNr = 0; succNr < graph.getNumSuccessors(); succNr ++) {
				RabinTransitionUtil trans = ValueObject.asObject(edgeProp.get(succNr)).getObject();
				if(finEdge != -1 && trans.getLabeling().get(finEdge)) continue;
				for(int labelNr = 0; labelNr < infiniteStates.size() ; labelNr ++) {
					if(infiniteStates.get(labelNr).isFalse()) return false;
					if(infiniteStates.get(labelNr).isTrue()) return true;
					int inf = infiniteStates.get(labelNr).getStateSet();
					if(trans.getLabeling().get(inf)) {
						labels.clear(labelNr);
					}
				}
			}
		}
		if(labels.isEmpty()) return true;
		return false;
	}


}

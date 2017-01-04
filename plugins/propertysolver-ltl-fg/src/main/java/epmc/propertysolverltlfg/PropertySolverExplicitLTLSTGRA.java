package epmc.propertysolverltlfg;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import epmc.automaton.Automaton;
import epmc.automaton.AutomatonStateUtil;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.propertysolverltlfg.algorithm.ComponentsEX;
import epmc.propertysolverltlfg.automaton.AcceptanceCondition;
import epmc.propertysolverltlfg.automaton.AcceptanceLabel;
import epmc.propertysolverltlfg.automaton.AutomatonDSTA;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
import epmc.propertysolverltlfg.automaton.RabinTransitionUtil;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import epmc.value.ValueObject;


/**
 * input formula will be LTL, but automaton should be part GRA
 * and support HOA format
 * @author Yong Li
 */
//TODO: since currently it seems that only nondeterministic automata support
// transition-based labeling, at the same time, they do not support state-based
// labeling. Maybe we can use nondeterministic automata and then just get 
//state-information from the automata
public final class PropertySolverExplicitLTLSTGRA extends PropertySolverExplicitLTL {
	public final static String IDENTIFIER = "ltl-stgra-explicit";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
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
	AutomatonType getAutoType() {
		return AutomatonType.STGRA;
	} 
	
	@Override
	Automaton constructRabinExplicit(AutomatonRabin rabin)
			throws EPMCException {
		return new AutomatonDSTA(rabin);
	}

	
	@Override
	BitSet computeAcceptedMECs(GraphExplicit graph, Automaton automatonExplicit) 
			throws EPMCException {
//		System.out.println(graph.toString());
		AutomatonDSTA automaton = (AutomatonDSTA)automatonExplicit;
        BitSet acc = new BitSetUnboundedLongArray(graph.getNumNodes());        
        int numComponents = 0;
        
        if(! this.mecComputation) {
            Map<Integer, BitSet> sccsInMecs = new HashMap<>();
			NodeProperty fromAuto = graph.getNodeProperty(CommonProperties.NODE_AUTOMATON);
			NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
			BitSet actions = new BitSetUnboundedLongArray(graph.getNumNodes());
			for(int node = 0; node < graph.getNumNodes(); node ++) {
				graph.queryNode(node);
				if(! isState.getBoolean()) {
					actions.set(node);
					continue;
				}
				AutomatonStateUtil stateUtil = fromAuto.getObject();
				int autoNum = stateUtil.getNumber();
				BitSet label = automaton.getStateLabels(autoNum);
				for(int labelNr = label.nextSetBit(0); 
						labelNr >= 0 ; 
						labelNr = label.nextSetBit(labelNr + 1)) {
					BitSet result = sccsInMecs.get(labelNr);
					if(result == null) {
						result = new BitSetUnboundedLongArray(graph.getNumNodes());
					}
					result.set(node);
					sccsInMecs.put(labelNr, result);
				}
			}
			List<AcceptanceCondition> labels = automaton.getAccConditions();
			for (AcceptanceCondition label : labels) {
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
				BitSet existing = sccsInMecs.get(finEdge);
				if(existing == null) {
					continue;
				}
				existing.or(actions);
				ComponentsEX sccs = new ComponentsEX(graph, existing, finEdge, true);
				sccs.computeSCCs();
				for(BitSet leafSCC = sccs.next(); leafSCC != null ; leafSCC = sccs.next()) {
					if(decideComponentRabinMDPLeaf(graph, leafSCC, automaton,
							finEdge, label.getInfiniteStates())) {
						acc.or(leafSCC);
						++ numComponents;
					}
				}

			}
		} else {
		}
        log.send(MessagesLTLTGRA.LTL_TGRA_COMPUTING_END_COMPONENTS_DONE, numComponents);
		return acc;
	}

	private boolean decideComponentRabinMDPLeaf(GraphExplicit graph,
			BitSet leafSCC, AutomatonDSTA automaton, int finEdge,
			List<AcceptanceLabel> infiniteStates) throws EPMCException {
		if(infiniteStates.size() == 0) return true;
		BitSet labels = new BitSetUnboundedLongArray(infiniteStates.size());
		labels.set(0, infiniteStates.size(), true);
		EdgeProperty edgeProp = graph.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
		for(int node = leafSCC.nextSetBit(0); node >= 0 ; node = leafSCC.nextSetBit(node + 1)) {
			graph.queryNode(node);   
			for(int succNr = 0; succNr < graph.getNumSuccessors() ; succNr ++) {
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

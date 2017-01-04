package epmc.propertysolverltlfg;

import java.util.ArrayList;
import java.util.List;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.algorithms.explicit.EndComponents;
import epmc.automaton.Automaton;
import epmc.automaton.AutomatonStateUtil;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.propertysolverltlfg.automaton.AcceptanceCondition;
import epmc.propertysolverltlfg.automaton.AcceptanceLabel;
import epmc.propertysolverltlfg.automaton.AutomatonDSA;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;


/**
 * input formula will be LTL, but automaton should be GRA
 * and support HOA format
 * @author Yong Li
 */
public final class PropertySolverExplicitLTLGRA extends PropertySolverExplicitLTL {
	public final static String IDENTIFIER = "ltl-gra-explicit";

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return IDENTIFIER;
	}

	
	@Override
	AutomatonType getAutoType() {
		return AutomatonType.GRA;
	} 
	
	@Override
	Automaton constructRabinExplicit(AutomatonRabin rabin)
			throws EPMCException {
		return new AutomatonDSA(rabin);
	}

	
	@Override
	BitSet computeAcceptedMECs(GraphExplicit graph, Automaton automatonExplicit) 
			throws EPMCException{
		AutomatonDSA automaton = (AutomatonDSA)automatonExplicit;
        BitSet acc = new BitSetUnboundedLongArray();        
		ComponentsExplicit components = UtilAlgorithms.newComponentsExplicit();
        int numComponents = 0;
        NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
        NodeProperty nodeLabels = graph.getNodeProperty(CommonProperties.NODE_AUTOMATON);
        
        if(! this.mecComputation) {
			List<BitSet> labelSets = new ArrayList<>(automaton.getNumLabels());
			for (int labelNr = 0; labelNr < automaton.getNumLabels(); labelNr++) {
				labelSets
						.add(new BitSetUnboundedLongArray(graph.getNumNodes()));
			}
			// collect label states
			BitSet nodeSpace = new BitSetUnboundedLongArray(graph.getNumNodes());
			for (int node = 0; node < graph.getNumNodes(); node++) {
				graph.queryNode(node);
				if (!isState.getBoolean()) {
					continue;
				}
				AutomatonStateUtil stateUtil = nodeLabels.getObject();
				BitSet label = automaton.getStateLabels(stateUtil.getNumber()); 
				for (int labelNr = label.nextSetBit(0); labelNr >= 0; labelNr = label
						.nextSetBit(labelNr + 1)) {
					labelSets.get(labelNr).set(node);
				}
				nodeSpace.set(node);
			}

			List<AcceptanceCondition> labels = automaton.getAccConditions();
			for (AcceptanceCondition label : labels) {
				BitSet finStates = new BitSetUnboundedLongArray(
						graph.getNumNodes());
				AcceptanceLabel fin = label.getFiniteStates();
				if (fin == null || fin.isFalse())
					;
				else if (fin.isTrue())
					finStates.flip(0, graph.getNumNodes());
				else {
					finStates.or(labelSets.get(fin.getStateSet()));
				}
				finStates.flip(0, graph.getNumNodes());

				if (finStates.isEmpty())
					continue;
				EndComponents endComponents = components.maximalEndComponents(
						graph, finStates);
				for (BitSet leafSCC = endComponents.next(); leafSCC != null; leafSCC = endComponents
						.next()) {
					if (decideComponentRabinMDPLeaf(graph, leafSCC, automaton,
							label.getInfiniteStates())) {
						acc.or(leafSCC);
						++numComponents;
					}
				}

			}
		} else {
			EndComponents endComponents = components.stronglyConnectedComponents(graph);
			for (BitSet leafSCC = endComponents.next(); leafSCC != null; leafSCC = endComponents
					.next()) {
				List<AcceptanceCondition> labels = automaton.getAccConditions();
				for (AcceptanceCondition label : labels) {
					BitSet finStates = null;
					AcceptanceLabel fin = label.getFiniteStates();
					if (fin == null || fin.isFalse())
						finStates = new BitSetUnboundedLongArray(graph.getNumNodes());
					else if (fin.isTrue())
						finStates = leafSCC.clone();
					else {
						finStates = new BitSetUnboundedLongArray(graph.getNumNodes());
						for (int node = leafSCC.nextSetBit(0); node >= 0; node = leafSCC
								.nextSetBit(node + 1)) {
							graph.queryNode(node);
							if (!isState.getBoolean()) {
								continue;
							}
							AutomatonStateUtil stateUtil = nodeLabels
									.getObject();
							int num = stateUtil.getNumber(); //automaton node state
							if (automaton.getStateLabels(num).get(fin.getStateSet()))
								finStates.set(node);
						}
					}
					finStates.flip(0, graph.getNumNodes());
					finStates.and(leafSCC);
					if (finStates.isEmpty())
						continue;
					EndComponents sccInMEC = components.maximalEndComponents(
							graph, finStates);
					for (BitSet mec = sccInMEC.next(); mec != null; mec = sccInMEC
							.next()) {
						if (decideComponentRabinMDPLeaf(graph, mec, automaton,
								label.getInfiniteStates())) {
							acc.or(mec);
							++numComponents;
						}
					}
				}
			}
		}
        log.send(MessagesLTLTGRA.LTL_TGRA_COMPUTING_END_COMPONENTS_DONE, numComponents);
		return acc;
	}


	private boolean decideComponentRabinMDPLeaf(GraphExplicit graph,
			BitSet mec, AutomatonDSA automaton, List<AcceptanceLabel> infiniteStates) 
					throws EPMCException {
		NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
		NodeProperty nodeLabels = graph.getNodeProperty(CommonProperties.NODE_AUTOMATON);
		BitSet labels =  new BitSetUnboundedLongArray(automaton.getNumLabels());
		for(int node=mec.nextSetBit(0); node >= 0; node = mec.nextSetBit(node + 1)) {
			graph.queryNode(node);
            if (! isState.getBoolean()) {
                continue;
            }
            AutomatonStateUtil stateUtil = nodeLabels.getObject();
            int num = stateUtil.getNumber();
			labels.or(automaton.getStateLabels(num));
		}
		
		for(AcceptanceLabel inf : infiniteStates) {
			if(inf.isFalse()) {
				return false;
			}
			if(inf.isTrue()) continue;
			int lab = inf.getStateSet();
			if(inf.isNegated() && labels.get(lab)) return false; 
			else if(! labels.get(lab)) return false;
		}
		
		return true;
	}

}

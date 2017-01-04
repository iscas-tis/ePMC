package epmc.automaton;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.automaton.BuechiTransition;
import epmc.automaton.BuechiTransitionImpl;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import epmc.value.ContextValue;
import epmc.value.TypeInteger;
import epmc.value.TypeObject;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;

final class GraphPreparator {
	private final static class Transition {
	    private final int to;
	    private final Expression guard;
	    private final BitSet label;
	    
	    private Transition(int to, Expression guard, BitSet label) {
	    	this.to = to;
	    	this.guard = guard;
	    	this.label = label;
	    }
	    
	    private int getTo() {
			return to;
		}
	    
	    private Expression getGuard() {
			return guard;
		}
	    
	    private BitSet getLabel() {
			return label;
		}
	}
	
	private final HanoiHeader header;
	private final List<Set<Transition>> transitions;

	GraphPreparator(HanoiHeader header) {
		assert header != null;
		this.header = header;
		transitions = new ArrayList<>();
		for (int i = 0; i < header.getNumStates(); i++) {
			transitions.add(new LinkedHashSet<>());
		}
	}
	
	void addTransition(int from, int to, Expression guard, BitSet label) {
		assert from >= 0;
		assert from < transitions.size();
		if (label == null) {
			label = new BitSetUnboundedLongArray();
		}
		Set<Transition> stateTransitions = transitions.get(from);
		stateTransitions.add(new Transition(to, guard, label));
	}
	
	GraphExplicitWrapper toGraph() throws EPMCException {
		ContextValue context = getContextValue();
	    GraphExplicitWrapper graph = new GraphExplicitWrapper(context);
	    TypeInteger typeInteger = TypeInteger.get(context);
	    TypeObject typeLabel = new TypeObject.Builder()
                .setContext(context)
                .setClazz(BuechiTransition.class)
                .build();
	    ValueInteger numLabels = typeInteger.newValue();
	    graph.addSettableGraphProperty(CommonProperties.NUM_LABELS, typeInteger);
	    EdgeProperty labelProp = graph.addSettableEdgeProperty(CommonProperties.AUTOMATON_LABEL, typeLabel);
	    
	    ValueObject transitionValue = typeLabel.newValue();
	    for (int from = 0; from < transitions.size(); from++) {
	    	Set<Transition> stateTransitions = transitions.get(from);
	    	int numSuccessors = stateTransitions.size();
		    graph.queryNode(from);
		    graph.prepareNode(numSuccessors);
		    int succNr = 0;
		    for (Transition transition : stateTransitions) {
		    	BuechiTransition buchiTransition = new BuechiTransitionImpl(transition.getGuard(), transition.getLabel());
		    	graph.setSuccessorNode(succNr, transition.getTo());
		    	transitionValue.set(buchiTransition);
		    	labelProp.set(transitionValue, succNr);
		    	succNr++;
		    }
	    }
	    
	    numLabels.set(header.getNumAcc());
	    BitSet init = graph.getInitialNodes();
	    init.or(header.getStartStates());
	    graph.setGraphProperty(CommonProperties.NUM_LABELS, numLabels);
	    return graph;
	}
	
	private ContextValue getContextValue() {
		return header.getContext();
	}
	
	HanoiHeader getHeader() {
		return header;
	}
}

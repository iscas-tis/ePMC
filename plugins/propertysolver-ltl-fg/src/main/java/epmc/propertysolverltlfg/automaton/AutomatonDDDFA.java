package epmc.propertysolverltlfg.automaton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import epmc.automaton.AutomatonDD;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.util.BitSet;
/**
 * This is an abstract class for Finite Deterministic Automata
 * can handle state-based or transition-based acceptances
 * @author Yong Li
 * */
public abstract class AutomatonDDDFA implements AutomatonDD {
    protected final ContextDD contextDD;
    protected final int numLabels;
    private final GraphExplicit automaton;
    protected final ArrayList<DD> labels;
    private final ArrayList<DD> presVars;
    private final ArrayList<DD> nextVars;
    private final ArrayList<VariableDD> stateVariables = new ArrayList<>();
    protected final ArrayList<DD> labelDDs ;   // used for labels in acceptances
    private final DD presCube;
    private final DD nextCube;
    private final DD labelsCube;
    private final DD init;
    private final DD trans;
    protected final AutomatonRabin rabin;
    protected final ExpressionToDD expressionToDD;
	protected final Map<Integer, DD> labelStates;  // used for finite states 

    /* constructors */
    public AutomatonDDDFA(
    		ExpressionToDD expressionToDD
    		, AutomatonRabin rabin
    		, DD states
    		) throws EPMCException {
        assert expressionToDD != null;
        assert rabin != null;
        assert states != null;
        assert rabin.getNumLabels() > 0;
        this.expressionToDD = expressionToDD;
        this.rabin = rabin;
        this.contextDD = expressionToDD.getContextDD();
        this.numLabels = rabin.getNumLabels();
        this.automaton = rabin.getGraph();
        this.labelStates = new HashMap<>();
        this.labels = new ArrayList<>();
        this.presVars = new ArrayList<>();
        this.nextVars = new ArrayList<>();
        this.labelDDs = new ArrayList<>(this.numLabels);
        for(int labelNr=0; labelNr < this.numLabels; labelNr ++) {
        	this.labelDDs.add(contextDD.newConstant(false));
        }
        prepareVariables();
        this.presCube = contextDD.listToCube(presVars);
        this.nextCube = contextDD.listToCube(nextVars);
        this.labelsCube = contextDD.listToCube(labels);
        DD init = computeInit();
        this.init = init.andWith(states.clone());
        
        trans = images();
    }

    @Override
    public DD getInitial() {
        return init;
    }

    @Override
    public DD getTransitions() {
        return trans;
    }

    @Override
    public List<DD> getPresVars() {
        return presVars;
    }
    
    @Override
    public List<DD> getNextVars() {
        return nextVars;
    }

    @Override
    public List<DD> getLabelVars() {
        return labels;
    }
   
    
    public DD getLabelVar(int labelID) {
    	return labelDDs.get(labelID).clone();
    }

    /* private auxiliary methods */
    
    private void prepareVariables() throws EPMCException {
    	labels.add(contextDD.newConstant(true));
    	VariableDD stateCounter = contextDD.newInteger("%autstate", 2, 0, automaton.getNumNodes()-1);
    	stateVariables.add(stateCounter);
        this.presVars.addAll(contextDD.clone(stateCounter.getDDVariables(0)));
        this.nextVars.addAll(contextDD.clone(stateCounter.getDDVariables(1)));
    }
    
    private DD computeInit() throws EPMCException {
    	BitSet initSets = automaton.getInitialNodes();
        DD init = contextDD.newConstant(true);
        VariableDD stateCounter = this.stateVariables.get(0);
        for (int node = 0; node < automaton.getNumNodes(); node++) {
        	if(initSets.get(node)) {
        		init = init.andWith(stateCounter.newIntValue(0, node));
        	}else {
        		init = init.andWith(stateCounter.newIntValue(0, node).notWith());
        	}
        }
        return init;
    }
    
    abstract void putStateLabels(VariableDD states, BitSet labeling, int state)
    throws EPMCException;
    abstract void putEdgeLabels(BitSet labeling, DD guard)
    throws EPMCException;
    
    private DD images() throws EPMCException {
    	DD result = contextDD.newConstant(false);
    	VariableDD states = stateVariables.get(0);
        NodeProperty nodeLabels = automaton.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        EdgeProperty edgeLabels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            automaton.queryNode(state);
            // get labeling
            if(nodeLabels != null) {
                RabinStateUtil stateLabel = nodeLabels.getObject();
                if(stateLabel != null) {
                	putStateLabels(states, stateLabel.getLabeling(), state);
                }
            }

            DD presVar = states.newIntValue(0, state);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
            	int succ = automaton.getSuccessorNode(succNr);
                RabinTransitionUtil trans = edgeLabels.getObject(succNr);
                DD guard = expressionToDD.translate(trans.getExpression());
                guard = guard.andWith(presVar.clone());
                guard = guard.andWith(states.newIntValue(1, succ));
                result = result.orWith(guard.clone());
                // get labeling
                putEdgeLabels(trans.getLabeling(), guard);
                guard.dispose();
            }
            presVar.dispose();
        }

        return result;
    }
    

    public AutomatonRabin getBuechi() {
        return rabin;
    }

    public List<VariableDD> getStateVariables() {
        return stateVariables;
    }

    @Override
    public void close() {
        contextDD.dispose(labels);
        contextDD.dispose(presVars);
        contextDD.dispose(nextVars);
        contextDD.dispose(labelDDs);
        presCube.dispose();
        nextCube.dispose();
        labelsCube.dispose();
        init.dispose();
        trans.dispose();
        for(DD vals : this.labelStates.values()) {
        	vals.dispose();
        }
    }

    @Override
    public DD getPresCube() {
        return presCube;
    }

    @Override
    public DD getNextCube() {
        return nextCube;
    }

    @Override
    public DD getLabelCube() {
        return labelsCube;
    }
    
    public List<AcceptanceCondition> getAccConditions() {
    	return rabin.getAcceptances();
    }
    
    public List<Expression> getAccExpressions() {
    	return rabin.getAccExpressions();
    }
    
}

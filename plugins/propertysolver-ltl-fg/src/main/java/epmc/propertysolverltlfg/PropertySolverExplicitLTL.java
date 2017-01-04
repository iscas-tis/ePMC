package epmc.propertysolverltlfg;


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.automaton.Automaton;
import epmc.automaton.ProductGraphExplicit;
import epmc.error.EPMCException;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.SemanticsMarkovChain;
import epmc.graph.SemanticsNonDet;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.options.Options;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
import epmc.propertysolverltlfg.automaton.LTL2DA;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorNot;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;


/**
 * input formula will be LTL
 * @author Yong Li
 */
public abstract class PropertySolverExplicitLTL implements PropertySolver {
	public final static String IDENTIFIER = "ltl-auto-explicit";

	protected ContextValue contextValue;
	protected GraphExplicit graph;
	protected Options options;
	protected Log log;
	protected ModelChecker modelChecker;
	protected StateSetExplicit forStates;
	protected Set<Expression> stateLabels;
	protected boolean mecComputation;
	protected Expression property;
	protected ExpressionQuantifier propertyQuantifier;
	
	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return IDENTIFIER;
	}

	@Override
	public void setModelChecker(ModelChecker modelChecker) {
		assert modelChecker != null;
		this.modelChecker = modelChecker;
		this.options = modelChecker.getModel().getContextValue().getOptions();
		this.contextValue = modelChecker.getModel().getContextValue();
		if (modelChecker.getEngine() instanceof EngineExplicit) {
			this.graph = modelChecker.getLowLevel();
		}
		this.log = options.get(OptionsMessages.LOG);
	}
	
    /** solve linear equation system */
    private ValueArrayAlgebra prepareAndIterate(GraphExplicit graph, BitSet acc)
            throws EPMCException {
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit(graph.getOptions());
        GraphSolverObjectiveExplicitUnboundedReachability objective = new GraphSolverObjectiveExplicitUnboundedReachability();
        objective.setMin(false);
        objective.setGraph(graph);
        objective.setTarget(acc);
        configuration.setObjective(objective);
        configuration.solve();
        ValueArrayAlgebra values = objective.getResult();
        return values;
    }
    
    private StateMapExplicit prodToOrigResult(ValueArrayAlgebra iterResult,
            GraphExplicit prodGraph) throws EPMCException {
        // TODO implement more cleanly
        assert iterResult != null;
        assert prodGraph != null;
        Type typeWeight = TypeWeight.get(contextValue);
//        Value result = contextValue.newValueArrayWeight(modelGraph.getQueriedNodesLength());
        Value entry = typeWeight.newValue();
        BitSet nodes = forStates.getStatesExplicit();
//        NodeProperty nodeAutomaton = prodGraph.getNodeProperty(CommonProperties.NODE_MODEL);
        ValueArray resultValues = UtilValue.newArray(typeWeight.getTypeArray(), forStates.size());
        int i = 0;
        for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node+1)) {
            prodGraph.queryNode(node);
//            int modelState = nodeAutomaton.getInt();
            iterResult.get(entry, i);
            resultValues.set(entry, i);
            i++;
        }
        return UtilGraph.newStateMap(forStates.clone(), resultValues);
    }
    
	@Override
	public StateMap solve()
			throws EPMCException {
        assert property != null;
        assert forStates != null;
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        Set<Expression> inners = UtilLTL.collectLTLInner(quantifiedProp);
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());

        for (Expression inner : inners) {
            StateMapExplicit innerResult = (StateMapExplicit)modelChecker.check(inner, allStates);
            registerResult(inner, innerResult);
        }
        stateLabels = inners;
        allStates.close();
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        boolean min = dirType == DirType.MIN;
        StateMap result = doSolve(quantifiedProp, forStates, min);
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (propertyQuantifier.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType(contextValue);
            result = result.applyWith(op, compare);
        }
        return result;
	}

	private StateMap doSolve(Expression quantifiedProp, StateSet forStates,
			boolean min) throws EPMCException {
	       this.forStates = (StateSetExplicit) forStates;
	        Semantics type = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
	        if (!SemanticsNonDet.isNonDet(type)) {
	            min = false;
	        }
	        Expression expression = null;
	        if (min) {
	        	expression = not(quantifiedProp);
	        } else {
	        	expression = quantifiedProp;
	        }
	        AutomatonRabin rabin = constructRabin(expression);
	        StateMapExplicit innerResult = checkTemporalLTL(rabin);
	        
	        if (min) {
	            ValueAlgebra entry = ValueAlgebra.asAlgebra(innerResult.getType().newValue());
	            for (int i = 0; i < innerResult.size(); i++) {
	                innerResult.getExplicitIthValue(entry, i);
	                entry.subtract(TypeAlgebra.asAlgebra(innerResult.getType()).getOne(), entry);
	                innerResult.setExplicitIthValue(entry, i);
	            }
	        }
	        return innerResult;
	}
	abstract AutomatonType getAutoType() ;
	AutomatonRabin constructRabin(Expression expression) throws EPMCException {
		return LTL2DA.convertLTL2DA(expression, getAutoType(), true, false);
	}
	abstract Automaton constructRabinExplicit(AutomatonRabin rabin) throws EPMCException;

	private StateMapExplicit checkTemporalLTL(AutomatonRabin rabin
			) throws EPMCException {
        //error, since 		
        StopWatch timer = new StopWatch(true);
        Automaton automaton = constructRabinExplicit(rabin);
        System.out.println("Done building automaton in " + timer.getTimeSeconds() + " seconds ...");
        ProductGraphExplicit prodGraph = new ProductGraphExplicit.Builder()
        		.setAutomaton(automaton)
        		.setModel(graph)
        		.setModelInitialNodes(forStates.getStatesExplicit())
        		.addGraphProperties(graph.getGraphProperties())
        		.addNodeProperty(CommonProperties.PLAYER)
        		.addNodeProperty(CommonProperties.STATE)
        		.addEdgeProperty(CommonProperties.WEIGHT)
        		.build();
        GraphExplicitWrapper graph = new GraphExplicitWrapper(prodGraph);
        graph.addDerivedGraphProperties(prodGraph.getGraphProperties());
        graph.addDerivedNodeProperty(CommonProperties.STATE);
        graph.addDerivedNodeProperty(CommonProperties.PLAYER);
        if(isTransitionBased()) {
        	graph.addDerivedEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        }else if(isStateBased()) {
        	graph.addDerivedNodeProperty(CommonProperties.AUTOMATON_LABEL);
        }
        graph.addDerivedNodeProperty(CommonProperties.NODE_AUTOMATON);
        graph.addDerivedNodeProperty(CommonProperties.NODE_MODEL);
        graph.addDerivedEdgeProperty(CommonProperties.WEIGHT);
        
        log.send(MessagesLTLTGRA.LTL_TGRA_EXPLORING_STATE_SPACE);
        graph.explore();
        log.send(MessagesLTLTGRA.LTL_TGRA_EXPLORING_STATE_SPACE_DONE, graph.computeNumStates(),
                automaton.getNumStates());
        log.send(MessagesLTLTGRA.LTL_TGRA_COMPUTING_END_COMPONENTS);
        BitSet acc = computeAcceptedMECs(graph, automaton);
        return prodToOrigResult(prepareAndIterate(graph, acc), graph);
	}
	
	protected boolean isTransitionBased() {
		return false;
	}
	protected boolean isStateBased() {
		return true;
	}
	
	abstract BitSet computeAcceptedMECs(GraphExplicit product, Automaton automaton) 
			throws EPMCException;
	protected boolean canHandle(Expression property) { return true; }
    @Override
    public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        Semantics semantics = modelChecker.getModel().getSemantics();
        if (!SemanticsMarkovChain.isMarkovChain(semantics) && !SemanticsMDP.isMDP(semantics)) {
        	return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        if (propertyQuantifier.getQuantified() instanceof ExpressionReward) {
            return false;
        }
        Set<Expression> inners = UtilLTL.collectLTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            modelChecker.ensureCanHandle(inner, allStates);
        }
        if (allStates != null) {
        	allStates.close();
        }
        return canHandle(propertyQuantifier.getQuantified());
    }
    
    @Override
    public Set<Object> getRequiredGraphProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.SEMANTICS);
    	return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.STATE);
    	required.add(CommonProperties.PLAYER);
        Set<Expression> inners = UtilLTL.collectLTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
    	return Collections.unmodifiableSet(required);
    }
    
    @Override
    public Set<Object> getRequiredEdgeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.WEIGHT);
    	return Collections.unmodifiableSet(required);
    }
    
    private void registerResult(Expression property, StateMapExplicit results)
            throws EPMCException {
        if (!graph.getNodeProperties().contains(property)) {
        	graph.addSettableNodeProperty(property, property.getType(graph));
        }
        NodeProperty nodeProp = graph.getNodeProperty(property);
        Value entry = results.getType().newValue();
        for (int i = 0; i < results.size(); i++) {
            results.getExplicitIthValue(entry, i);
            int state = results.getExplicitIthState(i);
            graph.queryNode(state);
            nodeProp.set(entry);
        }
    }
    
	@Override
	public void setProperty(Expression property) {
		this.property = property;
		if (property instanceof ExpressionQuantifier) {
			this.propertyQuantifier = (ExpressionQuantifier) property;
		}
	}

	@Override
	public void setForStates(StateSet forStates) {
		if (forStates != null && (forStates instanceof StateSetExplicit)) {
			this.forStates = (StateSetExplicit) forStates;
		}
	}

    private Expression not(Expression expression) {
    	return new ExpressionOperator.Builder()
        	.setOperator(contextValue.getOperator(OperatorNot.IDENTIFIER))
        	.setOperands(expression)
        	.build();
    }
}

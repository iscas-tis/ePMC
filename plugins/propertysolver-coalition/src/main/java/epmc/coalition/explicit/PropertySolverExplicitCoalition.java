package epmc.coalition.explicit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.automaton.AutomatonParity;
import epmc.automaton.AutomatonParityLabel;
import epmc.automaton.ProductGraphExplicit;
import epmc.automaton.UtilAutomaton;
import epmc.coalition.UtilCoalition;
import epmc.coalition.messages.MessagesCoalition;
import epmc.coalition.options.OptionsCoalition;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionCoalition;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.SMGPlayer;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.SemanticsSMG;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorNot;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueBoolean;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

/**
 * Explicit-state solver for coalition properties.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PropertySolverExplicitCoalition implements PropertySolver {
	/** Identifier of this property solver. */
    public final static String IDENTIFIER = "coalition-explicit";
    
    /** Model checker used in combination with this solver. */
    private ModelChecker modelChecker;
    /** Property to check. */
	private Expression property;
	private ExpressionCoalition propertyCoalition;
	/** For which states property shall be checked. */
	private StateSet forStates;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
    
    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
    }

    @Override
	public void setProperty(Expression property) {
		this.property = property;
		if (property instanceof ExpressionCoalition) {
			this.propertyCoalition = (ExpressionCoalition) property;
		}
	}

	@Override
	public void setForStates(StateSet forStates) {
		this.forStates = forStates;
	}

    @Override
	public boolean canHandle() throws EPMCException {
	    assert property != null;
	    if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
	        return false;
	    }
	    if (!(property instanceof ExpressionCoalition)) {
	        return false;
	    }
	    if (!SemanticsSMG.isSMG(modelChecker.getModel().getSemantics())) {
	        return false;
	    }
	    if (!(property instanceof ExpressionCoalition)) {
	        return false;
	    }
	    ExpressionQuantifier quantifier = propertyCoalition.getQuantifier();
	    Set<Expression> inners = UtilCoalition.collectLTLInner(quantifier.getQuantified());
	    StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
	    for (Expression inner : inners) {
	        modelChecker.ensureCanHandle(inner, allStates);
	    }
	    if (allStates != null) {
	    	allStates.close();
	    }
	    return true;
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
//    	required.add(CommonProperties.NODE_EXPLORER);
        ExpressionQuantifier quantifier = propertyCoalition.getQuantifier();
	    Expression path = quantifier.getQuantified();
	    if (UtilCoalition.isDirTypeMin(quantifier)) {
	    	path = not(path);
	    }
        Set<Expression> inners = UtilCoalition.collectLTLInner(path);
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
        	required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
        if (allStates != null) {
        	allStates.close();
        }
        ExpressionCoalition propertyCoalition = (ExpressionCoalition) property;
        required.addAll(propertyCoalition.getPlayers());
    	return Collections.unmodifiableSet(required);
    }
    
    @Override
    public Set<Object> getRequiredEdgeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
	    if (!UtilCoalition.isQualitative(propertyCoalition)) {
	    	required.add(CommonProperties.WEIGHT);
	    }
    	return Collections.unmodifiableSet(required);
    }
    
    @Override
	public StateMap solve() throws EPMCException {
		assert property != null;
		assert forStates != null;
	    ExpressionQuantifier quant = propertyCoalition.getQuantifier();
		getLog().send(MessagesCoalition.COALITION_MODEL_NODES, getLowLevel().getNumNodes());
	    Expression path = quant.getQuantified();
	    if (UtilCoalition.isDirTypeMin(quant)) {
	    	path = not(path);
	    }
	    Set<Expression> inners = UtilCoalition.collectLTLInner(path);
	    StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
	    for (Expression inner : inners) {
	        StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(inner, allStates);
	        UtilGraph.registerResult(getLowLevel(), inner, innerResult);
	    }

	    allStates.close();
	    StateSetExplicit init = (StateSetExplicit) forStates;
	    
	    boolean qualitative = UtilCoalition.isQualitative(propertyCoalition);
	    boolean trivialTrue = UtilCoalition.isTrivialTrue(propertyCoalition);
	    boolean trivialFalse = UtilCoalition.isTrivialFalse(propertyCoalition);
	    boolean strictEven = UtilCoalition.isStrictEven(propertyCoalition);
	
	    StateMap result;
	    if (trivialTrue || trivialFalse) {
	        ValueArray resultValue = UtilValue.newArray(TypeBoolean.get(getContextValue()).getTypeArray(),
	        		init.size());
	        ValueBoolean valueTrivialTrue = TypeBoolean.get(getContextValue()).newValue();
	        valueTrivialTrue.set(trivialTrue);
	        for (int i = 0; i < init.size(); i++) {
	            int node = init.getExplicitIthState(i);
	            resultValue.set(valueTrivialTrue, node);
	        }
	        result = UtilGraph.newStateMap(init.clone(), resultValue);
	    } else if (qualitative) {
	        GraphExplicit game = buildGame(init, path, qualitative);
	        assert game != null;
	        StopWatch gameSolverWatch = new StopWatch(true);
	        Options options = game.getOptions();
	        QualitativeResult regions = null;
	        SolverQualitative solver = UtilOptions.getInstance(options,
	                OptionsCoalition.COALITION_SOLVER);
	        getLog().send(MessagesCoalition.COALITION_SOLVING_USING, solver.getIdentifier());
			solver.setGame(game);
			solver.setComputeStrategies(false, false);
//			solver.setComputeStrategies(true, true);
			solver.setStrictEven(strictEven);
			regions = solver.solve();
			/*
			game.removeNodeProperty(CommonProperties.AUTOMATON_LABEL);
			game.registerNodeProperty(CommonProperties.STRATEGY, regions.getStrategies());
	        GraphExporterJSON exporter = new GraphExporterJSON();
	        exporter.setGraph(game);
	        try {
				exporter.setOutputStream(new FileOutputStream("/Users/emhahn/asdf.json"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        exporter.export();
	        */
	        getLog().send(MessagesCoalition.COALITION_SOLVING_DONE, gameSolverWatch.getTimeSeconds(), regions.getSet0().cardinality(), regions.getSet1().cardinality());
	        result = toOrig(game, init, regions.getSet0());
	    } else {
	    	GraphExplicit game = buildGame(init, path, qualitative);
	        assert game != null;
	        SolverQuantitative solver = new SolverQuantitativeSchewe();
	        solver.setGame(game);
	        QuantitativeResult res = solver.solve();
	    	result = toOrig(game, init, res.getProbabilities());
	    }
	    
	    return result;
	}

	/**
	 * Build product of original model and parity automaton.
	 * None of the parameters may be {@code null}.
	 * 
	 * @param forStates for which states results are needed
	 * @param path LTL path formula within property to check
	 * @param qualitative whether a purely qualitative game shall be constructed
	 * @return 
	 * @throws EPMCException thrown in case of problems
	 */
	private GraphExplicitWrapper buildGame(StateSetExplicit forStates, Expression path, boolean qualitative) throws EPMCException {
		assert forStates != null;
		assert path != null;
	    Expression[] expressions = UtilCoalition.collectLTLInner(path).toArray(new Expression[0]);
	    AutomatonParity automaton = UtilAutomaton.newAutomatonParity(forStates.getContextValue(), path, expressions);
		getLog().send(MessagesCoalition.COALITION_PRODUCT_START);
		StopWatch watch = new StopWatch(true);
        ExpressionCoalition propertyCoalition = (ExpressionCoalition) property;
	    List<SMGPlayer> playerList = propertyCoalition.getPlayers();
	    List<Object> edgeProperties = new ArrayList<>();
	    if (!qualitative) {
	    	assert getLowLevel().getEdgeProperties().contains(CommonProperties.WEIGHT);
	        edgeProperties.add(CommonProperties.WEIGHT);
	    }
	    ProductGraphExplicit product = new ProductGraphExplicit.Builder()
	    		.setModel(getLowLevel())
	    		.setModelInitialNodes(forStates.getStatesExplicit())
	    		.setAutomaton(automaton)
	    		.addGraphProperties(getLowLevel().getGraphProperties())
	    		.addNodeProperty(CommonProperties.STATE)
//	    	    .addNodeProperty(CommonProperties.NODE_EXPLORER)
	    		.addNodeProperties(playerList)
	    		.addEdgeProperties(edgeProperties)
//	    		.setManual()
	    		.build();
	    
	    /*
	    for (long init : product.getManualInitNodes()) {
	    	int modelNode = product.combinedToModelNode(init);
	    	int automatonNode = product.combinedToAutomatonNode(init);
	    	product.queryNode(init);
	    }
	    */
	    
	    GraphExplicitWrapper wrapper = new GraphExplicitWrapper(product);
	    wrapper.addDerivedGraphProperty(CommonProperties.SEMANTICS);
	    wrapper.addDerivedNodeProperty(CommonProperties.AUTOMATON_LABEL);
	    wrapper.addDerivedNodeProperty(CommonProperties.NODE_AUTOMATON);
	    wrapper.addDerivedNodeProperty(CommonProperties.NODE_MODEL);
//	    wrapper.addDerivedNodeProperty(CommonProperties.NODE_EXPLORER);
	    if (!qualitative) {
	    	wrapper.addDerivedEdgeProperty(CommonProperties.WEIGHT);
	    }
	    NodeProperty[] playerNodes = new NodeProperty[playerList.size()];
	    for (int i = 0; i < playerList.size(); i++) {
	        playerNodes[i] = product.getNodeProperty(playerList.get(i));
	    }
	    
	    NodeProperty isState = product.getNodeProperty(CommonProperties.STATE);
	    Type playerType = TypeEnum.get(getContextValue(), Player.class);
	    NodeProperty playerProp = wrapper.addSettableNodeProperty(CommonProperties.PLAYER, playerType);
	    NodeProperty labels = wrapper.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
	    boolean hasInfPrio = false;
	    int maxPriority = -1;

	    
        TIntStack todo = new TIntArrayStack();
        BitSet initStates = product.getInitialNodes();
        BitSet exploredNodes = UtilBitSet.newBitSetUnbounded();
        for (int node = initStates.nextSetBit(0); node >= 0; node = initStates.nextSetBit(node+1)) {
        	exploredNodes.set(node);
        	todo.push(node);
        }

        // Note: following works because the wrapper queries the product graph
        // if the according node has not been cached before.
        // Take care when modifying!
        while (todo.size() > 0) {
            int node = todo.pop();
            wrapper.queryNode(node);
	        product.queryNode(node);
	        AutomatonParityLabel label = labels.getObject();
	        int priority = label.getPriority();
	        if (priority == Integer.MAX_VALUE) {
	        	hasInfPrio = true;
	        } else {
	        	maxPriority = Math.max(maxPriority, priority);
	        }
	
	        playerProp.set(computePlayer(isState, playerNodes));
	        
            for (int succNr = 0; succNr < wrapper.getNumSuccessors(); succNr++) {
                int succ = wrapper.getSuccessorNode(succNr);
                assert succ >= 0;
                if (!exploredNodes.get(succ)) {
                    exploredNodes.set(succ);
                    todo.push(succ);
                }
            }
        }

	    if (hasInfPrio) {
	    	maxPriority++;
	    }
	    getLog().send(MessagesCoalition.COALITION_PRODUCT_DONE, watch.getTimeSeconds(), wrapper.getNumNodes());
	    getLog().send(MessagesCoalition.COALITION_NUMBER_COLORS, maxPriority + 1);
	    
	    return wrapper;
	}

	private Player computePlayer(NodeProperty isState, NodeProperty[] playerNodes) throws EPMCException {
		Player player;
        if (isState.getBoolean()) {
            player = Player.TWO;
            for (NodeProperty playerNode : playerNodes) {
                if (playerNode.getBoolean()) {
                    player = Player.ONE;
                }
            }
        } else {
            player = Player.STOCHASTIC;
        }
        return player;
	}

	/**
	 * Maps back qualitative result to original model states.
	 * None of the parameters may be {@code null}.
	 * 
	 * @param forStates model states for which results are needed
	 * @param solverResult result from solver to be mapped back
	 * @return result map for states of original model
	 * @throws EPMCException thrown in case of problems
	 */
	private StateMap toOrig(GraphExplicit game, StateSetExplicit forStates, BitSet solverResult) throws EPMCException {
		assert forStates != null;
		assert solverResult != null;
		TypeBoolean typeBoolean = TypeBoolean.get(getContextValue());
		Value entry = typeBoolean.newValue();
	//        BitSet nodes = game.getInitialNodes();
	//        NodeProperty nodeAutomaton = game.getNodeProperty(CommonProperties.NODE_MODEL);
		ValueArray resultValues = UtilValue.newArray(TypeBoolean.get(getContextValue())
				.getTypeArray(), forStates.size());
		// TODO check!
		for (int i = 0; i < forStates.size(); i++) {
			int node = forStates.getExplicitIthState(i);
			game.queryNode(node);
	//            int modelState = nodeAutomaton.getInt();
			boolean value = solverResult.get(node);
			entry.set(value
					? TypeBoolean.get(getContextValue()).getTrue()
					: TypeBoolean.get(getContextValue()).getFalse());
			resultValues.set(entry, i);
		}
		StateMap result = UtilGraph.newStateMap(forStates, resultValues);
		return result;
	}

	/**
	 * Maps back quantitative result to original model states.
	 * None of the parameters may be {@code null}.
	 * 
	 * @param forStates model states for which results are needed
	 * @param solverResult result from solver to be mapped back
	 * @return result map for states of original model
	 * @throws EPMCException thrown in case of problems
	 */
	private StateMap toOrig(GraphExplicit game, StateSetExplicit forStates, ValueArray solverResult) throws EPMCException {
		assert forStates != null;
		assert solverResult != null;
	//        BitSet nodes = game.getInitialNodes();
	//        NodeProperty nodeAutomaton = game.getNodeProperty(CommonProperties.NODE_MODEL);
		ValueArray resultValues = UtilValue.newArray(solverResult.getType(), forStates.size());
	        // TODO check!
		Value entry = TypeArray.asArray(solverResult.getType()).getEntryType().newValue();
		for (int i = 0; i < forStates.size(); i++) {
			int node = forStates.getExplicitIthState(i);
			game.queryNode(node);
			//int modelState = nodeAutomaton.getInt();
			solverResult.get(entry, node);
			resultValues.set(entry, i);
		}
		StateMap result = UtilGraph.newStateMap(forStates, resultValues);
		return result;
	}

    /**
     * Get value context used.
     * 
     * @return value context used.
     */
    private ContextValue getContextValue() {
    	return modelChecker.getModel().getContextValue();
    }
    
    /**
     * Get low level model of model checker.
     * 
     * @return low level model of model checker
     */
    private GraphExplicit getLowLevel() {
    	return modelChecker.getLowLevel();
    }
    
    private Expression not(Expression expression) {
    	return new ExpressionOperator.Builder()
        	.setOperator(getContextValue().getOperator(OperatorNot.IDENTIFIER))
        	.setOperands(expression)
        	.build();
    }

    /**
     * Get log used for analysis.
     * 
     * @return log used for analysis
     */
    private Log getLog() {
    	return modelChecker.getModel().getContextValue().getOptions().get(OptionsMessages.LOG);
    }
}
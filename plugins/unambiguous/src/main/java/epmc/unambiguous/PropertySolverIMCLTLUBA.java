package epmc.unambiguous;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.algorithms.explicit.EndComponents;
import epmc.automaton.Automaton;
import epmc.automaton.ProductGraphExplicit;
import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.Feature;
import epmc.error.EPMCException;
import epmc.expression.CmpType;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.StateMap;
import epmc.graph.StateMapExplicit;
import epmc.graph.StateSet;
import epmc.graph.StateSetExplicit;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;

import epmc.messages.Log;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.modelchecker.UtilModelChecker;
import epmc.options.Options;
import epmc.unambiguous.automaton.AutomatonUBA;
import epmc.unambiguous.automaton.AutomatonUBALabel;
import epmc.unambiguous.util.UtilUBA;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.Util;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public class PropertySolverIMCLTLUBA implements PropertySolver {
	public final static String IDENTIFIER = "ltl-uba-imc";
	private ModelChecker modelChecker;
	private ContextExpression context;
	private ContextValue contextValue;
	private GraphExplicit graph;
	private Options options;
	private Log log;
	private StateSetExplicit forStates;
	private Expression property;
	private List<EdgeVariable> edgeVariables = new ArrayList<>();
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        this.options = modelChecker.getOptions();
        this.context = modelChecker.getContextExpression();
        this.contextValue = context.getContextValue();
        if (modelChecker.getEngine() instanceof EngineExplicit) {
        	this.graph = modelChecker.getLowLevel();
        }
        this.log = options.get(OptionsMessages.LOG);
	}

	@Override
	public void setProperty(Expression property) {
		this.property = property;
	}

	@Override
	public void setForStates(StateSet forStates) {
		if (forStates != null && (forStates instanceof StateSetExplicit)) {
			this.forStates = (StateSetExplicit) forStates;
		}
	}

	@Override
	public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        Semantics semantics = modelChecker.getModel().getSemantics();
        if(!(semantics.isIMC() || semantics.isIMDP())) {
        	return false;
        }
//        if (!semantics.isIMC() || semantics.isMDP()) {
//        	return false;
//        }
        if (!property.isQuantifier()) {
            return false;
        }
        if (property.getQuantified().isReward()) {
            return false;
        }

        Set<Expression> inners = UtilUBA.collectLTLInner(property.getQuantified());
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
    	required.add(CommonProperties.PLAYER);
        Set<Expression> inners = UtilUBA.collectLTLInner(property.getQuantified());
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

	@Override
	public StateMap solve() throws EPMCException {
        assert property != null;
        assert forStates != null;
        Expression quantifiedProp = property.getQuantified();
        Set<Expression> inners = UtilUBA.collectLTLInner(quantifiedProp);
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(inner, allStates);
            UtilGraph.registerResult(graph, inner, innerResult);
        }
        System.out.println("ltl-uba-solve-after-model-build-start");
        StopWatch timer = Util.newStopWatch();
        allStates.close();
        StateMap result = doSolve(quantifiedProp);
        if (property.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(property.getCompare(), forStates);
            Operator op = property.getCompareType().asExOpType(contextValue);
            result = result.applyWith(op, compare);
        }
        System.out.println("ltl-uba-solve-after-model-build-done " + timer.getTimeSeconds());
        return result;
	}

	private StateMap doSolve(Expression expression) throws EPMCException {
    	StopWatch timer = Util.newStopWatch();
    	Automaton automaton = new AutomatonUBA(expression);
    	log.send(MessagesLTLUBA.LTL_UBA_BUILD_DONE, timer.getTimeSeconds());
    	log.send(MessagesLTLUBA.LTL_UBA_NUM_AUTOMATON_STATES, automaton.getBuechi().getNumStates());
//    	System.out.println("init: " + graph.getInitialNodes() + " \nmodel: " + graph.toString());
		// here we remove all actions in graph
    	//GraphExplicit model2 = uniformizeMDP(graph);
    	//System.out.println("init: " + model2.getInitialNodes() + " \nunifomized model: " + model2.toString());
    	//System.exit(-1);
    	System.out.println("remove redundant edge labels");
    	GraphExplicit model = removeAllActions(graph); 
//		System.out.println("init: " + model.getInitialNodes() + " \nmodel: " + model.toString());
    	log.send(MessagesLTLUBA.LTL_UBA_NUM_MODEL_STATES, model.getNumNodes());
    	return checkProductModel(createProductModel(model, automaton), model);
	}

	private StateMap checkProductModel(GraphExplicit product,
			GraphExplicit model) throws EPMCException {
		System.out.println("ltl-uba-check-product-start");
        BitSet acc = getContextValue().newBitSetUnbounded();
        
        ComponentsExplicit components = UtilAlgorithms.newComponentsExplicit();
        EndComponents endComponents = components.stronglyConnectedComponents(product);
        int numComponents = 0;
        int numECCStates = 0;
        int numSCCChecked = 0;
        StopWatch timer = Util.newStopWatch();
        NodeProperty fromModel = product.getNodeProperty(CommonProperties.NODE_MODEL);
        List<BitSet> posSCCs = new ArrayList<>();
        for (BitSet leafSCC = endComponents.next()
        		; leafSCC != null
        		; leafSCC = endComponents.next()) {
            
            /* should not be trivial SCC */
            if(isTrivialSCC(product, leafSCC)) {
            	continue;
            }
            numSCCChecked ++;
            BitSet origBSCC = getContextValue().newBitSetUnbounded(model.getNumNodes());
            for (int node = leafSCC.nextSetBit(0); node >= 0; node = leafSCC.nextSetBit(node+1)) {
            	product.queryNode(node);
                numECCStates++;
                /* get corresponding states in original model from product model */
                origBSCC.set(fromModel.getInt());
            }
             
             /*  mapped to a BSCC in the original Markov chains */
            if(! isBSCC(model, origBSCC)) continue;
            /* check whether it is an accepting component */
            if(! decideComponentUBAMCLeaf(product, leafSCC))  
            	continue;
            acc.or(leafSCC);
            posSCCs.add(leafSCC);
            numComponents++;
        }
        
        log.send(MessagesLTLUBA.LTL_UBA_NUM_SCC_CHECKED, numSCCChecked);
        System.out.println("ltl-uba-check-product-done " + timer.getTimeSeconds());
        log.send(MessagesLTLUBA.LTL_UBA_SCC_ACCEPTED, numComponents);
        log.send(MessagesLTLUBA.LTL_UBA_NUM_ECC_STATES, numECCStates);
//        System.out.println("product init: " + product.getInitialNodes() + "\nproduct target: " + acc );
//        System.out.println("product: \n" + product.toString());
        BitSet zeros = computeProb0(product, acc);
        if(acc.cardinality() == 0) {
        	Value resultValues = contextValue.getTypeReal().getTypeArray().newValue(forStates.size());
        	for(int node = 0; node < forStates.getStatesExplicit().cardinality(); node ++) {
        		resultValues.set(contextValue.getTypeReal().newValue(0), node);
        	}
        	return UtilGraph.newStateMap(forStates.clone(), resultValues);
        }else if(zeros.cardinality() == 0 ) {
        	Value resultValues = contextValue.getTypeReal().getTypeArray().newValue(forStates.size());
        	for(int node = 0; node < forStates.getStatesExplicit().cardinality(); node ++) {
        		resultValues.set(contextValue.getTypeReal().newValue(1), node);
        	}
        	return UtilGraph.newStateMap(forStates.clone(), resultValues);
        }
        Value results = solveNonLinearEquation(product, model, acc, zeros, posSCCs);
        return prodToOrigResult(results, product);
	}
	
	
    private StateMapExplicit prodToOrigResult(Value iterResult,
            GraphExplicit prodGraph) throws EPMCException {
        // TODO implement more cleanly
        assert iterResult != null;
        assert prodGraph != null;
        Type typeReal = contextValue.getTypeReal();
//        Value result = contextValue.newValueArrayWeight(modelGraph.getQueriedNodesLength());
        Value entry = typeReal.newValue();
        BitSet nodes = forStates.getStatesExplicit();
//        NodeProperty nodeAutomaton = prodGraph.getNodeProperty(CommonProperties.NODE_MODEL);
        Value resultValues = typeReal.getTypeArray().newValue(forStates.size());
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
    
	private boolean decideComponentUBAMCLeaf(GraphExplicit graph,
			BitSet leafSCC) throws EPMCException {
        AutomatonUBA automaton = graph.getGraphProperty(CommonProperties.AUTOMATON).getObject();
        EdgeProperty automatonLabel = graph.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        int numLabels = automaton.getNumLabels();
        BitSet accLabels = getContextValue().newBitSetBounded(numLabels);

    	/* new implementation , easy to understand */
        for (int node = leafSCC.nextSetBit(0); node >= 0; node = leafSCC.nextSetBit(node+1)) {
            graph.queryNode(node);                                         /* start node <s, q> */
            for(int succNr = 0; succNr < graph.getNumSuccessors() ; succNr ++) {
            	int succ = graph.getSuccessorNode(succNr);
            	
            	AutomatonUBALabel label =  automatonLabel.get(succNr).getObject();
            	if(leafSCC.get(succ) && label != null) {
                    accLabels.or(label.getLabel());
            	}
            }

            if(accLabels.cardinality() == numLabels) return true;
        }/* should meet all accepting labels */
		return false;
	}

	private boolean isBSCC(GraphExplicit model, BitSet ecc) 
			throws EPMCException {
        boolean isBSCC = true;

		for (int node = ecc.nextSetBit(0); node >= 0; node = ecc
				.nextSetBit(node + 1)) {
			model.queryNode(node);
			for (int succNr = 0; succNr < model.getNumSuccessors(); succNr++) {
				int succ = model.getSuccessorNode(succNr);
				if (!ecc.get(succ)) {
					isBSCC = false;
					break;
				}
			}
		}
        
        return isBSCC;
	}

	// rule out self loops
	private boolean isTrivialSCC(GraphExplicit graph, BitSet scc) 
			throws EPMCException {
    	if(scc.cardinality() != 1 ) return false;
    	int node = scc.nextSetBit(0); // the node in scc
    	graph.queryNode(node);
    	
    	for(int succNr = 0 ; succNr < graph.getNumSuccessors() ; succNr ++) {
    		int succ = graph.getSuccessorNode(succNr);
    		if(succ == node) return false; // successor contains itself
    	}
    	return true;
	}

    
	private BitSet computeProb0(GraphExplicit product, BitSet acc) 
			throws EPMCException {
		log.send(MessagesLTLUBA.LTL_UBA_COMPUTING_ZERO_STATES);
		StopWatch timer = Util.newStopWatch();
		product.computePredecessors();
		BitSet reachSome = contextValue.newBitSetUnbounded(product.getNumNodes());
		BitSet oldPrevs = acc.clone();
		while(oldPrevs.cardinality() != 0) { // some predecessors
			reachSome.or(oldPrevs);
			BitSet newPrevs = oldPrevs.clone();
			for(int node = newPrevs.nextSetBit(0)
					; node >= 0
					; node = newPrevs.nextSetBit(node + 1)
					) {
				product.queryNode(node);
				for(int preNr = 0; preNr < product.getNumPredecessors(); preNr ++) {
					int prev = product.getPredecessorNode(preNr);
					oldPrevs.set(prev);
				}
			}
			oldPrevs.andNot(reachSome);
		}
		BitSet nodes = contextValue.newBitSetUnbounded(product.getNumNodes());
		nodes.flip(0, product.getNumNodes());
		nodes.andNot(reachSome);
		log.send(MessagesLTLUBA.LTL_UBA_COMPUTING_ZERO_STATES_DONE, timer.getTimeSeconds());
		return nodes;
	}
    
    public Value solveNonLinearEquation(GraphExplicit graph, GraphExplicit model,BitSet acc
    		, BitSet zeros , List<BitSet> posSCCs) throws EPMCException {
    	ContextExpression context = graph.getContextExpression();
    	ContextValue contextValue = context.getContextValue();
        ConstraintSolverConfiguration configuration = new ConstraintSolverConfiguration(context);
        configuration.requireFeature(Feature.SMT);
        ConstraintSolver solver = configuration.newProblem();
        Type typeReal = contextValue.getTypeReal();
        Value one = typeReal.newValue(1);
        Value zero = typeReal.newValue(0);
        StopWatch timerLP =  Util.newStopWatch();
        int numVars = 0;
        int numConstraints = 0;
        Map<String, Integer> idMap = new HashMap<>();
        // add edge variables
        for(EdgeVariable edgeVar : edgeVariables) {
    		int id = solver.addVariable("e" + edgeVar.index
    				, typeReal, edgeVar.interval.getIntervalLower(), edgeVar.interval.getIntervalUpper());
        	idMap.put("e" + edgeVar.index, id);
        	numVars ++;
        }
        
        // add state variables
        BitSet undecided = contextValue.newBitSetUnbounded(graph.getNumNodes());
        undecided.or(zeros);
        undecided.flip(0, graph.getNumNodes());
        for(int node = undecided.nextSetBit(0)
        		; node >= 0 && node < graph.getNumNodes()
        		; node = undecided.nextSetBit(node + 1)) {
    		int id = solver.addVariable("s" + node, typeReal, zero, one);
        	idMap.put("s" + node, id);
        	numVars ++;
        }

        // the probabilities of product states mapped to the same model state
        // in accepting SCC should sum up to 1
        for(BitSet scc : posSCCs) {
        	// first get model states
        	Map<Integer, BitSet> stateMap = new HashMap<>();
        	NodeProperty nodeProp = graph.getNodeProperty(CommonProperties.NODE_MODEL);
        	for(int node = scc.nextSetBit(0)
        			; node >= 0
        			; node = scc.nextSetBit(node + 1)) {
        		graph.queryNode(node);
        		int modelState = nodeProp.getInt();
        		BitSet productStates = stateMap.get(modelState);
        		if(productStates == null) {
        			productStates = contextValue.newBitSetUnbounded(graph.getNumNodes());
        		}
        		productStates.set(node);
        		stateMap.put(modelState, productStates);
        	}
        	// 
        	for(Map.Entry<Integer, BitSet> entry : stateMap.entrySet()) {
            	StringBuilder sums = new StringBuilder();
            	BitSet states = entry.getValue();
            	sums.append("0");
            	for(int node = states.nextSetBit(0); node >= 0; node = states.nextSetBit(node + 1)) {
            		sums.append(" + s" + node);
            	}
            	solver.addConstraint(UtilModelChecker.parse(context, sums.toString() + " = 1" ));
            	numConstraints ++;
        	}
        }
        // threshold
        BitSet inits = graph.getInitialNodes();
        for(int node = inits.nextSetBit(0); node >= 0 ; node = inits.nextSetBit(node + 1)) {
        	solver.addConstraint(UtilModelChecker.parse(context, "s" + node + property.getCompareType() +  property.getCompare() ));
        	numConstraints ++;
        }
        
        // edge variables sum up to 1
        EdgeProperty edgeLabelPropModel = model.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
        EdgeProperty edgeWeightModel = model.getEdgeProperty(CommonProperties.WEIGHT);
        for(int node = 0; node < model.getNumNodes(); node ++) {
        	model.queryNode(node);
        	StringBuilder weightSums = new StringBuilder();
        	weightSums.append("0 ");
        	for(int succNr = 0; succNr < model.getNumSuccessors(); succNr ++) {
        		Value label = edgeLabelPropModel.get(succNr);
        		if(label.getInt() == -1) {
        			weightSums.append("+ " + edgeWeightModel.get(succNr).getIntervalLower());
        		}else {
        			weightSums.append("+ e" + label.getInt());
        		}
        	}
        	solver.addConstraint(UtilModelChecker.parse(context, weightSums.toString() + " = 1"));
        	numConstraints ++;
        }
        
        // question states, normal sum up
        EdgeProperty edgeLabelProp = graph.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
        EdgeProperty edgeWeight = graph.getEdgeProperty(CommonProperties.WEIGHT);
        for(int node = undecided.nextSetBit(0)
        		; node >= 0 && node < graph.getNumNodes()
        		; node = undecided.nextSetBit(node + 1)) {
        	graph.queryNode(node);
            StringBuilder probSums = new StringBuilder();
            probSums.append("0");
        	for(int succNr = 0; succNr < graph.getNumSuccessors(); succNr ++) {
        		int succ = graph.getSuccessorNode(succNr);
        		Value label = edgeLabelProp.get(succNr);
        		if(zeros.get(succ)) continue;   // ignore zero states
                if(label.getInt() != -1) {
                	probSums.append("+ s" + succ + " * " + "e" + label.getInt());
                }else {
                	probSums.append("+ s" + succ + " * " + edgeWeight.get(succNr).getIntervalLower());
                }
        	}
        	probSums.append(" = s" + node); // 
        	solver.addConstraint(UtilModelChecker.parse(context, probSums.toString()));
        	numConstraints ++;
        }
        log.send(MessagesLTLUBA.LTL_UBA_CONSTRUCT_LP_EQUATION_DONE, timerLP.getTimeSeconds());
        log.send(MessagesLTLUBA.LTL_UBA_LP_SOLVING);
        System.out.println("num-vairiable-lp-solve " + numVars);
        System.out.println("num-constraint-lp-solve " + numConstraints);
        StopWatch timer =  Util.newStopWatch();
        ConstraintSolverResult result = solver.solve();
        log.send(MessagesLTLUBA.LTL_UBA_LP_SOLVING_DONE, timer.getTimeSeconds());
        System.out.println("SATISFIABLE ? : " + result );
        Value values = typeReal.getTypeArray().newValue(graph.getNumNodes());
        if(result.isSat()) {
        	Value[] vals = solver.getResultVariablesValues();
        	for(int node = 0; node < graph.getNumNodes(); node ++) {
        		if(zeros.get(node)) {
        			values.set(zero, node);
        		}else {
        			values.set(vals[idMap.get("s" + node)], node);
        		}
        	}
        }else {
        	Value val = null;
        	if(property.getCompareType().isGe() || property.getCompareType().isGt()) {
        		val = zero;
        	}else {
        		val = one;
        	}
        	for(int node = 0; node < graph.getNumNodes(); node ++) {
        		values.set(val, node);
        	}
        }
		return values;
    }

	private GraphExplicit createProductModel(GraphExplicit model, Automaton automaton) 
			throws EPMCException {
		System.out.println("ltl-uba-construct-product-start");
		StopWatch timer = Util.newStopWatch();
        ProductGraphExplicit prodGraph = new ProductGraphExplicit.Builder()
			.setModel(model)
			.setModelInitialNodes(forStates.getStatesExplicit())
			.setAutomaton(automaton)
			.addGraphProperties(model.getGraphProperties())
			.addNodeProperty(CommonProperties.PLAYER)
			.addNodeProperty(CommonProperties.STATE)
			.addEdgeProperty(CommonProperties.WEIGHT)
			.addEdgeProperty(CommonProperties.TRANSITION_LABEL)
			.build();
        GraphExplicitWrapper graph = new GraphExplicitWrapper(prodGraph);
        graph.addDerivedGraphProperties(prodGraph.getGraphProperties());
        graph.addDerivedNodeProperty(CommonProperties.STATE);
        graph.addDerivedNodeProperty(CommonProperties.PLAYER);
        graph.addDerivedNodeProperty(CommonProperties.NODE_AUTOMATON);
        graph.addDerivedNodeProperty(CommonProperties.NODE_MODEL);
        graph.addDerivedEdgeProperty(CommonProperties.WEIGHT);
        graph.addDerivedEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        graph.addDerivedEdgeProperty(CommonProperties.TRANSITION_LABEL);

        graph.explore();
        log.send(MessagesLTLUBA.LTL_UBA_NUM_PRODUCT_STATES, graph.getNumNodes());
        System.out.println("ltl-uba-construct-product-done " + timer.getTimeSeconds());
        return graph;
	}
	
	private ContextValue getContextValue() {
		return context.getContextValue();
	}
	
	private GraphExplicit removeAllActions(GraphExplicit graph)
			throws EPMCException {
		
		GraphExplicitWrapper builder = new GraphExplicitWrapper(graph.getContextExpression());
		NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
		
		// graph properties
        for (Object property : graph.getGraphProperties()) {
        	Type type = graph.getGraphPropertyType(property);
        	builder.addSettableGraphProperty(property, type);
            Value value = graph.getGraphProperty(property);
            if (graph.getGraphProperties().contains(property)) {
                builder.setGraphProperty(property, value);
            }
        }
        // node properties 
        Set<Object> nodeProperties = graph.getNodeProperties();
        Set<Object> edgeProperties = graph.getEdgeProperties();
        
        for (Object property : nodeProperties) {
            Type type = graph.getNodePropertyType(property);
            builder.addSettableNodeProperty(property, type);
        }
        // edge properties
        for (Object property : edgeProperties) {
            Type type = graph.getEdgePropertyType(property);
            builder.addSettableEdgeProperty(property, type);
        }
        
        Type typeInteger = contextValue.getTypeInteger();
        builder.addSettableEdgeProperty(CommonProperties.TRANSITION_LABEL, typeInteger);
        EdgeProperty edgeProp = builder.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
        EdgeProperty weightProp = builder.getEdgeProperty(CommonProperties.WEIGHT);
        
		int counter = 0;
		Map<Integer, Integer> statesMap = new HashMap<>();
		Map<Integer, Integer> actionMap = new HashMap<>();
		int numTransitions = 0;
		for(int node = 0; node < graph.getNumNodes(); node ++) {
			graph.queryNode(node);
			if(isState.getBoolean()) { // it is a state
				assert graph.getNumSuccessors() == 1 : "not Markov chain";
				int succAction = graph.getSuccessorNode(0);
				actionMap.put(succAction, node);
				statesMap.put(node, counter ++);
			}else { // action
				int preNode = actionMap.get(node);
				int state = statesMap.get(preNode);
				builder.queryNode(state);
				int numSucc = graph.getNumSuccessors();
				builder.prepareNode(numSucc);
				for(int succNr = 0; succNr < numSucc; succNr ++) {
					int succ = graph.getSuccessorNode(succNr);
					Integer stateSucc = statesMap.get(succ);
					if(stateSucc == null) { // will not happen?
						stateSucc = ++ counter ;
						statesMap.put(succ, stateSucc);
					}
					builder.setSuccessorNode(succNr, stateSucc);
					for(Object property : edgeProperties) {
						Value value = graph.getEdgeProperty(property).get(succNr);
						builder.getEdgeProperty(property).set(value, succNr);
					}
					Value weightValue = weightProp.get(succNr).clone();
					assert weightValue.isInterval();
					Value lower = weightValue.getIntervalLower();
					Value upper = weightValue.getIntervalUpper();
					Value edgeIdxValue = typeInteger.newValue();
					if(lower.compareTo(upper) != 0) { //it is just a number
						edgeVariables.add(new EdgeVariable(numTransitions, weightValue));
						edgeIdxValue.set(numTransitions ++);
						assert edgeVariables.size() == numTransitions;
					}else {
						edgeIdxValue.set(-1);
					}
					edgeProp.set(edgeIdxValue, succNr);
				}
				// node prop
				graph.queryNode(preNode);
				for(Object property : nodeProperties) {
					Value value = graph.getNodeProperty(property).get();
					builder.getNodeProperty(property).set(value);
				}
			}
		}
		
		builder.getInitialNodes().or(graph.getInitialNodes());
		return builder;
	}
	
	// the number of enabled actions may not be one, so
	// give uniform distribution
	private GraphExplicit uniformizeMDP(GraphExplicit graph)
			throws EPMCException {
		
		GraphExplicitWrapper builder = new GraphExplicitWrapper(graph.getContextExpression());
		NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
		
		// graph properties
        for (Object property : graph.getGraphProperties()) {
        	Type type = graph.getGraphPropertyType(property);
        	builder.addSettableGraphProperty(property, type);
            Value value = graph.getGraphProperty(property);
            if (graph.getGraphProperties().contains(property)) {
                builder.setGraphProperty(property, value);
            }
        }
        // node properties 
        Set<Object> nodeProperties = graph.getNodeProperties();
        Set<Object> edgeProperties = graph.getEdgeProperties();
        
        for (Object property : nodeProperties) {
            Type type = graph.getNodePropertyType(property);
            builder.addSettableNodeProperty(property, type);
        }
        // edge properties
        for (Object property : edgeProperties) {
            Type type = graph.getEdgePropertyType(property);
            builder.addSettableEdgeProperty(property, type);
        }
        
        Type typeInteger = contextValue.getTypeInteger();
        builder.addSettableEdgeProperty(CommonProperties.TRANSITION_LABEL, typeInteger);
        EdgeProperty edgeProp = builder.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
        EdgeProperty weightProp = builder.getEdgeProperty(CommonProperties.WEIGHT);
        Type typeWeight =  contextValue.getTypeWeight();
        
		int numTransitions = 0;
		int counter = 0;
		Map<Integer, Integer> statesMap = new HashMap<>();
		Map<Integer, Integer> actionMap = new HashMap<>();
        Queue<Integer> statesTodo = new LinkedList<>();
        BitSet queriedNodes = contextValue.newBitSetUnbounded(graph.getNumNodes());
        BitSet initNodes = graph.getInitialNodes();
        for(int initNode = initNodes.nextSetBit(0); 
        		initNode >= 0; 
        		initNode = initNodes.nextSetBit(initNode + 1)) {
        	if(queriedNodes.get(initNode)) continue;
        	statesTodo.offer(initNode); // must be state
        	
        	while(! statesTodo.isEmpty()) {
        		int node = statesTodo.poll();
            	graph.queryNode(node);
            	assert isState.getBoolean();
				// node prop
            	Map<Object, Value> newNodeProps = new HashMap<>();
            	
				for(Object property : nodeProperties) {
					Value value = graph.getNodeProperty(property).get();
					newNodeProps.put(property, value);
				}
				
            	int numSucc = graph.getNumSuccessors();
    			Value valueProb = typeWeight.newValue(1);
    			valueProb.divide(valueProb, typeWeight.newValue(numSucc));
    			
    			Map<Object, Value> edgeProps = new HashMap<>();
    			for(int succNr = 0; succNr < numSucc; succNr ++) {
    				int succ = graph.getSuccessorNode(succNr);
    				builder.setSuccessorNode(succNr, succ);
    				for(Object property : edgeProperties) {
    					Value value = graph.getEdgeProperty(property).get(succNr);
    					builder.getEdgeProperty(property).set(value, succNr);
    				}
    				builder.getEdgeProperty(CommonProperties.WEIGHT).set(valueProb, succNr);
    				Value edgeIdxValue = typeInteger.newValue(-1);
    				edgeProp.set(edgeIdxValue, succNr);
    			}
        		
        	}

        	
        }
        
		
		for(int node = 0; node < graph.getNumNodes(); node ++) {
			graph.queryNode(node);
			if(isState.getBoolean()) { // it is a state, give probabilities
				builder.queryNode(node);
				int numSucc = graph.getNumSuccessors();
				builder.prepareNode(numSucc);
				Value valueProb = typeWeight.newValue(1);
				valueProb.divide(valueProb, typeWeight.newValue(numSucc));
				for(int succNr = 0; succNr < numSucc; succNr ++) {
					int succ = graph.getSuccessorNode(succNr);
					builder.setSuccessorNode(succNr, succ);
					for(Object property : edgeProperties) {
						Value value = graph.getEdgeProperty(property).get(succNr);
						builder.getEdgeProperty(property).set(value, succNr);
					}
					builder.getEdgeProperty(CommonProperties.WEIGHT).set(valueProb, succNr);
					Value edgeIdxValue = typeInteger.newValue(-1);
					edgeProp.set(edgeIdxValue, succNr);
				}
			}else { // action
				builder.queryNode(node);
				int numSucc = graph.getNumSuccessors();
				builder.prepareNode(numSucc);
				for(int succNr = 0; succNr < numSucc; succNr ++) {
					int succ = graph.getSuccessorNode(succNr);
					builder.setSuccessorNode(succNr, succ);
					for(Object property : edgeProperties) {
						Value value = graph.getEdgeProperty(property).get(succNr);
						builder.getEdgeProperty(property).set(value, succNr);
					}
					Value weightValue = weightProp.get(succNr).clone();
					assert weightValue.isInterval();
					edgeVariables.add(new EdgeVariable(numTransitions, weightValue));
					Value edgeIdxValue = typeInteger.newValue(numTransitions ++);
					assert edgeVariables.size() == numTransitions;
					edgeProp.set(edgeIdxValue, succNr);
				}
				// node prop
				for(Object property : nodeProperties) {
					Value value = graph.getNodeProperty(property).get();
					builder.getNodeProperty(property).set(value);
				}
				builder.getNodeProperty(CommonProperties.STATE).set(isState.getType().newValue(true));
			}
		}
		
		builder.getInitialNodes().or(graph.getInitialNodes());
		return builder;
	}
	
	private class EdgeVariable {
		public int index ;
		public Value interval;
		
		public EdgeVariable(int idx, Value interval) {
			this.index = idx;
			this.interval = interval;
		}
		
		public String toString() {
			return index + ":" + interval;
		}
	}

}

package epmc.unambiguous;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.algorithms.explicit.EndComponents;
import epmc.automaton.Automaton;
import epmc.automaton.ProductGraphExplicit;
import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.ConstraintType;
import epmc.constraintsolver.Feature;
import epmc.error.EPMCException;
import epmc.expression.CmpType;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsStandard;
import epmc.graph.StateMap;
import epmc.graph.StateMapExplicit;
import epmc.graph.StateSet;
import epmc.graph.StateSetExplicit;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitModifier;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.messages.Log;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.modelchecker.UtilModelChecker;
import epmc.options.Options;
import epmc.param.options.OptionsParam;
import epmc.param.plugin.BeforeModelLoadingPARAM;
import epmc.param.value.ContextValuePARAM;
import epmc.param.value.TypeFunction;
import epmc.param.value.ValueFunction;
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

public class PropertySolverParamLTLUBA implements PropertySolver {
	
	public final static String IDENTIFIER = "ltl-uba-param";
	
    protected ModelChecker modelChecker;
    protected ContextExpression context;
    protected ContextValue contextValue;
    protected GraphExplicit graph;
    protected Options options;
    protected Log log;
    protected StateSetExplicit forStates;
    protected Expression property;
	
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
        if (!semantics.isMarkovChain()) {
        	return false;
        }
        // right now only support for >= p or <= p
        if (!property.isQuantifier() || (property.getCompareType() != CmpType.GE
        		&& property.getCompareType() != CmpType.LE)) {
            return false;
        }
        if (property.getQuantified().isReward()) {
            return false;
        }
        if (!property.getContextValue().getTypeWeight().isReal()) {
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
    	Semantics semantic = graph.getGraphProperty(CommonProperties.SEMANTICS).getObject();
    	GraphExplicit model = null;
    	/* specify model, if it is CTMC, need normalization */
    	if(semantic.isCTMC()) {
		    model = buildEmbededDTMC();
    	}else {
    		model = graph;
    	}
    	model.explore();
//    	System.out.println("init: " + model.getInitialNodes() + " \nmodel: " + model.toString());
    	log.send(MessagesLTLUBA.LTL_UBA_NUM_MODEL_STATES, model.getNumNodes());
    	return checkProductModel(createProductModel(model, automaton), model);
	}

	private StateMap checkProductModel(GraphExplicit product,
			GraphExplicit model) throws EPMCException {
		System.out.println("ltl-uba-check-product-start");
        BitSet acc = contextValue.newBitSetUnbounded(product.getNumNodes());        
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
            BitSet origBSCC = contextValue.newBitSetUnbounded(model.getNumNodes());
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
//        System.out.println("product init: " + product.getInitialNodes() + "\nproduct target: " + acc + " \nproduct: " + product.toString());
        BitSet zeros = computeProb0(product, acc);
        if(acc.cardinality() == 0) {
        	Value resultValues = contextValue.getTypeReal().getTypeArray().newValue(forStates.size());
        	for(int node = 0; node < forStates.getStatesExplicit().cardinality(); node ++) {
        		resultValues.set(contextValue.getTypeReal().newValue(0), node);
        	}
        	return UtilGraph.newStateMap(forStates.clone(), resultValues);
        }else if(zeros.cardinality() == 0) {
        	Value resultValues = contextValue.getTypeReal().getTypeArray().newValue(forStates.size());
        	for(int node = 0; node < forStates.getStatesExplicit().cardinality(); node ++) {
        		resultValues.set(contextValue.getTypeReal().newValue(1), node);
        	}
        	return UtilGraph.newStateMap(forStates.clone(), resultValues);
        }
        return prodToOrigResult(solveNonLinearEquation(product, acc, zeros, posSCCs), product);
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
        BitSet accLabels = contextValue.newBitSetBounded(numLabels);

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

	// rule out trivial scc, i.e., no self loop
	private boolean isTrivialSCC(GraphExplicit graph, BitSet scc) 
			throws EPMCException {
    	if(scc.cardinality() != 1) return false;
    	int node = scc.nextSetBit(0); // the node in scc
    	graph.queryNode(node);
    	
    	for(int succNr = 0 ; succNr < graph.getNumSuccessors() ; succNr ++) {
    		int succ = graph.getSuccessorNode(succNr);
    		if(succ == node) return false; // successor contains itself
    	}
    	return true;
	}
    
    // use constraint solver to solve problem
    private Value solveNonLinearEquation(GraphExplicit graph, BitSet acc
    		, BitSet zeros, List<BitSet> posSCCs) 
    		throws EPMCException {
        ConstraintSolverConfiguration configuration = new ConstraintSolverConfiguration(context);
        configuration.requireFeature(Feature.SMT);
        ConstraintSolver solver = configuration.newProblem();
        Expression threshold = property.getCompare();
        Type typeReal = contextValue.getTypeReal();
        Value one = typeReal.newValue(1);
        Value zero = typeReal.newValue(0);
        int numVars = 0;
        int numConstraints = 0;
        // add constraints
        StopWatch timerLP =  Util.newStopWatch();
        Map<String, Integer> idMap = new HashMap<>();
        int id ;
        // add parameters
        List<String> parameters = (List<String>) options.getOption(OptionsParam.PARAM_PARAMETER).get();
        for(String param : parameters) {
        	id = solver.addVariable(param, typeReal, zero, one);
        	idMap.put(param, id);
        	numVars ++;
        }
        
        BitSet undecided = contextValue.newBitSetUnbounded(graph.getNumNodes());
        undecided.or(zeros);
        undecided.flip(0, graph.getNumNodes());
        
        // add state variables
        for(int node = undecided.nextSetBit(0)
        		; node >= 0 && node < graph.getNumNodes()
        		; node = undecided.nextSetBit(node + 1)) {
        	id = solver.addVariable("s" + node, typeReal, zero, one);
        	idMap.put("s" + node, id);
        	numVars ++;
        }
        
        EdgeProperty edgeProp = graph.getEdgeProperty(CommonProperties.WEIGHT);
        BitSet inits = graph.getInitialNodes();
        for(int node = inits.nextSetBit(0); node >= 0 ; node = inits.nextSetBit(node + 1)) {
        	solver.addConstraint(UtilModelChecker.parse(context, "s" + node + property.getCompareType() +  threshold ));
        	numConstraints ++;
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
        
        // add normal constraints according to transition system
        Set<Expression> weightConstraints = new HashSet<>();
        for(int node = undecided.nextSetBit(0)
        		; node >= 0 && node < graph.getNumNodes()
        		; node = undecided.nextSetBit(node + 1)) {
        	graph.queryNode(node);
            StringBuilder sums = new StringBuilder();
            boolean first = true;
        	for(int succNr = 0; succNr < graph.getNumSuccessors(); succNr ++) {
        		int succ = graph.getSuccessorNode(succNr);
        		if(zeros.get(succ)) continue;
        		ValueFunction weight = (ValueFunction) edgeProp.get(succNr).clone();
        		if(! weight.isConstant()) {
        			Expression lower = UtilModelChecker.parse(context, weight + " > 0" );
        			Expression upper = UtilModelChecker.parse(context, weight + " <= 1" );
            		if(! weightConstraints.contains(lower)) {
            			solver.addConstraint(lower);
            			weightConstraints.add(lower);
            			numConstraints ++;
            		}
            		if(! weightConstraints.contains(upper)) {
            			solver.addConstraint(upper);
            			weightConstraints.add(upper);
            			numConstraints ++;
            		}
        		}
        		String mulStr = "s" + succ + "*" + "(" + weight + ")";
        		if(first) {
        			sums.append(mulStr);
        			first = false;
        		}
        		else sums.append("+" + mulStr);
        	}
        	// it is not possible that it did not add anything, which concludes that this state is zero state 
            solver.addConstraint(UtilModelChecker.parse(context, "s" + node + " = " + sums.toString()));
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
		StopWatch timer = Util.newStopWatch();
		System.out.println("ltl-uba-build-product-start");
        ProductGraphExplicit prodGraph = new ProductGraphExplicit.Builder()
			.setModel(model)
			.setModelInitialNodes(forStates.getStatesExplicit())
			.setAutomaton(automaton)
			.addGraphProperties(model.getGraphProperties())
			.addNodeProperty(CommonProperties.PLAYER)
			.addNodeProperty(CommonProperties.STATE)
			.addEdgeProperty(CommonProperties.WEIGHT)
			.build();
        GraphExplicitWrapper graph = new GraphExplicitWrapper(prodGraph);
        graph.addDerivedGraphProperties(prodGraph.getGraphProperties());
        graph.addDerivedNodeProperty(CommonProperties.STATE);
        graph.addDerivedNodeProperty(CommonProperties.PLAYER);
        graph.addDerivedNodeProperty(CommonProperties.NODE_AUTOMATON);
        graph.addDerivedNodeProperty(CommonProperties.NODE_MODEL);
        graph.addDerivedEdgeProperty(CommonProperties.WEIGHT);
        graph.addDerivedEdgeProperty(CommonProperties.AUTOMATON_LABEL);

        graph.explore();
        log.send(MessagesLTLUBA.LTL_UBA_NUM_PRODUCT_STATES, graph.getNumNodes());
        System.out.println("ltl-uba-build-product-done " + timer.getTimeSeconds());
        return graph;
	}

	private GraphExplicit buildEmbededDTMC() throws EPMCException {
		graph.explore();
		/* need explore, otherwise initial states will be empty*/
		GraphExplicitWrapper modelWrapper = new GraphExplicitWrapper(graph);
		modelWrapper.addDerivedNodeProperties(graph.getNodeProperties());
		modelWrapper.addDerivedEdgeProperties(graph.getEdgeProperties());
		modelWrapper.addDerivedGraphProperties(graph.getGraphProperties());
		modelWrapper.explore();
		/* need to explore, otherwise no nodes in graph */
		GraphExplicitModifier.embed(modelWrapper);
		
		modelWrapper.setGraphProperty(CommonProperties.SEMANTICS, SemanticsStandard.DTMC);
		System.out.println("Done. Converting CTMC into its embeded DTMC ...");
		return modelWrapper;
	}


}

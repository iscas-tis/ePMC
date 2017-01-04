package epmc.unambiguous;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.algorithms.dd.ComponentsDD;
import epmc.automaton.Automaton;
import epmc.automaton.ProductGraphDD;
import epmc.automaton.ProductGraphDDDD;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.CmpType;
import epmc.expression.ContextExpression;
import epmc.expression.DirType;
import epmc.expression.Expression;
import epmc.expression.ExpressionToDD;
import epmc.expression.UtilExpression;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderDD;
import epmc.graph.Semantics;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.StateSetDD;
import epmc.graph.UtilGraph;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.GraphDDProperties;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.GraphSolverConfigurationDD;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveDDUnboundedReachability;
import epmc.messages.Log;
import epmc.messages.MessagesEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.options.Options;
import epmc.unambiguous.automaton.AutomatonDDUBA;
import epmc.unambiguous.automaton.AutomatonUBA;
import epmc.unambiguous.options.OptionsLTLUBA;
import epmc.unambiguous.util.ComponentDD;
import epmc.unambiguous.util.UtilUBA;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.Util;
import epmc.value.Operator;
import epmc.value.Value;

public final class PropertySolverDDLTLUBA implements PropertySolver {

    public final static String IDENTIFIER = "ltl-uba-dd";

    private ModelChecker modelChecker;
    private Log log;
    private GraphDD modelGraph;
    private ExpressionToDD expressionToDD;
    private Options options;
    private ContextDD contextDD;
	private Expression property;
	private StateSet forStates;
    
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
	}

	@Override
	public void setForStates(StateSet forStates) {
		this.forStates = forStates;		
	}

	@Override
	public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        Semantics semantics = modelChecker.getModel().getSemantics();
        if (!semantics.isMarkovChain()) {
        	return false;
        }
        if (!property.isQuantifier()) {
            return false;
        }
        if (property.getQuantified().isReward()) {
            return false;
        }
        if (!property.getContextValue().getTypeWeight().isReal()) {
        	return false;
        }
        Set<Expression> inners = UtilUBA.collectLTLInner(property.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
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
    	required.add(CommonProperties.EXPRESSION_TO_DD);
    	return Collections.unmodifiableSet(required);
	}

	@Override
	public Set<Object> getRequiredNodeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.STATE);
    	required.add(CommonProperties.PLAYER);
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
        if (modelChecker.getEngine() instanceof EngineDD) {
        	this.modelGraph = modelChecker.getLowLevel();
            this.expressionToDD = modelGraph.getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
        }
        this.options = modelChecker.getOptions();
        this.contextDD = modelChecker.getModel().getContextDD();
        this.log = this.options.get(OptionsMessages.LOG);
        Expression quantifiedProp = property.getQuantified();
        Set<Expression> inners = UtilUBA.collectLTLInner(quantifiedProp);
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            StateMapDD innerResult = (StateMapDD) modelChecker.check(inner, allStates);
            ExpressionToDD expressionToDD = modelGraph
                    .getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
            expressionToDD.putConstantWith(inner, innerResult.getValuesDD());
        }
        allStates.close();
        StateMap result = doSolve(quantifiedProp);
        if (property.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(property.getCompare(), forStates);
            Operator op = property.getCompareType().asExOpType(contextDD.getContextValue());
            result = result.applyWith(op, compare);
        }
        return result;
	}

	private StateMap doSolve(Expression expression) throws EPMCException {
    	StopWatch timer = Util.newStopWatch();
    	DD modelStates = modelGraph.getNodeProperty(CommonProperties.STATE);
    	Permutation bddPerm = modelGraph.getSwapPresNext();
    	AutomatonDDUBA automaton = new AutomatonDDUBA(expressionToDD, expression, modelStates, bddPerm);
    	log.send(MessagesLTLUBA.LTL_UBA_BUILD_DONE, timer.getTimeSeconds());
    	Semantics semantic = modelGraph.getGraphProperty(CommonProperties.SEMANTICS).getObject();
    	GraphDD model = null;
    	/* specify model, if it is CTMC, need normalization */
    	if(semantic.isCTMC()) {
		    model = buildEmbededDTMC(modelGraph);
    	}else {
    		model = modelGraph;
    	}
    	log.send(MessagesLTLUBA.LTL_UBA_NUM_MODEL_STATES, model.getNumNodes());
		return checkProductModel(createProductModel(model, automaton), model, automaton);
	}
	
    private StateMap checkProductModel(ProductGraphDD product,
			GraphDD model, AutomatonDDUBA automaton) throws EPMCException {
		DD nodeSpace = product.getNodeSpace().clone();
         /*may introduce action variables in nodespace */
        ComponentDD sccs = new ComponentDD(product, nodeSpace, false, false);
        
        DD acc = contextDD.newConstant(false);  /** initial empty set */
        int numSCCs = 0;
        DD decided  = contextDD.newConstant(false);
        List<DD> posSCCs = new ArrayList<>();
        log.send(MessagesLTLUBA.LTL_UBA_COMPUTING_END_COMPONENTS);
        for (DD component = sccs.next(decided); component != null; component = sccs.next(decided)) {
            //rule out trivial scc
            if(isTrivialSCC(product, component.clone())) continue;
            numSCCs++;
            /** 1 it must be some BSCC in modelGraph */
            assert automaton.getLabelCube().isTrue();
            DD leafSCC = component.abstractExist(automaton.getPresCube());
            if(! isBSCC(model, leafSCC)) continue;
            /* may contain action variables, but I think it is OK */
            DD reachStates = succs(product, component, product.getTransitions());
            decided = decided.orWith(reachStates);
            /** 2 it should be accepted */
            if(! decideComponentMCLeaf(automaton, component)) continue;
            posSCCs.add(component.clone());
            acc = acc.orWith(component);
        }
        
        log.send(MessagesLTLUBA.LTL_UBA_COMPUTING_END_COMPONENTS_DONE, numSCCs);
        
        decided.dispose();
        sccs.close();
        
        DD reachProbs = null;
        if(options.get(OptionsLTLUBA.LTL_UBA_LP_SOLVE_METHOD) == OptionsLTLUBA.LPMethod.ITERATION){
        	reachProbs = computeReachProbs(product, acc, nodeSpace);
        }else {
        	reachProbs = solveLinearEquation(product, acc, posSCCs, nodeSpace);
        }
        
        nodeSpace.dispose();
        acc.dispose();
        reachProbs = reachProbs.multiplyWith(product.getInitialNodes().toMT());
        //reachProbs = reachProbs.abstractSumWith(automaton.getPresCube().clone());
        reachProbs = abstractAutomatonVarsWith(automaton, reachProbs);
        
        product.close();
        automaton.close();
        for(DD scc : posSCCs) {
        	scc.dispose();
        }
//        if(graph.isCTMC() && model != null) {
//        	model.close();
//        }

        return UtilGraph.newStateMap((StateSetDD)forStates.clone(), reachProbs);
	}

	private ProductGraphDD createProductModel(GraphDD model, AutomatonDDUBA automaton) 
			throws EPMCException {
		
		StateSetDD initStates = (StateSetDD)forStates;
        ProductGraphDD product = new ProductGraphDDDD(model, initStates.getStatesDD(), automaton);
        
        DD nodeSpace = product.getNodeSpace().clone();
        DD presAndAction = product.getPresCube().and(product.getActionCube());
        BigInteger numModelStates = nodeSpace.countSat(product.getPresCube());
        int numAutomatonStates;
        DD nodeSpaceEx = nodeSpace.abstractExist(presAndAction);
        numAutomatonStates = nodeSpaceEx.countSat(automaton.getPresCube()).intValue();
        nodeSpaceEx.dispose();
        nodeSpace.dispose();
        presAndAction.dispose();
        log.send(MessagesLTLUBA.LTL_UBA_NUM_AUTOMATON_STATES, numAutomatonStates);
        log.send(MessagesLTLUBA.LTL_UBA_NUM_PRODUCT_STATES, numModelStates);
		return product;
	}

	private GraphDD buildEmbededDTMC(GraphDD graph) throws EPMCException {
//    	/* no need to clone*/
    	DD weights = graph.getEdgeProperty(CommonProperties.WEIGHT);
//    	/* normalize probabilities */
    	DD sum = weights.abstractSum(graph.getNextCube().and(graph.getActionCube()));
    	weights = weights.divideIgnoreZeroWith(sum);
    	graph.registerEdgeProperty(CommonProperties.WEIGHT, weights);
    	return graph;
	}

	private boolean decideComponentMCLeaf(AutomatonDDUBA automaton, DD component) 
    		throws EPMCException {
		// TODO Auto-generated method stub
    	List<DD> labels = automaton.getLabels();
    	boolean isAccepted = true;
    	for(int labelNr = 0 ; labelNr < labels.size() ; labelNr ++) {
    		DD label = labels.get(labelNr);
    		if(label.and(component).isFalseWith()) {
    			isAccepted  = false;
    			break;
    		}
    	}
    	if(isAccepted) {
    		log.send(MessagesLTLUBA.LTL_UBA_SCC_ACCEPTED);
    	}
		return isAccepted;
	}
	
	
    private DD abstractAutomatonVarsWith(AutomatonDDUBA automatonDD, DD reachProb) 
    		throws EPMCException {
    	
    	Set<VariableDD> reachVarSet = contextDD.highLevelSupport(reachProb);
    	
    	List<DD> automatonPreVars = automatonDD.getPresVars();
    	HashSet<DD> automatonVarSet = new HashSet<>();
    	for(DD var : automatonPreVars) {
    		automatonVarSet.add(var);
    	}
    	DD automatonCube = contextDD.newConstant(true);
    	
    	for(VariableDD ddVars : reachVarSet) {
    		for (DD dd : ddVars.getDDVariables(0)) {
    			if(automatonVarSet.contains(dd)) {
    				automatonCube = automatonCube.andWith(dd.clone());
    			}
    		}
    	}
    	automatonVarSet.clear();
    	reachProb = reachProb.abstractSumWith(automatonCube);
		return reachProb;
    }

    private boolean isBSCC(GraphDD graph, DD scc) throws EPMCException {
    	DD next = post(graph, scc, graph.getTransitions());
    	return next.andNotWith(scc).isFalseWith(); /* also dispose scc */
    }
    
    private boolean isTrivialSCC(GraphDD graph, DD scc) throws EPMCException {
    	DD next = post(graph, scc, graph.getTransitions());
    	return next.andWith(scc).isFalseWith(); /* also dispose scc */
    }

    private DD post(GraphDD graph, DD nodes, DD trans) throws EPMCException {
        Permutation nextToPres = graph.getSwapPresNext(); 
        DD presAndAction = graph.getPresCube().and(graph.getActionCube());
        DD result = trans.abstractAndExist(nodes,  presAndAction); 
        result = result.permuteWith(nextToPres);
        presAndAction.dispose();
        return result;
    }
    
    private DD pre(GraphDD graph, DD trans, DD nodes) throws EPMCException {
        Permutation nextToPres = graph.getSwapPresNext(); 
        DD primedNodes = nodes.permute(nextToPres);
        DD nextAndAction = graph.getNextCube().and(graph.getActionCube());
        DD result = trans.abstractAndExist(primedNodes, nextAndAction); 
        nextAndAction.dispose();
        return result;
    }
    
    private DD succs(GraphDD graph, DD from, DD trans) 
    		throws EPMCException {
    	DD oldNodes = contextDD.newConstant(false);
    	DD newNodes = from.clone();
    	
    	while(! newNodes.equals(oldNodes)) {
    		oldNodes.dispose();
    		oldNodes = newNodes.clone();
    		DD succ = post(graph, oldNodes, trans);
    		newNodes = newNodes.orWith(succ);
    	}
    	oldNodes.dispose();
    	return newNodes;
    }
    
    private DD computeReachProbs(GraphDD graphDD, DD target, DD nodeSpace)
            throws EPMCException {
        GraphSolverConfigurationDD configuration = UtilGraphSolver.newGraphSolverConfigurationDD(graphDD.getOptions());
        List<DD> sinks = new ArrayList<>();
        DD someNodes = ComponentsDD.reachMaxSome(graphDD, target, nodeSpace).andNotWith(target.clone());
        DD zeroNodes = nodeSpace.clone().andNotWith(someNodes).andNotWith(target.clone());
        DD init = graphDD.getInitialNodes();
        if (init.andNot(target).isFalseWith()
                || init.andNot(zeroNodes).isFalseWith()) {
            zeroNodes.dispose();
            return target.toMT();
        }
        sinks.add(zeroNodes);
        sinks.add(target);
        configuration.setGraph(graphDD);
        configuration.setTargetStates(target);
        GraphSolverObjectiveDDUnboundedReachability objective = new GraphSolverObjectiveDDUnboundedReachability();
        objective.setMin(false);
        configuration.setObjective(objective);
        configuration.setSinkStatesDD(sinks);
        configuration.solve();
        zeroNodes.dispose();
        return configuration.getOutputValuesDD();
    }
    
    // first convert DD to explicit, then use LP solver
    private DD solveLinearEquation(GraphDD graphDD, DD targetDD
    		, List<DD> posSCCs, DD nodeSpace) throws EPMCException {
        List<DD> sinks = new ArrayList<>();
        DD someNodes = ComponentsDD.reachMaxSome(graphDD, targetDD, nodeSpace).andNotWith(targetDD.clone());
        DD zeroNodes = nodeSpace.clone().andNotWith(someNodes).andNotWith(targetDD.clone());
//        sinks.add(zeroNodes);
        @SuppressWarnings("resource")
		GraphBuilderDD builderDD = new GraphBuilderDD(graphDD, sinks, false);
        GraphExplicit product = builderDD.buildGraph();
        BitSet targetBits = builderDD.ddToBitSet(targetDD);
        System.out.println("target: " + targetBits);
        
        BitSet zeros = builderDD.ddToBitSet(zeroNodes);
        List<BitSet> posSCCsBits = new ArrayList<>();
        for(DD scc : posSCCs) {
        	posSCCsBits.add(builderDD.ddToBitSet(scc));
        }
        log.send(MessagesLTLUBA.LTL_UBA_LP_SOLVING);
        StopWatch timer =  Util.newStopWatch();
        Value resultBits = PropertySolverExplicitLTLUBA.solveLinearEquation(
        		product, targetBits, zeros, posSCCsBits, options);
        log.send(MessagesLTLUBA.LTL_UBA_LP_SOLVING_DONE, timer.getTimeSeconds());
        DD resultDD = builderDD.valuesToDD(resultBits);
        resultDD = resultDD.multiplyWith(graphDD.getNodeSpace().toMT());
        resultDD = resultDD.addWith(targetDD.andNot(graphDD.getNodeSpace()).toMTWith());
    	return resultDD;
    }


}

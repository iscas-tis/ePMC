package epmc.propertysolverltlfg;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.algorithms.dd.ComponentsDD;
import epmc.automaton.AutomatonDD;
import epmc.automaton.ProductGraphDD;
import epmc.automaton.ProductGraphDDDD;
import epmc.automaton.ProductGraphDDExplicit;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderDD;
import epmc.graph.Semantics;
import epmc.graph.SemanticsNonDet;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.StateSetDD;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.options.Options;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
import epmc.propertysolverltlfg.automaton.LTL2DA;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorNot;
import epmc.value.ValueArray;
/**
 * input formula will be LTL
 * @author Yong Li
 */
public abstract class PropertySolverDDLTL implements PropertySolver {
    public final static String IDENTIFIER = "ltl-auto-dd";

    protected ModelChecker modelChecker;
    protected Log log;
    protected GraphDD modelGraph;
    protected ExpressionToDD expressionToDD;
    protected boolean nonDet;
    protected Options options;
    protected Semantics type;
    protected Set<Expression> stateLabels ;
    protected boolean skipTransient;
    protected boolean mecComputation;
	private Expression property;
	private ExpressionQuantifier propertyQuantifier;
	private StateSet forStates;
	private ContextDD contextDD ;
    
    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
    }

    private StateMap solve(Expression property, StateSet forStates, boolean min)
            throws EPMCException {
        Expression expression = null;
        if(min) {
        	expression = not(forStates.getContextValue(), property);
        }else {
        	expression = property;
        }
        AutomatonRabin rabin = constructRabin(expression);
        StateSetDD forStatesDD = (StateSetDD) forStates;
        DD result = checkProperty(rabin, forStatesDD, min);
        return new StateMapDD(forStatesDD.clone(), result);
    }
    
    abstract AutomatonType getAutoType() ;
    AutomatonRabin constructRabin(Expression expression) 
    		throws EPMCException {
    	return LTL2DA.convertLTL2DA(expression, getAutoType(), true, false);
    }
    abstract AutomatonDD constructRabinDD(AutomatonRabin rabin, DD modelStates) 
    		throws EPMCException;

    private DD checkProperty(AutomatonRabin rabin, StateSetDD forStates, boolean negate)
            throws EPMCException {
        DD innerResult = checkTemporalLTL(rabin, forStates);
        if (innerResult == null) {
            return null;
        }
        if (negate) {
            innerResult = getContextDD().newConstant(1).subtractWith(innerResult);
        }
        
        return innerResult;
    }

    protected boolean checkReachOne(DD component, ProductGraphDDDD subsetProduct,
            DD leafSCC, GraphDD product, DD productSpace)
                    throws EPMCException {
        // states reaching this leaf component with probability 1
        DD reaching = ComponentsDD.reachMaxOne(product, leafSCC, productSpace);
        // variables only of Rabin and not of other
        DD removeVars = reaching.supportDD().abstractExistWith(leafSCC.supportDD());
        // attach subset variables if necessary
        if (product instanceof ProductGraphDDExplicit) {
//            AutomatonDDSubset automatonSubset = (AutomatonDDSubset) subsetProduct.getAutomaton();
//            List<VariableDD> variables = automatonSubset.getStateVariables();
//            reaching = reaching.andWith(rabinSubsetToDD((ProductGraphDDExplicit) product, variables));
        }
        // abstracting to state set in subset automaton
        reaching = reaching.abstractExistWith(removeVars);
        // checking if component contains any states reach with prob 1
        return !component.clone().andWith(reaching).isFalseWith();
    }


    private DD checkTemporalLTL( AutomatonRabin rabin,
            StateSetDD forStates) throws EPMCException {

        DD modelStates = modelGraph.getNodeProperty(CommonProperties.STATE);
        AutomatonDD automatonDD = constructRabinDD(rabin, modelStates);
        //ProductGraphDDExplicit()
        ProductGraphDD product = new ProductGraphDDDD(modelGraph, forStates.getStatesDD(), automatonDD);
        DD nodeSpace = product.getNodeSpace();
        DD productStates = product.getNodeProperty(CommonProperties.STATE);
        DD nodeSpaceAndSpace = nodeSpace.and(productStates);
        BigInteger numModelStates = nodeSpaceAndSpace.countSat(product.getPresCube());
        nodeSpaceAndSpace.dispose();
        int numAutomatonStates;
        
        DD nodeSpaceEx = nodeSpace.abstractExist(modelGraph.getPresCube());
        numAutomatonStates = nodeSpaceEx.countSat(automatonDD.getPresCube()).intValue();
        nodeSpaceEx.dispose();
        
        log.send(MessagesLTLTGRA.LTL_TGRA_EXPLORING_STATE_SPACE_DONE, numModelStates, numAutomatonStates);
        log.send(MessagesLTLTGRA.LTL_TGRA_COMPUTING_END_COMPONENTS);

        DD oneNodes = computeAcceptedMECs(product, automatonDD, nodeSpace);

        oneNodes = oneNodes.abstractExistWith(automatonDD.getLabelCube().clone());
        DD reachProbs = computeReachProbs(product, oneNodes, nodeSpace);
        nodeSpace.dispose();
        oneNodes.dispose();
        reachProbs = reachProbs.multiplyWith(product.getInitialNodes().toMT());
        reachProbs = reachProbs.abstractSumWith(automatonDD.getPresCube().clone());

        product.close();
        if (automatonDD != null) {
            automatonDD.close();
        }
        return reachProbs;
    }

    
    abstract DD computeAcceptedMECs(ProductGraphDD product, AutomatonDD automatonDD, DD nodeSpace) 
             throws EPMCException;

    private DD computeReachProbs(GraphDD graphDD, DD target, DD nodeSpace)
            throws EPMCException {
//        target = ComponentsDD.reachMaxOne(graphDD, target, nodeSpace);
        DD someNodes = ComponentsDD.reachMaxSome(graphDD, target, nodeSpace).andNot(target);
        DD zeroNodes = nodeSpace.andNot(someNodes).andNot(target);
        
        DD init = graphDD.getInitialNodes();
        
        if (init.andNot(target).isFalse() || init.andNot(zeroNodes).isFalse()) {
            return target.toMT();
        }

        List<DD> sinks = new ArrayList<>();
        sinks.add(zeroNodes);
        sinks.add(target);
        Semantics semantics = graphDD.getGraphPropertyObject(CommonProperties.SEMANTICS);
        GraphBuilderDD converter = new GraphBuilderDD(graphDD, sinks, SemanticsNonDet.isNonDet(semantics));
        GraphExplicit graph = converter.buildGraph();
        BitSet targets = converter.ddToBitSet(target);
        BitSet targetS = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
        
        for (int nodeNr = 0; nodeNr < graph.getNumNodes(); nodeNr++) {
            targetS.set(nodeNr, targets.get(nodeNr));
        }
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit(graph.getOptions());
        GraphSolverObjectiveExplicitUnboundedReachability objective = new GraphSolverObjectiveExplicitUnboundedReachability();
        objective.setGraph(graph);
        objective.setMin(false);
        objective.setTarget(targetS);
        configuration.setObjective(objective);
        configuration.solve();
        ValueArray values = objective.getResult();
        DD result = converter.valuesToDD(values);
        converter.close();
        result = result.multiply(graphDD.getNodeSpace().toMT());
        result = result.add(target.andNot(graphDD.getNodeSpace()).toMT());
        
        return result;
    }
    
    @Override
    public StateMap solve() throws EPMCException {
        if (modelChecker.getEngine() instanceof EngineDD) {
        	this.modelGraph = modelChecker.getLowLevel();
            this.expressionToDD = modelGraph.getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
        }
        this.type = modelChecker.getModel().getSemantics();
        this.nonDet = SemanticsNonDet.isNonDet(this.type);
        this.options = modelChecker.getModel().getContextValue().getOptions();
        this.log = this.options.get(OptionsMessages.LOG);
        this.skipTransient = options.getBoolean(OptionsLTLTGRA.LTL_TGRA_SCC_SKIP_TRANSIENT);
        this.mecComputation = options.getBoolean(OptionsLTLTGRA.LTL_TGRA_MEC_COMPUTATION);
        this.contextDD = modelGraph.getContextDD();
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        Set<Expression> inners = UtilLTL.collectLTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            StateMapDD innerResult = (StateMapDD) modelChecker.check(inner, allStates);            
            ExpressionToDD expressionToDD = modelGraph
                    .getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
            expressionToDD.putConstantWith(inner, innerResult.getValuesDD());
        }
        this.stateLabels = inners;
        allStates.close();
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        boolean min = dirType == DirType.MIN;
        StateMap result = solve(quantifiedProp, forStates, min);
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (propertyQuantifier.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType(contextDD.getContextValue());
            result = result.applyWith(op, compare);
        }
        return result;
    }
    
    protected boolean canHandle(Expression property) { return true; }
    
    @Override
    public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        if (propertyQuantifier.getQuantified() instanceof ExpressionReward) {
            return false;
        }
        Set<Expression> inners = UtilLTL.collectLTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
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
	public void setProperty(Expression property) {
		this.property = property;
		if (property instanceof ExpressionQuantifier) {
			this.propertyQuantifier = (ExpressionQuantifier) property;
		}
	}

	@Override
	public void setForStates(StateSet forStates) {
		this.forStates = forStates;
	}

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }    
    
    public ContextDD getContextDD() throws EPMCException {
    	return ContextDD.get(modelChecker.getModel().getContextValue());
	}

    private static Expression not(ContextValue contextValue, Expression expression) {
    	return new ExpressionOperator.Builder()
        	.setOperator(contextValue.getOperator(OperatorNot.IDENTIFIER))
        	.setOperands(expression)
        	.build();
    }
}

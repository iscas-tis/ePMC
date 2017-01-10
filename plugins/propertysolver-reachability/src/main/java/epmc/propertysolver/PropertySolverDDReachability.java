/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.propertysolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.algorithms.dd.ComponentsDD;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.TemporalType;
import epmc.expression.standard.TimeBound;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderDD;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDTMC;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.StateSetDD;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBounded;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedReachability;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.util.BitSet;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorNot;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueReal;

/**
 * This class implements a propertysolver which uses a BDD data structure
 * to compute reachability probability in a DTMC, the formula is in form of P=? [F a] 
 * 
 * @author Yong Li
 * 
 * */

public final class PropertySolverDDReachability implements PropertySolver {
    public final static String IDENTIFIER = "reachability-dd";
    private ModelChecker modelChecker;
    private GraphDD graph;
    private ExpressionToDD expressionToDD;
    private Expression property;
	private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
    }

    private DD doSolve(Expression property, StateSet forStates)
            throws EPMCException {
        
        DD nodeSpace = graph.getNodeSpace();
        ExpressionTemporal propertyTemporal = (ExpressionTemporal) property;
        // get a out of F a
        Expression innerProposition = propertyTemporal.getOperand1();
        // translate a to a BDD representation
        DD rightDD = expressionToDD.translate(innerProposition);
        DD oneStatesDD = rightDD.and(nodeSpace);
        
        rightDD.dispose();
        DD someDD = ComponentsDD.reachPre(graph, oneStatesDD, nodeSpace, false, false);
        // those states can not reach one states are zeroStates
        DD zeroStatesDD = nodeSpace.andNot(someDD);
        DD result;
        
        result = computeProbability(oneStatesDD, zeroStatesDD);
        return result;
    }

    private DD computeProbability(DD oneStatesDD, DD zeroStatesDD)
            throws EPMCException {
        DD nodeSpace = graph.getNodeSpace();
        List<DD> sinks = new ArrayList<>();
        
        // compute sink states
        zeroStatesDD = zeroStatesDD.orWith(nodeSpace.not());
        sinks.add(zeroStatesDD);
        sinks.add(oneStatesDD);

        // converter which is able to convert DD graph to explicit graph
        GraphBuilderDD converter = new GraphBuilderDD(graph, sinks, false);

        GraphExplicit graph = converter.buildGraph();
        BitSet targetBitSet = converter.ddToBitSet(oneStatesDD);
        
        ValueArrayAlgebra values = UtilReachability.computeReachabilityProbability(graph, targetBitSet);
        DD result = converter.valuesToDD(values);

        return result;
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
    public StateMap solve() throws EPMCException {
        if (modelChecker.getEngine() instanceof EngineDD) {
        	this.graph = modelChecker.getLowLevel();
            this.expressionToDD = graph.getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
        }
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        Set<Expression> inners = UtilReachability.collectReachabilityInner(quantifiedProp);
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        // model check the satisfaction of innners
        for (Expression inner : inners) {
            StateMapDD innerResult = (StateMapDD) modelChecker.check(inner, allStates);
            ExpressionToDD expressionToDD = graph
                    .getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
            expressionToDD.putConstantWith(inner, innerResult.getValuesDD());
        }
        allStates.close();
        
        DD resultDD = doSolve(quantifiedProp, forStates);
       
        // store the results in StateMap
        StateMap result = new StateMapDD((StateSetDD) forStates.clone(), resultDD);
        return result;
    }


    @Override
    public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        
        Semantics semantics = modelChecker.getModel().getSemantics();
        // only allow DTMC
        if (! SemanticsDTMC.isDTMC(semantics)) {
        	return false;
        }
        
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (!UtilReachability.isReachability(propertyQuantifier.getQuantified())) {
            return false;
        }
        Set<Expression> inners = UtilReachability.collectReachabilityInner(propertyQuantifier.getQuantified());
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
    	/** EXPRESSION_TO_DD stores the oracle which is able to 
    	 * translate an expression to a BDD */
    	required.add(CommonProperties.EXPRESSION_TO_DD);
    	required.add(CommonProperties.SEMANTICS);
    	return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.STATE);
    	required.add(CommonProperties.PLAYER);
    	ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Set<Expression> inners = UtilReachability.collectReachabilityInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
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
    public String getIdentifier() {
        return IDENTIFIER;
    }

 
}

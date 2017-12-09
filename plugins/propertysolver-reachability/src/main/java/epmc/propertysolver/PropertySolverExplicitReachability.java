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

import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitBoolean;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDTMC;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

/**
 * This class implements a propertysolver which uses an explicit graph data structure
 * to compute reachability probability in a DTMC, the formula is in form of P=? [F a] 
 * 
 * @author Yong Li
 * 
 * */
public final class PropertySolverExplicitReachability implements PropertySolver {
    public final static String IDENTIFIER = "reachability-explicit";
    private ModelChecker modelChecker;
    private GraphExplicit graph;
    private StateSetExplicit computeForStates;
    private Expression property;
    private StateSet forStates;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /** setModelchecker will be called by the main program
	    to establish the procedure for model checking */
    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        if (modelChecker.getEngine() instanceof EngineExplicit) {
            this.graph = modelChecker.getLowLevel();
        }
        // no need to use contextExpression
        //this.contextExpression = modelChecker.getContextExpression();
    }

    @Override
    public void setProperty(Expression property) {
        this.property = property;
    }

    // initial states
    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    /**
     * main program will call this function to solve the model checking problem
     * */
    @Override
    public StateMap solve() {
        assert property != null;
        assert forStates != null;
        assert property instanceof ExpressionQuantifier;
        StateSetExplicit forStatesExplicit = (StateSetExplicit) forStates;
        graph.explore(forStatesExplicit.getStatesExplicit());
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        // remove P=? part, get F a
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        // doSolve does the model checking
        StateMap result = doSolve(quantifiedProp, forStates);
        //StateMap stores the reachability probabilities for every state in the graph
        return result;
    }

    /** collect the states which satisfy a */
    private StateMap doSolve(Expression property, StateSet states)
    {
        // set states we are interested in
        this.computeForStates = (StateSetExplicit) states;

        // use propertysolver propositional to check the innners
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        Set<Expression> inners = UtilReachability.collectReachabilityInner(property);
        Expression[] expressions = inners.toArray(new Expression[0]);
        Value[] evalValues = new Value[expressions.length];
        for (int varNr = 0; varNr < expressions.length; varNr++) {
            Expression inner = expressions[varNr];
            StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(inner, allStates);
            UtilGraph.registerResult(graph, inner, innerResult);
            evalValues[varNr] = innerResult.getType().newValue();
        }
        allStates.close();

        BitSet oneStates = UtilBitSet.newBitSetUnbounded();        
        // collect all states which satisfied the propositional formula property

        ExpressionTemporalFinally propertyTemporal = ExpressionTemporalFinally.as(property);
        // evaluator for propositional formula a out of F a
        EvaluatorExplicitBoolean evaluator = UtilEvaluatorExplicit.newEvaluatorBoolean(
                propertyTemporal.getOperand(), graph, expressions);

        for (int node = 0; node < graph.getNumNodes(); node ++) {
            // collect the truth value of all atomic propositions in this state
            for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
                evalValues[exprNr] = graph.getNodeProperty(expressions[exprNr]).get(node);
            }

            // check whether current state satisfies a out of F a 
            // under the satisfaction values of inners
            evaluator.setValues(evalValues);
            boolean sat = evaluator.evaluateBoolean();

            if (sat)  oneStates.set(node);

        }
        Log log = Options.get().get(OptionsMessages.LOG);
        log.send(MessagesReachability.REACHABILITY_NUM_ONE_STATES, graph.getNumNodes(), oneStates.cardinality());
        // compute the reachability probabilities for every state in the model
        ValueArrayAlgebra results = UtilReachability.computeReachabilityProbability(graph, oneStates);
        return prepareStateMap(results);
    }



    /** get results for states we are interested in */
    private StateMap prepareStateMap(ValueArrayAlgebra values) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        ValueArray resultValues = UtilValue.newArray(typeArray, computeForStates.size());
        TypeAlgebra typeWeight = TypeWeight.get();
        Value val = typeWeight.newValue();
        for (int i = 0; i < computeForStates.size(); i++) {
            values.get(val, i);
            resultValues.set(val, i);
        }

        return UtilGraph.newStateMap(computeForStates.clone(), resultValues);
    }

    /**
     * this function determine whether the propertysolver can handle the model checking task
     * */
    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }

        Semantics semantics = modelChecker.getModel().getSemantics();
        // only allow DTMC
        if (! SemanticsDTMC.isDTMC(semantics)) {
            return false;
        }
        // has P=? quantifier
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }

        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        // only allow F a
        if (!UtilReachability.isReachability(propertyQuantifier.getQuantified())) {
            return false;
        }

        // check whether atomic propositions can be handled by propertysolver-propositional (if exists)
        Set<Expression> inners = UtilReachability.collectReachabilityInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            modelChecker.ensureCanHandle(inner, allStates);
        }
        if (allStates != null) {
            allStates.close();
        }
        return true;
    }


    // ---------------- some functions which are used for building the model graph
    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        // semantics for the graph (MDP, MC and etc.)
        required.add(CommonProperties.SEMANTICS);
        return required;
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        // for node properties
        Set<Object> required = new LinkedHashSet<>();
        // STATE and PLAYER properties are required for all models
        // will be used in compute SCCs
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);

        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        // collect atomic propositions in the formula
        Set<Expression> inners = UtilReachability.collectReachabilityInner(propertyQuantifier.getQuantified());
        // get the state set in graph
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        // evaluate the satisfaction for all atomic propositions in the states
        for (Expression inner : inners) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
        return required;
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        // require the edges of constructed graph being labelled by weights (probabilities)
        required.add(CommonProperties.WEIGHT);
        return required;
    }






}

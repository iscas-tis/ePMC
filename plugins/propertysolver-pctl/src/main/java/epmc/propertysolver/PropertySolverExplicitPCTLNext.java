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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.TimeBound;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBounded;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorExp;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueObject;

public final class PropertySolverExplicitPCTLNext implements PropertySolver {
    public final static String IDENTIFIER = "pctl-explicit-next";
    private ModelChecker modelChecker;
    private GraphExplicit graph;
    private StateSetExplicit computeForStates;
    private boolean negate;
    private Expression property;
    private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        if (modelChecker.getEngine() instanceof EngineExplicit) {
            this.graph = modelChecker.getLowLevel();
        }
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
    public StateMap solve() {
        assert property != null;
        assert forStates != null;
        assert property instanceof ExpressionQuantifier;
        StateSetExplicit forStatesExplicit = (StateSetExplicit) forStates;
        graph.explore(forStatesExplicit.getStatesExplicit());
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        StateMap result = doSolve(quantifiedProp, forStates, dirType.isMin());
        if (!propertyQuantifier.getCompareType().isIs()) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType();
            assert op != null;
            result = result.applyWith(op, compare);
        }
        return result;
    }

    public StateMap doSolve(Expression property, StateSet states, boolean min) {
        if (isNot(property)) {
            ExpressionOperator propertyOperator = ExpressionOperator.as(property);
            property = propertyOperator.getOperand1();
            negate = true;
            min = !min;
        } else {
            negate = false;
        }
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        ExpressionTemporalNext propertyTemporal = ExpressionTemporalNext.as(property);
        Expression inner = propertyTemporal.getOperand();
        StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(inner, allStates);
        UtilGraph.registerResult(graph, inner, innerResult);
        allStates.close();
        this.computeForStates = (StateSetExplicit) states;
        return solve(propertyTemporal, min, innerResult);
    }

    private StateMap solve(ExpressionTemporalNext pathTemporal, boolean min, StateMapExplicit inner) {
        assert pathTemporal != null;
        TypeAlgebra typeWeight = TypeWeight.get();
        Value one = UtilValue.newValue(typeWeight, 1);
        ValueArray resultValues = solveNext(pathTemporal, inner, min);
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeWeight.get(), TypeWeight.get());
        if (negate) {
            ValueAlgebra entry = typeWeight.newValue();            
            for (int i = 0; i < resultValues.size(); i++) {
                resultValues.get(entry, i);
                subtract.apply(entry, one, entry);
                resultValues.set(entry, i);
            }
        }
        return UtilGraph.newStateMap(computeForStates.clone(), resultValues);
    }

    private ValueArrayAlgebra solveNext(ExpressionTemporalNext pathTemporal, StateMapExplicit inner, boolean min) {
        TypeAlgebra typeWeight = TypeWeight.get();
        ValueAlgebra zero = UtilValue.newValue(typeWeight, 0);
        ValueAlgebra one = UtilValue.newValue(typeWeight, 1);
        Semantics semanticsType = ValueObject.as(graph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        BitSet allNodes = UtilBitSet.newBitSetUnbounded();
        allNodes.set(0, graph.getNumNodes());
        List<Object> nodeProperties = new ArrayList<>();
        nodeProperties.add(CommonProperties.STATE);
        nodeProperties.add(CommonProperties.PLAYER);
        List<Object> edgeProperties = new ArrayList<>();
        edgeProperties.add(CommonProperties.WEIGHT);
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        int iterNumStates = graph.computeNumStates();
        ValueArrayAlgebra values = UtilValue.newArray(TypeWeight.get().getTypeArray(), iterNumStates);
        ValueBoolean valueInner = TypeBoolean.get().newValue();
        for (int i = 0; i < inner.size(); i++) {
            int state = inner.getExplicitIthState(i);
            inner.getExplicitIthValue(valueInner, i);
            boolean innerBoolean = valueInner.getBoolean();
            values.set(innerBoolean ? one : zero, state);
        }
        GraphSolverObjectiveExplicitBounded objective = new GraphSolverObjectiveExplicitBounded();
        objective.setValues(values);
        objective.setGraph(graph);
        objective.setMin(min);
        objective.setValues(values);
        objective.setTime(UtilValue.newValue(TypeInteger.get(), 1));
        configuration.setObjective(objective);
        configuration.solve();
        values = objective.getResult();
        TimeBound timeBound = pathTemporal.getTimeBound();
        if (SemanticsContinuousTime.isContinuousTime(semanticsType)) {
            OperatorEvaluator exp = ContextValue.get().getEvaluator(OperatorExp.EXP, TypeReal.get());
            Value rightValue = ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(timeBound.getRight()));
            ValueAlgebra entry = typeWeight.newValue();
            BitSet iterStates = UtilBitSet.newBitSetUnbounded();
            iterStates.set(0, graph.getNumNodes());
            ValueAlgebra leftValue = TypeWeight.get().newValue();
            OperatorEvaluator setLV = ContextValue.get().getEvaluator(OperatorSet.SET, leftValue.getType(), ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())).getType());
            setLV.apply(leftValue, ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())));
            ValueAlgebra sum = TypeWeight.get().newValue();
            ValueAlgebra jump = TypeWeight.get().newValue();
            EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
            OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
            OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, jump.getType());
            OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, entry.getType(), jump.getType());
            OperatorEvaluator setW = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
            for (int state = 0; state < iterNumStates; state++) {
                setW.apply(sum, UtilValue.newValue(TypeWeight.get(), 0));
                for (int succNr = 0; succNr < graph.getNumSuccessors(state); succNr++) {
                    Value succWeight = weight.get(state, succNr);
                    add.apply(sum, sum, succWeight);
                }
                multiply.apply(jump, leftValue, sum);
                addInverse.apply(jump, jump);
                exp.apply(jump, jump);
                values.get(entry, state);
                multiply.apply(entry, entry, jump);
                values.set(entry, state);
            }
        }
        return values;
    }

    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        Semantics semantics = modelChecker.getModel().getSemantics();
        if (!SemanticsDiscreteTime.isDiscreteTime(semantics)
                && !SemanticsContinuousTime.isContinuousTime(semantics)) {
            return false;
        }
        if (!ExpressionQuantifier.is(property)) {
            return false;
        }
        ExpressionQuantifier propertyQuantifier = ExpressionQuantifier.as(property);
        if (!isNext(propertyQuantifier.getQuantified())) {
            return false;
        }
        Set<Expression> inners = UtilPCTL.collectPCTLInner(propertyQuantifier.getQuantified());
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
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        return required;
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Set<Expression> inners = UtilPCTL.collectPCTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
        return required;
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        return required;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    private ValueArray newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }

    private static boolean isNot(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorNot.NOT);
    }

    private static boolean isNext(Expression expression) {
        return ExpressionTemporalNext.is(expression);
    }
}

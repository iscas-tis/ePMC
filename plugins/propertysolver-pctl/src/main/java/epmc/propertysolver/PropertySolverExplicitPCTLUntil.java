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
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.TimeBound;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBounded;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedReachability;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.Operator;
import epmc.operator.OperatorGt;
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
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;

public final class PropertySolverExplicitPCTLUntil implements PropertySolver {
    public final static String IDENTIFIER = "pctl-explicit";
    private ModelChecker modelChecker;
    private GraphExplicit graph;
    private StateSetExplicit computeForStates;
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

    private StateMap doSolve(Expression property, StateSet states, boolean min) {
        boolean negate;
        if (isNot(property)) {
            ExpressionOperator propertyOperator = ExpressionOperator.as(property);
            property = propertyOperator.getOperand1();
            negate = true;
            min = !min;
        } else if (isRelease(property)) {
            ExpressionTemporalRelease pathTemporal = ExpressionTemporalRelease.as(property);
            Expression left = pathTemporal.getOperandLeft();
            Expression right = pathTemporal.getOperandRight();
            property = new ExpressionTemporalUntil.Builder()
                    .setOperandLeft(not(left))
                    .setOperandRight(not(right))
                    .setTimeBound(pathTemporal.getTimeBound())
                    .setPositional(property.getPositional())
                    .build();
            min = !min;
            negate = true;
        } else if (isFinally(property)) {
            ExpressionTemporalFinally pathTemporal = ExpressionTemporalFinally.as(property);
            Expression left = ExpressionLiteral.getTrue();
            Expression right = pathTemporal.getOperand();
            property = new ExpressionTemporalUntil.Builder()
                    .setOperandLeft(left)
                    .setOperandRight(right)
                    .setTimeBound(pathTemporal.getTimeBound())
                    .setPositional(property.getPositional())
                    .build();
            negate = false;
        } else if (isGlobally(property)) {
            ExpressionTemporalGlobally pathTemporal = ExpressionTemporalGlobally.as(property);
            Expression left = ExpressionLiteral.getTrue();
            Expression right = not(pathTemporal.getOperand());
            property = new ExpressionTemporalUntil.Builder()
                    .setOperandLeft(left)
                    .setOperandRight(right)
                    .setTimeBound(pathTemporal.getTimeBound())
                    .setPositional(property.getPositional())
                    .build();
            min = !min;
            negate = true;
        } else {
            negate = false;
        }
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        ExpressionTemporalUntil propertyTemporal = ExpressionTemporalUntil.as(property);
        Expression op1 = propertyTemporal.getOperandLeft();
        StateMapExplicit innerResult1 = (StateMapExplicit) modelChecker.check(op1, allStates);
        UtilGraph.registerResult(graph, op1, innerResult1);
        Expression op2 = propertyTemporal.getOperandRight();
        StateMapExplicit innerResult2 = (StateMapExplicit) modelChecker.check(op2, allStates);
        UtilGraph.registerResult(graph, op1, innerResult2);
        allStates.close();
        this.computeForStates = (StateSetExplicit) states;
        return solve(propertyTemporal, min, negate, innerResult1, innerResult2);
    }

    private StateMap solve(ExpressionTemporalUntil pathTemporal, boolean min, boolean negate, StateMapExplicit innerLeft, StateMapExplicit innerRight) {
        assert pathTemporal != null;
        Semantics semanticsType = ValueObject.as(graph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        TimeBound timeBound = pathTemporal.getTimeBound();

        BitSet sinkSet = UtilBitSet.newBitSetUnbounded();
        TypeAlgebra typeWeight = TypeWeight.get();
        Value zero = UtilValue.newValue(typeWeight, 0);
        Value one = UtilValue.newValue(typeWeight, 1);
        ValueArray resultValues = newValueArrayWeight(computeForStates.size());
        //        ValueArray result = typeArray.newValue(computeForStates.length());

        BitSet allNodes = UtilBitSet.newBitSetUnbounded();
        allNodes.set(0, graph.getNumNodes(), true);
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        ValueAlgebra transValue = typeWeight.newValue();
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, typeWeight, typeWeight);
        set.apply(transValue, UtilValue.newValue(typeWeight, 0));

        BitSet zeroSet = UtilBitSet.newBitSetUnbounded();
        BitSet oneSet = UtilBitSet.newBitSetUnbounded();
        NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
        ValueBoolean valueLeft = TypeBoolean.get().newValue();
        ValueBoolean valueRight = TypeBoolean.get().newValue();
        for (int i = 0; i < innerLeft.size(); i++) {
            int state = innerLeft.getExplicitIthState(i);
            if (!stateProp.getBoolean(state)) {
                continue;
            }
            innerLeft.getExplicitIthValue(valueLeft, i);
            innerRight.getExplicitIthValue(valueRight, i);
            boolean left = valueLeft.getBoolean();
            boolean right = valueRight.getBoolean();
            if (!left && !right) {
                zeroSet.set(state);
            } else if (right) {
                oneSet.set(state);
            }
        }

        ValueArrayAlgebra values;
        NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);            
        if (timeBound.isRightBounded()) {
            if (SemanticsContinuousTime.isContinuousTime(semanticsType)) {
                GraphSolverObjectiveExplicitBoundedReachability objective = new GraphSolverObjectiveExplicitBoundedReachability();
                objective.setGraph(graph);
                objective.setMin(min);
                objective.setTime(ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(timeBound.getRight())));
                objective.setZeroSink(zeroSet);
                objective.setTargets(oneSet);
                configuration.setObjective(objective);
                configuration.solve();
                values = objective.getResult();
            } else {
                int leftBound = ValueInteger.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())).getInt();
                int rightBound = ValueInteger.as(UtilEvaluatorExplicit.evaluate(timeBound.getRight())).getInt();
                if (timeBound.isRightOpen()) {
                    rightBound--;
                }
                if (timeBound.isLeftOpen()) {
                    leftBound++;
                }
                GraphSolverObjectiveExplicitBoundedReachability objective = new GraphSolverObjectiveExplicitBoundedReachability();
                objective.setZeroSink(zeroSet);
                objective.setTargets(oneSet);
                objective.setMin(min);
                objective.setGraph(graph);
                Value time = UtilValue.newValue(TypeInteger.get(), rightBound - leftBound);
                objective.setTime(time);
                configuration.setObjective(objective);
                configuration.solve();
                values = objective.getResult();
            }
        } else {
            GraphSolverObjectiveExplicitUnboundedReachability objective = new GraphSolverObjectiveExplicitUnboundedReachability();
            objective.setTarget(oneSet);
            objective.setZeroSink(zeroSet);
            objective.setMin(min);
            objective.setGraph(graph);
            objective.setComputeFor(computeForStates.getStatesExplicit());
            //                System.out.println(graph);
            //              System.out.println(oneSet);
            configuration.setObjective(objective);
            configuration.solve();
            values = objective.getResult();
        }
        ValueBoolean cmp = TypeBoolean.get().newValue();
        OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())).getType(), ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())).getType());
        gt.apply(cmp, ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())), UtilValue.newValue(ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())).getType(), 0));
        if (cmp.getBoolean() || timeBound.isLeftOpen()) {
            configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
            sinkSet.clear();
            for (int i = 0; i < innerLeft.size(); i++) {
                int state = innerLeft.getExplicitIthState(i);
                if (isState.getBoolean(state)) {
                    innerLeft.getExplicitIthValue(valueLeft, i);
                    boolean left = valueLeft.getBoolean();
                    if (!left) {
                        sinkSet.set(state);
                        values.set(zero, state);
                    }
                }
            }
            Value val = typeWeight.newValue();

            for (int i = 0; i < computeForStates.size(); i++) {
                int state = computeForStates.getExplicitIthState(i);
                values.get(val, state);
                resultValues.set(val, i);
            }

            if (SemanticsContinuousTime.isContinuousTime(semanticsType)) {
                GraphSolverObjectiveExplicitBounded objective = new GraphSolverObjectiveExplicitBounded();
                configuration.setObjective(objective);
                objective.setGraph(graph);
                objective.setValues(values);
                objective.setMin(min);
                objective.setTime(ValueAlgebra.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())));
                configuration.solve();
                values = objective.getResult();
            } else {
                int leftBound = ValueInteger.as(UtilEvaluatorExplicit.evaluate(timeBound.getLeft())).getInt();
                if (timeBound.isLeftOpen()) {
                    leftBound++;
                }
                GraphSolverObjectiveExplicitBounded objective = new GraphSolverObjectiveExplicitBounded();
                objective.setGraph(graph);
                objective.setValues(values);
                objective.setMin(min);
                objective.setComputeFor(computeForStates.getStatesExplicit());
                Value time = UtilValue.newValue(TypeInteger.get(), leftBound);
                objective.setTime(time);
                configuration.setObjective(objective);
                configuration.solve();
                values = objective.getResult();
            }
        }
        Value val = typeWeight.newValue();
        for (int i = 0; i < computeForStates.size(); i++) {
            int state = computeForStates.getExplicitIthState(i);
            values.get(val, i);
            resultValues.set(val, i);
        }
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, typeWeight, typeWeight);
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
        handleSimplePCTLExtensions();
        ExpressionQuantifier propertyQuantifier = ExpressionQuantifier.as(property);
        if (!UtilPCTL.isPCTLPathUntil(propertyQuantifier.getQuantified())) {
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

    private void handleSimplePCTLExtensions() {
        ExpressionQuantifier propertyQuantifier = ExpressionQuantifier.as(property);
        Expression quantified = propertyQuantifier.getQuantified();
        if (isNot(quantified)
                && isFinally((ExpressionOperator.as(quantified)).getOperand1())) {
            ExpressionOperator quantifiedOperator = (ExpressionOperator) quantified;
            ExpressionTemporalFinally quantifiedOp1 =
                    ExpressionTemporalFinally.as(quantifiedOperator.getOperand1());
            quantified = new ExpressionTemporalGlobally.Builder()
                    .setOperand(new ExpressionOperator.Builder()
                            .setOperator(OperatorNot.NOT)
                            .setOperands(quantifiedOp1.getOperand())
                            .build())
                    .setTimeBound(quantifiedOp1.getTimeBound())
                    .setPositional(quantified.getPositional())
                    .build();
            property = new ExpressionQuantifier.Builder()
                    .setCmpType(propertyQuantifier.getCompareType())
                    .setCompare(propertyQuantifier.getCompare())
                    .setCondition(propertyQuantifier.getCondition())
                    .setDirType(propertyQuantifier.getDirType())
                    .setPositional(propertyQuantifier.getPositional())
                    .setQuantified(quantified)
                    .build();
        }
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

    private Expression not(Expression expression) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorNot.NOT)
                .setPositional(expression.getPositional())
                .setOperands(expression)
                .build();
    }

    private static boolean isNot(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorNot.NOT);
    }

    private static boolean isFinally(Expression expression) {
        return ExpressionTemporalFinally.is(expression);
    }

    private static boolean isGlobally(Expression expression) {
        return ExpressionTemporalGlobally.is(expression);
    }

    private static boolean isRelease(Expression expression) {
        return ExpressionTemporalRelease.is(expression);
    }
}

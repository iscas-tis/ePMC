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
import epmc.graph.SemanticsContinuousTime;
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
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBounded;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedReachability;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueReal;
import epmc.value.operator.OperatorNot;
import epmc.value.operator.OperatorSubtract;

public final class PropertySolverDDPCTL implements PropertySolver {
    public final static String IDENTIFIER = "pctl-dd";
    private ModelChecker modelChecker;
    private GraphDD modelGraph;
    private ExpressionToDD expressionToDD;
    private boolean min;
    private boolean negate;
    private Expression property;
    private Semantics type;
    private boolean qualitative;
    private StateSet forStates;
    private Expression inner;

    @Override

    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        this.type = modelChecker.getModel().getSemantics();
    }

    private DD solve(boolean min, Expression property, boolean qualitative,
            StateSet forStates)
    {
        if (isNot(property)) {
            ExpressionOperator propertyOperator = (ExpressionOperator) property;
            property = propertyOperator.getOperand1();
            negate = true;
            min = !min;
        } else if (isRelease(property)) {
            ExpressionTemporal pathTemporal = (ExpressionTemporal) property;
            Expression left = pathTemporal.getOperand1();
            Expression right = pathTemporal.getOperand2();
            property = newTemporal(TemporalType.UNTIL, not(left), not(right), pathTemporal.getTimeBound(), property.getPositional());
            min = !min;
            negate = true;
        } else if (isFinally(property)) {
            ExpressionTemporal pathTemporal = (ExpressionTemporal) property;
            Expression left = ExpressionLiteral.getTrue();
            Expression right = pathTemporal.getOperand1();
            property = newTemporal(TemporalType.UNTIL, left, right, pathTemporal.getTimeBound(), property.getPositional());
            negate = false;
        } else if (isGlobally(property)) {
            ExpressionTemporal pathTemporal = (ExpressionTemporal) property;
            Expression left = ExpressionLiteral.getTrue();
            Expression right = not(pathTemporal.getOperand1());
            property = newTemporal(TemporalType.UNTIL, left, right, pathTemporal.getTimeBound(), property.getPositional());
            min = !min;
            negate = true;
        } else {
            negate = false;
        }
        this.min = min;
        this.qualitative = qualitative;
        this.inner = property;

        if (isNext(property)) {
            //            Expression inner = property.getOperand1();
            // TODO implement
        } else {
            return solveUntil(forStates);
        }
        assert false;
        return null;
    }

    private DD solveUntil(StateSet forStates) {
        DD nodeSpace = modelGraph.getNodeSpace();
        ExpressionTemporal innerTemporal = (ExpressionTemporal) inner;
        TimeBound timeBound = innerTemporal.getTimeBound();
        Expression leftExpr = innerTemporal.getOperand1();
        Expression rightExpr = innerTemporal.getOperand2();
        DD leftDD = expressionToDD.translate(leftExpr);
        DD rightDD = expressionToDD.translate(rightExpr);
        DD targetDD = rightDD.and(nodeSpace);
        DD failDD = nodeSpace.clone().andWith(leftDD.not(), rightDD.not());

        leftDD.dispose();
        rightDD.dispose();
        if (!timeBound.isRightBounded()) {
            DD targetDDOld = targetDD;
            targetDD = ComponentsDD.reachPre(modelGraph, targetDD, nodeSpace,
                    failDD, min, true);
            targetDDOld.dispose();
        }
        DD someDD = ComponentsDD.reachPre(modelGraph, targetDD, nodeSpace,
                failDD, min, false);
        failDD.dispose();
        failDD = nodeSpace.andNot(someDD);
        DD result;
        if (qualitative || nodeSpace.clone().isSubsetWith(targetDD.or(failDD))) {
            result = checkUntilQualitative(targetDD, failDD);
        } else {
            result = checkUntilQuantitative(targetDD, failDD);
        }
        if (negate) {
            DD constOne = getContextDD().newConstant(1);
            DD negResult = constOne.subtract(result);
            result.dispose();
            result = negResult;
            constOne.dispose();
        }

        return result;
    }

    private DD checkUntilQuantitative(DD targetDD, DD failDD)
    {
        DD nodeSpace = modelGraph.getNodeSpace();
        ExpressionTemporal innerTemporal = (ExpressionTemporal) inner;
        TimeBound timeBound = innerTemporal.getTimeBound();
        List<DD> sinks = new ArrayList<>();
        failDD = failDD.orWith(nodeSpace.not());
        sinks.add(failDD);
        sinks.add(targetDD);
        Semantics semantics = modelGraph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        GraphBuilderDD converter = new GraphBuilderDD(modelGraph, sinks, SemanticsNonDet.isNonDet(semantics));
        if (SemanticsContinuousTime.isContinuousTime(type)
                && timeBound.isRightBounded()) {
            converter.setUniformise(true);
        }
        GraphExplicit graph = converter.buildGraph();
        int numStates = graph.computeNumStates();
        BitSet targetBitSet = converter.ddToBitSet(targetDD);
        BitSet failBitSet = converter.ddToBitSet(failDD);
        BitSet targetS = UtilBitSet.newBitSetUnbounded(numStates);
        ValueArrayAlgebra values;
        boolean trans = !isUntil(inner);
        for (int state = 0; state < numStates; state++) {
            if (targetBitSet.get(state)) {
                targetS.set(state, true);
            } else if (failBitSet.get(state)) {
                targetS.set(state, false);
            } else {
                targetS.set(state, trans);
            }
        }
        ValueAlgebra leftValue = timeBound.getLeftValue();
        ValueAlgebra rightValue = timeBound.getRightValue();
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        OperatorEvaluator subtract = ContextValue.get().getOperatorEvaluator(OperatorSubtract.SUBTRACT, TypeReal.get(), TypeReal.get());
        if (timeBound.isRightBounded()) {
            if (SemanticsContinuousTime.isContinuousTime(type)) {
                Value unifRate = converter.getUnifRate();
                ValueReal rate = TypeReal.get().newValue();
                subtract.apply(rate, rightValue, leftValue);
                rate.multiply(rate, unifRate);
                GraphSolverObjectiveExplicitBoundedReachability objective = new GraphSolverObjectiveExplicitBoundedReachability();
                objective.setGraph(graph);
                objective.setMin(min);
                objective.setTargets(targetS);
                objective.setTime(rate);
                configuration.setObjective(objective);
                configuration.solve();
                values = objective.getResult();
            } else {
                int leftBound = timeBound.getLeftInt();
                if (timeBound.isLeftOpen()) {
                    leftBound++;
                }
                int rightBound = timeBound.getRightInt();
                if (timeBound.isRightOpen()) {
                    rightBound--;
                }
                GraphSolverObjectiveExplicitBoundedReachability objective = new GraphSolverObjectiveExplicitBoundedReachability();
                objective.setGraph(graph);
                objective.setTargets(targetS);
                objective.setMin(min);
                Value time = UtilValue.newValue(TypeInteger.get(), rightBound - leftBound);
                objective.setTime(time);
                configuration.setObjective(objective);
                configuration.solve();
                values = objective.getResult();
            }
        } else {
            GraphSolverObjectiveExplicitUnboundedReachability objective = new GraphSolverObjectiveExplicitUnboundedReachability();
            objective.setGraph(graph);
            objective.setTarget(targetS);
            objective.setMin(min);
            configuration.setObjective(objective);
            configuration.solve();
            values = objective.getResult();
        }
        // TODO following line is unnecessarily slow. To speed up:
        // 1.) only convert values which we actually need, set other to zero
        // 2.) If given probability bound e.g. P<= 0.5 do check for 0.5 be
        // fore converting to symbolic (requires some structural changes
        // but will be worth it)
        DD result = converter.valuesToDD(values);

        targetDD.dispose();
        failDD.dispose();
        Expression leftExpr = innerTemporal.getOperand1();
        Expression rightExpr = innerTemporal.getOperand2();

        DD leftDD = expressionToDD.translate(leftExpr);
        DD leftNotDD = leftDD.not();
        DD rightDD = expressionToDD.translate(rightExpr);
        DD rightNotDD = rightDD.not();

        if (leftValue.isGt(leftValue.getType().getZero())
                || timeBound.isLeftOpen()) {
            failDD = isUntil(inner) ? leftNotDD : rightNotDD;
            DD failNotDD = failDD.not();
            DD failNotIntDD = failNotDD.toMT();
            DD resultMin = result.min(failNotIntDD);
            failNotIntDD.dispose();
            failNotDD.dispose();
            result.dispose();
            result = resultMin;
            DD constantFalse = getContextDD().newConstant(false);
            constantFalse.dispose();
            DD states = modelGraph.getNodeProperty(CommonProperties.STATE);
            numStates = modelGraph.getNodeSpace().and(states).countNodes().intValue();
            converter.close();
            sinks.clear();
            failDD = failDD.orWith(nodeSpace.not());
            sinks.add(failDD);
            semantics = modelGraph.getGraphPropertyObject(CommonProperties.SEMANTICS);
            converter = new GraphBuilderDD(modelGraph, sinks, SemanticsNonDet.isNonDet(semantics));
            if (SemanticsContinuousTime.isContinuousTime(type)) {
                converter.setUniformise(true);
            }
            graph = converter.buildGraph();
            values = ValueArrayAlgebra.asArrayAlgebra(converter.ddToValueArray(result));
            if (SemanticsContinuousTime.isContinuousTime(type)) {
                ValueReal rate = TypeReal.get().newValue();
                Value unifRate = converter.getUnifRate();
                rate.multiply(timeBound.getLeftValue(), unifRate);
                GraphSolverObjectiveExplicitBounded objective = new GraphSolverObjectiveExplicitBounded();
                objective.setGraph(graph);
                objective.setMin(min);
                objective.setTime(rate);
                objective.setValues(values);
                configuration.setObjective(objective);
                configuration.solve();
                values = objective.getResult();
            } else {
                int leftBound = timeBound.getLeftInt();
                if (timeBound.isLeftOpen()) {
                    leftBound++;
                }
                GraphSolverObjectiveExplicitBounded objective = new GraphSolverObjectiveExplicitBounded();
                objective.setGraph(graph);
                objective.setValues(values);
                objective.setMin(min);
                Value lb = UtilValue.newValue(TypeInteger.get(), leftBound);
                objective.setTime(lb);
                configuration.setObjective(objective);
                configuration.solve();
                values = objective.getResult();
            }
            result = converter.valuesToDD(values);
            result = result.multiply(modelGraph.getNodeSpace().toMT());
            converter.close();
        }

        rightNotDD.dispose();
        leftNotDD.dispose();

        return result;
    }

    private DD checkUntilQualitative(DD targetDD, DD failDD)
    {
        DD nodeSpace = modelGraph.getNodeSpace();
        Value oneHalf = UtilValue.newValue(TypeReal.get(), "1/2");
        Value zero = TypeReal.get().getZero();
        DD rest = nodeSpace.andNot(targetDD, failDD).toMTWith(oneHalf, zero);
        DD result = targetDD.toMT().addWith(rest);
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
    public StateMap solve() {
        if (modelChecker.getEngine() instanceof EngineDD) {
            this.modelGraph = modelChecker.getLowLevel();
            this.expressionToDD = modelGraph.getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
        }
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        Set<Expression> inners = UtilPCTL.collectPCTLInner(quantifiedProp);
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            StateMapDD innerResult = (StateMapDD) modelChecker.check(inner, allStates);
            ExpressionToDD expressionToDD = modelGraph
                    .getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
            expressionToDD.putConstantWith(inner, innerResult.getValuesDD());
        }
        allStates.close();
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        boolean min = dirType == DirType.MIN;
        StateMap compare = null;
        Operator op = null;
        boolean qualitative = false;
        if (propertyQuantifier.getCompareType() != CmpType.IS) {
            compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            op = propertyQuantifier.getCompareType().asExOpType();
            if (compare.isConstant() && (ValueAlgebra.asAlgebra(compare.getSomeValue()).isZero() 
                    || ValueAlgebra.asAlgebra(compare.getSomeValue()).isOne())) {
                qualitative = true;
            }
        }
        DD resultDD = solve(min, quantifiedProp, qualitative, forStates);
        StateMap result = new StateMapDD((StateSetDD) forStates.clone(), resultDD);
        if (propertyQuantifier.getCompareType() != CmpType.IS) {
            result = result.applyWith(op, compare);
        }
        return result;
    }


    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (!UtilPCTL.isPCTLPath(propertyQuantifier.getQuantified())) {
            return false;
        }
        Set<Expression> inners = UtilPCTL.collectPCTLInner(propertyQuantifier.getQuantified());
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
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.EXPRESSION_TO_DD);
        required.add(CommonProperties.SEMANTICS);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Set<Expression> inners = UtilPCTL.collectPCTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        return Collections.unmodifiableSet(required);
    }    

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    private ContextDD getContextDD() {
        return ContextDD.get();
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

    private static boolean isNext(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.NEXT;
    }

    private static boolean isFinally(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.FINALLY;
    }

    private static boolean isGlobally(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.GLOBALLY;
    }

    private static boolean isRelease(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.RELEASE;
    }

    private static boolean isUntil(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.UNTIL;
    }

    private static ExpressionTemporal newTemporal
    (TemporalType type, Expression op1, Expression op2,
            TimeBound bound, Positional positional) {
        assert type != null;
        assert bound != null;
        return new ExpressionTemporal
                (op1, op2, type, bound, positional);
    }
}

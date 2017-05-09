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

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.TemporalType;
import epmc.expression.standard.TimeBound;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitBoolean;
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
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorNot;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.TypeInteger;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueObject;
import epmc.value.ValueReal;

public final class PropertySolverExplicitPCTL implements PropertySolver {
    public final static String IDENTIFIER = "pctl-explicit";
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
    public StateMap solve() throws EPMCException {
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
            Operator op = ContextValue.get().getOperator(propertyQuantifier.getCompareType().asExOpType());
            assert op != null;
            result = result.applyWith(op, compare);
        }
        return result;
    }

    public StateMap doSolve(Expression property, StateSet states, boolean min)
            throws EPMCException {
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
        Set<Expression> inners = UtilPCTL.collectPCTLInner(property);
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(inner, allStates);
            UtilGraph.registerResult(graph, inner, innerResult);
        }
        allStates.close();
        this.computeForStates = (StateSetExplicit) states;
        return solve((ExpressionTemporal) property, min);
    }
    
    private StateMap solve(ExpressionTemporal pathTemporal, boolean min)
            throws EPMCException {
        assert pathTemporal != null;
        Semantics semanticsType = ValueObject.asObject(graph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        TimeBound timeBound = pathTemporal.getTimeBound();

        Expression[] expressions = UtilPCTL.collectPCTLInner(pathTemporal).toArray(new Expression[0]);
        Value[] evalValues = new Value[expressions.length];
        for (int varNr = 0; varNr < expressions.length; varNr++) {
            evalValues[varNr] = expressions[varNr].getType(graph).newValue();
        }
        EvaluatorExplicitBoolean[] evaluators = new EvaluatorExplicitBoolean[pathTemporal.getOperands().size()];
        for (int i = 0; i < pathTemporal.getOperands().size(); i++) {
        	evaluators[i] = UtilEvaluatorExplicit.newEvaluatorBoolean(pathTemporal.getOperands().get(i), graph, expressions);
        }
        
        BitSet sinkSet = UtilBitSet.newBitSetUnbounded();
        TypeAlgebra typeWeight = TypeWeight.get();
        Value zero = UtilValue.newValue(typeWeight, 0);
        Value one = UtilValue.newValue(typeWeight, 1);
        ValueArray resultValues = newValueArrayWeight(computeForStates.size());
//        ValueArray result = typeArray.newValue(computeForStates.length());

        if (isNext(pathTemporal)) {
            solveNext(pathTemporal, expressions, evalValues, evaluators, min);
        } else {
        	BitSet allNodes = UtilBitSet.newBitSetUnbounded();
        	allNodes.set(0, graph.getNumNodes(), true);
            GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
            ValueAlgebra transValue = typeWeight.newValue();
            transValue.set(transValue.getType().getZero());

            BitSet zeroSet = UtilBitSet.newBitSetUnbounded();
            BitSet oneSet = UtilBitSet.newBitSetUnbounded();
            NodeProperty stateProp = graph.getNodeProperty(CommonProperties.STATE);
            for (int node = allNodes.nextSetBit(0); node >= 0; node = allNodes.nextSetBit(node+1)) {
                if (!stateProp.getBoolean(node)) {
                    continue;
                }
                for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
                    evalValues[exprNr] = graph.getNodeProperty(expressions[exprNr]).get(node);
                }
                for (EvaluatorExplicit evaluator : evaluators) {
                	evaluator.evaluate(evalValues);
                }
                boolean left = evaluators[0].evaluateBoolean(evalValues);
                boolean right = evaluators[1].evaluateBoolean(evalValues);
                if (!left && !right) {
                    zeroSet.set(node);
                } else if (right) {
                    oneSet.set(node);
                }
            }

            ValueArrayAlgebra values;
            NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);            
            if (timeBound.isRightBounded()) {
                if (SemanticsContinuousTime.isContinuousTime(semanticsType)) {
                    GraphSolverObjectiveExplicitBoundedReachability objective = new GraphSolverObjectiveExplicitBoundedReachability();
                    objective.setGraph(graph);
                    objective.setMin(min);
                    objective.setTime(timeBound.getRightValue());
                    objective.setZeroSink(zeroSet);
                    objective.setTargets(oneSet);
                    configuration.setObjective(objective);
                    configuration.solve();
                    values = objective.getResult();
                } else {
                    int leftBound = timeBound.getLeftInt();
                    int rightBound = timeBound.getRightInt();
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
//                System.out.println(graph);
  //              System.out.println(oneSet);
                configuration.setObjective(objective);
                configuration.solve();
                values = objective.getResult();
            }
            if (timeBound.getLeftValue().isGt(timeBound.getLeftValue().getType().getZero())
                    || timeBound.isLeftOpen()) {
                configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
                sinkSet.clear();
                for (int state = allNodes.nextSetBit(0); state >= 0; state = allNodes.nextSetBit(state+1)) {
                    if (isState.getBoolean(state)) {
                        for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
                            evalValues[exprNr] = graph.getNodeProperty(expressions[exprNr]).get(state);
                        }
                        boolean left = evaluators[0].evaluateBoolean(evalValues);
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
                    objective.setTime(timeBound.getLeftValue());
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
        }
        if (negate) {
            ValueAlgebra entry = typeWeight.newValue();            
            for (int i = 0; i < resultValues.size(); i++) {
                resultValues.get(entry, i);
                entry.subtract(one, entry);
                resultValues.set(entry, i);
            }
        }
        return UtilGraph.newStateMap(computeForStates.clone(), resultValues);
    }

    private void solveNext(ExpressionTemporal pathTemporal, Expression[] expressions, Value[] evalValues, EvaluatorExplicitBoolean[] evaluators, boolean min) throws EPMCException {
        TypeAlgebra typeWeight = TypeWeight.get();
        ValueAlgebra zero = UtilValue.newValue(typeWeight, 0);
        ValueAlgebra one = UtilValue.newValue(typeWeight, 1);
        Semantics semanticsType = ValueObject.asObject(graph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
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
        for (int state = allNodes.nextSetBit(0); state >= 0; state = allNodes.nextSetBit(state+1)) {
            for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
                evalValues[exprNr] = graph.getNodeProperty(expressions[exprNr]).get(state);
            }
            boolean innerBoolean = evaluators[0].evaluateBoolean(evalValues);
            values.set(innerBoolean ? one : zero, state);
        }
        GraphSolverObjectiveExplicitBounded objective = new GraphSolverObjectiveExplicitBounded();
        objective.setValues(values);
        objective.setGraph(graph);
        objective.setMin(min);
        objective.setValues(values);
        objective.setTime(TypeInteger.get().getOne());
        configuration.setObjective(objective);
        configuration.solve();
        values = objective.getResult();
        TimeBound timeBound = pathTemporal.getTimeBound();
        if (SemanticsContinuousTime.isContinuousTime(semanticsType)) {
            Value rightValue = timeBound.getRightValue();
            ValueAlgebra entry = typeWeight.newValue();
            BitSet iterStates = UtilBitSet.newBitSetUnbounded();
            iterStates.set(0, graph.getNumNodes());
            Value leftValue = TypeWeight.get().newValue();
            leftValue.set(timeBound.getLeftValue());
            ValueAlgebra sum = TypeWeight.get().newValue();
            ValueAlgebra jump = TypeWeight.get().newValue();
            EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
            for (int state = 0; state < iterNumStates; state++) {
                sum.set(TypeWeight.get().getZero());
                for (int succNr = 0; succNr < graph.getNumSuccessors(state); succNr++) {
                    Value succWeight = weight.get(state, succNr);
                    sum.add(sum, succWeight);
                }
                jump.multiply(leftValue, sum);
                jump.addInverse(jump);
                ValueReal.asReal(jump).exp(jump);
                values.get(entry, state);
                entry.multiply(entry, jump);
                values.set(entry, state);
            }
        }
    }

    @Override
    public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        Semantics semantics = modelChecker.getModel().getSemantics();
        if (!SemanticsDiscreteTime.isDiscreteTime(semantics)
        		&& !SemanticsContinuousTime.isContinuousTime(semantics)) {
        	return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        handleSimplePCTLExtensions();
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (!UtilPCTL.isPCTLPath(propertyQuantifier.getQuantified())) {
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
    	ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
    	Expression quantified = propertyQuantifier.getQuantified();
    	if (isNot(quantified)
    			&& isFinally(((ExpressionOperator) quantified).getOperand1())) {
    		ExpressionOperator quantifiedOperator = (ExpressionOperator) quantified;
    		ExpressionTemporal quantifiedOp1 =
    				(ExpressionTemporal) quantifiedOperator.getOperand1();
    		quantified = new ExpressionTemporal.Builder()
    				.setType(TemporalType.GLOBALLY)
    				.setPositional(quantified.getPositional())
    				.setChildren(new ExpressionOperator.Builder()
    						.setOperator(OperatorNot.IDENTIFIER)
    						.setOperands(quantifiedOp1.getOperand1())
    						.build(),
    						quantifiedOp1.getChildren().get(1),
    						quantifiedOp1.getChildren().get(2))
    				.setLeftOpen(false)
    				.setRightOpen(false)
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
    public Set<Object> getRequiredGraphProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.SEMANTICS);
    	return required;
    }

    @Override
    public Set<Object> getRequiredNodeProperties() throws EPMCException {
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
    public Set<Object> getRequiredEdgeProperties() throws EPMCException {
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
    			.setOperator(OperatorNot.IDENTIFIER)
    			.setPositional(expression.getPositional())
    			.setOperands(expression)
    			.build();
    }
    
    private static boolean isNot(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperatorId()
                .equals(OperatorNot.IDENTIFIER);
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
    
    private static ExpressionTemporal newTemporal
    (TemporalType type, Expression op1, Expression op2,
            TimeBound bound, Positional positional) {
        assert type != null;
        assert bound != null;
        return new ExpressionTemporal
                (op1, op2, type, bound, positional);
    }
}

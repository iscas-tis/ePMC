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
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsMDP;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.Operator;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorSet;
import epmc.petl.model.ModelMAS;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeDouble;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueBoolean;
import epmc.value.ValueDouble;

public class PropertySolverPETLUntilUCT implements PropertySolver{
	public final static String IDENTIFIER = "petl-until-uct";
	private ModelChecker modelChecker;
    private GraphExplicit graph;
    private StateSetExplicit computeForStates;
    private Expression property;
    private StateSet forStates;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

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
	public boolean canHandle() {
		assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        Semantics semantics = modelChecker.getModel().getSemantics();
        if (!SemanticsDTMC.isDTMC(semantics) && !SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        } 
        //The model should be a mas
        if (!(modelChecker.getModel() instanceof ModelMAS)) {
            return false;
        }
        
        handleSimplePCTLExtensions();
        
        ExpressionQuantifier propertyQuantifier = ExpressionQuantifier.as(property);
        if (!UtilPETL.isPCTLPath(propertyQuantifier.getQuantified())) {
            return false;
        }
        if (!UtilPETL.isPCTLPathUntil(propertyQuantifier.getQuantified())) {
            return false;
        }
        
        Set<Expression> inners = UtilPETL.collectPCTLInner(propertyQuantifier.getQuantified());
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
        Set<Expression> inners = UtilPETL.collectPCTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }

        Set<Expression> expOfEquiv = ((ModelMAS) modelChecker.getModel()).getEquivalenceRelations().getAllExpressions();
        for (Expression inner : expOfEquiv) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
        
        return required;
	}

	@Override
	public Set<Object> getRequiredEdgeProperties() {
		Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        required.add(CommonProperties.TRANSITION_LABEL);
        return required;
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
            ExpressionOperator propertyOperator = (ExpressionOperator) property;
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
        UtilGraph.registerResult(graph, op2, innerResult2);
        allStates.close();
        this.computeForStates = (StateSetExplicit) states;

        return solve(propertyTemporal, min, negate, innerResult1, innerResult2);
	}
	
	private StateMap solve(ExpressionTemporalUntil pathTemporal, boolean min, boolean negate, StateMapExplicit innerLeft, StateMapExplicit innerRight) {
		assert pathTemporal != null;
        TypeAlgebra typeWeight = TypeWeight.get();    
        BitSet allNodes = UtilBitSet.newBitSetUnbounded();
        allNodes.set(0, graph.getNumNodes(), true);

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
        BitSet unKnown = UtilPETL.getUnKnownStates(oneSet, zeroSet, graph);
        double[] resultValue = UtilUCT.computeProbabilities(oneSet, zeroSet, min, negate,unKnown, computeForStates, graph,modelChecker);
        
        Type type = TypeDouble.get();
        ValueArray resultValues = UtilValue.newArray(type.getTypeArray(), computeForStates.size());
        for (int stateNr = 0; stateNr < computeForStates.size(); stateNr++) {
            ValueDouble value = (ValueDouble) type.newValue();
            value.set(resultValue[stateNr]);
            resultValues.set(value, stateNr);
        }
		return UtilGraph.newStateMap(computeForStates.clone(), resultValues);
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
    
    private Expression not(Expression expression) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorNot.NOT)
                .setPositional(expression.getPositional())
                .setOperands(expression)
                .build();
    }
}

package epmc.propertysolver;

import java.util.LinkedHashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;

public final class PropertySolverExplicitSteadyState implements PropertySolver {
    public final static String IDENTIFIER = "explicit-steady-state";
	private ModelChecker modelChecker;
	private Expression property;
	private StateSet forStates;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setModelChecker(ModelChecker modelChecker) {
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
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        Expression quantified = propertyQuantifier.getQuantified();
        if (!ExpressionSteadyState.isSteadyState(quantified)) {
        	return false;
        }
		return true;
	}

	@Override
	public Set<Object> getRequiredGraphProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.SEMANTICS);
    	ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
    	ExpressionSteadyState steadyState = ExpressionSteadyState.asSteadyState(propertyQuantifier.getQuantified());
    	required.addAll(modelChecker.getRequiredNodeProperties(steadyState.getOperand1(), null));
    	return required;
	}

	@Override
	public Set<Object> getRequiredNodeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.STATE);
    	required.add(CommonProperties.PLAYER);
    	ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
    	ExpressionSteadyState steadyState = ExpressionSteadyState.asSteadyState(propertyQuantifier.getQuantified());
    	required.addAll(modelChecker.getRequiredNodeProperties(steadyState.getOperand1(), null));
    	return required;
	}

	@Override
	public Set<Object> getRequiredEdgeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.WEIGHT);
    	ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
    	ExpressionSteadyState steadyState = ExpressionSteadyState.asSteadyState(propertyQuantifier.getQuantified());
    	required.addAll(modelChecker.getRequiredEdgeProperties(steadyState.getOperand1(), null));

    	return required;
	}

	@Override
	public StateMap solve() throws EPMCException {
    	ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
    	ExpressionSteadyState steadyState = ExpressionSteadyState.asSteadyState(propertyQuantifier.getQuantified());

		// TODO Auto-generated method stub
		return null;
	}

}

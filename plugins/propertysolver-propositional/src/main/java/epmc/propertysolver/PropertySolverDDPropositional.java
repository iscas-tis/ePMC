package epmc.propertysolver;

import static epmc.expression.standard.ExpressionPropositional.isPropositional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.StateSetDD;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;

public final class PropertySolverDDPropositional implements PropertySolver {
    public final static String IDENTIFIER = "propositional-dd";
    private ModelChecker modelChecker;
    private ExpressionToDD expressionToDD;
	private Expression property;
	private StateSet forStates;
    
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
    public StateMap solve() throws EPMCException {
        assert property != null;
        assert forStates != null;
        DD value;
        if (modelChecker.getEngine() instanceof EngineDD) {
        	GraphDD graphDD = modelChecker.getLowLevel();
            this.expressionToDD = graphDD.getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
        }
        
        if (property instanceof ExpressionLiteral) {
        	ExpressionLiteral propertyLiteral = (ExpressionLiteral) property;
            value = (ContextDD.get(modelChecker.getModel().getContextValue())).newConstant(propertyLiteral.getValue());
        } else {
            value = expressionToDD.translate(property);
        }
        StateMap result = new StateMapDD((StateSetDD) forStates.clone(), value);
        return result;
    }
    
    @Override
    public Set<Object> getRequiredGraphProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.EXPRESSION_TO_DD);
    	return Collections.unmodifiableSet(required);
    }
    
    @Override
    public Set<Object> getRequiredNodeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	return Collections.unmodifiableSet(required);
    }
    
    @Override
    public Set<Object> getRequiredEdgeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	return Collections.unmodifiableSet(required);
    }

    @Override
    public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!isPropositional(property)) {
            return false;
        }
        return true;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}

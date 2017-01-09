package epmc.filter.propertysolver;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionFilter;
import epmc.expression.standard.FilterType;
import epmc.graph.CommonProperties;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.StateSetDD;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.value.ContextValue;
import epmc.value.OperatorAdd;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

/**
 * Solver for filter properties for the DD engine.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PropertySolverDDFilter implements PropertySolver {
	/** Identifier of this property solver class. */
    public final static String IDENTIFIER = "filter-dd";
    /** Model checker used in the property solver class. */
    private ModelChecker modelChecker;
	private Expression property;
	private ExpressionFilter propertyFilter;
	private StateSet forStates;
    
    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
    }
        

	@Override
	public void setProperty(Expression property) {
		this.property = property;
		this.propertyFilter = ExpressionFilter.asFilter(property);
	}

	@Override
	public void setForStates(StateSet forStates) {
		this.forStates = forStates;
	}


    @Override
    public StateMap solve() throws EPMCException {
        assert property != null;
        assert forStates != null;
        StateSetDD allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        StateMapDD props = (StateMapDD) modelChecker.check(propertyFilter.getProp(), allStates);
        StateMapDD states = (StateMapDD) modelChecker.check(propertyFilter.getStates(), allStates);
        FilterType type = propertyFilter.getFilterType();
        DD resultDD = solve(type, props.getValuesDD(), states.getValuesDD(), allStates.getStatesDD());
        allStates.close();
        assert resultDD != null;
        return new StateMapDD((StateSetDD) forStates.clone(), resultDD);
    }

    @Override
    public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!ExpressionFilter.isFilter(property)) {
            return false;
        }
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        modelChecker.ensureCanHandle(propertyFilter.getProp(), allStates);
        modelChecker.ensureCanHandle(propertyFilter.getStates(), allStates);
        if (allStates != null) {
        	allStates.clone();
        }
        return true;
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
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        ExpressionFilter propertyFilter = ExpressionFilter.asFilter(property);
        required.addAll(modelChecker.getRequiredNodeProperties(propertyFilter.getProp(), allStates));
        required.addAll(modelChecker.getRequiredNodeProperties(propertyFilter.getStates(), allStates));
    	return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        ExpressionFilter propertyFilter = ExpressionFilter.asFilter(property);
        required.addAll(modelChecker.getRequiredEdgeProperties(propertyFilter.getProp(), allStates));
        required.addAll(modelChecker.getRequiredEdgeProperties(propertyFilter.getStates(), allStates));
    	return Collections.unmodifiableSet(required);
    }
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Performs the actual filtering process.
     * This method shall be used once the properties specified in the filter
     * have been solved.
     * 
     * @param filter filter type used
     * @param property property values
     * @param states state of the filter
     * @param modelStates all states of the model
     * @return result of filter computation
     * @throws EPMCException thrown in case of problems during filtering
     */
    private DD solve(FilterType filter, DD property, DD states,
	            DD modelStates) throws EPMCException {
    	assert filter != null;
    	assert property != null;
    	assert states != null;
    	assert modelStates != null;
    	// TODO type checks
    	DD result = null;
    	DD reachableStates = modelStates.and(getModel().getNodeProperty(CommonProperties.STATE));
    	switch (filter) {
    	case ARGMAX:
    		break;
    	case ARGMIN:
    		break;
    	case AVG: {
    		DD checkFor = reachableStates.and(states);
    		Value avg = property.applyOverSat(getContextValue().getOperator(OperatorAdd.IDENTIFIER), getModel().getPresCube(), checkFor);
    		int numStates = checkFor.countSat(getModel().getPresCube()).intValue();
            Value numStatesValue = UtilValue.newValue(TypeInteger.get(getContextValue()), numStates);
    		ValueAlgebra.asAlgebra(avg).divide(avg, numStatesValue);
    		checkFor.dispose();
    		result = getContextDD().newConstant(avg);
    		break;
    	}
    	case COUNT:
    		DD countDD = reachableStates.and(states, property);
    		BigInteger numSat = countDD.countSat(getModel().getPresCube());
    		// TODO what to do if this value is too large?
    		result = getContextDD().newConstant(numSat.intValue());
    		countDD.dispose();
    		break;
    	case EXISTS: {
    		DD checkFor = reachableStates.and(states);
    		Value value = property.orOverSat(checkFor);
    		checkFor.dispose();
    		result = getContextDD().newConstant(value);
    		break;            
    	}
    	case FIRST:
    		break;
    	case FORALL: {
    		DD checkFor = reachableStates.and(states);
    		Value value = property.andOverSat(checkFor);
    		checkFor.dispose();
    		result = getContextDD().newConstant(value);
    		break;            
    	}
    	case MAX: {
    		DD checkFor = reachableStates.and(states);
    		Value value = property.maxOverSat(checkFor);
    		checkFor.dispose();
    		result = getContextDD().newConstant(value);
    		break;
    	}
    	case MIN: {
    		DD checkFor = reachableStates.and(states);
    		Value value = property.minOverSat(checkFor);
    		checkFor.dispose();
    		result = getContextDD().newConstant(value);
    		break;
    	}
    	case PRINT:
    		break;
    	case PRINTALL:
    		break;
    	case RANGE: {
    		DD checkFor = reachableStates.and(states);
    		Value min = property.minOverSat(checkFor);
    		Value max = property.maxOverSat(checkFor);
    		checkFor.dispose();
    		Value interval = TypeInterval.get(getContextValue()).newValue(min, max);
    		result = getContextDD().newConstant(interval);
    		break;
    	}
    	case STATE: {
    		// TODO check that only one state fulfils the filter
    		DD checkFor = reachableStates.and(states);
    		Value value = property.maxOverSat(checkFor);
    		checkFor.dispose();
    		result = getContextDD().newConstant(value);
	/*
	            DD checkFor = reachableStates.and(states);
	//            int numFulfil = checkFor.countSat(model.getPresCube()).intValue();
	            checkFor.dispose();
	            */
    		break;
    	}
    	case SUM: {
    		DD checkFor = reachableStates.and(states);
    		Value sum = property.applyOverSat(getContextValue().getOperator(OperatorAdd.IDENTIFIER), getModel().getPresCube(), checkFor);
    		checkFor.dispose();
    		result = getContextDD().newConstant(sum);
    		break;
    	}
    	default:
    		break;
    	}
    	reachableStates.dispose();
    	return result;
    }

    /**
     * Get DD context used.
     * 
     * @return DD context used
     * @throws EPMCException thrown in case of failure getting DD context
     */
	private ContextDD getContextDD() throws EPMCException {
    	return ContextDD.get(modelChecker.getModel().getContextValue());
	}

	/**
	 * Get value context used.
	 * 
	 * @return value context used
	 */
    private ContextValue getContextValue() {
    	return modelChecker.getModel().getContextValue();
    }

    /**
     * Get model to analyse.
     * Note that the model checking engine must be DD.
	 *
     * @return model to analyse
     */
    private GraphDD getModel() {
    	return modelChecker.getLowLevel();
    }
}
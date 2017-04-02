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

package epmc.filter.propertysolver;

import static epmc.error.UtilError.ensure;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionFilter;
import epmc.filter.error.ProblemsFilter;
import epmc.filter.messages.MessagesFilter;
import epmc.graph.CommonProperties;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.value.Type;
import epmc.value.TypeArrayConstant;
import epmc.value.TypeNumber;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueBoolean;

// TODO complete documentation

/**
 * Solver for filter properties for the explicit-state engine.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PropertySolverExplicitFilter implements PropertySolver {
	/** Identifier of this property solver class. */
    public final static String IDENTIFIER = "filter-explicit";
    /** Model checker used in the property solver class. */
    private ModelChecker modelChecker;
    /** Property to be handled by this solver. */
	private Expression property;
	private ExpressionFilter propertyFilter;
	private StateSet forStates;
    
    @Override
	public String getIdentifier() {
	    return IDENTIFIER;
	}

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
        assert forStates != null;
        // TODO should first check states to compute values for, then only compute values for these
        StateSetExplicit allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        StateMapExplicit prop = (StateMapExplicit) modelChecker.check(propertyFilter.getProp(), allStates);
        StateMapExplicit states = (StateMapExplicit) modelChecker.check(propertyFilter.getStates(), allStates);
        Value statesEntry = states.getType().newValue();
        Value propEntry = prop.getType().newValue();
        int statesSize = states.size();
        for (int i = 0; i < statesSize; i++) {
            states.getExplicitIthValue(statesEntry, i);
            if (ValueBoolean.asBoolean(statesEntry).getBoolean()) {
                prop.getExplicitIthValue(propEntry, i);
                break;
            }
        }
        allStates.close();
        Value resultValue = propertyFilter.initialAccumulatorValue(modelChecker.getLowLevel(), propEntry);
        int numStatesInFilter = 0;
        for (int i = 0; i < statesSize; i++) {
            states.getExplicitIthValue(statesEntry, i);
            if (ValueBoolean.asBoolean(statesEntry).getBoolean()) {
                numStatesInFilter++;
            }
        }
        getLog().send(MessagesFilter.NUM_STATES_IN_FILTER, numStatesInFilter, propertyFilter.getStates());
        if (propertyFilter.isPrint()) {
            getLog().send(MessagesFilter.PRINTING_FILTER_RESULTS);            
        } else if (propertyFilter.isPrintAll()) {
            getLog().send(MessagesFilter.PRINTING_ALL_FILTER_RESULTS);
        }
        int stateNr = 0;
        Type typeProperty = propertyFilter.getType(modelChecker.getLowLevel());
        for (int i = 0; i < allStates.size(); i++) {
            int state = allStates.getExplicitIthState(i);
            prop.getExplicitIthValue(propEntry, i);
            states.getExplicitIthValue(statesEntry, i);
            if (ValueBoolean.asBoolean(statesEntry).getBoolean()) {
            	propertyFilter.accumulate(resultValue, propEntry);
                if (propertyFilter.isPrint()) {
                    if (!ValueAlgebra.asAlgebra(propEntry).isZero()) {
                        getLog().send(MessagesFilter.PRINT_FILTER, stateNr, state, propEntry);
                    }
                } else if (propertyFilter.isPrintAll()) {
                    getLog().send(MessagesFilter.PRINT_FILTER, stateNr, state, propEntry);
                }
            }
            stateNr++;
        }
        ensure(!propertyFilter.isState() || numStatesInFilter <= 1,
                ProblemsFilter.FILTER_STATE_MORE_THAN_ONE, property);
        if (propertyFilter.isAvg()) {
            Value num = UtilValue.newValue(TypeNumber.asNumber(resultValue.getType()), numStatesInFilter);
            ValueAlgebra.asAlgebra(resultValue).divide(resultValue, num);
        }
        
        ValueArray resultValues = null;
        if (propertyFilter.isSameResultForAllStates()) {
        	resultValues = UtilValue.newArray(new TypeArrayConstant(typeProperty), forStates.size());
            resultValues.set(resultValue, 0);
        } else if (propertyFilter.isArgMin() || propertyFilter.isArgMax()) {
            resultValues = UtilValue.newArray(typeProperty.getTypeArray(), forStates.size());
            ValueBoolean compare = ValueBoolean.asBoolean(typeProperty.newValue());
            int allStatesNr = 0;
            int allState;
            StateSetExplicit forStatesExplicit = (StateSetExplicit) forStates;
            for (int forStatesNr = 0; forStatesNr < forStates.size(); forStatesNr++) {
                int forState = forStatesExplicit.getExplicitIthState(forStatesNr);
                allState = allStates.getExplicitIthState(allStatesNr);
                while (allState < forState) {
                    allStatesNr++;
                    allState = allStates.getExplicitIthState(allStatesNr);
                }
                prop.getExplicitIthValue(propEntry, allStatesNr);
                compare.set(propEntry.isEq(resultValue));
                resultValues.set(compare, forStatesNr);
            }
        }
        StateMap result;
        if (propertyFilter.isPrint() || propertyFilter.isPrintAll()) {
            result = prop.restrict(forStates);
        } else {
            result = UtilGraph.newStateMap((StateSetExplicit) forStates.clone(), resultValues);
        }
        
        return result;
    }

    @Override
    public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        if (!ExpressionFilter.isFilter(property)) {
            return false;
        }
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        modelChecker.ensureCanHandle(propertyFilter.getProp(), allStates);
        modelChecker.ensureCanHandle(propertyFilter.getStates(), allStates);
        if (allStates != null) {
        	allStates.close();
        }
        return true;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.SEMANTICS);
    	return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
    	required.add(CommonProperties.STATE);
    	required.add(CommonProperties.PLAYER);
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        required.addAll(modelChecker.getRequiredNodeProperties(propertyFilter.getProp(), allStates));
        required.addAll(modelChecker.getRequiredNodeProperties(propertyFilter.getStates(), allStates));
    	return Collections.unmodifiableSet(required);
    }
    
    @Override
    public Set<Object> getRequiredEdgeProperties() throws EPMCException {
    	Set<Object> required = new LinkedHashSet<>();
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        required.addAll(modelChecker.getRequiredEdgeProperties(propertyFilter.getProp(), allStates));
        required.addAll(modelChecker.getRequiredEdgeProperties(propertyFilter.getStates(), allStates));
    	return Collections.unmodifiableSet(required);
    }

    /**
     * Get log used.
     * 
     * @return log used
     */
    private Log getLog() {
    	return modelChecker.getModel().getContextValue().getOptions().get(OptionsMessages.LOG);
    }
}

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

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
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
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueInterval;

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
        this.propertyFilter = ExpressionFilter.as(property);
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }


    @Override
    public StateMap solve() {
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
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!ExpressionFilter.is(property)) {
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
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        required.add(CommonProperties.EXPRESSION_TO_DD);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        ExpressionFilter propertyFilter = ExpressionFilter.as(property);
        required.addAll(modelChecker.getRequiredNodeProperties(propertyFilter.getProp(), allStates));
        required.addAll(modelChecker.getRequiredNodeProperties(propertyFilter.getStates(), allStates));
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        ExpressionFilter propertyFilter = ExpressionFilter.as(property);
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
     */
    private DD solve(FilterType filter, DD property, DD states,
            DD modelStates) {
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
            Value avg = property.applyOverSat(OperatorAdd.ADD, getModel().getPresCube(), checkFor);
            int numStates = checkFor.countSat(getModel().getPresCube()).intValue();
            Value numStatesValue = UtilValue.newValue(TypeInteger.get(), numStates);
            OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, avg.getType(), numStatesValue.getType());
            divide.apply(avg, avg, numStatesValue);
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
            ValueInterval interval = TypeInterval.get().newValue();
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
            set.apply(interval.getIntervalLower(), min);
            set.apply(interval.getIntervalUpper(), max);
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
            Value sum = property.applyOverSat(OperatorAdd.ADD, getModel().getPresCube(), checkFor);
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
     */
    private ContextDD getContextDD() {
        return ContextDD.get();
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

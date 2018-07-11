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

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionFilter;
import epmc.expression.standard.FilterType;
import epmc.expression.standard.ProblemsExpression;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expressionevaluator.ExpressionToType;
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
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorOr;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayConstant;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeNumber;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;

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
        this.propertyFilter = ExpressionFilter.as(property);
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public StateMap solve() {
        assert forStates != null;
        // TODO should first check states to compute values for, then only compute values for these
        StateSetExplicit allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        StateMapExplicit prop = (StateMapExplicit) modelChecker.check(propertyFilter.getProp(), allStates);
        StateMapExplicit states = (StateMapExplicit) modelChecker.check(propertyFilter.getStates(), allStates);
        Value statesEntry = states.getType().newValue();
        Value propEntry = prop.getType().newValue();
        ValueBoolean cmp = TypeBoolean.get().newValue();
        OperatorEvaluator isZero = null;
        if (propertyFilter.isPrint()) {
            isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, propEntry.getType());
        }
        int statesSize = states.size();
        for (int i = 0; i < statesSize; i++) {
            states.getExplicitIthValue(statesEntry, i);
            if (ValueBoolean.as(statesEntry).getBoolean()) {
                prop.getExplicitIthValue(propEntry, i);
                break;
            }
        }
        allStates.close();
        Value resultValue = initialAccumulatorValue(propertyFilter.getFilterType(), modelChecker.getLowLevel(), propEntry);
        int numStatesInFilter = 0;
        for (int i = 0; i < statesSize; i++) {
            states.getExplicitIthValue(statesEntry, i);
            if (ValueBoolean.as(statesEntry).getBoolean()) {
                numStatesInFilter++;
            }
        }
        getLog().send(MessagesFilter.NUM_STATES_IN_FILTER, numStatesInFilter, UtilExpressionStandard.niceForm(propertyFilter.getStates()));
        if (propertyFilter.isPrint()) {
            getLog().send(MessagesFilter.PRINTING_FILTER_RESULTS);            
        } else if (propertyFilter.isPrintAll()) {
            getLog().send(MessagesFilter.PRINTING_ALL_FILTER_RESULTS);
        }
        int stateNr = 0;
        Type typeProperty = getType(propertyFilter.getFilterType(), propertyFilter.getPositional(), prop.getType(), states.getType());
        OperatorEvaluator accumulator = getAccumulator(propertyFilter.getFilterType(), resultValue, propEntry);
        for (int i = 0; i < allStates.size(); i++) {
            int state = allStates.getExplicitIthState(i);
            prop.getExplicitIthValue(propEntry, i);
            states.getExplicitIthValue(statesEntry, i);
            if (ValueBoolean.as(statesEntry).getBoolean()) {
                accumulator.apply(resultValue, resultValue, propEntry);
                if (propertyFilter.isPrint()) {
                    isZero.apply(cmp, propEntry);
                    if (!cmp.getBoolean()) {
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
            Value num = UtilValue.newValue(TypeNumber.as(resultValue.getType()), numStatesInFilter);
            OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, resultValue.getType(), num.getType());
            divide.apply(ValueAlgebra.as(resultValue), resultValue, num);
        }

        ValueArray resultValues = null;
        OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, prop.getType(), resultValue.getType());
        if (propertyFilter.isSameResultForAllStates()) {
            resultValues = UtilValue.newArray(new TypeArrayConstant(resultValue.getType()), forStates.size());
            resultValues.set(resultValue, 0);
        } else if (propertyFilter.isArgMin() || propertyFilter.isArgMax()) {
            resultValues = UtilValue.newArray(typeProperty.getTypeArray(), forStates.size());
            ValueBoolean compare = ValueBoolean.as(typeProperty.newValue());
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
                eq.apply(cmp, propEntry, resultValue);
                compare.set(cmp.getBoolean());
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
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        if (!ExpressionFilter.is(property)) {
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
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        required.addAll(modelChecker.getRequiredNodeProperties(propertyFilter.getProp(), allStates));
        required.addAll(modelChecker.getRequiredNodeProperties(propertyFilter.getStates(), allStates));
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        required.addAll(modelChecker.getRequiredEdgeProperties(propertyFilter.getProp(), allStates));
        required.addAll(modelChecker.getRequiredEdgeProperties(propertyFilter.getStates(), allStates));
        return Collections.unmodifiableSet(required);
    }

    private static OperatorEvaluator getAccumulator(FilterType type, Value resultValue, Value value) {
        switch (type) {
        case ARGMAX: case MAX: {
            OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, resultValue.getType(), value.getType());
            return max;
        }
        case ARGMIN: case MIN: {
            OperatorEvaluator min = ContextValue.get().getEvaluator(OperatorMin.MIN, resultValue.getType(), value.getType());
            return min;
        }
        case AVG: {
            OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, resultValue.getType(), value.getType());
            return add;
        }
        case COUNT: {
            return new OperatorEvaluator() {
                private final OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, resultValue.getType(), value.getType());
                private Value zero = UtilValue.newValue(TypeAlgebra.as(resultValue.getType()), 0);
                
                @Override
                public Type resultType() {
                    return resultValue.getType();
                }
                
                @Override
                public void apply(Value result, Value... operands) {
                    add.apply(resultValue, resultValue, ValueBoolean.as(value).getBoolean()
                            ? UtilValue.newValue(TypeAlgebra.as(resultValue.getType()), 1)
                                    : zero);
                }
            };
        }
        case EXISTS: {
            OperatorEvaluator or = ContextValue.get().getEvaluator(OperatorOr.OR, TypeBoolean.get(), TypeBoolean.get());
            return or;
        }
        case FIRST:
        case PRINT:
        case PRINTALL:
        case STATE:
            return new OperatorEvaluator() {
                
                @Override
                public Type resultType() {
                    return resultValue.getType();
                }
                
                @Override
                public void apply(Value result, Value... operands) {
                }
            };
        case FORALL: {
            OperatorEvaluator and = ContextValue.get().getEvaluator(OperatorAnd.AND, TypeBoolean.get(), TypeBoolean.get());
            return and;
        }
        case RANGE: {
            return new OperatorEvaluator() {
                private final OperatorEvaluator min = ContextValue.get().getEvaluator(OperatorMin.MIN, resultValue.getType(), value.getType());
                private final OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, resultValue.getType(), value.getType());
                
                @Override
                public Type resultType() {
                    return resultValue.getType();
                }
                
                @Override
                public void apply(Value result, Value... operands) {
                    Value resLo = ValueInterval.as(resultValue).getIntervalLower();
                    Value resUp = ValueInterval.as(resultValue).getIntervalUpper();
                    min.apply(resLo, resLo, value);
                    max.apply(resUp, resUp, value);
                }
            };
        }
        case SUM: {
            OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, resultValue.getType(), value.getType());
            return add;
        }
        default:
            throw new RuntimeException();
        }
    }

    private static Value initialAccumulatorValue(FilterType type, ExpressionToType expressionToType, Value value) {
        assert expressionToType != null;
        assert value != null;
        switch (type) {
        case COUNT:
            return UtilValue.clone(UtilValue.newValue(TypeInteger.get(), 0));
        case EXISTS:
            return UtilValue.clone(UtilValue.newValue(TypeBoolean.get(), false));
        case FORALL:
            return UtilValue.clone(UtilValue.newValue(TypeBoolean.get(), true));
        case RANGE:
            ValueInterval result = TypeInterval.get().newValue();
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
            set.apply(result.getIntervalLower(), value);
            set.apply(result.getIntervalUpper(), value);
            return result;
        case AVG:
            return UtilValue.clone(UtilValue.newValue(TypeWeight.get(), 0));
        case SUM:
            return UtilValue.clone(UtilValue.newValue(TypeWeight.get(), 0));
        default:
            return UtilValue.clone(UtilValue.clone(value));
        }
    }
    
    /**
     * Get log used.
     * 
     * @return log used
     */
    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
    
    private Type getType(FilterType type,
            Positional positional,
            Type propType, Type statesType) {
        Type result = null;
        if (TypeInteger.isIntegerWithBounds(propType)) {
            propType = TypeInteger.get();
        }
        ensure(statesType == null || TypeBoolean.is(statesType),
                ProblemsExpression.EXPR_INCONSISTENT, positional, "", this);
        switch (type) {
        case AVG:
            ensure(propType == null || TypeWeight.is(propType),
            ProblemsExpression.EXPR_INCONSISTENT, positional, "", this);
            result = TypeWeight.get();
            break;
        case SUM:
            ensure(propType == null || TypeWeight.is(propType)
            || TypeInteger.is(propType),
            ProblemsExpression.EXPR_INCONSISTENT, positional, "", this);
            result = TypeWeight.get();
            break;
        case RANGE:
            ensure(propType == null || TypeReal.is(propType)
            || TypeInterval.is(propType),
            ProblemsExpression.EXPR_INCONSISTENT, positional, "", this);
            result = TypeInterval.get();
            break;
        case MAX: case MIN:
            ensure(propType == null || TypeReal.is(propType)
            || TypeInteger.is(propType),
            ProblemsExpression.EXPR_INCONSISTENT, positional, "", this);
            result = TypeReal.get();
            break;
        case COUNT:
            ensure(propType == null || TypeBoolean.is(propType),
            ProblemsExpression.EXPR_INCONSISTENT, positional, "");
            result = TypeInteger.get();
            break;
        case FIRST: case STATE: case PRINT: case PRINTALL:
            result = propType;
            break;
        case FORALL: case EXISTS:
            ensure(propType == null || TypeBoolean.is(propType),
            ProblemsExpression.EXPR_INCONSISTENT, positional, "", this);
            result = TypeBoolean.get();
            break;
        case ARGMAX: case ARGMIN:
            ensure(propType == null || TypeReal.is(propType),
            ProblemsExpression.EXPR_INCONSISTENT, positional, "", this);
            result = TypeBoolean.get();
            break;
        }
        return result;
    }
}

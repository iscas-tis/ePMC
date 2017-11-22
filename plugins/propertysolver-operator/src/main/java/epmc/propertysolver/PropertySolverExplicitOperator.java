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

import epmc.expression.standard.ExpressionOperator;
import epmc.value.TypeArrayConstant;
import epmc.value.UtilValue;
import epmc.expression.Expression;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class PropertySolverExplicitOperator implements PropertySolver {
    public final static String IDENTIFIER = "operator-explicit";
    private ModelChecker modelChecker;
    private Expression property;
    private ExpressionOperator propertyOperator;
    private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
    }

    @Override
    public void setProperty(Expression property) {
        this.property = property;
        if (property instanceof ExpressionOperator) {
            this.propertyOperator = (ExpressionOperator) property;
        }
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public StateMap solve() {
        assert forStates != null;
        List<StateMapExplicit> innerResults = new ArrayList<>();
        boolean allConstant = true;
        for (Expression innerProperty : propertyOperator.getOperands()) {
            StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(innerProperty, forStates);
            if (!innerResult.isConstant()) {
                allConstant = false;
            }
            innerResults.add(innerResult);
        }
        Value[] operands = new Value[innerResults.size()];
        Type[] types = new Type[innerResults.size()];
        for (int operandNr = 0; operandNr < innerResults.size(); operandNr++) {
            Type opType = innerResults.get(operandNr).getType();
            operands[operandNr] = opType.newValue();
            types[operandNr] = opType;
        }
        ValueArray resultValues;

        OperatorEvaluator evaluator = ContextValue.get().getEvaluator(propertyOperator.getOperator(), types);
        Type type = evaluator.resultType();
        if (allConstant) {
            resultValues = UtilValue.newArray(new TypeArrayConstant(type), forStates.size());
        } else {
            resultValues = UtilValue.newArray(type.getTypeArray(), forStates.size());
        }
        Value res = type.newValue();
        int forStatesSize = forStates.size();
        int innerResultsSize = innerResults.size();
        for (int node = 0; node < forStatesSize; node++) {
            for (int operandNr = 0; operandNr < innerResultsSize; operandNr++) {
                innerResults.get(operandNr).getExplicitIthValue(operands[operandNr], node);
            }
            evaluator.apply(res, operands);
            resultValues.set(res, node);
        }
        StateMap result = UtilGraph.newStateMap((StateSetExplicit) forStates.clone(), resultValues);

        return result;
    }

    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        if (!(property instanceof ExpressionOperator)) {
            return false;
        }
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression operand : propertyOperator.getOperands()) {
            modelChecker.ensureCanHandle(operand, allStates);
        }
        if (allStates != null) {
            allStates.close();
        }
        return true;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression operand : propertyOperator.getOperands()) {
            required.addAll(modelChecker.getRequiredGraphProperties(operand, allStates));
        }
        return required;
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression operand : propertyOperator.getOperands()) {
            required.addAll(modelChecker.getRequiredNodeProperties(operand, allStates));
        }
        return required;
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        for (Expression operand : propertyOperator.getOperands()) {
            required.addAll(modelChecker.getRequiredEdgeProperties(operand, forStates));
        }
        return required;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}

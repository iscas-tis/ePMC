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
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.expression.Expression;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.dd.StateSetDD;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;

public final class PropertySolverDDOperator implements PropertySolver {
    public final static String IDENTIFIER = "operator-dd";
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
        List<DD> operandsDD = new ArrayList<>();
        List<StateMap> operandsState = new ArrayList<>();
        for (Expression operand : propertyOperator.getOperands()) {
            StateMapDD stateMap = (StateMapDD) modelChecker.check(operand, forStates);
            operandsState.add(stateMap);
            operandsDD.add(stateMap.getValuesDD());
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) property;
        DD resultDD = getContextDD().apply(expressionOperator.getOperator(), operandsDD);
        for (StateMap operand : operandsState) {
            operand.close();
        }
        return new StateMapDD((StateSetDD) forStates.clone(), resultDD);
    }

    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!(property instanceof ExpressionOperator)) {
            return false;
        }
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
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
        return required;
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        for (Expression operand : propertyOperator.getOperands()) {
            required.addAll(modelChecker.getRequiredNodeProperties(operand, forStates));
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

    public ContextDD getContextDD() {
        return ContextDD.get();
    }


}

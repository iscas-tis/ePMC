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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.dd.GraphDD;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.Operator;

// TODO check whether this works for JANI MDPs - probably not
// TODO transform to DD checker

public final class PropertySolverDDReward implements PropertySolver {
    public final static String IDENTIFIER = "reward-dd";
    private ModelChecker modelChecker;
    private GraphDD graph;
    private Expression property;
    private ExpressionQuantifier propertyQuantifier;
    private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        if (modelChecker.getEngine() instanceof EngineDD) {
            this.graph = modelChecker.getLowLevel();
        }
    }

    @Override
    public void setProperty(Expression property) {
        this.property = property;
        if (property instanceof ExpressionQuantifier) {
            this.propertyQuantifier = (ExpressionQuantifier) property;
        }
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.EXPRESSION_TO_DD);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        return Collections.emptySet();
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        return Collections.emptySet();
    }

    @Override
    public StateMap solve() {
        ExpressionReward quantifiedProp = (ExpressionReward) propertyQuantifier.getQuantified();
        if (quantifiedProp.getRewardType().isReachability()) {
            StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
            Expression reachSet = ((ExpressionReward) (propertyQuantifier.getQuantified())).getRewardReachSet();
            StateMapDD innerResult = (StateMapDD) modelChecker.check(reachSet, allStates);
            ExpressionToDD expressionToDD = graph
                    .getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
            expressionToDD.putConstantWith(reachSet, innerResult.getValuesDD());
            allStates.close();
        }
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        boolean min = dirType == DirType.MIN;
        //        StateMap result = doSolve(quantifiedProp, forStates, min);
        if (propertyQuantifier.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType();
            //            result = result.applyWith(op, compare);
        }
        //      return result;
        return null;
    }


    @Override
    public boolean canHandle() {
        assert property != null;
        assert forStates != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        if (!(propertyQuantifier.getQuantified() instanceof ExpressionReward)) {
            return false;
        }
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        if (((ExpressionReward) (propertyQuantifier.getQuantified())).getRewardType().isReachability()) {
            modelChecker.ensureCanHandle(((ExpressionReward) (propertyQuantifier.getQuantified())).getRewardReachSet(), allStates);
        }
        allStates.close();
        return true;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}

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

package epmc.constraintsolver;

import java.util.LinkedHashSet;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.expression.Expression;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueArray;

// TODO correct class once we actually use it

/**
 * Solve constraint problems over finite variables using decision diagrams.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ConstraintSolverDD implements ConstraintSolver {
    public final static String IDENTIFIER = "dd";

    private boolean closed;
    private ExpressionToDD expressionToDD;
    private DD conjunction;
    private Expression objective;
    private Direction direction;
    private final Set<Feature> features = new LinkedHashSet<>();

    @Override
    public void requireFeature(Feature feature) {
        assert feature != null;
        features.add(feature);
    }

    @Override
    public boolean canHandle() {
        // TODO 
        return false;
    }

    @Override
    public void build() {
        this.conjunction = getContextDD().newConstant(false);
    }


    private ContextDD getContextDD() {
        return ContextDD.get();
    }

    public void addConstraint(DD constraint) {
        assert !closed;
        assert constraint != null;
        conjunction = conjunction.andWith(constraint);
    }

    @Override
    public void addConstraint(Expression expression) {
        DD dd = expressionToDD.translate(expression);
        addConstraint(dd);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        conjunction.dispose();
    }

    @Override
    public void setObjective(Expression objective) {
        assert objective != null;
        this.objective = objective;
    }

    @Override
    public int addVariable(String name, Type type) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void addConstraint(ValueArray row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addConstraint(Value[] row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDirection(Direction direction) {
        assert direction != null;
        this.direction = direction;
    }

    @Override
    public void setObjective(ValueArray row, int[] variables) {
        // TODO Auto-generated method stub

    }

    @Override
    public ConstraintSolverResult solve() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setObjective(Value[] row, int[] variables) {
        // TODO Auto-generated method stub

    }

    @Override
    public Value getResultObjectiveValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ValueArray getResultVariablesValuesSingleType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int addVariable(String name, Type type, Value lower, Value upper) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Value[] getResultVariablesValues() {
        // TODO Auto-generated method stub
        return null;
    }

}


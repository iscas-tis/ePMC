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

import java.io.Closeable;

import epmc.expression.Expression;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;

/**
 * Interface which constraint solvers should implement.
 * A constraint solver might be a linear-programming solver, an SMT solver, etc.
 * 
 * Note: the interface will probably be changed once if have time to do so.
 * Also, the solvers will be divided into several plugins. Before this is done,
 * I won't work much on the documentation of this class.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ConstraintSolver extends Closeable {
    /* methods to be implemented by implementing classes */

    String getIdentifier();

    void requireFeature(Feature feature);

    boolean canHandle();

    void build();

    int addVariable(String name, Type type, Value lower, Value upper);

    void addConstraint(Expression expression);

    void setObjective(Expression objective);

    void setDirection(Direction direction);

    ConstraintSolverResult solve();

    Value[] getResultVariablesValues();

    Value getResultObjectiveValue();

    @Override
    void close();


    /* default methods */

    /*
    default int addVariable(ExpressionIdentifierStandard identifier) {
        assert identifier != null;
        String name = identifier.getName();
        Object scope = identifier.getScope();
        if (scope != null) {
            name = name + ":" + scope;
        }
        Type type = identifier.getType();
        return addVariable(name, type);
    }
     */

    default int addVariable(String name, Type type) {
        // TODO
        //		Value lower = type.getNegInf();
        //		Value upper = type.getPosInf();
        return addVariable(name, type, null, null);
    }

    default ValueArray getResultVariablesValuesSingleType() {
        Value[] values = getResultVariablesValues();
        Type type = values[0].getType();
        for (Value value : values) {
            type = UtilValue.upper(type, value.getType());
        }
        ValueArray result = UtilValue.newArray(type.getTypeArray(), values.length);
        for (int index = 0; index < values.length; index++) {
            result.set(values[index], index);
        }
        return result;
    }

    default String getVariableName(int number) {
        return null;
    }

    default void addConstraint(ValueArray row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        addConstraint(UtilConstraintSolver.linearToExpression(this, row, variables, constraintType, rightHandSide));
    }

    default void addConstraint(Value[] row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        addConstraint(UtilConstraintSolver.linearToExpression(this, row, variables, constraintType, rightHandSide));
    }

    default void setObjective(ValueArray row, int[] variables) {
        setObjective(UtilConstraintSolver.linearToExpression(this, row, variables));
    }

    default void setObjective(Value[] row, int[] variables) {
        setObjective(UtilConstraintSolver.linearToExpression(this, row, variables));
    }
}

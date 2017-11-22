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

package epmc.dd;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import epmc.value.Type;
import epmc.value.TypeEnumerable;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueEnumerable;

// TODO documentation
// TODO divide VariableImpl into different classes for different types

/**
 * <p>
 * Represents a {@link Type} in terms of a list of DD variables.
 * </p>
 * <p>
 * Assuming that a finite number of values needs to be represented, it is
 * possible to use a finite number of boolean DD variables such that each
 * assignment to these variables represents one of these values.
 * Different copies and thus lists of DD variables can be used, so as to
 * allow to store e.g. present and next state versions of variable values.
 * This class is intended to be used to help translate expressions over
 * non-boolean variables, or expressions in which non-boolean variables occur
 * as intermediate states, to decision diagrams.
 * </p>
 * 
 * @author Ernst Moritz Hahn
 */
public interface VariableDD extends Closeable {
    /* methods to be implemented by implementing class */

    /**
     * Obtain the context the DD variable belongs to.
     * 
     * @return context the DD variable belongs to
     */
    ContextDD getContext();    

    /**
     * Get the type of the value represented.
     * 
     * @return type of the value represented
     */
    Type getType();

    /**
     * Get the number of copies of the variable.
     * 
     * @return number of copies
     */
    int getNumCopies();

    /**
     * Obtain the list of DD variables of a given coppy.
     * The copy parameter must be nonnegative and strictly smaller than the
     * value obtained by {@link #getNumCopies()}. The variables returned are not
     * cloned, and thus the caller must not call {@link DD#dispose()} on them.
     * 
     * @param copy copy to obtain DD variables of
     * @return DD variables of given copy
     */
    List<DD> getDDVariables(int copy);

    /**
     * Obtain the possible values of the type as an MTBDD.
     * The result will be an MTBDD mapping each assignment of variables used to
     * encode the type to the resulting value, stored in a leaf node.
     * The copy parameter must be nonnegative and strictly smaller than the
     * value obtained by {@link #getNumCopies()}.
     * 
     * @param copy copy to use for encoding
     * @return possible values of the type as an MTBDD
     */
    DD getValueEncoding(int copy);

    @Override
    void close();

    /**
     * Checks whether the variable is alive.
     * After creation of the variable, the method should return {@code true}.
     * After a call to {@link #close()} the method should return {@code false},
     * indicating that further usage of any of the methods is not allowed
     * (except further calls to {@link #close()}, which have no effect, though).
     * This method is intended to be used in {@code assert} statements to ease
     * debugging.
     * 
     * @return whether the variable is alive
     */
    boolean alive();

    /**
     * Obtain the name of the variable.
     * This function is intended for debugging purposes.
     * 
     * @return name of the variable
     */
    String getName();

    // TODO should be changed to getVariableValue(), should not recompute each time
    DD newIntValue(int copy, int value);

    DD newVariableValue(int copy, Value value);

    /* default methods */

    default DD newVariableValue(int copy, int valueNr) {
        ValueEnumerable value = TypeEnumerable.as(getType()).newValue();
        value.setValueNumber(valueNr);
        return newVariableValue(copy, value);
    }


    default List<DD> getDDVariables() {
        List<DD> result = new ArrayList<>();
        for (int copy = 0; copy < getNumCopies(); copy++) {
            result.addAll(getDDVariables(copy));
        }
        return result;
    }

    default DD newValidValues(int copy) {
        assert copy >= 0;
        assert copy < getNumCopies();
        DD result = getContext().newConstant(false);
        for (int valueNr = 0; valueNr < TypeEnumerable.as(getType()).getNumValues(); valueNr++) {
            result = result.orWith(newVariableValue(copy, valueNr));
        }
        return result;
    }

    // TODO should be changed to getCube(), should not recompute cube each time
    default DD newCube(int copy) {
        assert alive();
        assert copy >= 0;
        assert copy < getNumCopies();
        List<DD> variables = getDDVariables(copy);
        DD result = getContext().newConstant(true);
        for (DD variable : variables) {
            DD oldResult = result;
            result = result.and(variable);
            oldResult.dispose();
        }
        return result;
    }

    /**
     * Checks whether this variable is of integer type.
     * 
     * @return whether this variable is of integer type
     */
    default boolean isInteger() {
        assert alive();
        return TypeInteger.is(getType());
    }

    /**
     * Get the lower bound of an integer variable.
     * The method must only be called if the variable is of integer type.
     * 
     * @return lower bound of an integer variable
     */
    default int getLower() {
        assert alive();
        assert TypeInteger.is(getType());
        return TypeInteger.as(getType()).getLowerInt();
    }

    /**
     * Get the upper bound of an integer variable.
     * The method must only be called if the variable is of integer type.
     * 
     * @return upper bound of an integer variable
     */
    default int getUpper() {
        assert alive();
        assert TypeInteger.is(getType());
        return TypeInteger.as(getType()).getUpperInt();
    } 

    default DD newEqCopies(int copy1, int copy2) {
        DD cube1 = newCube(copy1);
        DD cube2 = newCube(copy2);
        Walker walker1 = cube1.walker();
        Walker walker2 = cube2.walker();
        DD result = getContext().newConstant(true);
        while (!walker1.isTrue()) {
            DD variable1 = getContext().variable(walker1.variable());
            DD variable2 = getContext().variable(walker2.variable());
            result = result.andWith(variable1.eq(variable2));
            walker1.high();
            walker2.high();
        }
        cube1.dispose();
        cube2.dispose();
        return result;
    }
}

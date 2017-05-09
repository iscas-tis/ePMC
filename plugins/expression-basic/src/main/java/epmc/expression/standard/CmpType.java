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

package epmc.expression.standard;

import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorNe;

/**
 * Comparison type, e.g. in quantifiers or reward properties.
 * 
 * @author Ernst Moritz Hahn
 */
public enum CmpType {
    /** Compute and return value without comparing it. */
    IS("=?", null),
    /** Check whether value computed equals given value. */
    EQ("=", OperatorEq.IDENTIFIER),
    /** Check whether value computed does not equal given value. */
    NE("!=", OperatorNe.IDENTIFIER),
    /** Check whether value computed is larger than given value. */
    GT(">", OperatorGt.IDENTIFIER),
    /** Check whether value computed is larger or equal than given value. */
    GE(">=", OperatorGe.IDENTIFIER),
    /** Check whether value computed is smaller than given value. */
    LT("<", OperatorLt.IDENTIFIER),
    /** Check whether value computed is smaller or equal than given value. */
    LE("<=", OperatorLe.IDENTIFIER);
    
    /** User-readable {@link String} representing the comparism. */
    private final String string;
    private final String operator;

    /**
     * Construct new comparison type.
     * The parameter must not be {@code null}.
     * 
     * @param string string representing comparison type.
     */
    private CmpType(String string, String operator) {
        assert string != null;
        this.string = string;
        this.operator = operator;
    }
    
    @Override
    public String toString() {
        return string;
    }
    
    /**
     * Check whether this comparison type requests to compute a value.
     * 
     * @return whether this comparison type requests to compute a value
     */
    public boolean isIs() {
        return this == IS;
    }

    /**
     * Checks whether comparison asks if computed value equals given one.
     * 
     * @return whether comparison asks if computed value equals given one
     */
    public boolean isEq() {
        return this == EQ;
    }

    /**
     * Checks whether comparison asks if computed value smaller than given one.
     * 
     * @return whether comparison asks if computed value smaller than given one
     */
    public boolean isLt() {
        return this == LT;
    }

    /**
     * Checks whether comparison asks if computed value smaller or equal to given one.
     * 
     * @return whether comparison asks if computed value smaller or equal to given one
     */
    public boolean isLe() {
        return this == LE;
    }

    /**
     * Checks whether comparison asks if computed value larger than given one.
     * 
     * @return whether comparison asks if computed value larger than given one
     */
    public boolean isGt() {
        return this == GT;
    }
    
    /**
     * Checks whether comparison asks if computed value larger or equal to given one.
     * 
     * @return whether comparison asks if computed value larger or equal to given one
     */
    public boolean isGe() {
        return this == GE;
    }

    /**
     * Transform comparison type to equivalent operator.
     * {@link CmpType#EQ} will not be translated and the function must not be
     * called on this object. For the other comparison types, the following
     * translation is used:
     * <table>
     * <tr>
     * <td>{@link CmpType#IS}</td>
     * <td>-</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#EQ}</td>
     * <td>{@link OperatorEq#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#NE}</td>
     * <td>{@link OperatorNe#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#GT}</td>
     * <td>{@link OperatorGt#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#GE}</td>
     * <td>{@link OperatorGe#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#LT}</td>
     * <td>{@link OperatorLe#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#LE}</td>
     * <td>{@link OperatorLe#IDENTIFIER}</td>
     * </tr>
     * </table>
     * The parameter of this function must not be {@code null}.
     * 
     * @return equivalent operator
     */
    public String asExOpType() {
        assert this != IS;
        return operator;
    }
}

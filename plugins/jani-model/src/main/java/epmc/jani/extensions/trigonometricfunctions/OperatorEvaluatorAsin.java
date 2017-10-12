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

package epmc.jani.extensions.trigonometricfunctions;

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.ValueNumber;

/**
 * Operator to compute sinus of a value.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OperatorEvaluatorAsin implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorAsin.ASIN;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        if (types.length != 1) {
            return false;
        }
        if (!TypeDouble.is(types[0]) && !TypeInteger.is(types[0])) {
            return false;
        }
        return true;
    }


    @Override
    public Type resultType(Type... types) {
        assert types != null;
        return TypeReal.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        assert operands[0] != null;
        ValueDouble resultDouble = ValueDouble.as(result);
        ValueNumber operandNumber = ValueNumber.as(operands[0]);
        resultDouble.set(Math.asin(operandNumber.getDouble()));
    }
}

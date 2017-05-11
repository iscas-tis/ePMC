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

package epmc.value;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorCeil implements Operator {
    public final static String IDENTIFIER = "ceil"; //"⌈⌉";

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
        ValueAlgebra.asAlgebra(result).set(ValueNumber.asNumber(operands[0]).ceilInt());
    }

    @Override
    public Type resultType(Type... types) {
        Type result;
        if (!TypeUnknown.isUnknown(types[0]) && !(TypeReal.isReal(types[0])
        		|| TypeInteger.isInteger(types[0]))) {
            return null;
        }
        result = TypeInteger.get();
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}

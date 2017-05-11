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
import epmc.value.Value;

public final class OperatorDivideIgnoreZero implements Operator {
    public final static String IDENTIFIER = "divide-ignore-zero";

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	if (ValueAlgebra.asAlgebra(operands[1]).isZero()) {
    		result.set(operands[0]);
    	} else {
    		ValueAlgebra.asAlgebra(result).divide(operands[0], operands[1]);
    	}
    }

    @Override
    public Type resultType(Type... types) {
        return UtilValue.upper(types);
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}

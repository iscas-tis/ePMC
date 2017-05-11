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

public final class OperatorIte implements Operator {
    /** If-then-else, ternary operator. */
    public final static String IDENTIFIER = "ite"; //"?:";

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	if (ValueBoolean.asBoolean(operands[0]).getBoolean()) {
    		result.set(operands[1]);
    	} else {
    		result.set(operands[2]);    		
    	}
    }

    @Override
    public Type resultType(Type... types) {
        assert types != null;
        assert types[0] != null;
        assert types[1] != null;
        assert types[2] != null;
        if (!TypeBoolean.isBoolean(types[0])) {
            return null;
        }
        Type itUpper = UtilValue.upper(types[1], types[2]);
        if (itUpper == null) {
            return null;
        }
        Type result = itUpper;
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}

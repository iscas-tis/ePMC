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

package epmc.prism.value;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

/**
 * Operator "pow" of PRISM.
 * This operator is different from the one of the JANI language:
 * In this version, if both operands are integers, then the result type is an
 * integer too. In the JANI version however, the result is a real number in
 * any case.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorPRISMPow implements Operator {
    public final static String IDENTIFIER = "pow-prism";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	if (ValueInteger.isInteger(result)) {
    		ValueInteger.asInteger(result).pow(ValueInteger.asInteger(operands[0]), ValueInteger.asInteger(operands[1]));
    	} else if (ValueReal.isReal(result)) {
    		ValueReal.asReal(result).pow(operands[0], operands[1]);
    	} else {
    		assert false : result.getType();
    	}
    }

    @Override
    public Type resultType(Type... types) {
        assert types != null;
        assert types.length == 2 : types.length;
        boolean allInteger = true;
        for (Type type : types) {
        	allInteger &= TypeInteger.isInteger(type);
        }
        if (allInteger) {
        	return TypeInteger.get();
        } else {
        	return TypeReal.get();
        }
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}

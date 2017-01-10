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
import epmc.value.Value;

public interface ValueTrigonometric extends ValueAlgebra {
	static boolean isTrigonometric(Value value) {
		return value instanceof ValueTrigonometric;
	}
	
	static ValueTrigonometric asTrigonometric(Value value) {
		if (isTrigonometric(value)) {
			return (ValueTrigonometric) value;
		} else {
			return null;
		}
	}
	
    void cos(Value operand) throws EPMCException;
    
    void sin(Value operand) throws EPMCException;

    void tanh(Value operand) throws EPMCException;

    void cosh(Value operand) throws EPMCException;

    void sinh(Value operand) throws EPMCException;

    void atan(Value operand) throws EPMCException;

    void acos(Value operand) throws EPMCException;

    void asin(Value operand) throws EPMCException;

    void tan(Value operand) throws EPMCException;

    void acosh(Value operand) throws EPMCException;

    void atanh(Value operand) throws EPMCException;
    
    void asinh(Value operand) throws EPMCException;
}

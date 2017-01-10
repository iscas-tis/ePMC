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

import java.nio.ByteBuffer;

import epmc.value.Value;

public interface ValueContentMemory { // extends Value
	static boolean isMemory(Value value) {
		return value instanceof ValueContentMemory;
	}
	
	static ValueContentMemory asMemory(Value value) {
		if (isMemory(value)) {
			return (ValueContentMemory) value;
		} else {
			return null;
		}
	}
	
	static ByteBuffer getMemory(Value value) {
		ValueContentMemory valueMemory = asMemory(value);
		if (valueMemory != null) {
			return valueMemory.getMemory();
		} else {
			return null;
		}
	}
	
    ByteBuffer getMemory();
}

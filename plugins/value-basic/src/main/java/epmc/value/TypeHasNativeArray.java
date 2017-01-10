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

import epmc.value.Type;
import epmc.value.TypeArray;

public interface TypeHasNativeArray extends Type {
	static boolean isHasNativeArray(Type type) {
		return type instanceof TypeHasNativeArray;
	}
	
	static TypeHasNativeArray asHasNativeArray(Type type) {
		if (isHasNativeArray(type)) {
			return (TypeHasNativeArray) type;
		} else {
			return null;
		}
	}
	
	static TypeArray getTypeNativeArray(Type type) {
		TypeHasNativeArray typeHasNativeArray = asHasNativeArray(type);
		if (typeHasNativeArray != null) {
			return typeHasNativeArray.getTypeArrayNative();
		} else {
			return null;
		}
	}
	
    TypeArray getTypeArrayNative();
}

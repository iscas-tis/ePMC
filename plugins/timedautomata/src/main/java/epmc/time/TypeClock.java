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

package epmc.time;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeInteger;

public final class TypeClock implements TypeAlgebra {
	public static boolean isClock(Type type) {
		return type instanceof TypeClock;
	}
	
	public static TypeClock asClock(Type type) {
		if (isClock(type)) {
			return (TypeClock) type;
		} else {
			return null;
		}
	}
	
	private final static String CLOCK = "clock";
	/** Value context to which this type belongs. */
	private ContextValue contextValue;

	public TypeClock(ContextValue contextValue) {
		assert contextValue != null;
		this.contextValue = contextValue;
	}
	
	@Override
	public ValueClock newValue() {
		return new ValueClock(this);
	}
	
	@Override
	public ContextValue getContext() {
		return contextValue;
	}

	@Override
	public boolean canImport(Type type) {
		assert type != null;
		if (type instanceof TypeClock) {
			return true;
		}
		if (TypeInteger.isInteger(type)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return CLOCK;
	}

	@Override
	public ValueClock getZero() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueClock getOne() {
		// TODO Auto-generated method stub
		return null;
	}
		
	@Override
	public TypeArrayAlgebra getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}
}

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

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

public final class ValueClock implements ValueAlgebra {
	/** 1L, as I don't know any better. */
	private static final long serialVersionUID = 1L;
	private boolean immutable;
	private TypeClock type;
	private int value;

	ValueClock(TypeClock type) {
		assert false;
		this.type = type;
	}
	
	@Override
	public ValueClock clone() {
		ValueClock clone = type.newValue();
		clone.set(value);
		return clone;
	}

	@Override
	public TypeClock getType() {
		return type;
	}

	@Override
	public void setImmutable() {
		this.immutable = true;
	}

	@Override
	public boolean isImmutable() {
		return immutable;
	}

	@Override
	public void set(int value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "valueClock(" + value + ")";
	}

	@Override
	public void add(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void divide(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subtract(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void multiply(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addInverse(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void multInverse(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOne() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPosInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void set(Value value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double norm() throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double distance(Value other) throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}

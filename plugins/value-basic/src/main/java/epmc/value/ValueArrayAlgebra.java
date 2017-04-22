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

import java.util.Arrays;

import epmc.value.Value;
import epmc.value.ValueArray;

public abstract class ValueArrayAlgebra extends ValueArray {
	public static boolean isArrayAlgebra(Value value) {
		return value instanceof ValueArrayAlgebra;
	}
	
	public static ValueArrayAlgebra asArrayAlgebra(Value value) {
		if (isArrayAlgebra(value)) {
			return (ValueArrayAlgebra) value;
		} else {
			return null;
		}
	}
	
    private ValueAlgebra[] entryAccs = new ValueAlgebra[1];

    protected ValueAlgebra getEntryAcc(int number) {
        assert number >= 0;
        int numEntryAccs = this.entryAccs.length;
        while (number >= numEntryAccs) {
            numEntryAccs *= 2;
        }
        if (numEntryAccs != this.entryAccs.length) {
            this.entryAccs = Arrays.copyOf(this.entryAccs, numEntryAccs);
        }
        if (this.entryAccs[number] == null) {
            this.entryAccs[number] = getType().getEntryType().newValue();
        }
        return this.entryAccs[number];
    }
    
    public final void set(int entry, int index) {
        assert !isImmutable();
        getEntryAcc(0).set(entry);
        set(getEntryAcc(0), index);
    }
    
    @Override
    public abstract TypeArrayAlgebra getType();
    
    @Override
    public abstract ValueArrayAlgebra clone();
}

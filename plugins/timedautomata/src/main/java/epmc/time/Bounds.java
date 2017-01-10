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

import epmc.value.UtilValue;
import epmc.value.Value;

final class Bounds {
	private Value lower;
	Value upper;

	private Bounds() {
	}
	
	Bounds(Value value) {
		assert value != null;
		lower = UtilValue.clone(value);
		upper = UtilValue.clone(value);
	}

	public Bounds(Value lower, Value upper) {
		assert lower != null;
		assert upper != null;
		this.lower = UtilValue.clone(lower);
		this.upper = UtilValue.clone(upper);
	}
}

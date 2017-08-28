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

import java.util.Comparator;

public final class ValueAlgebraComparator implements Comparator<ValueAlgebra> {

    @Override
    public int compare(ValueAlgebra o1, ValueAlgebra o2) {
        if (o1.isEq(o2)) {
            return 0;
        } else if (o1.isLt(o2)) {
            return -1;
        } else {
            assert o1.isGt(o2);
            return 1;
        }
    }
}

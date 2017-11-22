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

package epmc.jani.measure;

import epmc.value.Type;
import epmc.value.Value;

public interface DiscreteMeasure {
    static int NEG_INF = Integer.MIN_VALUE;
    static int POS_INF = Integer.MAX_VALUE;	

    Type getEntryType();

    void getTotal(Value total);

    int getFrom();

    int getTo();

    void getValue(Value value, int of);
}

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

package epmc.lumping.lumpingexplicitsignature;

import java.util.Comparator;

final class ComparatorSmallFirst implements Comparator<TodoElem> {

    @Override
    public int compare(TodoElem o1, TodoElem o2) {
        int[] block1 = o1.getBlock();
        int [] block2 = o2.getBlock();
        int o1Size = block1.length;
        int o2Size = block2.length;
        if (o1Size < o2Size) {
            return -1;
        } else if (o2Size > o2Size) {
            return 1;
        }
        return o1.compareTo(o2);
    }

}

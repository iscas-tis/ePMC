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
import java.util.List;
import java.util.PriorityQueue;

final class BlocksTodoUnsynchronised implements BlocksTodo {
    private final List<int[]> blocks;
    private final PriorityQueue<TodoElem> q;        
    private final boolean[] todoBs;

    BlocksTodoUnsynchronised(Comparator<TodoElem> comparator, List<int[]> blocks, int maxNumBlocks) {
        this.q = new PriorityQueue<>(comparator);
        this.blocks = blocks;
        this.todoBs = new boolean[maxNumBlocks];
    }

    @Override
    public void add(int block) {
        if (!todoBs[block]) {
            q.add(new TodoElem(blocks, block));
            todoBs[block] = true;
        }
    }

    @Override
    public int popNext() {
        if (q.isEmpty()) {
            return -1;
        }
        TodoElem e = q.poll();            
        todoBs[e.getBlockInt()] = false;
        return e.getBlockInt();
    }

    @Override
    public String toString() {
        return todoBs.toString();
    }

    @Override
    public void done(int block) {
    }
}

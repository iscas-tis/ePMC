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

import java.util.List;
import java.util.Random;

final class TodoElem implements Comparable<TodoElem> {
    private final static Random RANDOM = new Random();
    private final List<int[]> blocks;
    private final int block;
    private final int randomNumber;

    TodoElem(List<int[]> blocks, int block) {
        this.blocks = blocks;
        this.block = block;
        this.randomNumber = RANDOM.nextInt();
    }

    @Override
    public int compareTo(TodoElem o) {
        int[] thisBlock = blocks.get(this.block);
        int[] oBlock = blocks.get(o.block);
        int thisSize = thisBlock.length;
        int otherSize = oBlock.length;

        if (thisSize < otherSize) {
            return 1;
        } else if (thisSize > otherSize) {
            return -1;
        }
        for (int i = 0; i < thisSize; i++) {
            if (thisBlock[i] < oBlock[i]) {
                return 1;
            } else if (thisBlock[i] < oBlock[i]) {
                return -1;
            }
        }
        return 0;
    }

    int[] getBlock() {
        return blocks.get(this.block);
    }

    int getBlockInt() {
        return block;
    }

    int getRandom() {
        return randomNumber;
    }
}

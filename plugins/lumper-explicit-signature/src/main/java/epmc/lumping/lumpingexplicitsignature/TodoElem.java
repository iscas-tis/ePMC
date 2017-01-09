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

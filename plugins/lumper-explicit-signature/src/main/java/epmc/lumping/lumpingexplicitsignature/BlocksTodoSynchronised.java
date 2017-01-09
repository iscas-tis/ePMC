package epmc.lumping.lumpingexplicitsignature;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

final class BlocksTodoSynchronised implements BlocksTodo {
    private final static int NOT_PRESENT = 0;
    private final static int PRESENT = 1;
    private final static int CHECKED_OUT = 2;
    private final static int PENDING = 3;
    
    private final List<int[]> blocks;
    private final PriorityQueue<TodoElem> q;
    private final byte[] todoBs;
    private int numPresent;

    BlocksTodoSynchronised(Comparator<TodoElem> comparator, List<int[]> blocks, int maxNumBlocks) {
        this.q = new PriorityQueue<>(comparator);
        this.blocks = blocks;
        this.todoBs = new byte[maxNumBlocks];
    }
    
    @Override
    public void add(int block) {
        synchronized (this) {
            if (todoBs[block] == NOT_PRESENT) {
                q.add(new TodoElem(blocks, block));
                todoBs[block] = PRESENT;
                numPresent++;
            } else if (todoBs[block] == CHECKED_OUT) {
                todoBs[block] = PENDING;
            }
        }
    }
    
    @Override
    public int popNext() {
        synchronized (this) {
            if (numPresent == 0) {
                return -1;
            }
            while (numPresent > 0 && q.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e1) {
                }
            }
            if (numPresent == 0) {
                return -1;
            }
            TodoElem e = q.poll();
            todoBs[e.getBlockInt()] = CHECKED_OUT;
            return e.getBlockInt();
        }
    }
    
    @Override
    public String toString() {
        return todoBs.toString();
    }

    @Override
    public void done(int block) {
        synchronized (this) {
            assert todoBs[block] != NOT_PRESENT;
            assert todoBs[block] != PRESENT;
            if (todoBs[block] == PENDING) {
                q.add(new TodoElem(blocks, block));
                todoBs[block] = PRESENT;
                notifyAll();
            } else { // todoBs[block] == CHECKED_OUT
                assert todoBs[block] == CHECKED_OUT;
                todoBs[block] = NOT_PRESENT;
                numPresent--;
                notifyAll();
            }
        }
    }
}

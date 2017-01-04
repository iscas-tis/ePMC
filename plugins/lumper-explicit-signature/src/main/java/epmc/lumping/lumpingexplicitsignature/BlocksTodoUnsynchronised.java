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

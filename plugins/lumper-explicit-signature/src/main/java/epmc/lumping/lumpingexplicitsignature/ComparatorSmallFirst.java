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

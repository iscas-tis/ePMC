package epmc.lumping.lumpingexplicitsignature;

import java.util.Comparator;

final class ComparatorRandom implements Comparator<TodoElem> {

    @Override
    public int compare(TodoElem o1, TodoElem o2) {
        if (o1.getRandom() < o2.getRandom()) {
            return -1;
        } else if (o1.getRandom() < o2.getRandom()) {
            return 1;
        }
        return o1.compareTo(o2);
    }
}

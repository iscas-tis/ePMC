package epmc.imdp.graphsolver;

import com.google.common.base.MoreObjects;

final class SortEntryJavaDouble implements Comparable<SortEntryJavaDouble> {
    private final static String VALUE = "value";
    private final static String LOWER = "lower";
    private final static String UPPER = "upper";
    private final static String SUCCESSOR = "successor";

    private double value;
    private double lower;
    private double upper;
    private int successor;

    SortEntryJavaDouble() {
    }

    void setValue(double value) {
        this.value = value;
    }

    double getValue() {
        return value;
    }

    void setLower(double lower) {
        this.lower = lower;
    }

    double getLower() {
        return lower;
    }

    void setUpper(double upper) {
        this.upper = upper;
    }

    double getUpper() {
        return upper;
    }

    void setSuccessor(int successor) {
        this.successor = successor;
    }

    int getSuccessor() {
        return successor;
    }

    @Override
    public int compareTo(SortEntryJavaDouble o) {
        assert o != null;
        if (this.value < o.value) {
            return -1;
        } else if (this.value > o.value) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(VALUE, value)
                .add(LOWER, lower)
                .add(UPPER, upper)
                .add(SUCCESSOR, successor)
                .toString();
    }
}

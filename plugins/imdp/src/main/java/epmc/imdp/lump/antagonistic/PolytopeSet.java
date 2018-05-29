package epmc.imdp.lump.antagonistic;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;

final class PolytopeSet implements Comparable<PolytopeSet>, Cloneable {
    /** String &quot;numClasses&quot; */
    private final static String NUM_CLASSES = "numClasses";
    /** String &quot;numActions&quot; */
    private final static String NUM_ACTIONS = "numActions";
    /** String containing a single space. */
    private final static String SPACE = " ";
    /** String containing a single newline. */
    private final static String NEWLINE = "\n";

    /** Number of classes in the problem set. */
    private int numClasses;
    /** Number of actions in the problem set. */
    private int numActions;
    /** Array storing the intervals of both challenger and defender. */
    private ValueArrayAlgebra intervals;
    /** Variable of interval type. */
    private final ValueInterval entry;
    private final OperatorEvaluator divideInterval;
    /** Precision used. */
    private final Value precision;
    private final OperatorEvaluator eq;
    private final OperatorEvaluator lt;
    private final OperatorEvaluator gt;
    private final OperatorEvaluator add;
    private final ValueBoolean cmp;
    private final OperatorEvaluator setArray;

    PolytopeSet() {
        this.entry = TypeInterval.get().newValue();
        this.intervals = UtilValue.newArray(TypeInterval.get().getTypeArray(), 1);
        // TODO avoid hardcoding precision
        this.precision = UtilValue.newValue(TypeReal.get(), "1E-6");
        divideInterval = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeInterval.get(), TypeInterval.get());
        eq = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeInterval.get(), TypeInterval.get());
        lt = ContextValue.get().getEvaluator(OperatorLt.LT, TypeReal.get(), TypeReal.get());
        gt = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeInterval.get(), TypeInterval.get());
        setArray = ContextValue.get().getEvaluator(OperatorSet.SET, TypeInterval.get().getTypeArray(), TypeInterval.get().getTypeArray());
        cmp = TypeBoolean.get().newValue();
    }

    /**
     * Reset the polytope set.
     * Afterwards, it will have 0 classes and 0 actions.
     * This method may be used to avoid frequent object creation.
     */
    void reset() {
        Value zero = UtilValue.newValue(getTypeInterval(), 0);
        int totalSize = intervals.size();
        for (int entryNr = 0; entryNr < totalSize; entryNr++) {
            intervals.set(zero, entryNr);
        }
        numClasses = 0;
        numActions = 0;
    }

    /**
     * Set the dimensions of the polytope set in terms of classes and actions.
     * The numbers of classes and actions must be nonnegative.
     * 
     * @param numClasses number of classes to set
     * @param numActions number of actions to set
     */
    void setDimensions(int numClasses, int numActions) {
        assert numClasses > 0 : numClasses;
        assert numActions >= 0 : numActions;

        this.numClasses = numClasses;
        this.numActions = numActions;
        int totalSize = numClasses * numActions;
        if (totalSize > intervals.size()) {
            intervals = UtilValue.newArray(getTypeIntervalArray(), totalSize);
        }
    }

    /**
     * Get the interval type used.
     * 
     * @return interval type used
     */
    private TypeInterval getTypeInterval() {
        return TypeInterval.get();
    }

    /**
     * Get the type of arrays of intervals used.
     * 
     * @return
     */
    private TypeArray getTypeIntervalArray() {
        return getTypeInterval().getTypeArray();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = numClasses + (hash << 6) + (hash << 16) - hash;
        hash = numActions + (hash << 6) + (hash << 16) - hash;
        int totalSize = numClasses * numActions;
        for (int entryNr = 0; entryNr < totalSize; entryNr++) {
            intervals.get(entry, entryNr);
            divideInterval.apply(entry, entry, precision);
            double valueLower = entry.getIntervalLower().getDouble();
            long roundedLower = Math.round(valueLower);
            hash = Long.hashCode(roundedLower) + (hash << 6) + (hash << 16) - hash;
            double valueUpper = entry.getIntervalLower().getDouble();
            long roundedUpper = Math.round(valueUpper);
            hash = Long.hashCode(roundedUpper) + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PolytopeSet)) {
            return false;
        }
        PolytopeSet other = (PolytopeSet) obj;
        return equals(other);
    }

    boolean equals(PolytopeSet other) {
        assert other != null;
        if (this.numClasses != other.numClasses) {
            return false;
        }
        if (this.numActions != other.numActions) {
            return false;
        }
        int totalSize = numClasses * numActions;
        for (int entryNr = 0; entryNr < totalSize; entryNr++) {
            this.intervals.get(this.entry, entryNr);
            other.intervals.get(other.entry, entryNr);
            eq.apply(cmp, this.entry, other.entry);
            if (!cmp.getBoolean()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(NUM_CLASSES + SPACE + numClasses + NEWLINE);
        builder.append(NUM_ACTIONS + SPACE + numActions + NEWLINE);
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            for (int classNr = 0; classNr < numClasses; classNr++) {
                intervals.get(entry, numClasses * actionNr + classNr);
                builder.append(entry);
                if (classNr < numClasses - 1) {
                    builder.append(SPACE);
                }
            }
            builder.append(NEWLINE);
        }

        return builder.toString();
    }

    @Override
    public int compareTo(PolytopeSet o) {
        assert o != null;

        if (this.numActions < o.numActions) {
            return -1;
        } else if (this.numActions > o.numActions) {
            return 1;
        } else if (this.numClasses < o.numClasses) {
            return -1;
        } else if (this.numClasses > o.numClasses) {
            return 1;
        }
        int totalSize = numClasses * numActions;
        for (int entry = 0; entry < totalSize; entry++) {
            this.intervals.get(this.entry, entry);
            o.intervals.get(o.entry, entry);
            int compare = compare(this.entry, o.entry);
            if (compare < 0) {
                return -1;
            } else if (compare > 0) {
                return 1;
            }
        }
        return 0;
    }

    int getNumClasses() {
        return numClasses;
    }

    int getNumActions() {
        return numActions;
    }

    ValueArrayAlgebra getIntervals() {
        return intervals;
    }

    void set(int action, int classNr, ValueInterval weight) {
        assert action >= 0;
        assert action < numActions;
        assert classNr >= 0;
        assert classNr < numClasses;
        assert weight != null;

        int position = numClasses * action + classNr;
        intervals.set(weight, position);
    }

    void add(int action, int classNr, ValueInterval weight) {
        assert action >= 0;
        assert action < numActions;
        assert classNr >= 0;
        assert classNr < numClasses;
        assert weight != null;

        int position = numClasses * action + classNr;
        intervals.get(entry, position);
        add.apply(entry, entry, weight);
        intervals.set(entry, position);
    }

    void get(int action, int classNr, ValueInterval weight) {
        assert action >= 0;
        assert action < numActions;
        assert classNr >= 0;
        assert classNr < numClasses;
        assert weight != null;

        int position = numClasses * action + classNr;
        intervals.get(weight, position);
    }

    void set(PolytopeSet other) {
        assert other != null;
        setDimensions(other.numClasses, other.numActions);
        setArray.apply(intervals, other.intervals);
    }

    @Override
    protected PolytopeSet clone() {
        PolytopeSet clone;
        clone = new PolytopeSet();
        clone.set(this);
        return clone;
    }
    
    private int compare(ValueInterval op1, ValueInterval op2) {
        assert op1 != null;
        assert op2 != null;
        eq.apply(cmp, op1, op2);
        if (cmp.getBoolean()) {
            return 0;
        }
        lt.apply(cmp, op1.getIntervalLower(), op2.getIntervalLower());
        if (cmp.getBoolean()) {
            return -1;
        }
        lt.apply(cmp, op1.getIntervalUpper(), op2.getIntervalUpper());
        if (cmp.getBoolean()) {
            return -1;
        }
        gt.apply(cmp, op1.getIntervalLower(), op2.getIntervalLower());        
        if (cmp.getBoolean()) {
            return 1;
        }
        gt.apply(cmp, op1.getIntervalUpper(), op2.getIntervalUpper());        
        if (cmp.getBoolean()) {
            return 1;
        }
        assert false;
        return Integer.MAX_VALUE;
    }
}

package epmc.imdp.lump.cooperative;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLt;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayInterval;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;

final class ProblemSet implements Comparable<ProblemSet> {
    /** String &quot;numClasses&quot; */
    private final static String NUM_CLASSES = "numClasses";
    /** String &quot;numActions&quot; */
    private final static String NUM_ACTIONS = "numActions";
    /** String &quot;challengerWeights&quot; */
    private final static String CHALLENGER_WEIGHTS = "challengerWeights";
    /** String &quot;defenderWeights&quot; */
    private final static String DEFENDER_WEIGHTS = "defenderWeights";
    /** String containing a single space. */
    private final static String SPACE = " ";
    /** String containing a single newline. */
    private final static String NEWLINE = "\n";

    /** Number of classes in the problem set. */
    private int numClasses;
    /** Number of actions in the problem set. */
    private int numActions;
    /** Array storing the intervals of both challenger and defender. */
    private ValueArrayInterval intervals;
    /** Variable of interval type. */
    private final ValueInterval entry;
    /** Precision used. */
    private final Value precision;

    private final OperatorEvaluator divideInterval;
    private final OperatorEvaluator eqInterval;
    private final OperatorEvaluator ltReal;
    private final OperatorEvaluator gtReal;
    private final OperatorEvaluator addInterval;
    private final ValueBoolean cmp;
    
    /**
     * Generate a new problem set.
     * The problem set will initially have 0 classes and 0 actions.
     * The value context parameter must not be {@code null}.
     * 
     */
    ProblemSet() {
        this.entry = TypeInterval.get().newValue();
        this.intervals = UtilValue.newArray(TypeInterval.get().getTypeArray(), 1);
        // TODO avoid hardcoding precision
        this.precision = UtilValue.newValue(TypeReal.get(), "1E-6");
        divideInterval = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeInterval.get(), TypeInterval.get());
        eqInterval = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeInterval.get(), TypeInterval.get());
        ltReal = ContextValue.get().getEvaluator(OperatorLt.LT, TypeReal.get(), TypeReal.get());
        gtReal = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
        addInterval = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeInterval.get(), TypeInterval.get());
        cmp = TypeBoolean.get().newValue();
    }

    /**
     * Reset the problem set.
     * Afterwards, it will have 0 classes and 0 actions.
     * This method may be used to avoid frequent object creation.
     */
    void reset() {
        Value zero = UtilValue.newValue(getTypeInterval(), 0);
        int totalSize = numClasses + (numClasses * numActions);
        for (int entryNr = 0; entryNr < totalSize; entryNr++) {
            intervals.set(zero, entryNr);
        }
        numClasses = 0;
        numActions = 0;
    }

    /**
     * Set the dimensions of the problem set in terms of classes and actions.
     * The numbers of classes and actions must be nonnegative.
     * 
     * @param numClasses number of classes to set
     * @param numActions number of actions to set
     */
    void setDimensions(int numClasses, int numActions) {
        assert numClasses > 0 : numClasses;
        assert numActions > 0 : numActions;

        this.numClasses = numClasses;
        this.numActions = numActions;
        int totalSize = numClasses + (numClasses * numActions);
        if (totalSize > intervals.size()) {
            intervals = UtilValue.newArray(getTypeIntervalArray(), totalSize);
        }
    }

    /**
     * Get the number of classes of the problem set.
     * 
     * @return number of classes of the problem set
     */
    int getNumClasses() {
        return numClasses;
    }

    /**
     * Get the number of actions of the problem set.
     * 
     * @return number of actions of the problem set
     */
    int getNumActions() {
        return numActions;
    }

    /**
     * Sets an entry of the challenger.
     * Thus, this method sets the upper and lower bounds of the probability
     * the challenger may assign to a given class number.
     * The class number must be nonnegative and smaller than the number of
     * classes of the problem set.
     * The weight value must not be {@code null}, and must be of a type which
     * can be imported by the interval type of the current value context.
     * 
     * @param classNr class to set restriction for
     * @param bounds bounds to set for the class
     */
    void setChallenger(int classNr, Value bounds) {
        assert classNr >= 0;
        assert classNr < numClasses;
        assert bounds != null;

        intervals.set(bounds, classNr);
    }

    /**
     * Add interval an entry of the challenger.
     * Thus, this method increases the upper and lower bounds of the probability
     * the challenger may assign to a given class number by adding the according
     * interval.
     * The class number must be nonnegative and smaller than the number of
     * classes of the problem set.
     * The weight value must not be {@code null}, and must be of a type which
     * can be imported by the interval type of the current value context.
     * 
     * @param classNr class to set restriction for
     * @param interval interval to add for the class
     */
    void addChallenger(int classNr, Value interval) {
        assert classNr >= 0;
        assert classNr < numClasses;
        assert interval != null;

        intervals.get(entry, classNr);
        addInterval.apply(entry, entry, interval);
        intervals.set(entry, classNr);
    }

    void getChallenger(Value to, int classNr) {
        assert to != null;
        assert classNr >= 0;
        assert classNr < numClasses;
        intervals.get(to, classNr);
    }

    void setDefender(int action, int classNr, Value weight) {
        assert action >= 0;
        assert action < numActions;
        assert classNr >= 0;
        assert classNr < numClasses;
        assert weight != null;

        int position = numClasses + numClasses * action + classNr;
        intervals.set(weight, position);
    }

    void addDefender(int action, int classNr, Value weight) {
        assert action >= 0;
        assert action < numActions;
        assert classNr >= 0;
        assert classNr < numClasses;
        assert weight != null;

        int position = numClasses + numClasses * action + classNr;
        intervals.get(entry, position);
        addInterval.apply(entry, entry, weight);
        intervals.set(entry, position);
    }

    void getDefender(Value to, int action, int classNr) {
        assert to != null;
        assert action >= 0;
        assert action < numActions;
        assert classNr >= 0;
        assert classNr < numClasses;
        int position = numClasses + numClasses * action + classNr;
        intervals.get(to, position);
    }

    @Override
    protected ProblemSet clone() {		
        ProblemSet result;
        result = new ProblemSet();
        result.numClasses = this.numClasses;
        result.numActions = this.numActions;
        int totalSize = numClasses + (numClasses * numActions);
        result.intervals = UtilValue.newArray(getTypeIntervalArray(), totalSize);
        for (int entryNr = 0; entryNr < totalSize; entryNr++) {
            this.intervals.get(entry, entryNr);
            result.intervals.set(entry, entryNr);
        }

        return result;
    }

    @Override
    public int compareTo(ProblemSet o) {
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
        int totalSize = numClasses + (numClasses * numActions);
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(NUM_CLASSES + SPACE + numClasses + NEWLINE);
        builder.append(NUM_ACTIONS + SPACE + numActions + NEWLINE);
        builder.append(CHALLENGER_WEIGHTS + SPACE);
        for (int classNr = 0; classNr < numClasses; classNr++) {
            intervals.get(entry, classNr);
            builder.append(entry);
            if (classNr < numClasses - 1) {
                builder.append(SPACE);
            }
        }
        builder.append(NEWLINE);
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            builder.append(DEFENDER_WEIGHTS + SPACE + actionNr + SPACE);
            for (int classNr = 0; classNr < numClasses; classNr++) {
                intervals.get(entry, numClasses + numClasses * actionNr + classNr);
                builder.append(entry);
                if (classNr < numClasses - 1) {
                    builder.append(SPACE);
                }
            }
            builder.append(NEWLINE);
        }

        return builder.toString();
    }

    ValueArrayInterval getIntervals() {
        return intervals;
    }

    /**
     * Get the type of arrays of intervals used.
     * 
     * @return
     */
    private TypeArray getTypeIntervalArray() {
        return getTypeInterval().getTypeArray();
    }

    /**
     * Get the interval type used.
     * 
     * @return interval type used
     */
    private TypeInterval getTypeInterval() {
        return TypeInterval.get();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = numClasses + (hash << 6) + (hash << 16) - hash;
        hash = numActions + (hash << 6) + (hash << 16) - hash;
        int totalSize = numClasses + (numClasses * numActions);
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
        if (!(obj instanceof ProblemSet)) {
            return false;
        }
        ProblemSet other = (ProblemSet) obj;
        if (this.numClasses != other.numClasses) {
            return false;
        }
        if (this.numActions != other.numActions) {
            return false;
        }
        int totalSize = numClasses + (numClasses * numActions);
        for (int entryNr = 0; entryNr < totalSize; entryNr++) {
            this.intervals.get(this.entry, entryNr);
            other.intervals.get(other.entry, entryNr);
            eqInterval.apply(cmp, this.entry, other.entry);
            if (!cmp.getBoolean()) {
                return false;
            }
        }

        return true;
    }
    
    private int compare(ValueInterval op1, ValueInterval op2) {
        assert op1 != null;
        assert op2 != null;
        eqInterval.apply(cmp, op1, op2);
        if (cmp.getBoolean()) {
            return 0;
        }
        ltReal.apply(cmp, op1.getIntervalLower(), op2.getIntervalLower());
        if (cmp.getBoolean()) {
            return -1;
        }
        ltReal.apply(cmp, op1.getIntervalUpper(), op2.getIntervalUpper());
        if (cmp.getBoolean()) {
            return -1;
        }
        gtReal.apply(cmp, op1.getIntervalLower(), op2.getIntervalLower());
        if (cmp.getBoolean()) {
            return 1;
        }
        gtReal.apply(cmp, op1.getIntervalUpper(), op2.getIntervalUpper());
        if (cmp.getBoolean()) {
            return 1;
        }
        assert false;
        return Integer.MAX_VALUE;
    }
}

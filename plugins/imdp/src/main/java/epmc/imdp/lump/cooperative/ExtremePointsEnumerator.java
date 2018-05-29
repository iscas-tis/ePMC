package epmc.imdp.lump.cooperative;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorSubtract;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;

/**
 * Class to enumerate extreme distributions of an interval array.
 * The class operates on arrays of intervals. These intervals represent lower
 * and upper bounds of probabilities which can be assigned to the indices of
 * the array. The array of intervals operated on is assumed to be normalised
 * as in
 * Daniel Klink:
 * &quot;Three-Valued Abstraction for Stochastic Systems&quot;,
 * &quot;3.3 Normalization&quot;.
 * The distributions enumerated then are the ones of 
 * &quot;4.1 Extreme probability measures&quot;
 * of this thesis.
 * The algorithm used is not explicitly described in the thesis, but can be
 * derived.
 * 
 * @author Ernst Moritz Hahn
 */
final class ExtremePointsEnumerator {
    /** Integer value to mark an unset interval entry. */
    private final static int UNSET = -1;

    /**
     * Class defining the functional interface called for extremal points.
     * 
     * @author Ernst Moritz Hahn
     */
    @FunctionalInterface
    static interface ExtremePointHandler {
        /**
         * Function handling an extreme point.
         * The function will be called with an array of the real type of the
         * value context used. After having performed its task, the function
         * shall return {@code false} if the enumeration of further points is
         * required, and {@code true} if the enumeration is to be stopped.
         * 
         * @param value extreme point to be handled
         * @return {@code true} to stop extreme point enumeration
         */
        boolean call(ValueArrayAlgebra value);
    }
    /** Value "1" in the real domain type used. */
    private final Value oneReal;
    /** Sum of values already assigned in distribution. */
    private final ValueAlgebra sum;
    /** Value of interval type. */
    private final ValueInterval entryInterval;
    /** One minus {@link #sum}. */
    private final ValueReal oneMinusSum;
    /** Current distribution over array indices. */
    private ValueArrayAlgebra distribution;
    /** Current intervals being enumerated. */
    private ValueArrayAlgebra intervals;
    /** Size of current intervals being enumerated. */
    private int size;
    /** Used to indicate that recursive enumeration should stop. */
    private boolean stop;
    /** Method to call for enumerated points. */
    private ExtremePointHandler method;
    private OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeReal.get(), TypeReal.get());
    private OperatorEvaluator lt = ContextValue.get().getEvaluator(OperatorLt.LT, TypeReal.get(), TypeReal.get());
    private OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
    private OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeReal.get(), TypeReal.get());
    private ValueBoolean cmp = TypeBoolean.get().newValue();
    
    /**
     * Construct a new extreme points enumerator.
     * The value context must not be {@code null}.
     */
    ExtremePointsEnumerator() {
        oneReal = UtilValue.newValue(TypeReal.get(), 1);
        sum = UtilValue.newValue(TypeReal.get(), 0);
        distribution = UtilValue.newArray(TypeReal.get().getTypeArray(), 0);
        entryInterval = TypeInterval.get().newValue();
        oneMinusSum = TypeReal.get().newValue();
    }

    /**
     * Enumerate extreme points of given interval array.
     * For each of them, the method {@code method} will be called, until one
     * of these calls returns {@true}. The value returned by this function
     * is either {@code false} if none of the method calls for the extreme
     * points returned {@code true}, and {@code true} else.
     * 
     * None of the parameter may be {@code null}.
     * The intervals array must be one-dimensional an array of intervals.
     * The size parameter must be nonnegative and must not be larger than the
     * size of the interval array.
     * 
     * @param method method to call for each extreme point
     * @param intervals array of intervals
     * @param size size of array to be used
     * @return {@code true} if one of the method call returned {@code true}
     */
    boolean enumerate(ExtremePointHandler method, ValueArrayAlgebra intervals, int size) {
        assert method != null;
        assert intervals != null;
        assert ValueArray.is(intervals);
        assert TypeInterval.is(intervals.getType().getEntryType());
        assert size >= 0;
        assert size <= intervals.size();

        sum.set(0); // set again to avoid numerical imprecisions
        stop = false;
        if (distribution.size() < size) {
            distribution = UtilValue.newArray(TypeReal.get().getTypeArray(), size);
        }
        this.intervals = intervals;
        this.size = size;
        this.method = method;
        enumerateRec(UNSET, 0);
        return stop;
    }

    /**
     * Recursively enumerate the extremal points.
     * This method will be called by
     * {@link #enumerate(ExtremePointHandler, Value, int)}
     * which will set some fields in preparation of this method call.
     * 
     * @param unassigned array index to leave unassigned, or {@link #UNSET}
     * @param level recursion level
     */
    private void enumerateRec(int unassigned, int level) {
        gt.apply(cmp, sum, oneReal);
        if (stop || cmp.getBoolean()) {
            return;
        }

        if (level == size) {
            enumerateRecTerminal(unassigned);
        } else {
            enumerateRecNonterminal(unassigned, level);
        }
    }	

    /**
     * Terminal case of extremal point enumeration.
     * 
     * @param unassigned interval index left unassigned, or {@link #UNSET}
     */
    private void enumerateRecTerminal(int unassigned) {
        lt.apply(cmp, sum, oneReal);
        if (cmp.getBoolean()) {
            eq.apply(cmp, sum, oneReal);
            if (!cmp.getBoolean() && unassigned == UNSET) {
                return;
            }
        }
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeReal.get(), TypeReal.get());
        OperatorEvaluator leReal = ContextValue.get().getEvaluator(OperatorLe.LE, TypeReal.get(), TypeReal.get());
        OperatorEvaluator geReal = ContextValue.get().getEvaluator(OperatorGe.GE, TypeReal.get(), TypeReal.get());
        ValueBoolean cmpLe = TypeBoolean.get().newValue();
        ValueBoolean cmpGe = TypeBoolean.get().newValue();
        if (unassigned != UNSET) {
            subtract.apply(oneMinusSum, oneReal, sum);
            intervals.get(entryInterval, unassigned);
            leReal.apply(cmpLe, oneMinusSum, entryInterval.getIntervalLower());
            geReal.apply(cmpGe, oneMinusSum, entryInterval.getIntervalUpper());
            if (cmpLe.getBoolean() || cmpGe.getBoolean()) {
                return;
            }
            distribution.set(oneMinusSum, unassigned);
        }
        stop = method.call(distribution);
    }

    /**
     * Nonterminal case of recursive point enumeration.
     * 
     * @param unassigned  interval index left unassigned, or {@link #UNSET}
     * @param level recursion level
     */
    private void enumerateRecNonterminal(int unassigned, int level) {
        intervals.get(entryInterval, level);
        eq.apply(cmp, entryInterval.getIntervalLower(), entryInterval.getIntervalUpper());
        boolean pointInterval = cmp.getBoolean();
        if (unassigned == UNSET && !pointInterval) {
            enumerateRec(level, level + 1);
            intervals.get(entryInterval, level);
        }
        add.apply(sum, sum, entryInterval.getIntervalLower());
        distribution.set(entryInterval.getIntervalLower(), level);
        enumerateRec(unassigned, level + 1);
        intervals.get(entryInterval, level);
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeReal.get(), TypeReal.get());
        subtract.apply(sum, sum, entryInterval.getIntervalLower());
        if (!pointInterval) {
            add.apply(sum, sum, entryInterval.getIntervalUpper());
            distribution.set(entryInterval.getIntervalUpper(), level);
            enumerateRec(unassigned, level + 1);
            intervals.get(entryInterval, level);
            subtract.apply(sum, sum, entryInterval.getIntervalUpper());
        }
    }
}

package epmc.imdp.lump;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
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
		 * @throws EPMCException thrown in case of problems
		 */
		boolean call(ValueArrayAlgebra value) throws EPMCException;
	}

	private final ContextValue contextValue;
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
	
	/**
	 * Construct a new extreme points enumerator.
	 * The value context must not be {@code null}.
	 * 
	 * @param contextValue value context used
	 */
	ExtremePointsEnumerator(ContextValue contextValue) {
		assert contextValue != null;
		this.contextValue = contextValue;
		oneReal = TypeReal.get(contextValue).getOne();
		sum = UtilValue.newValue(TypeReal.get(contextValue), 0);
		distribution = UtilValue.newArray(TypeReal.get(getContextValue()).getTypeArray(), 0);
		entryInterval = TypeInterval.get(getContextValue()).newValue();
		oneMinusSum = TypeReal.get(getContextValue()).newValue();
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
	 * @throws EPMCException thrown in case of problems
	 */
	boolean enumerate(ExtremePointHandler method, ValueArrayAlgebra intervals, int size) throws EPMCException {
		assert method != null;
		assert intervals != null;
		assert ValueArray.isArray(intervals);
		assert TypeInterval.isInterval(intervals.getType().getEntryType());
		assert intervals.getNumDimensions() == 1;
		assert size >= 0;
		assert size <= intervals.size();
		
		sum.set(0); // set again to avoid numerical imprecisions
		stop = false;
		if (distribution.size() < size) {
			distribution = UtilValue.newArray(TypeReal.get(getContextValue()).getTypeArray(), size);
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
	 * @throws EPMCException thrown in case of problems
	 */
	private void enumerateRec(int unassigned, int level) throws EPMCException {
		if (stop || sum.isGt(oneReal)) {
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
	 * @throws EPMCException
	 */
	private void enumerateRecTerminal(int unassigned) throws EPMCException {
		if (sum.isLt(oneReal) && !sum.isEq(oneReal) && unassigned == UNSET) {
			return;
		}
		if (unassigned != UNSET) {
			oneMinusSum.subtract(oneReal, sum);
			intervals.get(entryInterval, unassigned);
			if (oneMinusSum.isLe(entryInterval.getIntervalLower())
				|| oneMinusSum.isGe(entryInterval.getIntervalUpper())) {
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
	 * @throws EPMCException thrown in case of problems
	 */
	private void enumerateRecNonterminal(int unassigned, int level) throws EPMCException {
		intervals.get(entryInterval, level);
		boolean pointInterval = entryInterval.getIntervalLower().isEq(entryInterval.getIntervalUpper());
		if (unassigned == UNSET && !pointInterval) {
			enumerateRec(level, level + 1);
			intervals.get(entryInterval, level);
		}
		sum.add(sum, entryInterval.getIntervalLower());
		distribution.set(entryInterval.getIntervalLower(), level);
		enumerateRec(unassigned, level + 1);
		intervals.get(entryInterval, level);
		sum.subtract(sum, entryInterval.getIntervalLower());
		if (!pointInterval) {
			sum.add(sum, entryInterval.getIntervalUpper());
			distribution.set(entryInterval.getIntervalUpper(), level);
			enumerateRec(unassigned, level + 1);
			intervals.get(entryInterval, level);
			sum.subtract(sum, entryInterval.getIntervalUpper());
		}
	}

	/**
	 * Get value context used.
	 * 
	 * @return value context used
	 */
	private ContextValue getContextValue() {
		return contextValue;
	}
}

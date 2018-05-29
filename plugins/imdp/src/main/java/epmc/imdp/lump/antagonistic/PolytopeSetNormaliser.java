package epmc.imdp.lump.antagonistic;

import java.util.Arrays;
import java.util.Comparator;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueArrayInterval;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;

final class PolytopeSetNormaliser {
    // TODO normalisation of class numbers would be nice, but would
    // require that both states be taken into account to avoid generating
    // different orders for each of them
    private final class ComparatorInterval implements Comparator<ValueArrayInterval> {
        private final OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeInterval.get(), TypeInterval.get());
        private final OperatorEvaluator ltReal = ContextValue.get().getEvaluator(OperatorLt.LT, TypeReal.get(), TypeReal.get());
        private final OperatorEvaluator gtReal = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
        private final ValueBoolean cmp = TypeBoolean.get().newValue();
        
        @Override
        public int compare(ValueArrayInterval o1, ValueArrayInterval o2) {
            assert o1 != null;
            assert o2 != null;
            assert o1.size() == o2.size() : o1.size() + " " + o2.size();
            int size = o1.size();
            for (int entryNr = 0; entryNr < size; entryNr++) {
                o1.get(entry, entryNr);
                o2.get(entry2, entryNr);
                int entryCmp = compare(entry, entry2);
                if (entryCmp != 0) {
                    return entryCmp;
                }
            }
            return 0;
        }

        private int compare(ValueInterval op1, ValueInterval op2) {
            assert op1 != null;
            assert op2 != null;
            eq.apply(cmp, op1, op2);
            if (cmp.getBoolean()) {
                return 0;
            }
            ltReal.apply(cmp, op1.getIntervalLower(), op2.getIntervalUpper());
            if (cmp.getBoolean()) {
                return -1;
            }
            gtReal.apply(cmp, op1.getIntervalLower(), op2.getIntervalLower());
            if (cmp.getBoolean()) {
                return 1;
            }
            ltReal.apply(cmp, op1.getIntervalUpper(), op2.getIntervalUpper());
            if (cmp.getBoolean()) {
                return -1;
            }
            gtReal.apply(cmp, op1.getIntervalUpper(), op2.getIntervalUpper());
            if (cmp.getBoolean()) {
                return 1;
            }
            assert false;
            return Integer.MAX_VALUE;
        }
    }

    private ValueArrayInterval[] sortIntervals = new ValueArrayInterval[0];
    private final ValueInterval entry;
    private final ValueInterval entry2;
    private final ValueInterval entry3;
    private final ComparatorInterval comparatorInterval;
    private final OperatorEvaluator add;
    private final OperatorEvaluator set;

    PolytopeSetNormaliser() {
        entry = getTypeTransitionWeight().newValue();
        entry2 = getTypeTransitionWeight().newValue();
        entry3 = getTypeTransitionWeight().newValue();
        comparatorInterval = new ComparatorInterval();
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeInterval.get(), TypeInterval.get());
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeInterval.get(), TypeInterval.get());
    }

    void normalise(PolytopeSet polytopeSet, boolean unique) {
        assert polytopeSet != null;
        normaliseIntervals(polytopeSet);
        prepareSortIntervals(polytopeSet);
        readSortIntervals(polytopeSet);
        sortIntervals(polytopeSet);
        if (unique) {
            writeBackUnique(polytopeSet);
        } else {
            writeBack(polytopeSet);
        }
    }

    private void normaliseIntervals(PolytopeSet polytopeSet) {
        assert polytopeSet != null;
        int numClasses = polytopeSet.getNumClasses();
        int numActions = polytopeSet.getNumActions();
        ValueArrayAlgebra intervals = polytopeSet.getIntervals();
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            int from = actionNr * numClasses;
            int to = (actionNr + 1) * numClasses;
            normaliseIntervals(intervals, from, to);
        }
    }

    private void normaliseIntervals(ValueArrayAlgebra intervals, int from, int to) {
        assert intervals != null;
        assert from >= 0;
        assert to >= 0;
        assert from <= to;
        Value one = UtilValue.newValue(getTypeTransitionWeight(), 1);
        Value zero = UtilValue.newValue(getTypeTransitionWeight(), 0);
        set.apply(entry, zero);
        for (int index = from; index < to; index++) {
            intervals.get(entry2, index);
            add.apply(entry, entry, entry2);
        }
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeWeightTransition.get(), TypeWeightTransition.get());
        subtract.apply(entry, one, entry);
        OperatorEvaluator min = ContextValue.get().getEvaluator(OperatorMin.MIN, entry2.getIntervalUpper().getType(), entry3.getIntervalLower().getType());
        OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, entry2.getIntervalUpper().getType(), entry3.getIntervalLower().getType());
        for (int index = from; index < to; index++) {
            intervals.get(entry2, index);
            add.apply(entry3, entry, entry2);
            max.apply(entry2.getIntervalLower(), entry2.getIntervalLower(), entry3.getIntervalUpper());
            min.apply(entry2.getIntervalUpper(), entry2.getIntervalUpper(), entry3.getIntervalLower());
            intervals.set(entry2, index);
        }
    }

    private void prepareSortIntervals(PolytopeSet polytopeSet) {
        assert polytopeSet != null;

        int numActions = polytopeSet.getNumActions();
        int numClasses = polytopeSet.getNumClasses();
        TypeArrayAlgebra typeArrayWeight = getTypeWeightTransitionArray();
        int maxNumActions = sortIntervals.length;
        int requiredClassLength = numClasses;
        if (sortIntervals.length > 0) {
            requiredClassLength = Math.max(sortIntervals[0].size(), numClasses);
        }
        int requiredNumActions = Math.max(numActions, sortIntervals.length);
        if (numActions > maxNumActions) {
            ValueArrayInterval[] newSortIntervals = Arrays.copyOf(sortIntervals, numActions);
            for (int actionNr = 0; actionNr < requiredNumActions; actionNr++) {
                newSortIntervals[actionNr] = UtilValue.newArray(getTypeWeightTransitionArray(), requiredClassLength);
            }
            sortIntervals = newSortIntervals;
            maxNumActions = numActions;
        }
        int oldClassLength = sortIntervals[0].size();
        if (numClasses > oldClassLength) {
            for (int actionNr = 0; actionNr < requiredNumActions; actionNr++) {
                sortIntervals[actionNr] = UtilValue.newArray(typeArrayWeight, requiredClassLength);
            }
        }
    }

    private void readSortIntervals(PolytopeSet polytopeSet) {
        assert polytopeSet != null;

        int numActions = polytopeSet.getNumActions();
        int numClasses = polytopeSet.getNumClasses();
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            for (int classNr = 0; classNr < numClasses; classNr++) {
                polytopeSet.get(actionNr, classNr, entry);
                assert classNr < sortIntervals[actionNr].size() :
                    numClasses + " " + sortIntervals[actionNr].size() + " " + actionNr;
                sortIntervals[actionNr].set(entry, classNr);
            }
            /* following necessary to make comparisons work */
            int sortIntSize = sortIntervals[0].size();
            for (int classNr = numClasses; classNr < sortIntSize; classNr++) {
                sortIntervals[actionNr].set(0, classNr);
            }
        }
    }

    private void sortIntervals(PolytopeSet polytopeSet) {
        assert polytopeSet != null;

        Arrays.sort(sortIntervals, 0, polytopeSet.getNumActions(), comparatorInterval);
    }

    private void writeBack(PolytopeSet polytopeSet) {
        assert polytopeSet != null;

        int numActions = polytopeSet.getNumActions();
        int numClasses = polytopeSet.getNumClasses();
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            for (int classNr = 0; classNr < numClasses; classNr++) {
                sortIntervals[actionNr].get(entry, classNr);
                polytopeSet.set(actionNr, classNr, entry);
            }
        }
    }

    private void writeBackUnique(PolytopeSet polytopeSet) {
        assert polytopeSet != null;

        int numActions = polytopeSet.getNumActions();
        int numClasses = polytopeSet.getNumClasses();
        int actionFrom = 0;
        int newActionNr = 0;
        while (actionFrom < numActions) {
            for (int classNr = 0; classNr < numClasses; classNr++) {
                sortIntervals[actionFrom].get(entry, classNr);
                polytopeSet.set(newActionNr, classNr, entry);
            }
            newActionNr++;
            int actionTo = actionFrom;
            while (actionTo < numActions
                    && arraysEquals(sortIntervals[actionFrom], sortIntervals[actionTo])) {
                actionTo++;
            }
            actionFrom = actionTo;
        }
        polytopeSet.setDimensions(numClasses, newActionNr);
    }

    private TypeInterval getTypeTransitionWeight() {
        return TypeInterval.as(TypeWeightTransition.get());
    }

    private TypeArrayAlgebra getTypeWeightTransitionArray() {
        return getTypeTransitionWeight().getTypeArray();
    }    
    
    private boolean arraysEquals(ValueArrayInterval array1, ValueArrayInterval array2) {
        assert array1 != null;
        assert array2 != null;
        assert array1.size() == array2.size();
        return comparatorInterval.compare(array1, array2) == 0;
    }
}

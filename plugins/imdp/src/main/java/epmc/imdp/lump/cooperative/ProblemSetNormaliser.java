package epmc.imdp.lump.cooperative;

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
import epmc.value.ValueArrayInterval;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;

final class ProblemSetNormaliser {
    private final class ComparatorClass implements Comparator<ValueArrayInterval> {
        @Override
        public int compare(ValueArrayInterval o1, ValueArrayInterval o2) {
            assert o1 != null;
            assert o2 != null;
            o1.get(entry, 0);
            o2.get(entry2, 0);
            return ProblemSetNormaliser.this.compare(entry, entry2);
        }

    }

    private final class ComparatorInterval implements Comparator<ValueArrayInterval> {
        @Override
        public int compare(ValueArrayInterval o1, ValueArrayInterval o2) {
            assert o1 != null;
            assert o2 != null;
            int size = o1.size();
            for (int entryNr = 0; entryNr < size; entryNr++) {
                o1.get(entry, entryNr);
                o2.get(entry2, entryNr);
                int entryCmp = ProblemSetNormaliser.this.compare(entry, entry2);
                if (entryCmp != 0) {
                    return entryCmp;
                }
            }
            return 0;
        }

    }

    private ValueArrayInterval[] classes = new ValueArrayInterval[0];
    private ValueArrayInterval[] defenderIntervals = new ValueArrayInterval[0];
    private final ValueInterval entry;
    private final ValueInterval entry2;
    private final ValueInterval entry3;
    private int maxNumActions;
    private final ComparatorClass comparatorClass;
    private final ComparatorInterval comparatorInterval;
    private final OperatorEvaluator eqInterval;
    private final OperatorEvaluator addInterval;
    private final OperatorEvaluator setInterval;
    private final OperatorEvaluator ltReal;
    private final OperatorEvaluator gtReal;
    private final ValueBoolean cmp;

    ProblemSetNormaliser() {
        entry = getTypeTransitionWeight().newValue();
        entry2 = getTypeTransitionWeight().newValue();
        entry3 = getTypeTransitionWeight().newValue();
        comparatorClass = new ComparatorClass();
        comparatorInterval = new ComparatorInterval();
        eqInterval = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeInterval.get(), TypeInterval.get());
        addInterval = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeInterval.get(), TypeInterval.get());
        setInterval = ContextValue.get().getEvaluator(OperatorSet.SET, TypeInterval.get(), TypeInterval.get());
        ltReal = ContextValue.get().getEvaluator(OperatorLt.LT, TypeReal.get(), TypeReal.get());
        gtReal = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
        cmp = TypeBoolean.get().newValue();
    }

    void normalise(ProblemSet problemSet) {
        assert problemSet != null;

        normaliseIntervals(problemSet);
        prepareClassDataStructures(problemSet);
        prepareDefenderIntervals(problemSet);
        initialiseClasses(problemSet);
        sortClasses(problemSet);
        writeDefenderIntervals(problemSet);
        sortDefenderIntervals(problemSet);
        writeBack(problemSet);
    }

    private void normaliseIntervals(ProblemSet problemSet) {
        int numClasses = problemSet.getNumClasses();
        int numActions = problemSet.getNumActions();
        ValueArrayInterval intervals = problemSet.getIntervals();
        normaliseIntervals(intervals, 0, numClasses);
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            int from = (actionNr + 1) * numClasses;
            int to = (actionNr + 2) * numClasses;
            normaliseIntervals(intervals, from, to);
        }
    }

    private void normaliseIntervals(ValueArrayInterval intervals, int from, int to) {
        Value one = UtilValue.newValue(getTypeTransitionWeight(), 1);
        Value zero = UtilValue.newValue(getTypeTransitionWeight(), 0);
        setInterval.apply(entry, zero);
        OperatorEvaluator min = ContextValue.get().getEvaluator(OperatorMin.MIN, entry2.getIntervalUpper().getType(), entry3.getIntervalLower().getType());
        OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, entry2.getIntervalUpper().getType(), entry3.getIntervalLower().getType());
        OperatorEvaluator subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, TypeWeightTransition.get(), TypeWeightTransition.get());
        for (int index = from; index < to; index++) {
            intervals.get(entry2, index);
            addInterval.apply(entry, entry, entry2);
        }
        subtract.apply(entry, one, entry);
        for (int index = from; index < to; index++) {
            intervals.get(entry2, index);
            addInterval.apply(entry3, entry, entry2);
            max.apply(entry2.getIntervalLower(), entry2.getIntervalLower(), entry3.getIntervalUpper());
            min.apply(entry2.getIntervalUpper(), entry2.getIntervalUpper(), entry3.getIntervalLower());
            intervals.set(entry2, index);
        }
    }

    private void prepareClassDataStructures(ProblemSet problemSet) {
        assert problemSet != null;

        int numActions = problemSet.getNumActions();
        int numClasses = problemSet.getNumClasses();
        TypeArrayAlgebra typeArrayWeight = getTypeWeightTransitionArray();
        if (numActions > maxNumActions) {
            for (int classNr = 0; classNr < classes.length; classNr++) {
                classes[classNr] = UtilValue.newArray(typeArrayWeight, numActions + 1);
            }
            maxNumActions = numActions;
        }
        if (numClasses > classes.length) {
            ValueArrayInterval[] newClasses = Arrays.copyOf(classes, numClasses);
            for (int classNr = classes.length; classNr < numClasses; classNr++) {
                newClasses[classNr] = UtilValue.newArray(typeArrayWeight, maxNumActions + 1);
            }
            classes = newClasses;
        }
    }

    private void prepareDefenderIntervals(ProblemSet problemSet) {
        assert problemSet != null;

        int numActions = problemSet.getNumActions();
        int numClasses = problemSet.getNumClasses();
        TypeArrayAlgebra typeArrayWeight = getTypeWeightTransitionArray();
        int maxNumActions = defenderIntervals.length;
        int requiredClassLength = numClasses;
        if (defenderIntervals.length > 0) {
            requiredClassLength = Math.max(defenderIntervals[0].size(), numClasses);
        }
        int requiredNumActions = Math.max(numActions, defenderIntervals.length);
        if (numActions > maxNumActions) {
            ValueArrayInterval[] newDefenderIntervals = Arrays.copyOf(defenderIntervals, numActions);
            for (int actionNr = 0; actionNr < requiredNumActions; actionNr++) {
                newDefenderIntervals[actionNr] = UtilValue.newArray(getTypeWeightTransitionArray(), requiredClassLength);
            }
            defenderIntervals = newDefenderIntervals;
            maxNumActions = numActions;
        }
        int oldClassLength = defenderIntervals[0].size();
        if (numClasses > oldClassLength) {
            for (int actionNr = 0; actionNr < requiredNumActions; actionNr++) {
                defenderIntervals[actionNr] = UtilValue.newArray(typeArrayWeight, requiredClassLength);
            }
        }
    }

    private void initialiseClasses(ProblemSet problemSet) {
        assert problemSet != null;
        int numActions = problemSet.getNumActions();
        int numClasses = problemSet.getNumClasses();
        for (int classNr = 0; classNr < numClasses; classNr++) {
            problemSet.getChallenger(entry, classNr);
            classes[classNr].set(entry, 0);
            for (int actionNr = 0; actionNr < numActions; actionNr++) {
                problemSet.getDefender(entry, actionNr, classNr);
                classes[classNr].set(entry, actionNr + 1);
            }
        }
    }

    private void sortClasses(ProblemSet problemSet) {
        Arrays.sort(classes, 0, problemSet.getNumClasses(), comparatorClass);
    }

    private void writeDefenderIntervals(ProblemSet problemSet) {
        assert problemSet != null;

        int numActions = problemSet.getNumActions();
        int numClasses = problemSet.getNumClasses();
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            for (int classNr = 0; classNr < numClasses; classNr++) {
                classes[classNr].get(entry, actionNr + 1);
                assert classNr < defenderIntervals[actionNr].size() :
                    numClasses + " " + defenderIntervals[actionNr].size() + " " + actionNr;
                defenderIntervals[actionNr].set(entry, classNr);
            }
        }
    }

    private void sortDefenderIntervals(ProblemSet problemSet) {
        assert problemSet != null;

        Arrays.sort(defenderIntervals, 0, problemSet.getNumActions(), comparatorInterval);
    }

    private void writeBack(ProblemSet problemSet) {
        assert problemSet != null;

        int numActions = problemSet.getNumActions();
        int numClasses = problemSet.getNumClasses();
        for (int classNr = 0; classNr < numClasses; classNr++) {
            classes[classNr].get(entry, 0);
            problemSet.setChallenger(classNr, entry);
            for (int actionNr = 0; actionNr < numActions; actionNr++) {
                defenderIntervals[actionNr].get(entry, classNr);
                problemSet.setDefender(actionNr, classNr, entry);
            }
        }
    }

    private TypeInterval getTypeTransitionWeight() {
        return TypeInterval.as(TypeWeightTransition.get());
    }

    private TypeArrayAlgebra getTypeWeightTransitionArray() {
        return getTypeTransitionWeight().getTypeArray();
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

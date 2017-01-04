package epmc.imdp.lump;

import java.util.Arrays;
import java.util.Comparator;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInterval;

final class ProblemSetNormaliser {
	private final class ComparatorClass implements Comparator<ValueArrayAlgebra> {
		@Override
		public int compare(ValueArrayAlgebra o1, ValueArrayAlgebra o2) {
			assert o1 != null;
			assert o2 != null;
			o1.get(entry, 0);
			o2.get(entry2, 0);
			return entry.compareTo(entry2);
		}
		
	}
	
	private final class ComparatorInterval implements Comparator<ValueArrayAlgebra> {
		@Override
		public int compare(ValueArrayAlgebra o1, ValueArrayAlgebra o2) {
			assert o1 != null;
			assert o2 != null;
			int size = o1.size();
			for (int entryNr = 0; entryNr < size; entryNr++) {
				o1.get(entry, entryNr);
				o2.get(entry2, entryNr);
				int entryCmp = entry.compareTo(entry2);
				if (entryCmp != 0) {
					return entryCmp;
				}
			}
			return 0;
		}
		
	}
	
	private final ContextValue contextValue;
	private ValueArrayAlgebra[] classes = new ValueArrayAlgebra[0];
	private ValueArrayAlgebra[] defenderIntervals = new ValueArrayAlgebra[0];
	private final ValueInterval entry;
	private final ValueInterval entry2;
	private final ValueInterval entry3;
	private int maxNumActions;
	private final ComparatorClass comparatorClass;
	private final ComparatorInterval comparatorInterval;
	
	ProblemSetNormaliser(ContextValue contextValue) {
		assert contextValue != null;
		this.contextValue = contextValue;
		entry = getTypeTransitionWeight().newValue();
		entry2 = getTypeTransitionWeight().newValue();
		entry3 = getTypeTransitionWeight().newValue();
		comparatorClass = new ComparatorClass();
		comparatorInterval = new ComparatorInterval();
	}
	
	
	// TODO optionally remove dublicate action entries
	
	void normalise(ProblemSet problemSet) throws EPMCException {
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

	private void normaliseIntervals(ProblemSet problemSet) throws EPMCException {
		int numClasses = problemSet.getNumClasses();
		int numActions = problemSet.getNumActions();
		ValueArrayAlgebra intervals = problemSet.getIntervals();
		normaliseIntervals(intervals, 0, numClasses);
		for (int actionNr = 0; actionNr < numActions; actionNr++) {
			int from = (actionNr + 1) * numClasses;
			int to = (actionNr + 2) * numClasses;
			normaliseIntervals(intervals, from, to);
		}
	}

	private void normaliseIntervals(ValueArrayAlgebra intervals, int from, int to) throws EPMCException {
		Value one = getTypeTransitionWeight().getOne();
		entry.set(getTypeTransitionWeight().getZero());
		for (int index = from; index < to; index++) {
			intervals.get(entry2, index);
			entry.add(entry, entry2);
		}
		entry.subtract(one, entry);
		for (int index = from; index < to; index++) {
			intervals.get(entry2, index);
			entry3.add(entry, entry2);
			entry2.getIntervalLower().max(entry2.getIntervalLower(), entry3.getIntervalUpper());
			entry2.getIntervalUpper().min(entry2.getIntervalUpper(), entry3.getIntervalLower());
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
			ValueArrayAlgebra[] newClasses = Arrays.copyOf(classes, numClasses);
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
			ValueArrayAlgebra[] newDefenderIntervals = Arrays.copyOf(defenderIntervals, numActions);
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
	
	private void writeBack(ProblemSet problemSet) throws EPMCException {
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

	private ContextValue getContextValue() {
		return contextValue;
	}
	
	private TypeInterval getTypeTransitionWeight() {
		return TypeInterval.asInterval(TypeWeightTransition.get(getContextValue()));
	}
	
	private TypeArrayAlgebra getTypeWeightTransitionArray() {
		return getTypeTransitionWeight().getTypeArray();
	}
	

}

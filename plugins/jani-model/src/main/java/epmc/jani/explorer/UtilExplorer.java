package epmc.jani.explorer;

import static epmc.error.UtilError.ensure;

import java.util.LinkedHashMap;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.ModelJANI;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

/**
 * Utility functions for the explorer part of the JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilExplorer {
	/**
	 * Check whether the probability sum of an automaton is one.
	 * The check refers to the last node queried.
	 * the automaton parameter must not be {@code null}.
	 * 
	 * @param automaton automaton to be checked
	 * @throws EPMCException thrown in case sum not one or other problems
	 */
	public static void checkAutomatonProbabilitySum(ExplorerComponentAutomaton automaton) throws EPMCException {
		assert automaton != null;
		int numSuccessors = automaton.getNumSuccessors();
		ValueAlgebra probabilitySum = automaton.getProbabilitySum();
		if (automaton.isNonDet() && automaton.isState()) {
			return;
		}
		Value one = probabilitySum.getType().getOne();
		ensure(numSuccessors == 0
				|| probabilitySum.isOne()
				|| probabilitySum.distance(one) < 1E-10,
				ProblemsJANIExplorer.JANI_EXPLORER_PROBABILIY_SUM_NOT_ONE,
				probabilitySum);
	}
	
	/**
	 * Computes a map from the actions of a model to the integers.
	 * Subsequent calls of this method on the same model will lead to the same
	 * result.
	 * The action numbers computed will start with 0, will be different for each
	 * action, and the highest number will be the number of actions minus 1.
	 * The model parameter must not be {@code null}.
	 * 
	 * @param model the model to compute action map of
	 * @return map from action to integers
	 */
	public static Map<Action, Integer> computeActionToInteger(ModelJANI model) {
		assert model != null;
		Map<Action,Integer> result = new LinkedHashMap<>();
		int actionNumber = 0;
		Actions actions = model.getActionsOrEmpty();
		result.put(model.getSilentAction(), actionNumber);
		actionNumber++;
		for (Action action : actions) {
			result.put(action, actionNumber);
			actionNumber++;
		}
		return result;
	}

	/**
	 * Private constructor to prevent instantiation of this class.
	 */
	private UtilExplorer() {
	}
}

/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.prism.exporter.processor;

import static epmc.error.UtilError.ensure;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.Constant;
import epmc.jani.model.ModelJANIProcessor;
import epmc.jani.model.Variable;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.time.TypeClock;

/**
 * Class that is responsible for registering the JANI components; the transient variables are considered as corresponding to rewards.
 * 
 * @author Andrea Turrini
 *
 */
public class JANIComponentRegistrar {
	
	private static Set<String> usedNames;
	private static Map<Constant, String> constantNames;
	private static Map<String, String> constantNameNames;
	
	private static Set<Variable> globalVariables;
	
	private static Map<Variable, String> variableNames;
	private static Map<String, Variable> variableByName;
	private static Map<String, String> variableNameNames;
	private static Map<Variable, Map<Action, Expression>> rewardTransitionExpressions;
	private static Map<Variable, Expression> rewardStateExpressions;
	
	private static Map<Variable, Automaton> variablesAssignedByAutomaton;
	private static Map<Automaton, Set<Variable>> automatonAssignsVariables;
	
	private static Map<Action, String> actionNames;
	
	private static Automaton defaultAutomatonForUnassignedClocks;
	private static Set<Variable> unassignedClockVariables;
	
	private static boolean isTimedModel;
	
	private static int reward_counter;
	private static int variable_counter;
	private static int constant_counter;
	
	static {
		reset();
	}
	
	public static void reset() {
		usedNames = new HashSet<>(); 
		
		constantNames = new HashMap<>();
		constantNameNames = new HashMap<>();
		
		globalVariables  = new HashSet<>();
		variableNames = new HashMap<>();
		variableByName = new HashMap<>();
		variableNameNames = new HashMap<>();
		
		rewardTransitionExpressions = new HashMap<>();
		rewardStateExpressions = new HashMap<>();
		variablesAssignedByAutomaton = new HashMap<>();
		automatonAssignsVariables = new HashMap<>();
		actionNames = new HashMap<>();
		
		defaultAutomatonForUnassignedClocks = null;
		unassignedClockVariables = new HashSet<>();
		
		isTimedModel = false;
		reward_counter = 0;
		variable_counter = 0;
		constant_counter = 0;
	}
	
	public static void setIsTimedModel(boolean isTimedModel) {
		JANIComponentRegistrar.isTimedModel = isTimedModel;
	}
	
	public static boolean isTimedModel() {
		return isTimedModel;
	}
	
	public static void setDefaultAutomatonForUnassignedClocks(Automaton defaultAutomatonForUnassignedClocks) {
		assert defaultAutomatonForUnassignedClocks != null;
		
		JANIComponentRegistrar.defaultAutomatonForUnassignedClocks = defaultAutomatonForUnassignedClocks;
	}
	
	public static Automaton getDefaultAutomatonForUnassignedClocks() {
		return defaultAutomatonForUnassignedClocks;
	}
	
	public static Set<Variable> getUnassignedClockVariables() {
		return Collections.unmodifiableSet(unassignedClockVariables);
	}
	
	/**
	 * Register a constant.
	 * 
	 * @param constant the constant to register
	 * @throws EPMCException if the constant has been already registered
	 */
	public static void registerConstant(Constant constant) throws EPMCException {
		assert constant != null;

		ensure(!constantNames.containsKey(constant), ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_CONSTANT_DEFINED_TWICE, constant.getName());
		if (!constantNames.containsKey(constant)) {
			String name;
			name = constant.getName();
			//TODO: manage the case the variable name contains unexpected characters
			while (usedNames.contains(name)) {
				name = "constant_" + constant_counter;
				constant_counter++;
			}
			usedNames.add(name);
			constantNames.put(constant, name);
			constantNameNames.put(constant.getName(), name);
		}
	}
	
	/**
	 * Register a variable.
	 * 
	 * @param variable the variable to register
	 * @throws EPMCException if the variable has been already registered
	 */
	public static void registerVariable(Variable variable) throws EPMCException {
		assert variable != null;

		ensure(!variableNames.containsKey(variable), ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_VARIABLE_DEFINED_TWICE, variable.getName());
		if (!variableNames.containsKey(variable)) {
			variableByName.put(variable.getName(), variable);
			String name;
			if (variable.isTransient()) {
				do {
					name = "\"reward_" + reward_counter + "\"";
					reward_counter++;
				} while (usedNames.contains(name));
			} else {
				name = variable.getName();
				//TODO: manage the case the variable name contains unexpected characters
				while (usedNames.contains(name)) {
					name = "variable_" + variable_counter;
					variable_counter++;
				}
			}
			usedNames.add(name);
			variableNames.put(variable, name);
			variableNameNames.put(variable.getName(), name);
		}
	}
	
	/**
	 * Return the unique name for the variable respecting the PRISM syntax
	 * 
	 * @param variable the wanted variable
	 * @return the associated name or {@code null} if such a variable is unknown
	 */
	public static String getVariableNameByVariable(Variable variable) {
		assert variable != null;
		
		return variableNames.get(variable);
	}
	
	/**
	 * Return the unique name for the variable respecting the PRISM syntax
	 * 
	 * @param name the wanted name
	 * @return the associated name or {@code null} if such a variable is unknown
	 */
	public static String getVariableNameByName(String name) {
		assert name != null;
		
		return variableNameNames.get(name);
	}
	
	/**
	 * Return the variable for the given name
	 * 
	 * @param name the wanted name
	 * @return the associated variable or {@code null} if such a name is unknown
	 */
	public static Variable getVariableByName(String name) {
		assert name != null;
		
		return variableByName.get(name);
	}
	
	/**
	 * Register a variable as global whenever the variable is not assigned by an automaton.
	 * Make sure to first register the variables assigned by automata.
	 * 
	 * Note that a variable is not registered if it is transient.
	 * 
	 * @param variable the variable to register
	 */
	public static void registerGlobalVariable(Variable variable) throws EPMCException {
		assert variable != null;

		if (variable.isTransient()) {
			return;
		}
		
		if (variable.getType().toType() instanceof TypeClock && !variablesAssignedByAutomaton.containsKey(variable)) {
			unassignedClockVariables.add(variable);
		} else { 
			if (!variablesAssignedByAutomaton.containsKey(variable)) {
				globalVariables.add(variable);
			}
		}
	}
	
	public static Set<Variable> getGlobalVariables() {
		return Collections.unmodifiableSet(globalVariables);
	}
	
	/**
	 * Register a new expression for the given reward and action 
	 * 
	 * @param reward the reward structure
	 * @param action the action the expression refers to
	 * @param expression the expression
	 * @throws EPMCException if there is already a different expression associated with the reward and action
	 */
	public static void registerTransitionRewardExpression(Variable reward, Action action, Expression expression) throws EPMCException {
		assert reward != null;
		assert action != null;
		assert expression != null;
		assert reward.isTransient();
		
		ensure(variableNames.containsKey(reward), 
				ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_UNDEFINED_USED_VARIABLE, 
				reward.getName());
		
		Map<Action, Expression> mapAE = rewardTransitionExpressions.get(reward);
		if (mapAE == null) {
			mapAE = new HashMap<>();
			rewardTransitionExpressions.put(reward, mapAE);
		}
		Expression oldAssgn = mapAE.get(action);
		if (oldAssgn == null) {
			mapAE.put(action, expression);
		} else {
			ensure(expression.equals(oldAssgn), 
					ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_TRANSIENT_VARIABLE_DIFFERENT_EXPRESSIONS, 
					getVariableNameByVariable(reward));
		}
	}

	/**
	 * Register a new expression for the given reward 
	 * 
	 * @param reward the reward structure
	 * @param expression the expression
	 * @throws EPMCException if there is already a different expression associated with the reward
	 */
	public static void registerStateRewardExpression(Variable reward, Expression expression) throws EPMCException {
		assert reward != null;
		assert expression != null;
		assert reward.isTransient();
		
		ensure(variableNames.containsKey(reward), 
				ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_UNDEFINED_USED_VARIABLE, 
				"Variable used but not declared:", 
				reward.getName());
		
		Expression oldExp = rewardStateExpressions.get(reward);
		if (oldExp == null) {
			rewardStateExpressions.put(reward, expression);
		} else {
			ensure(expression.equals(oldExp), 
					ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_TRANSIENT_VARIABLE_DIFFERENT_EXPRESSIONS, 
					getVariableNameByVariable(reward));
		}
	}
	
	/**
	 * Register a new automaton for the given action 
	 * 
	 * @param variable the variable
	 * @param automaton the automaton
	 * @throws EPMCException if there is already a different automaton associated with the variable
	 */
	public static void registerNonTransientVariableAssignment(Variable variable, Automaton automaton) throws EPMCException {
		assert variable != null;
		assert automaton != null;
		
		if (variable.isTransient()) {
			return;
		}
		
		Automaton oldAut = variablesAssignedByAutomaton.get(variable);
		if (oldAut == null) {
			variablesAssignedByAutomaton.put(variable, automaton);
		} else {
			ensure(automaton.equals(oldAut), 
					ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_VARIABLE_ASSIGNED_MULTIPLE_AUTOMATA, 
					getVariableNameByVariable(variable));
		}
		
		Set<Variable> assignedVariables = automatonAssignsVariables.get(automaton);
		if (assignedVariables == null) {
			assignedVariables = new HashSet<>();
			automatonAssignsVariables.put(automaton, assignedVariables);
		}
		assignedVariables.add(variable);
	}
	
	public static Set<Variable> getAssignedVariablesOrEmpty(Automaton automaton) {
		assert automaton != null;
		
		Set<Variable> assignedVariables = automatonAssignsVariables.get(automaton);
		if (assignedVariables == null) {
			assignedVariables = new HashSet<>();
		}
		
		return Collections.unmodifiableSet(assignedVariables);
	}
	
	public static StringBuilder toPRISMRewards() throws EPMCException {
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 

		Expression expression;
		Action action;

		boolean remaining = false;
		for (Entry<Variable, String> entry: variableNames.entrySet()) {
			Variable reward = entry.getKey();
			if (!reward.isTransient()) {
				continue;
			}
			if (remaining) {
				prism.append("\n");
			} else {
				remaining = true;
			}
			prism.append("rewards ").append(entry.getValue()).append("\n");
			expression = rewardStateExpressions.get(reward);
			if (expression != null) {
				processor = ProcessorRegistrar.getProcessor(expression);
				prism.append(ModelJANIProcessor.INDENT).append("true : ").append(processor.toPRISM().toString()).append(";\n");
			}
			Map<Action, Expression> mapAA = rewardTransitionExpressions.get(reward);
			if (mapAA != null) {
				for(Entry<Action, Expression> entryAA : mapAA.entrySet()) {
					action = entryAA.getKey();
					processor = ProcessorRegistrar.getProcessor(action);
					prism.append(ModelJANIProcessor.INDENT).append(processor.toPRISM().toString()).append(" true : ");

					expression = entryAA.getValue();
					processor = ProcessorRegistrar.getProcessor(expression);
					prism.append(processor.toPRISM().toString()).append(";\n");
					
				}
			}
			prism.append("endrewards\n");
		}
		
		return prism;
	}
	
	/**
	 * Register an action.
	 * 
	 * @param action the variable to register
	 */
	public static void registerAction(Action action) {
		if (!actionNames.containsKey(action)) {
			String name;
			//TODO: manage the case the variable name contains unexpected characters
			if (isSilentAction(action)) {
				name = "";
			} else {
				name = action.getName();
			}
			actionNames.put(action, name);
		}
	}
	
	/**
	 * Return the unique name for the action respecting the PRISM syntax
	 * 
	 * @param action the wanted action
	 * @return the associated name
	 * @throws EPMCException if the action is not registered
	 */
	public static String getActionName(Action action) throws EPMCException {
		assert action != null;
		ensure(actionNames.containsKey(action), ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_UNDEFINED_USED_ACTION, action.getName());

		return actionNames.get(action);
	}
	
	public static boolean isSilentAction(Action action) {
		assert action != null;

		return "τ".equals(action.getName());
	}
}

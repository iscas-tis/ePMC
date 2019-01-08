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

package epmc.prism.exporter;

import static epmc.error.UtilError.ensure;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.expression.Expression;
import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.Constant;
import epmc.jani.model.InitialStates;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.Location;
import epmc.jani.model.PRISMExporter_ModelJANIProcessor;
import epmc.jani.model.Variable;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;
import epmc.prism.exporter.util.Range;
import epmc.time.TypeClock;

/**
 * Class that is responsible for registering the JANI components; the transient variables are considered as corresponding to rewards.
 * 
 * @author Andrea Turrini
 *
 */
public class JANIComponentRegistrar {

    private static final Collection<String> reservedWords = new HashSet<>();
    static {
        reservedWords.add("A");
        reservedWords.add("bool");
        reservedWords.add("clock");
        reservedWords.add("const");
        reservedWords.add("ctmc");
        reservedWords.add("C");
        reservedWords.add("double");
        reservedWords.add("dtmc");
        reservedWords.add("E");
        reservedWords.add("endinit");
        reservedWords.add("endinvariant");
        reservedWords.add("endmodule");
        reservedWords.add("endrewards");
        reservedWords.add("endsystem");
        reservedWords.add("false");
        reservedWords.add("formula");
        reservedWords.add("filter");
        reservedWords.add("func");
        reservedWords.add("F");
        reservedWords.add("global");
        reservedWords.add("G");
        reservedWords.add("init");
        reservedWords.add("invariant");
        reservedWords.add("I");
        reservedWords.add("int");
        reservedWords.add("label");
        reservedWords.add("max");
        reservedWords.add("mdp");
        reservedWords.add("min");
        reservedWords.add("module");
        reservedWords.add("X");
        reservedWords.add("nondeterministic");
        reservedWords.add("Pmax");
        reservedWords.add("Pmin");
        reservedWords.add("P");
        reservedWords.add("probabilistic");
        reservedWords.add("prob");
        reservedWords.add("pta");
        reservedWords.add("rate");
        reservedWords.add("rewards");
        reservedWords.add("Rmax");
        reservedWords.add("Rmin");
        reservedWords.add("R");
        reservedWords.add("S");
        reservedWords.add("stochastic");
        reservedWords.add("system");
        reservedWords.add("true");
        reservedWords.add("U");
        reservedWords.add("W");
        reset();
    }

    private static Set<String> usedNames;

    private static Set<Variable> globalVariables;

    private static Map<JANIIdentifier, String> identifierNames;
    private static Map<String, JANIIdentifier> identifierByName;
    private static Map<String, String> identifierNameNames;
    private static Map<Variable, Map<Action, Expression>> rewardTransitionExpressions;
    private static Map<Variable, Expression> rewardStateExpressions;

    private static Set<Variable> variablesAssignedByAutomata;
    private static Map<Variable, Map<Action, Set<Automaton>>> variablesAssignedByAutomataByAction;

    private static Set<String> usedActionNames;
    private static Map<Action, String> actionNames;
    private static Action silentAction;

    private static Map<Automaton, String> automatonLocationName;
    private static Map<Automaton, Map<Location, Integer>> automatonLocationIdentifier;

    private static Automaton defaultAutomatonForUnassignedClocks;
    private static Set<Variable> unassignedClockVariables;

    private static InitialStates initialStates;
    private static Map<Automaton, Collection<Location>> initialLocations;
    private static Boolean usesInitialConditions;

    private static boolean isTimedModel;
    private static boolean isNonDeterministicModel;

    private static int reward_counter;
    private static int identifier_counter;
    private static int action_counter;
    private static int location_counter_name;
    private static int location_counter_id;

    static void reset() {
        usedNames = new HashSet<>(); 

        globalVariables  = new HashSet<>();
        identifierNames = new HashMap<>();
        identifierByName = new HashMap<>();
        identifierNameNames = new HashMap<>();

        rewardTransitionExpressions = new HashMap<>();
        rewardStateExpressions = new HashMap<>();
        variablesAssignedByAutomata = new HashSet<>();
        variablesAssignedByAutomataByAction = new HashMap<>();
        usedActionNames = new HashSet<>();
        actionNames = new HashMap<>();
        silentAction = null;

        automatonLocationIdentifier = new HashMap<>();
        automatonLocationName = new HashMap<>();

        initialStates = null;
        initialLocations = new HashMap<>();
        usesInitialConditions = null;

        defaultAutomatonForUnassignedClocks = null;
        unassignedClockVariables = new HashSet<>();

        isTimedModel = false;
        isNonDeterministicModel = false;
        reward_counter = 0;
        identifier_counter = 0;
        action_counter = 0;
        location_counter_name = 0;
        location_counter_id = 0;
    }
    
    public static void addReservedWord(String word) {
        assert word != null;
        
        reservedWords.add(word);
    }
    
    public static void addSilentAction(Action action) {
    	assert (action != null) && action.getModel().getSilentAction().equals(action);
    	
    	actionNames.put(action, action.getName());
    	usedActionNames.add(action.getName());
    	silentAction = action;
    }

    public static void setIsTimedModel(boolean isTimedModel) {
        JANIComponentRegistrar.isTimedModel = isTimedModel;
    }

    public static boolean isTimedModel() {
        return isTimedModel;
    }

    public static void setIsNonDeterministicModel(boolean isNonDeterministicModel) {
        JANIComponentRegistrar.isNonDeterministicModel = isNonDeterministicModel;
    }

    public static boolean isNonDeterministicModel() {
        return isNonDeterministicModel;
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
     * Register a location.
     * 
     * @param automaton the the automaton the location belongs to
     * @param location the location to register
     */
    public static void registerLocation(Automaton automaton, Location location) {
        assert automaton != null;
        assert location != null;

        Map<Location, Integer> mapLI = automatonLocationIdentifier.get(automaton);
        if (mapLI == null) {
            mapLI = new HashMap<>();
            automatonLocationIdentifier.put(automaton, mapLI);
        }
        ensure(!mapLI.containsKey(location), 
                ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_LOCATION_DEFINED_TWICE, 
                location.getName());
        mapLI.put(location, location_counter_id++);

        String name = "location";
        while (usedNames.contains(name)) {
            name = "location_" + location_counter_name;
            location_counter_name++;
        }
        usedNames.add(name);
        automatonLocationName.put(automaton, name);
    }

    public static String getLocationName(Automaton automaton) {
        assert automatonLocationName.containsKey(automaton);

        return automatonLocationName.get(automaton);
    }

    public static Integer getLocationIdentifier(Automaton automaton, Location location) {
        assert automatonLocationIdentifier.containsKey(automaton);

        Map<Location, Integer> mapLI = automatonLocationIdentifier.get(automaton);
        assert (mapLI != null) && mapLI.containsKey(location);

        return mapLI.get(location);
    }

    public static Range getLocationRange(Automaton automaton) {
        assert automatonLocationIdentifier.containsKey(automaton);

        Map<Location, Integer> mapLI = automatonLocationIdentifier.get(automaton);
        assert !mapLI.isEmpty();

        int low = Integer.MAX_VALUE;
        int high = Integer.MIN_VALUE;

        for (int value : mapLI.values()) {
            if (low > value) {
                low = value;
            }
            if (high < value) {
                high = value;
            }
        }

        assert low <= high;

        return new Range(low, high);
    }

    /**
     * Register a JANI identifier.
     * 
     * @param identifier the identifier to register
     */
    public static void registerIdentifier(JANIIdentifier identifier) {
        assert identifier != null;

        ensure(!identifierNames.containsKey(identifier), 
                ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_IDENTIFIER_DEFINED_TWICE, 
                identifier.getName());
        if (!identifierNames.containsKey(identifier)) {
            identifierByName.put(identifier.getName(), identifier);
            String name;
            if ((identifier instanceof Variable) && (((Variable)identifier).isTransient())) {
                do {
                    name = "\"reward_" + reward_counter + "\"";
                    reward_counter++;
                } while (usedNames.contains(name));
            } else {
                name = identifier.getName();
                if (! name.matches("^[A-Za-z_][A-Za-z0-9_]*$") || reservedWords.contains(name)) {
                    name = "identifier_" + identifier_counter;
                }
                while (usedNames.contains(name)) {
                    name = "identifier_" + identifier_counter;
                    identifier_counter++;
                }
            }
            usedNames.add(name);
            identifierNames.put(identifier, name);
            identifierNameNames.put(identifier.getName(), name);
        }
    }

    /**
     * Return the unique name for the identifier respecting the PRISM syntax
     * 
     * @param identifier the wanted identifier
     * @return the associated name or {@code null} if such an identifier is unknown
     */
    public static String getIdentifierNameByIdentifier(JANIIdentifier identifier) {
        assert identifier != null;

        return identifierNames.get(identifier);
    }

    /**
     * Return the unique name for the identifier respecting the PRISM syntax
     * 
     * @param name the wanted name
     * @return the associated name or {@code null} if such an identifier is unknown
     */
    public static String getIdentifierNameByName(String name) {
        assert name != null;

        return identifierNameNames.get(name);
    }

    /**
     * Return the identifier for the given name
     * 
     * @param name the wanted name
     * @return the associated variable or {@code null} if such a name is unknown
     */
    public static JANIIdentifier getIdentifierByName(String name) {
        assert name != null;

        return identifierByName.get(name);
    }

    /**
     * Register a variable as global whenever the variable is not assigned by an automaton.
     * Make sure to first register the variables assigned by automata.
     * 
     * Note that a variable is not registered if it is transient.
     * 
     * @param variable the variable to register
     */
    public static void registerGlobalVariable(Variable variable) {
        assert variable != null;

        if (variable.isTransient()) {
            return;
        }

        if (variable.getType().toType() instanceof TypeClock && !variablesAssignedByAutomata.contains(variable)) {
            unassignedClockVariables.add(variable);
        } else { 
            if (!variablesAssignedByAutomata.contains(variable)) {
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
     */
    public static void registerTransitionRewardExpression(Variable reward, Action action, Expression expression) {
        assert reward != null;
        assert action != null;
        assert expression != null;
        assert reward.isTransient();

        ensure(identifierNames.containsKey(reward), 
                ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_UNDEFINED_USED_IDENTIFIER, 
                reward.getName());

        Map<Action, Expression> mapAE = rewardTransitionExpressions.get(reward);
        if (mapAE == null) {
            mapAE = new HashMap<>();
            rewardTransitionExpressions.put(reward, mapAE);
        }
        Expression oldAssgn = mapAE.get(action);
        if (oldAssgn == null) {
            mapAE.put(action, expression);
//        } else {
//            ensure(expression.equals(oldAssgn), 
//                    ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_TRANSIENT_VARIABLE_DIFFERENT_EXPRESSIONS, 
//                    getIdentifierNameByIdentifier(reward));
        }
    }

    /**
     * Register a new expression for the given reward 
     * 
     * @param reward the reward structure
     * @param expression the expression
     */
    public static void registerStateRewardExpression(Variable reward, Expression expression) {
        assert reward != null;
        assert expression != null;
        assert reward.isTransient();

        ensure(identifierNames.containsKey(reward), 
                ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_UNDEFINED_USED_IDENTIFIER, 
                "Variable used but not declared:", 
                reward.getName());

        Expression oldExp = rewardStateExpressions.get(reward);
        if (oldExp == null) {
            rewardStateExpressions.put(reward, expression);
        } else {
            ensure(expression.equals(oldExp), 
                    ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_TRANSIENT_VARIABLE_DIFFERENT_EXPRESSIONS, 
                    getIdentifierNameByIdentifier(reward));
        }
    }

    /**
     * Register which automata assign variables for the given action 
     * 
     * @param variable the variable
     * @param action the action
     * @param automaton the automaton
     */
    public static void registerNonTransientVariableAssignment(Variable variable, Action action, Automaton automaton) {
        assert variable != null;
        assert automaton != null;

        if (variable.isTransient()) {
            return;
        }

        variablesAssignedByAutomata.add(variable);

        Map<Action, Set<Automaton>> assignedByAutomataByAction = variablesAssignedByAutomataByAction.get(variable);
        if (assignedByAutomataByAction == null) {
            assignedByAutomataByAction = new HashMap<>();
            variablesAssignedByAutomataByAction.put(variable, assignedByAutomataByAction);
        }
        Set<Automaton> automata = assignedByAutomataByAction.get(action);
        if (automata == null) {
            automata = new HashSet<>();
            assignedByAutomataByAction.put(action, automata);
        }
        ensure(isSilentAction(action) || automata.isEmpty() || automata.contains(automaton),
                ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_VARIABLE_ASSIGNED_MULTIPLE_AUTOMATA,
                getIdentifierNameByIdentifier(variable)
                );
        automata.add(automaton);
        if (automata.size() > 1) {
            // multiple automata assign to the variable, so it must be a global variable
            globalVariables.add(variable);
        }
    }

    public static Set<Variable> getLocalVariablesOrEmpty(Automaton automaton) {
        assert automaton != null;

        Set<Variable> assignedVariables = new HashSet<>();
        for (Variable variable : variablesAssignedByAutomataByAction.keySet()) {
            for (Map.Entry<Action, Set<Automaton>> entry : variablesAssignedByAutomataByAction.get(variable).entrySet()) {
                if (entry.getValue().contains(automaton)) {
                    if (!isSilentAction(entry.getKey()) || entry.getValue().size() == 1) {
                        assignedVariables.add(variable);
                    }
                }
            }
        }
        
        return assignedVariables;
    }

    public static String toPRISMRewards() {
        StringBuilder prism = new StringBuilder();
        PRISMExporter_ProcessorStrict processor; 

        Expression expression;
        Action action;

        boolean remaining = false;
        for (Entry<JANIIdentifier, String> entry: identifierNames.entrySet()) {
        	JANIIdentifier identifier = entry.getKey();
        	if (identifier instanceof Variable) {
	            Variable reward = (Variable) identifier;
	            String name = entry.getValue();
	            if (!reward.isTransient()) {
	                continue;
	            }
	            if (remaining) {
	                prism.append("\n");
	            } else {
	                remaining = true;
	            }
	            prism.append("// Original variable name: ").append(reward.getName()).append("\n")
	                .append("// New name: ").append(name).append("\n");
	            prism.append("rewards ").append(name).append("\n");
	            expression = rewardStateExpressions.get(reward);
	            if (expression != null) {
	                processor = PRISMExporter_ProcessorRegistrar.getProcessor(expression);
	                prism.append(PRISMExporter_ModelJANIProcessor.INDENT).append("true : ").append(processor.toPRISM()).append(";\n");
	            }
	            Map<Action, Expression> mapAA = rewardTransitionExpressions.get(reward);
	            if (mapAA != null) {
	                for(Entry<Action, Expression> entryAA : mapAA.entrySet()) {
	                    action = entryAA.getKey();
	                    processor = PRISMExporter_ProcessorRegistrar.getProcessor(action);
	                    prism.append(PRISMExporter_ModelJANIProcessor.INDENT).append(processor.toPRISM()).append(" true : ");
	
	                    expression = entryAA.getValue();
	                    processor = PRISMExporter_ProcessorRegistrar.getProcessor(expression);
	                    prism.append(processor.toPRISM()).append(";\n");
	
	                }
	            }
	            prism.append("endrewards\n");
        	}
        }

        return prism.toString();
    }

    /**
     * Register an action.
     * 
     * @param action the variable to register
     */
    public static void registerAction(Action action) {
        if (isSilentAction(action)) {
            return;
        }
        if (!actionNames.containsKey(action)) {
            String name;
            //TODO: manage the case the variable name contains unexpected characters
            name = action.getName();
            if (!name.matches("^[A-Za-z_][A-Za-z0-9_]*$") || reservedWords.contains(name)) {
                name = "action_" + action_counter;
            }
            while (usedActionNames.contains(name)) {
                name = "action_" + action_counter;
                action_counter++;
            }
            usedActionNames.add(name);
            actionNames.put(action, name);
        }
    }

    /**
     * Return the unique name for the action respecting the PRISM syntax
     * 
     * @param action the wanted action
     * @return the associated name
     */
    public static String getActionName(Action action) {
        assert action != null;
        ensure(actionNames.containsKey(action), 
                ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_UNDEFINED_USED_ACTION, 
                action.getName());

        return actionNames.get(action);
    }

    public static boolean isSilentAction(Action action) {
        assert silentAction != null;
        assert action != null;

        return silentAction.equals(action);
    }

    public static String constantsRenaming() {
        StringBuilder prism = new StringBuilder();

        for (Entry<JANIIdentifier,String> entry : identifierNames.entrySet()) {
        	JANIIdentifier identifier = entry.getKey();
        	if (identifier instanceof Constant) {
	            Constant constant = (Constant) identifier;
	            String name = entry.getValue();
	            if (! constant.getName().equals(name)) {
	                prism.append("// Original constant name: ").append(constant.getName()).append("\n")
	                    .append("// New name: ").append(name).append("\n\n");
	            }
        	}
        }

        return prism.toString();		
    }

    public static String globalVariablesRenaming() {
        StringBuilder prism = new StringBuilder();

        for (Entry<JANIIdentifier,String> entry : identifierNames.entrySet()) {
        	JANIIdentifier identifier = entry.getKey();
        	if (identifier instanceof Variable) {
	            Variable variable = (Variable) identifier;
	            if (globalVariables.contains(variable)) {
	                String name = entry.getValue();
	                if (! variable.getName().equals(name)) {
	                    prism.append("// Original variable name: ").append(variable.getName()).append("\n")
	                        .append("// New name: ").append(name).append("\n\n");
	                }
	            }
        	}
        }

        return prism.toString();		
    }

    public static String variableRenaming(Variable variable, String prefix) {
        StringBuilder prism = new StringBuilder();

        String name = identifierNames.get(variable);
        if (! variable.getName().equals(name)) {
            prism.append(prefix).append("// Original variable name: ").append(variable.getName()).append("\n")
                .append(prefix).append("// New name: ").append(name).append("\n\n");
        }

        return prism.toString();		
    }

    public static String actionsRenaming() {
        StringBuilder prism = new StringBuilder();

        for (Entry<Action,String> entry : actionNames.entrySet()) {
            Action action = entry.getKey();
            String name = entry.getValue();
            if (!action.getName().equals(name)) {
                prism.append("// Original action name: ").append(action.getName()).append("\n")
                    .append("// New name: ").append(name).append("\n\n");
            }
        }

        return prism.toString();
    }

    public static String locationRenaming(Automaton automaton) {
        assert automatonLocationIdentifier.containsKey(automaton);

        StringBuilder prism = new StringBuilder();

        if (automatonLocationIdentifier.get(automaton).size() > 1) {
            String locationName = automatonLocationName.get(automaton);
            for (Entry<Location, Integer> entry : automatonLocationIdentifier.get(automaton).entrySet()) {
                prism.append("// Original location: ").append(entry.getKey().getName()).append("\n")
                    .append("// Condition: ").append(locationName).append(" = ").append(entry.getValue()).append("\n");
            }
        }

        return prism.toString();
    }

    public static void registerInitialRestriction(InitialStates initialStates) {
        JANIComponentRegistrar.initialStates = initialStates;
        if (usesInitialConditions == null) {
            usesInitialConditions = new Boolean(initialStates != null);
        } else {
            usesInitialConditions |= initialStates != null;
        }
    }

    public static void registerInitialLocation(Automaton automaton, Collection<Location> locations) {
        assert automaton != null;
        assert locations != null;

        initialLocations.put(automaton, locations);
        if (usesInitialConditions == null) {
            usesInitialConditions = new Boolean(locations.size() > 1);
        } else {
            usesInitialConditions |= locations.size() > 1;
        }
    }

    public static boolean areInitialConditionsUsed() {
        assert usesInitialConditions != null;

        return usesInitialConditions.booleanValue();
    }

    public static String processInitialConditions() {
        assert usesInitialConditions != null;

        StringBuilder prism = new StringBuilder();

        if (usesInitialConditions) {
            prism.append("init\n")
                .append(PRISMExporter_ModelJANIProcessor.INDENT);
            
            boolean addAnd = false;

            for (Entry<Automaton, Collection<Location>> entry : initialLocations.entrySet()) {
                Automaton automaton = entry.getKey();
                if (entry.getValue().size() > 1) {
                    String locationName = JANIComponentRegistrar.getLocationName(automaton);
                    prism.append("(");
                    boolean notFirst = false;
                    for (Location location : entry.getValue()) {
                        if (notFirst) {
                            prism.append("|");
                        } else {
                            notFirst = true;
                        }
                        prism.append("(")
                            .append(locationName)
                            .append("=")
                            .append(JANIComponentRegistrar.getLocationIdentifier(automaton, location))
                            .append(")");
                    }
                    prism.append(")");
                    addAnd = true;
                }
            }
            if (initialStates != null) {
                if (addAnd) {
                    prism.append("\n")
                        .append(PRISMExporter_ModelJANIProcessor.INDENT)
                        .append("&\n")
                        .append(PRISMExporter_ModelJANIProcessor.INDENT);
                }
                String comment = initialStates.getComment();
                if (comment != null) {
                    prism.append("// ").append(comment).append("\n")
                        .append(PRISMExporter_ModelJANIProcessor.INDENT);
                }
                prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(initialStates.getExp())
                        .toPRISM());
            }	

            prism.append("\nendinit\n");
        }

        return prism.toString();
    }
}

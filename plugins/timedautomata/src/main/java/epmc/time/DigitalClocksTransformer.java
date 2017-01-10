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

package epmc.time;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatordd.EvaluatorDD;
import epmc.expression.standard.evaluatordd.UtilEvaluatorDD;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsLTS;
import epmc.graph.SemanticsMDP;
import epmc.graph.SemanticsPTA;
import epmc.graph.SemanticsTA;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.Assignments;
import epmc.jani.model.Automaton;
import epmc.jani.model.Destination;
import epmc.jani.model.Edge;
import epmc.jani.model.Edges;
import epmc.jani.model.Guard;
import epmc.jani.model.Location;
import epmc.jani.model.Locations;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.Variable;
import epmc.jani.model.Variables;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;
import epmc.jani.model.component.ComponentParallel;
import epmc.jani.model.component.ComponentRename;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.JANITypeBounded;
import epmc.value.ContextValue;
import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.Type;
import epmc.value.TypeBounded;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;

import static epmc.error.UtilError.*;
import static epmc.time.UtilTime.*;

/**
 * Class for digital clocks semantics transformation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class DigitalClocksTransformer {
	/** Name of automaton responsible for incrementing global clocks during tick. */
	private final static String GLOBAL_VARIABLE_TICK_AUTOMATON_NAME = "global-variable-tick-automaton-name";
	/** Name of location of {@link #GLOBAL_VARIABLE_TICK_AUTOMATON_NAME}. */
	private final static String GLOBAL_VARIABLE_TICK_AUTOMATON_LOCATION = "location";
	/** Base name for tick action. */
	private final static String TICK = "tick";
	/** String containing a single underscore. */
	private final static String UNDERSCORE = "_";
	
	/** JANI model to be transformed. */
	private ModelJANI model;

	/**
	 * Set the model to be transformed.
	 * 
	 * @param model model to be transformed
	 */
	public void setModel(ModelJANI model) {
		this.model = model;
	}

	public boolean isApplicable() throws EPMCException {
		assert model != null;
		Semantics semantics = model.getSemantics();
		if (!SemanticsTA.isTA(semantics) && !SemanticsPTA.isPTA(semantics)) {
			return false;
		}
		if (!isClockUsageValidForDigitalClocks(model, model.getInitialStatesExpressionOrTrue())) {
			return false;
		}
		for (Automaton automaton : model.getAutomata()) {
			for (Location location : automaton.getLocations()) {
				Expression timeProgress = location.getTimeProgressExpressionOrTrue();
				if (!isClockUsageValidForDigitalClocks(model, timeProgress)) {
					return false;
				}
			}
			for (Edge edge : automaton.getEdges()) {
				if (!isClockUsageValidForDigitalClocks(model, edge.getGuardExpressionOrTrue())) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void transform() throws EPMCException {
		assert model != null;
		assert isApplicable();
		applyGCDTransform();
		replaceSemantics();
		Map<Expression, Integer> maxClockConstraints = prepareMaxClockConstraintsMap();
		Action tick = computeTickAction();
		modifyActions(tick);
		modifySystem(tick);
		modifyAutomata(maxClockConstraints, tick);
		addGlobalVariableTickAutomaton(maxClockConstraints, tick);
		modifyRewards();
		modifyProperties();
		replaceTypes(maxClockConstraints);
	}

	private void applyGCDTransform() {
		// TODO Auto-generated method stub
		
	}

	private void addGlobalVariableTickAutomaton(Map<Expression, Integer> maxClockConstraints, Action tick) throws EPMCException {
		Automaton automaton = new Automaton();
		automaton.setModel(model);
		automaton.setName(GLOBAL_VARIABLE_TICK_AUTOMATON_NAME);
		Locations locations = new Locations();
		locations.setModel(model);
		Location location = new Location();
		location.setModel(model);
		location.setName(GLOBAL_VARIABLE_TICK_AUTOMATON_LOCATION);
		locations.add(location);
		automaton.setLocations(locations);
		automaton.setInitialLocations(Collections.singleton(location));
		Edges edges = new Edges();
		Iterable<Variable> clocks = Iterables.filter(model.getGlobalVariablesOrEmpty(),
				(Variable variable) -> maxClockConstraints.containsKey(variable.getIdentifier()));
		Edge edge = computeTickEdge(location, tick, clocks, maxClockConstraints);
		edges.addEdge(edge);
		automaton.setEdges(edges);
		model.getAutomata().addAutomaton(automaton);
		
		Component origSystem = model.getSystem();
		ComponentParallel newSystem = new ComponentParallel();
		newSystem.setModel(model);
		newSystem.addAction(tick);
		newSystem.setLeft(origSystem);
		ComponentAutomaton compRewAut = new ComponentAutomaton();
		compRewAut.setModel(model);
		compRewAut.setAutomaton(automaton);
		newSystem.setRight(compRewAut);
		model.setSystem(newSystem);
	}

	private void modifyRewards() {
		// TODO what to do for rewards?
	}

	private void modifyProperties() {
		// TODO modify properties
	}

	private void modifyAutomata(Map<Expression, Integer> maxClockConstraints,
			Action tick) throws EPMCException {
		for (Automaton automaton : model.getAutomata()) {
			Iterable<Variable> clocks = Iterables.filter(automaton.getVariablesOrEmpty(),
					(Variable variable) -> maxClockConstraints.containsKey(variable.getIdentifier()));
			for (Location location : automaton.getLocations()) {
				Edge edge = computeTickEdge(location, tick, clocks, maxClockConstraints);
				automaton.getEdges().addEdge(edge);
			}
		}
	}

	private Edge computeTickEdge(Location location, Action tick, Iterable<Variable> clocks, Map<Expression, Integer> maxClockConstraints) throws EPMCException {
		Edge edge = new Edge();
		edge.setModel(model);
		edge.setAction(tick);
		edge.setLocation(location);
		Expression timeProgress = location.getTimeProgressExpressionOrTrue();
		location.setTimeProgress(null);
		timeProgress = model.replaceConstants(timeProgress);
		timeProgress = fixTimeProgressForTick(model, timeProgress);
		if (!isTrue(timeProgress)) {
			Guard guard = new Guard();
			guard.setModel(model);
			guard.setExp(timeProgress);
			edge.setGuard(guard);
		}
		Destination destination = new Destination();
		destination.setModel(model);
		Assignments assignments = new Assignments();
		assignments.setModel(model);
		for (Variable clock : clocks) {
			Expression incrementClock = incrementClock(clock, maxClockConstraints);
			AssignmentSimple assignment = new AssignmentSimple();
			assignment.setModel(model);
			assignment.setRef(clock);
			assignment.setValue(incrementClock);
			assignments.addAssignment(assignment);
		}
		destination.setLocation(location);
		destination.setAssignments(assignments);
		edge.getDestinations().addDestination(destination);
		return edge;
	}

	private Expression incrementClock(Variable clock, Map<Expression, Integer> maxClockConstraints) {
		int bound = maxClockConstraints.get(clock.getIdentifier());
		ContextValue context = clock.getModel().getContextValue();
		return UtilExpressionStandard.opMin(context, bound + 1, UtilExpressionStandard.opAdd(context, clock.getIdentifier(), 1));
	}

	private void modifySystem(Action tick) throws EPMCException {
		Component component = model.getSystem();
		modifySystem(component, tick);
	}

	private void modifySystem(Component component, Action tick) throws EPMCException {
		if (component instanceof ComponentAutomaton) {
			return;
		} else if (component instanceof ComponentParallel) {
			ComponentParallel parallel = (ComponentParallel) component;
			parallel.addAction(tick);
			modifySystem(parallel.getLeft(), tick);
			modifySystem(parallel.getRight(), tick);
		} else if (component instanceof ComponentRename) {
			ComponentRename rename = (ComponentRename) component;
			modifySystem(rename, tick);
		} else {
			fail(ProblemsJANITime.JANI_TIME_UNKNOWN_COMPONENT);
		}
	}

	private void modifyActions(Action tick) {
		Actions actions = model.getActionsOrEmpty();
		actions.addAction(tick);
		model.setActions(actions);
	}

	private Action computeTickAction() {
		Action tick = new Action();
		tick.setModel(model);
		String tickName = TICK;
		if (model.getActions() != null) {
			Actions actions = model.getActions();
			while (actions.containsKey(tickName)) {
				tickName += UNDERSCORE;
			}
		}
		tick.setName(tickName);
		return tick;
	}

	private void replaceSemantics() throws EPMCException {
		Semantics semantics = model.getSemantics();
		if (SemanticsTA.isTA(semantics)) {
			semantics = SemanticsLTS.LTS;
		} else if (SemanticsPTA.isPTA(semantics)) {
			semantics = SemanticsMDP.MDP;
		} else {
			assert false;
		}
		model.setSemantics(semantics.toString());
	}

	private void replaceTypes(Map<Expression, Integer> maxClockConstraints)
			throws EPMCException {
		for (Variable variable : model.getGlobalVariablesOrEmpty()) {
			if (variable.getType() instanceof JANITypeClock) {
				int upper = maxClockConstraints.get(variable.getIdentifier());
				JANITypeBounded typeBoundedInt = new JANITypeBounded();
				typeBoundedInt.setModel(model);
				typeBoundedInt.setContextValue(getContextValue());
				TypeInteger typeInteger = TypeInteger.get(getContextValue());
				typeBoundedInt.setLowerBound(new ExpressionLiteral.Builder()
						.setValue(typeInteger.getZero())
						.build());
				typeBoundedInt.setUpperBound(new ExpressionLiteral.Builder()
						.setValue(UtilValue.newValue(typeInteger, upper + 1))
						.build());
				variable.setType(typeBoundedInt);
//				variable.getIdentifier().getContext().registerType(variable.getIdentifier(), typeBoundedInt.toType());
			}
		}
		for (Automaton automaton : model.getAutomata()) {
			for (Variable variable : automaton.getVariablesOrEmpty()) {
				if (variable.getType() instanceof JANITypeClock) {
					int upper = maxClockConstraints.get(variable.getIdentifier());
					JANITypeBounded typeBoundedInt = new JANITypeBounded();
					typeBoundedInt.setModel(model);
					typeBoundedInt.setContextValue(getContextValue());
					TypeInteger typeInteger = TypeInteger.get(getContextValue());
					typeBoundedInt.setLowerBound(new ExpressionLiteral.Builder()
							.setValue(typeInteger.getZero())
							.build());
					typeBoundedInt.setUpperBound(new ExpressionLiteral.Builder()
							.setValue(UtilValue.newValue(typeInteger, upper + 1))
							.build());
					variable.setType(typeBoundedInt);
//					variable.getIdentifier().getContext().registerType(variable.getIdentifier(), typeBoundedInt.toType());
				}
			}
		}
	}
	
	private Map<Expression, Integer> prepareMaxClockConstraintsMap() throws EPMCException {
		Map<Expression, Integer> maxClockConstraints = new LinkedHashMap<>();
		addVariablesToMaxClockConstraintsMap(maxClockConstraints,
				model.getGlobalVariablesOrEmpty());
		for (Automaton automaton : model.getAutomata()) {
			addVariablesToMaxClockConstraintsMap(maxClockConstraints,
					automaton.getVariablesOrEmpty());
		}

		ContextValue context = model.getContextValue();
		Expression initialModelExpression = model.getInitialStatesExpressionOrTrue();
		Expression boundsModel = UtilModelParser.restrictToVariableRange(getContextValue(), model.getGlobalVariablesOrEmpty());
		initialModelExpression = UtilExpressionStandard.opAnd(context, initialModelExpression, boundsModel);
		initialModelExpression = model.replaceConstants(initialModelExpression);
		computeMaxClockConstraints(initialModelExpression, maxClockConstraints);
		
		for (Automaton automaton : model.getAutomata()) {
			Expression initialStates = automaton.getInitialStatesExpressionOrTrue();
			Expression bounds = UtilModelParser.restrictToVariableRange(getContextValue(), automaton.getVariablesOrEmpty());
			initialStates = UtilExpressionStandard.opAnd(context, initialStates, bounds);
			initialStates = model.replaceConstants(initialStates);
			computeMaxClockConstraints(initialStates, maxClockConstraints);
			for (Location location : automaton.getLocations()) {
				Expression timeProgress = location.getTimeProgressExpressionOrTrue();
				timeProgress = model.replaceConstants(timeProgress);
				timeProgress = fixTimeProgressForTick(model, timeProgress);
				computeMaxClockConstraints(timeProgress, maxClockConstraints);
			}
			for (Edge edge : automaton.getEdges()) {
				Expression guard = edge.getGuardExpressionOrTrue();
				guard = model.replaceConstants(guard);
				computeMaxClockConstraints(guard, maxClockConstraints);
				for (Destination destination : edge.getDestinations()) {
					for (AssignmentSimple entry : destination.getAssignments()) {
						if (!(entry.getRef().getIdentifier().getType(model) instanceof TypeClock)) {
							continue;
						}
						updateMaxClockConstraints(maxClockConstraints,
								entry.getRef().getIdentifier(),
								computeMaxBound(entry.getValue()));
					}
				}
			}
		}

		return maxClockConstraints;
	}

	private void updateMaxClockConstraints(Map<Expression, Integer> maxClockConstraints,
			Expression identifier,
			int newBound) {
		int oldBound = maxClockConstraints.get(identifier);
		int bound = Math.max(newBound, oldBound);
		maxClockConstraints.put(identifier, bound);
	}

	private void addVariablesToMaxClockConstraintsMap(Map<Expression, Integer> maximalClockConstraints,
			Variables variables) {
		for (Variable variable : variables) {
			JANIType type = variable.getType();
			if (!(type instanceof JANITypeClock)) {
				continue;
			}
			maximalClockConstraints.put(variable.getIdentifier(), 0);
		}
	}

	private void computeMaxClockConstraints(Expression expression,
			Map<Expression, Integer> maxClockConstraints) throws EPMCException {
		if (isCmp(expression)) {
			ExpressionOperator expressionOperator = (ExpressionOperator) expression;
			Expression left = expressionOperator.getOperand1();
			Expression right = expressionOperator.getOperand2();
			if (isClock(model, left) && isClock(model, right)) {
				return;
			} else if (isClock(model, left)) {
				int bound = computeMaxBound(right);
				bound = Math.max(bound, maxClockConstraints.get(left));
				maxClockConstraints.put(left, bound);
			} else if (isClock(model, right)) {
				int bound = computeMaxBound(left);
				bound = Math.max(bound, maxClockConstraints.get(right));
				maxClockConstraints.put(right, bound);
			}
		} else {
			for (Expression sub : expression.getChildren()) {
				computeMaxClockConstraints(sub, maxClockConstraints);
			}
		}
	}

	private int computeMaxBound(Expression expression) throws EPMCException {
		assert expression != null;
		Bounds bounds = computeBounds(expression);
		ensure(ValueInteger.isInteger(bounds.upper),
				ProblemsJANITime.JANI_TIME_NON_INTEGER_CLOCK_COMPARISON);
		return ValueInteger.asInteger(bounds.upper).getInt();
	}

	private Bounds computeBounds(Expression expression) throws EPMCException {
		assert expression != null;
		if (UtilExpressionStandard.collectIdentifiers(expression).isEmpty()) {
			Value value = evaluateValue(model, expression);
			return new Bounds(value);
		} else if (expression instanceof ExpressionIdentifier) {
			Type type = expression.getType(model);
			return new Bounds(TypeBounded.getLower(type), TypeBounded.getUpper(type));
		} else if (isAllIdentifersBounded(expression)) {
			return computeDDBounds(expression);
		} else {
			// TODO might later add some special cases for unbounded variables
			assert false : expression; // TODO exception
			return null;
		}
		
	}

	private Bounds computeDDBounds(Expression expression) throws EPMCException {
		assert expression != null;
		Set<Expression> identifiers = UtilExpressionStandard.collectIdentifiers(expression);
		Map<Expression, VariableDD> variables = new LinkedHashMap<>();
		DD validValues = getContextDD().newConstant(true);
		for (Expression identifier : identifiers) {
			ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) identifier;
			String name = expressionIdentifier.getName();
			Type type = identifier.getType(model);
			VariableDD variable = getContextDD().newVariable(name, type, 0);
			validValues = validValues.andWith(variable.newValidValues(0));
			variables.put(identifier, variable);
		}
		ContextValue context = model.getContextValue();
		EvaluatorDD evaluator = UtilEvaluatorDD.newEvaluator(context, expression, variables);
		DD dd = evaluator.getDD().clone();
		evaluator.close();
		Value max = dd.maxOverSat(validValues);
		Value min = dd.minOverSat(validValues);
		dd.dispose();
		validValues.dispose();
		return new Bounds(min, max);
	}

	private boolean isAllIdentifersBounded(Expression expression) throws EPMCException {
		assert expression != null;
		Set<Expression> identifiers = UtilExpressionStandard.collectIdentifiers(expression);
		for (Expression identifier : identifiers) {
			Type type = identifier.getType(model);
			if (type == null) {
				return false;
			}
			if (!TypeInteger.isIntegerBothBounded(type)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isCmp(Expression expression) {
		assert expression != null;
		if (isLe(expression)) {
			return true;
		}
		if (isLt(expression)) {
			return true;
		}
		if (isEq(expression)) {
			return true;
		}
		if (isGe(expression)) {
			return true;
		}
		if (isGt(expression)) {
			return true;
		}
		return false;
	}
	
	/*
	private static Bounds widen(Bounds left, Bounds right) {
		assert left != null;
		assert right != null;
	}
	*/
	
	private ContextValue getContextValue() {
		return model.getContextValue();
	}

	private ContextDD getContextDD() throws EPMCException {
		return ContextDD.get(model.getContextValue());
	}
	
    private static Value evaluateValue(ModelJANI model, Expression expression) throws EPMCException {
        assert expression != null;
        EvaluatorExplicit evaluator = UtilEvaluatorExplicit.newEvaluator(expression, model, new Expression[0]);
        return evaluator.evaluate();
    }
    
    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isTrue(expressionLiteral.getValue());
    }
    
    private static boolean isLe(Expression expression) {
    	assert expression != null;
    	if (!(expression instanceof ExpressionOperator)) {
    		return false;
    	}
    	ExpressionOperator expressionOperator = (ExpressionOperator) expression;
    	return expressionOperator
    			.getOperator()
    			.getIdentifier()
    			.equals(OperatorLe.IDENTIFIER);
    }
    
    private static boolean isGe(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator
                .getOperator()
                .getIdentifier()
                .equals(OperatorGe.IDENTIFIER);
    }
    
    private static boolean isGt(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator
                .getOperator()
                .getIdentifier()
                .equals(OperatorGt.IDENTIFIER);
    }
    
    private static boolean isLt(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator
                .getOperator()
                .getIdentifier()
                .equals(OperatorLt.IDENTIFIER);
    }
    
    private static boolean isEq(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator
                .getOperator()
                .getIdentifier()
                .equals(OperatorEq.IDENTIFIER);
    }
}

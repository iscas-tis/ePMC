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

package epmc.jani.dd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.Automaton;
import epmc.jani.model.Destination;
import epmc.jani.model.Edge;
import epmc.jani.model.Location;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.Variable;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;
import epmc.jani.value.TypeLocation;
import epmc.jani.value.ValueLocation;
import epmc.value.Value;

/**
 * DD-based symbolic representation of a {@link ComponentAutomaton}.
 * 
 * @author Ernst Moritz Hahn
 */
final class DDComponentAutomaton implements DDComponent {
    /** Whether DD component was already closed and cannot be used further. */
    private boolean closed;
    /** Encoding marker of present state variables. */
    private final static int PRES_STATE = 0;
    /** Encoding marker of next state variables. */
    private final static int NEXT_STATE = 1;

    /** Graph to which this DD component belongs. */
    private GraphDDJANI graph;
    /** Component which this DD component represents. */
    private ComponentAutomaton component;
    /** Automaton of the automaton component. */
    private Automaton automaton;
    /** Type representing the locations of this automaton. */
    private TypeLocation typeLocation;
    /** Variable DD representing the locations of this automaton. */
    private VariableDD locationVariableDD;
    /** List of local variable DDs and automaton location variable. */
    private Set<VariableDD> localVariableDDs = new LinkedHashSet<>();
    /** Unmodifiable list of local variable DDs and automaton location variable. */
    private Set<VariableDD> localVariableDDsExternal = Collections.unmodifiableSet(localVariableDDs);
    /** List of local variables. */
    private List<Variable> localVariables = new ArrayList<>();
    /** Maps local and global variables to DD representations of them. */
    private Map<Variable,VariableDD> variableToDD = new LinkedHashMap<>();
    /** Maps identifiers to variable DD representations. */
    private Map<Expression,VariableDD> identifierToDD = new LinkedHashMap<>();
    /** Maps variables to identifiers. */
    private Map<Variable,Expression> variableToIdentifier = new LinkedHashMap<>();
    /** List of symbolic transitions from edges of the automaton. */
    private List<DDTransition> transitions = new ArrayList<>();
    /** Unmodifiable list of symbolic transitions from edges of the automaton. */
    private List<DDTransition> transitionsExternal = Collections.unmodifiableList(transitions);
    /** Initial nodes of the automaton. */
    private DD initialNodes;
    /** Present state cube of the component. */
    private DD presCube;
    /** Next state cube of the component. */
    private DD nextCube;

    @Override
    public void setGraph(GraphDDJANI graph) {
        assert this.graph == null;
        assert graph != null;
        this.graph = graph;
    }

    @Override
    public void setComponent(Component component) {
        assert this.component == null;
        assert component != null;
        assert component instanceof ComponentAutomaton;
        this.component = (ComponentAutomaton) component;
    }

    @Override
    public void build() {
        assert graph != null;
        assert component != null;

        automaton = component.getAutomaton();
        buildVariables();
        buildCubes();
        try (ExpressionToDD expressionToDD = new ExpressionToDD(identifierToDD)) {
            buildTransitions(expressionToDD);
            computeInitialNodes(expressionToDD);
        }
    }

    /**
     * Build present and next state cubes of the variables of the automaton.
     * This includes the local variables and the variables to include the
     * location information of the automaton. It does not include global
     * variables.
     * 
     */
    private void buildCubes() {
        presCube = getContextDD().newConstant(true);
        for (VariableDD variable : localVariableDDs) {
            presCube = presCube.andWith(variable.newCube(PRES_STATE));
        }
        nextCube = getContextDD().newConstant(true);
        for (VariableDD variable : localVariableDDs) {
            nextCube = nextCube.andWith(variable.newCube(NEXT_STATE));
        }
    }

    /**
     * Prepare variables and types for DD representation of the automaton.
     * 
     */
    private void buildVariables() {
        typeLocation = TypeLocation.get(automaton.getLocations());
        locationVariableDD = getContextDD().newVariable(automaton.getLocations().toString(), typeLocation, 2);
        identifierToDD.putAll(graph.getGlobalIdentifiersToDD());
        variableToDD.putAll(graph.getGlobalVariablesToDD());
        variableToIdentifier.putAll(graph.getGlobalVariableToIdentifier());
        for (Variable variable : automaton.getVariablesOrEmpty()) {
            VariableDD variableDD = getContextDD().newVariable(variable.getName(), variable.toType(), 2);
            localVariableDDs.add(variableDD);
            localVariables.add(variable);
            variableToDD.put(variable, variableDD);
            identifierToDD.put(variable.getIdentifier(), variableDD);
            variableToIdentifier.put(variable, variable.getIdentifier());
        }
        localVariableDDs.add(locationVariableDD);
    }

    /**
     * Translates all edges of the automaton to symbolic transitions.
     * The parameter may not be {@code null}.
     * 
     * @param expressionToDD expression to DD helper to apply this method
     */
    private void buildTransitions(ExpressionToDD expressionToDD) {        
        assert expressionToDD != null;
        for (Edge edge : automaton.getEdges()) {
            DDTransition transition = translateEdge(edge, expressionToDD);
            if (transition.getGuard().isFalse()) {
                transition.close();
            } else {
                transitions.add(transition);
            }
        }
    }

    /**
     * Translate a single automaton edge to a {@link DDTransition}.
     * None of the parameter may be {@code null}.
     * 
     * @param edge edge to translate
     * @param expressionToDD expression to DD object
     * @return translated edge
     */
    private DDTransition translateEdge(Edge edge, ExpressionToDD expressionToDD) {
        assert edge != null;
        assert expressionToDD != null;
        DDTransition transition = new DDTransition();
        transition.setAction(edge.getActionOrSilent());
        Expression guardExpr = edge.getGuardExpressionOrTrue();
        guardExpr = edge.getModel().replaceConstants(guardExpr);
        DD guard = expressionToDD.translate(guardExpr);
        DD locationDD = locationToDD(edge.getLocation(), PRES_STATE);
        guard = guard.andWith(locationDD);
        transition.setGuard(guard);
        DD transitionsDD = getContextDD().newConstant(0);

        Set<VariableDD> edgeWrites = computeWrites(edge);
        /* translate the different possible destinations of the edge */
        Set<VariableValid> variableValid = new LinkedHashSet<>();
        for (Destination destination : edge.getDestinations()) {
            DD destinationDD = buildDestinationDD(destination, edgeWrites, expressionToDD);
            transitionsDD = transitionsDD.addWith(destinationDD);
            Set<VariableValid> varValid = buildDestinationVariableValid(destination, guard, expressionToDD);
            variableValid.addAll(varValid);
        }
        transitionsDD = transitionsDD.multiplyWith(guard.clone().toMTWith());
        transition.setTransitions(transitionsDD);
        transition.setWrites(edgeWrites);
        transition.setVariableValid(variableValid);
        return transition;
    }

    private Set<VariableValid> buildDestinationVariableValid(Destination destination, DD guard, ExpressionToDD expressionToDD) {
        Set<VariableValid> result = new LinkedHashSet<>();
        for (AssignmentSimple assg : destination.getAssignmentsOrEmpty()) {
            Variable variable = assg.getRef();
            VariableDD variableDD = variableToDD.get(variable);
            Expression assignment = assg.getValue();
            assignment = destination.getModel().replaceConstants(assignment);
            DD assignmentDD = expressionToDD.assign(variableDD, NEXT_STATE, assignment);
            DD validDD = assignmentDD.clone().andWith(variableDD.newValidValues(NEXT_STATE)).abstractExistWith(variableDD.newCube(NEXT_STATE));
            validDD = guard.clone().impliesWith(validDD);
            VariableValid valid = new VariableValid();
            valid.setValid(validDD);
            valid.setVariable(variableDD);
            result.add(valid);
        }
        //		AT: there are no transient/observable assignment in the JANI specification
        // TODO observables
        return result;
    }

    /**
     * Translate given edge destination to symbolic representation.
     * The translated DD will
     * <ul>
     * <li>make sure it can only execute if guard is valid,</li>
     * <li>change the location of the automaton as specified,</li>
     * <li>assign next-state local and global variable values according to the
     * assignments of the destination,</li>
     * <li>set all next-state values of variables which are written in some
     * other destination of the edge but this one to the present-state value
     * of the variable,</li>
     * <li>already be weighted with its probability.</li>
     * </ul>
     * None of the parameter may be {@code null} or contain {@code null}
     * entries.
     * 
     * @param destination destination to translate
     * @param edgeWrites variables written by the edge of the destination
     * @param expressionToDD used to translate expressions
     * @return symbolic representation of edge destination
     */
    private DD buildDestinationDD(Destination destination, Set<VariableDD> edgeWrites, ExpressionToDD expressionToDD) {
        assert destination != null;
        assert edgeWrites != null;
        for (VariableDD variable : edgeWrites) {
            assert variable != null;
        }
        assert expressionToDD != null;

        /* Handle all writes of the given destination. */
        DD destinationDD = getContextDD().newConstant(true);
        DD locationDD = locationToDD(destination.getLocation(), NEXT_STATE);
        destinationDD = destinationDD.andWith(locationDD);
        for (AssignmentSimple entry : destination.getAssignmentsOrEmpty()) {
            Variable variable = entry.getRef();
            VariableDD variableDD = variableToDD.get(variable);
            Expression assignment = entry.getValue();
            assignment = destination.getModel().replaceConstants(assignment);
            DD assignmentDD = expressionToDD.assign(variableDD, NEXT_STATE, assignment);
            destinationDD = destinationDD.andWith(assignmentDD);
        }
        // TODO observables

        /* Ensure that values not written do not change. This affects any
         * global variable written by one but not all destinations of the edge.
         */
        Set<VariableDD> destinationWrites = computeDestinationWrites(destination);
        for (VariableDD variable : edgeWrites) {
            if (destinationWrites.contains(variable)) {
                continue;
            }
            DD assignmentDD = variable.newEqCopies(PRES_STATE, NEXT_STATE);
            destinationDD = destinationDD.andWith(assignmentDD);
        }
        Expression probabilityExpr = destination.getProbabilityExpressionOrOne();
        probabilityExpr = destination.getModel().replaceConstants(probabilityExpr);
        DD probabilityDD = expressionToDD.translate(probabilityExpr);
        destinationDD = destinationDD.toMTWith().multiply(probabilityDD);
        return destinationDD;
    }

    /**
     * Obtain symbolic representation of the given location.
     * The transformation is performed using the location type of the automaton.
     * This function is intended to be used to construct DDs for guards of
     * edges, or to encode the change of the current location.
     * The location parameter must not be {@code null}. The copy paramter must
     * be either 0 or one.
     * 
     * @param location location to obtain symbolic representation of
     * @param copy instance of DD variables to use for encoding
     * @return symbolic representation of the given location
     */
    private DD locationToDD(Location location, int copy) {
        assert location != null;
        assert copy >= 0;
        assert copy < 2;
        ValueLocation locationValue = typeLocation.newValue(location);
        DD locationDD = locationVariableDD.newVariableValue(copy, locationValue);
        return locationDD;
    }

    /**
     * Compute the set of variables which a destination writes to.
     * This includes both global and automaton-local variables.
     * The parameter of the function may not be {@code null}.
     * 
     * @param destination destination to compute written variables of
     * @return variables written by destination
     */
    private Set<VariableDD> computeDestinationWrites(Destination destination) {
        assert destination != null;
        Set<VariableDD> destinationWrites = new LinkedHashSet<>();
        for (AssignmentSimple assg : destination.getAssignmentsOrEmpty()) {
            Variable written = assg.getRef();
            VariableDD variable = variableToDD.get(written);
            destinationWrites.add(variable);
        }
        // TODO observables
        destinationWrites.add(locationVariableDD);
        return destinationWrites;
    }

    /**
     * Compute the set of variables written by an edge.
     * 
     * @param edge edge to compute set of
     * @return set of variables written by an edge
     */
    private Set<VariableDD> computeWrites(Edge edge) {
        assert edge != null;
        Set<VariableDD> writes = new LinkedHashSet<>();
        for (Destination destination : edge.getDestinations()) {
            for (AssignmentSimple assg : destination.getAssignmentsOrEmpty()) {
                Variable written = assg.getRef();
                VariableDD variable = variableToDD.get(written);
                writes.add(variable);
            }
        }
        // TODO observables
        writes.add(locationVariableDD);
        return writes;
    }

    /**
     * Compute initial nodes of this DD automaton component.
     * The computed DD assigns each local variable of the automaton its initial
     * value. The expression to DD parameter must not be {@code null}.
     * 
     * @param expressionToDD expression to DD helper
     */
    private void computeInitialNodes(ExpressionToDD expressionToDD) {
        assert expressionToDD != null;
        Set<Location> initialLocations = automaton.getInitialLocations();
        ContextDD contextDD = ContextDD.get();
        DD initialNodes = contextDD.newConstant(true);
        for (Location initialLocation : initialLocations) {
            Value initialLocValue = typeLocation.newValue(initialLocation);			
            initialNodes = initialNodes.andWith(locationVariableDD.newVariableValue(PRES_STATE, initialLocValue));
        }
        Expression initialExpression = automaton.getInitialStatesExpressionOrTrue();
        Expression bounds = UtilModelParser.restrictToVariableRange(automaton.getVariablesOrEmpty());
        initialExpression = UtilExpressionStandard.opAnd(initialExpression, bounds);
        initialExpression = automaton.getModel().replaceConstants(initialExpression);
        initialNodes = initialNodes.andWith(expressionToDD.translate(initialExpression));
        this.initialNodes = initialNodes;
    }

    @Override
    public List<DDTransition> getTransitions() {
        return transitionsExternal;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (VariableDD variableDD : localVariableDDs) {
            variableDD.close();
        }
        for (DDTransition transition : transitions) {
            transition.close();
        }
        presCube.dispose();
        nextCube.dispose();
        initialNodes.dispose();		
    }

    /**
     * Get DD context used for this DD component.
     * The context is derived from the graph the DD component belongs to.
     * 
     * @return DD context used for this DD component
     */
    private ContextDD getContextDD() {
        return graph.getContextDD();
    }

    @Override
    public DD getInitialNodes() {
        return initialNodes;
    }

    @Override
    public DD getPresCube() {
        return presCube;
    }

    @Override
    public DD getNextCube() {
        return nextCube;
    }

    @Override
    public Set<VariableDD> getVariables() {
        return localVariableDDsExternal;
    }
}

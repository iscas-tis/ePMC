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

package epmc.jani.model;

import static epmc.error.UtilError.ensure;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expressionevaluator.ExpressionToType;
import epmc.util.UtilJSON;
import epmc.value.Type;

/**
 * Class representing an automaton of the model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Automaton implements JANINode, ExpressionToType {
    /** String identifying the name of an automaton. */
    private final static String NAME = "name";
    /** String identifying the variables of an automaton. */
    private final static String VARIABLES = "variables";
    /** String identifying the locations of an automaton. */
    private final static String LOCATIONS = "locations";
    /** String identifying the initial location of an automaton. */
    private final static String INITIAL_LOCATIONS = "initial-locations";
    /** String identifying the edges of an automaton. */
    private final static String EDGES = "edges";
    /** String identifying comment for this automaton. */
    private final static String COMMENT = "comment";
    /** String identifying initial variable values of this automaton. */
    private final static String RESTRICT_INITIAL = "restrict-initial";

    /** Name of the automaton. */
    private String name;
    /** Local variables of the automaton. */
    private Variables variables;
    /** Locations of the automaton. */
    private Locations locations = new Locations();
    /** Initial locations of the automaton. */
    private Set<Location> initialLocations;
    /** Edges of the automaton. */
    private Edges edges = new Edges();
    /** Model of which this automaton is part of. */
    private ModelJANI model;
    /** Comment for this automaton. */
    private String comment;
    /** Initial values for variables of this automaton. */
    private InitialStates restrictInitial;
    // TODO document
    private int number;

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert model != null;
        assert value != null;
        JsonObject object = UtilJSON.toObject(value);
        name = UtilJSON.getString(object, NAME);
        variables = UtilModelParser.parseOptional(model, () -> {
            Variables variables = new Variables();
            variables.setModel(model);
            variables.setAutomaton(this);
            return variables;
        }, object, VARIABLES);
        Variables globalVariables = model.getGlobalVariablesOrEmpty();
        if (variables != null) {
            for (String variable : variables.getVariables().keySet()) {
                ensure(!globalVariables.containsKey(variable),
                        ProblemsJANIParser.JANI_PARSER_VARIABLE_SHADOWS_GLOBAL, variable);
            }
            Constants constants = model.getModelConstantsOrEmpty();
            for (String variable : variables.getVariables().keySet()) {
                ensure(!constants.getConstants().containsKey(variable),
                        ProblemsJANIParser.JANI_PARSER_VARIABLE_SHADOWS_CONSTANT,
                        variable);
            }
        }
        Map<String,JANIIdentifier> validIdentifiers = new LinkedHashMap<>();
        validIdentifiers.putAll(globalVariables);
        validIdentifiers.putAll(model.getModelConstantsOrEmpty().getConstants());
        if (variables != null) {
            validIdentifiers.putAll(variables.getVariables());
        }
        locations.setValidIdentifiers(validIdentifiers);
        UtilModelParser.parse(model, locations, object, LOCATIONS);
        this.initialLocations = UtilJSON.toSubsetOf(object, INITIAL_LOCATIONS, locations.getLocations());
        edges.setValidIdentifiers(validIdentifiers);
        edges.setValidLocations(locations.getLocations());

        UtilModelParser.parse(model, edges, object, EDGES);
        comment = UtilJSON.getStringOrNull(object, COMMENT);
        restrictInitial = UtilModelParser.parseOptional(model, () -> {
            InitialStates initialStates = new InitialStates();
            initialStates.setModel(model);
            initialStates.setIdentifier(variables != null ?
                    variables : Collections.emptyMap());
            return initialStates;
        }, object, RESTRICT_INITIAL);
        // AT: removed the following part as RESTRICT_INITIAL is optional; 
        // moreover there is no requirement about global variables being present when
        // RESTRICT_INITIAL is present
        //    	ensure((variables == null) == (restrictInitial == null),
        //    			ProblemsJANIParser.JANI_PARSER_AUTOMATON_VARIABLES_INITIAL_STATES);
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public InitialStates getInitialStates() {
        return restrictInitial;
    }

    public Expression getInitialStatesExpressionOrTrue() {
        Expression initial;
        if (restrictInitial == null) {
            initial = ExpressionLiteral.getTrue();
        } else {
            initial = restrictInitial.getExp();
        }
        for (Variable variable : getVariablesOrEmpty()) {
            Expression varInitValue = variable.getInitialValueOrNull();
            if (varInitValue == null || variable.isTransient()) {
                continue;
            }
            Expression varInit = UtilExpressionStandard.opEq(
                    variable.getIdentifier(),
                    varInitValue);
            initial = UtilExpressionStandard.opAnd(initial, varInit);
        }
        return initial;
    }

    public void setInitialStates(InitialStates initialStates) {
        this.restrictInitial = initialStates;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(NAME, name);
        UtilModelParser.addOptional(result, VARIABLES, variables);
        result.add(LOCATIONS, locations.generate());
        JsonArrayBuilder initialLocations = Json.createArrayBuilder();
        for (Location initialLocation : this.initialLocations) {
            initialLocations.add(initialLocation.getName());
        }
        result.add(INITIAL_LOCATIONS, initialLocations);
        result.add(EDGES, edges.generate());
        UtilModelParser.addOptional(result, RESTRICT_INITIAL, restrictInitial);
        UtilJSON.addOptional(result, COMMENT, comment);
        return result.build();
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtain the name of the automaton.
     * This method may only be called after parsing.
     * 
     * @return name of the automaton
     */
    public String getName() {
        return name;
    }

    /**
     * Obtain the variables of the automaton.
     * This method may only be called after parsing.
     * 
     * @return variables of the automaton
     */
    public Variables getVariablesOrEmpty() {
        Variables result;
        if (variables == null) {
            result = new Variables();
            result.setAutomaton(this);
            result.setModel(model);
        } else {
            result = variables;
        }
        return result;
    }

    public Variables getVariablesNonTransient() {
        Variables result = new Variables();
        for (Variable variable : getVariablesOrEmpty()) {
            if (!variable.isTransient()) {
                result.addVariable(variable);
            }
        }
        return result;
    }

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    /**
     * Obtain the locations of the automaton.
     * This method may only be called after parsing.
     * 
     * @return locations of the automaton
     */
    public Locations getLocations() {
        return locations;
    }

    public void setLocations(Locations locations) {
        this.locations = locations;
    }

    public void setInitialLocations(Set<Location> initialLocations) {
        this.initialLocations = initialLocations;
    }

    /**
     * Obtain the initial locations of the automaton.
     * 
     * @return initial location of the automaton
     */
    public Set<Location> getInitialLocations() {
        return initialLocations;
    }

    /**
     * Obtain the edges of the automaton.
     * This method may only be called after parsing.
     * 
     * @return edges of the automaton
     */
    public Edges getEdges() {
        return edges;
    }

    public void setEdges(Edges edges) {
        this.edges = edges;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public Type getType(Expression expression) {
        assert expression != null;
        if (variables != null) {
            return variables.getType(expression);
        }
        return null;
    }	
}

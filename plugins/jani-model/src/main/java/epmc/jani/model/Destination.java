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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.util.UtilJSON;

/**
 * Destination of an edge of an automaton.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Destination implements JANINode {
    /** String indicating the probability of this destination. */
    private final static String PROBABILITY = "probability";
    /** String indicating the source location of this edge. */
    private final static String LOCATION = "location";
    /** String indicating the assignments of this edge. */
    private final static String ASSIGNMENTS = "assignments";
    //	AT: there are no transient/observable assignment in the JANI specification
    //	/** String indicating the observable assignments of this edge. */
    //	private final static String OBSERVABLES = "observables";
    /** String indicating comment of this destination. */
    private final static String COMMENT = "comment";

    /** Variables which can be assigned by this destination. */
    private Map<String, JANIIdentifier> validIdentifiers;
    /** Locations to which this destination may move. */
    private Map<String, Location> validLocations;

    /** Probability with which this destination is performed. */
    private Probability probability;
    /** Location to which this destination moves. */
    private Location location;
    /** Assignments performed by this destination. */
    private Assignments assignments;
    /** Observable assignments performed by this destination. */
    //	AT: there are no transient/observable assignment in the JANI specification
    //	private Assignments observableAssignments;
    /** Valid identifiers used during parsing. */
    private Map<String, JANIIdentifier> identifiers;
    /** Model to which this destination belongs. */
    private ModelJANI model;
    /** Comment of this destination. */
    private String comment;

    /**
     * Sets the variables which can be assigned by this destination.
     * 
     * @param variables variables which can be assigned by this destination
     */
    void setValidIdentifiers(Map<String,JANIIdentifier> variables) {
        this.validIdentifiers = variables;
        identifiers = new LinkedHashMap<>();
        for (JANIIdentifier variable : variables.values()) {
            identifiers.put(variable.getName(), variable);
        }
    }

    /**
     * Sets the locations to which this destination could move.
     * 
     * @param locations locations to which this destination could move
     */
    void setValidLocations(Map<String,Location> locations) {
        this.validLocations = locations;
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert model != null;
        assert value != null;
        UtilJSON.ensureObject(value);
        JsonObject object = (JsonObject) value;
        probability = UtilModelParser.parseOptional(model, () -> {
            Probability probability = new Probability();
            probability.setModel(model);
            probability.setIdentifiers(identifiers);
            return probability;
        }, object, PROBABILITY);
        location = UtilJSON.toOneOf(object, LOCATION, validLocations);
        assignments = UtilModelParser.parseOptional(model, () -> {
            Assignments assignments = new Assignments();
            assignments.setModel(model);
            assignments.setValidIdentifiers(validIdentifiers);
            return assignments;
        }, object, ASSIGNMENTS);

        //		AT: there are no transient/observable assignment in the JANI specification
        //		Map<String,JANIIdentifier> validObservable = new LinkedHashMap<>();
        //		validObservable.putAll(validIdentifiers);
        //		observableAssignments = UtilModelParser.parseOptional(model, () -> {
        //			Assignments assignments = new Assignments();
        //			assignments.setModel(model);
        //			assignments.setValidIdentifiers(validObservable);
        //			return assignments;
        //		}, object, OBSERVABLES);
        comment = UtilJSON.getStringOrNull(object, COMMENT);
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(LOCATION, location.getName());
        UtilModelParser.addOptional(result, PROBABILITY, probability);
        UtilModelParser.addOptional(result, ASSIGNMENTS, assignments);
        //		AT: there are no transient/observable assignment in the JANI specification
        //		UtilModelParser.addOptional(result, OBSERVABLES, observableAssignments);
        UtilJSON.addOptional(result, COMMENT, comment);
        return result.build();
    }

    public void setProbability(Probability probability) {
        this.probability = probability;
    }

    /**
     * Get the probability that this destination is performed.
     * 
     * @return probability that this destination is performed
     */
    public Probability getProbability() {
        return probability;
    }

    public Expression getProbabilityExpressionOrOne() {
        if (probability == null) {
            return new ExpressionLiteral.Builder()
                    .setValue("1")
                    .setType(ExpressionTypeInteger.TYPE_INTEGER)
                    .build();
        } else {
            return probability.getExp();
        }
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Get the location to which this destination moves.
     * 
     * @return location to which this destination moves
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the assignments performed by this destination.
     * 
     * @return assignments performed by this destination
     */
    public Assignments getAssignmentsOrEmpty() {
        Assignments result;
        if (assignments == null) {
            result = new Assignments();
            result.setModel(model);
            if (validIdentifiers != null) {
                result.setValidIdentifiers(validIdentifiers);
            }
        } else {
            result = assignments;
        }
        return result;
    }

    public Assignments getAssignments() {
        return assignments;
    }

    public void setAssignments(Assignments assignments) {
        this.assignments = assignments;
    }

    //	AT: there are no transient/observable assignment in the JANI specification
    //	/**
    //	 * Get the observable assignments performed by this destination.
    //	 * 
    //	 * @return observable assignments performed by this destination
    //	 */
    //	public Assignments getObservableAssignmentsOrEmpty() {
    //		Assignments result;
    //		if (observableAssignments == null) {
    //			result = new Assignments();
    //		} else {
    //			result = observableAssignments;
    //		}
    //		return result;
    //	}
    //
    //	public Assignments getObservableAssignments() {
    //		return observableAssignments;
    //	}
    //	
    //	public void setObservableAssignments(Assignments observableAssignments) {
    //		this.observableAssignments = observableAssignments;
    //	}

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}

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

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.util.UtilJSON;

public final class Edge implements JANINode {
    /** String identifying the source location. */
    private final static String LOCATION = "location";
    /** String identifying the action, if given. */
    private final static String ACTION = "action";
    /** String identifying the rate, if given. */
    private final static String RATE = "rate";
    /** String identifying the guard. */
    private final static String GUARD = "guard";
    /** String identifying the list of destinations. */
    private final static String DESTINATIONS = "destinations";
    /** String identifying comment of edge. */
    private final static String COMMENT = "comment";

    /** Possible locations of the edge. */
    private Map<String, Location> validLocations;
    /** Possible variables which can be assigned by the edge. */
    private Map<String, JANIIdentifier> validIdentifiers;

    /** Source location. */
    private Location location;
    /** Action of the edge, or {@code null}. */
    private Action action;
    /** Rate of the edge, or {@code null}. */
    private Rate rate;
    /** Guard of the edge. */
    private Guard guard;
    /** Destinations of the edge. */
    private Destinations destinations = new Destinations();
    /** Model to which this edge belongs to. */
    private ModelJANI model;
    /** Comment for this edge. */
    private String comment;

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    void setValidIdentifiers(Map<String,JANIIdentifier> validVariables) {
        this.validIdentifiers = validVariables;
    }

    void setValidLocations(Map<String, Location> validLocations) {
        this.validLocations = validLocations;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert model != null;
        assert value != null;
        assert this.validLocations != null;
        assert this.validIdentifiers != null;
        UtilJSON.ensureObject(value);
        JsonObject object = (JsonObject) value;
        location = UtilJSON.toOneOf(object, LOCATION, validLocations);
        action = null;
        if (object.get(ACTION) != null) {
            action = UtilJSON.toOneOf(object, ACTION, model.getActionsOrEmpty());
        }
        rate = UtilModelParser.parseOptional(model, () -> {
            Rate rate = new Rate();
            rate.setModel(model);
            rate.setIdentifiers(validIdentifiers);
            return rate;
        }, object, RATE);
        guard = UtilModelParser.parseOptional(model, () -> {
            Guard guard = new Guard();
            guard.setModel(model);
            guard.setIdentifier(validIdentifiers);
            return guard;
        }, object, GUARD);
        comment = UtilJSON.getStringOrNull(object, COMMENT);
        destinations.setValidLocations(validLocations);
        destinations.setValidIdentifiers(validIdentifiers);
        UtilModelParser.parse(model, destinations, object, DESTINATIONS);
        model.parseAfterModelNodeExtensions(this, value, validIdentifiers);
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(LOCATION, location.getName());
        if (action != null) {
            result.add(ACTION, action.getName());
        }
        UtilModelParser.addOptional(result, RATE, rate);
        UtilModelParser.addOptional(result, GUARD, guard);
        UtilJSON.addOptional(result, COMMENT, comment);
        result.add(DESTINATIONS, destinations.generate());
        return result.build();
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Action getActionOrSilent() {
        Action result;
        if (action == null) {
            result = model.getSilentAction();
        } else {
            result = action;
        }
        return result;
    }

    public Action getAction() {
        return action;
    }

    public void setRate(Rate rate) {
        this.rate = rate;
    }

    public Rate getRate() {
        return rate;
    }

    public void setGuard(Guard guard) {
        this.guard = guard;
    }

    public Guard getGuard() {
        return guard;
    }

    public Destinations getDestinations() {
        return destinations;
    }

    public void setDestinations(Destinations destinations) {
        this.destinations = destinations;
    }

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

    public Expression getGuardExpressionOrTrue() {
        if (guard == null) {
            return ExpressionLiteral.getTrue();
        } else {
            return guard.getExp();
        }
    }

    public Expression getRateExpression() {
        if (rate == null) {
            return null;
        } else {
            return rate.getExp();
        }
    }
}

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

package epmc.jani.model.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.ExpressionParser;
import epmc.util.UtilJSON;

/**
 * JANI property interval.
 * 
 * @author Andrea Turrini
 */
public final class JANIPropertyRewardBound implements JANINode {
    /** Identifier of this JANI expression type. */
    public final static String IDENTIFIER = "property-reward-bound";

    private final static String EXP = "exp";
    private final static String ACCUMULATE = "accumulate";
    private final static String BOUNDS = "bounds";

    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private boolean forProperty;
    private ModelJANI model;

    private boolean initialized;
    private Expression exp;
    private List<JANIPropertyAccumulateValue> accumulate;
    private JANIPropertyInterval bounds;

    private void resetFields() {
        initialized = false;
        exp = null;
        accumulate = null;
        bounds = null;
    }

    public JANIPropertyRewardBound() {
        resetFields();
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert model != null;
        assert validIdentifiers != null;
        assert value != null;
        resetFields();
        if (!forProperty) {
            return null;
        }
        if (!(value instanceof JsonObject)) {
            return null;
        }
        JsonObject object = (JsonObject) value;
        if (!object.containsKey(EXP)) {
            return null;
        }
        if (!object.containsKey(ACCUMULATE)) {
            return null;
        }
        if (!object.containsKey(BOUNDS)) {
            return null;
        }
        exp = ExpressionParser.parseExpression(model, object.get(EXP), validIdentifiers);
        JsonArray accumulate = UtilJSON.getArray(object, ACCUMULATE);
        this.accumulate = new ArrayList<>(accumulate.size());
        for (JsonValue acc : accumulate) {
            this.accumulate.add(UtilJSON.toOneOf(acc, JANIPropertyAccumulateValue.getAccumulateValues()));
        }
        bounds = new JANIPropertyInterval(); 
        bounds.setForProperty(forProperty);
        bounds.setIdentifiers(validIdentifiers);
        bounds.setModel(model);
        bounds.parse(object.get(BOUNDS));
        initialized = (exp != null) && (accumulate != null) && (bounds != null);
        return this;
    }

    @Override
    public JsonValue generate() {
        assert initialized;
        assert model != null;
        assert validIdentifiers != null;
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(EXP, ExpressionParser.generateExpression(model, exp));
        if (accumulate != null) {
            JsonArrayBuilder accumulateBuilder = Json.createArrayBuilder();
            for (JANIPropertyAccumulateValue acc : accumulate) {
                accumulateBuilder.add(acc.toString());
            }
            builder.add(ACCUMULATE, accumulateBuilder);
        }
        builder.add(BOUNDS, bounds.generate());
        return builder.build();
    }

    public void setExp(Expression exp) {
        this.exp = exp;
    }

    public Expression getExp() {
        return exp;
    }

    public void setBounds(JANIPropertyInterval bounds) {
        this.bounds = bounds;
        initialized = (exp != null) && (accumulate != null) && (bounds != null);
    }

    public JANIPropertyInterval getBounds() {
        return bounds;
    }

    public void setAccumulate(List<JANIPropertyAccumulateValue> accumulate) {
        this.accumulate = accumulate;
        initialized = (exp != null) && (accumulate != null) && (bounds != null);
    }

    public List<JANIPropertyAccumulateValue> getAccumulate() {
        assert accumulate != null;
        return accumulate;
    }

    public void setForProperty(boolean forProperty) {
        this.forProperty = forProperty;
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.validIdentifiers = identifiers;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}

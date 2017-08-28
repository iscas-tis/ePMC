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

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.TimeBound;
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
public final class JANIPropertyInterval implements JANINode {
    /** Identifier of this JANI expression type. */
    public final static String IDENTIFIER = "property-expression-property-interval";

    private final static String LOWER = "lower";
    private final static String LOWER_EXCLUSIVE = "lower-exclusive";
    private final static String UPPER = "upper";
    private final static String UPPER_EXCLUSIVE = "upper-exclusive";


    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private boolean forProperty;
    private ModelJANI model;

    private boolean initialized;
    private Expression lower;
    private boolean lowerExclusive;
    private Expression upper;
    private boolean upperExclusive;

    private void resetFields() {
        initialized = false;
        lower = null;
        lowerExclusive = false;
        upper = null;
        upperExclusive = false;
    }

    public JANIPropertyInterval() {
        resetFields();
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert validIdentifiers != null;
        assert model != null;
        assert value != null;
        resetFields();
        if (!forProperty) {
            return null;
        }
        if (!(value instanceof JsonObject)) {
            return null;
        }
        JsonObject object = (JsonObject) value;
        if (object.containsKey(LOWER)) {
            lower = ExpressionParser.parseExpression(model, object.get(LOWER), validIdentifiers);
            if (object.containsKey(LOWER_EXCLUSIVE)) {
                lowerExclusive = UtilJSON.getBoolean(object, LOWER_EXCLUSIVE);
            } else {
                lowerExclusive = false;
            }
        }
        if (object.containsKey(UPPER)) {
            upper = ExpressionParser.parseExpression(model, object.get(UPPER), validIdentifiers);
            if (object.containsKey(UPPER_EXCLUSIVE)) {
                upperExclusive = UtilJSON.getBoolean(object, UPPER_EXCLUSIVE);
            } else {
                upperExclusive = false;
            }
        }
        initialized = (lower != null) || (upper != null);
        return this;
    }

    @Override
    public JsonValue generate() {
        assert initialized;
        assert model != null;
        assert validIdentifiers != null;
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (lower != null) {
            builder.add(LOWER, ExpressionParser.generateExpression(model, lower));
            builder.add(LOWER_EXCLUSIVE, lowerExclusive);
        }
        if (upper != null) {
            builder.add(UPPER, ExpressionParser.generateExpression(model, upper));
            builder.add(UPPER_EXCLUSIVE, upperExclusive);
        }
        return builder.build();
    }

    public TimeBound getTimeBound() {
        assert initialized;
        assert model != null;
        assert validIdentifiers != null;
        TimeBound result;
        if (lower == null) {
            result = newTimeBound(ExpressionLiteral.getZero(), upper, false, upperExclusive);
        } else {
            if (upper == null) {
                result = newTimeBound(lower, ExpressionLiteral.getPosInf(), lowerExclusive, true);
            } else {
                result = newTimeBound(lower, upper, lowerExclusive, upperExclusive);
            }
        }
        return result;
    }

    public void setLower(Expression lower) {
        this.lower = lower;
        initialized = (lower != null) || (upper != null);
    }

    public Expression getLower() {
        return lower;
    }

    public void setLowerExclusive(boolean lowerExclusive) {
        this.lowerExclusive = lowerExclusive;
    }

    public boolean getLowerExclusive() {
        return lowerExclusive;
    }

    public void setUpper(Expression upper) {
        this.upper = upper;
        initialized = (lower != null) || (upper != null);
    }

    public Expression getUpper() {
        return upper;
    }

    public void setUpperExclusive(boolean upperExclusive) {
        this.upperExclusive = upperExclusive;
    }

    public boolean getUpperExclusive() {
        return upperExclusive;
    }

    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.validIdentifiers = identifiers;
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

    private static TimeBound newTimeBound(Expression left, Expression right,
            boolean leftOpen, boolean rightOpen) {
        return new TimeBound.Builder()
                .setLeft(left)
                .setRight(right)
                .setLeftOpen(leftOpen)
                .setRightOpen(rightOpen)
                .build();
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}

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

package epmc.jani.model.expression;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.error.UtilError;
import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ProblemsJANIParser;
import epmc.util.Util;
import epmc.util.UtilJSON;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

public final class ExpressionParser implements JANINode {
    private JANIExpression expression;
    /** Identifiers assumed to be valid. */
    private Map<String, ? extends JANIIdentifier> identifiers;
    /** Model for which this expression parser is used. */
    private ModelJANI model;
    /** Whether the parser shall be parsed to parse properties.
     * This allows expressions which cannot be used in other parts of the model
     * (probabilities, weights, etc.) such as e.g. Until formulas and the like.
     *  */
    private boolean forProperty;
    /** A string of arbitrary content. */
    private final static String ARBITRARY = "arbitrary";

    public ExpressionParser(ModelJANI model, Map<String, ? extends JANIIdentifier> identifiers, boolean forProperty) {
        this.model = model;
        this.identifiers = identifiers;
        this.forProperty = forProperty;
    }

    @Override
    public JANINode parse(JsonValue value) {
        return parseAsJANIExpression(value);
    }

    public JANIExpression parseAsJANIExpression(JsonValue value) {
        assert model != null;
        assert value != null;
        Map<String,Class<? extends JANIExpression>> types;
        if (forProperty) {
            types = model.getPropertyClasses();
        } else {
            types = model.getExpressionClasses();
        }
        for (Class<? extends JANIExpression> clazz : types.values()) {
            JANIExpression tryExpression = Util.getInstance(clazz);
            tryExpression.setModel(model);
            tryExpression.setForProperty(forProperty);
            tryExpression.setIdentifiers(identifiers);
            tryExpression = tryExpression.parseAsJANIExpression(value);
            if (tryExpression != null) {
                expression = tryExpression;
                break;
            }
        }
        UtilError.ensure(expression != null,
                ProblemsJANIParser.JANI_PARSER_CANNOT_PARSE_EXPRESSION, value);
        return expression;
    }

    public JANIExpression matchExpression(ModelJANI model, Expression expression) {
        assert model != null;
        this.expression = null;
        Map<String,Class<? extends JANIExpression>> types;
        if (forProperty) {
            types = model.getPropertyClasses();
        } else {
            types = model.getExpressionClasses();
        }
        for (Class<? extends JANIExpression> clazz : types.values()) {
            JANIExpression tryExpression = Util.getInstance(clazz);
            tryExpression.setModel(model);
            tryExpression.setIdentifiers(identifiers);
            tryExpression.setForProperty(forProperty);
            tryExpression = tryExpression.matchExpression(model, expression);
            if (tryExpression != null) {
                this.expression = tryExpression;
                break;
            }
        }
        assert this.expression != null : expression + " " + expression.getClass(); //TODO
        return this.expression;
    }

    @Override
    public JsonValue generate() {
        return expression.generate();
    }

    public JANIExpression getExpression() {
        return expression;
    }

    public void setForProperty(boolean forProperty) {
        this.forProperty = forProperty;
    }

    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    /**
     * Parse a JANI expression to an EPMC {@link Expression}.
     * The context and value parameter may not be {@code null}, whereas it is
     * allowed for the identifiers parameter to be {@code null}. The function
     * will transform a JANI expression to an according EPMC expression of
     * of the provided context. If the identifiers parameter is
     * non-{@code null}, then only identifiers from this map will be accepted.
     * That is, strings contained in the JSON value parsed will only be accepted
     * if they are keys in this map, and the resulting expression will be the
     * value to which they are mapped. If the identifiers parameter is {@code
     * null}, any valid identifier is accepted. If the identifiers map is not
     * {@code null}, it may not contain {@code null} keys or values.
     * 
     * @param context expression context of resulting expressions
     * @param value expression to be parsed
     * @param identifiers map of identifiers, or {@code null}
     * @return EPMC Expression
     */
    public static Expression parseExpression(ModelJANI model, JsonValue value, Map<String,? extends JANIIdentifier> identifiers) {
        assert model != null;
        assert value != null;
        ExpressionParser parser = new ExpressionParser(model, identifiers, false);
        return parser.parseAsJANIExpression(value).getExpression();
    }

    /**
     * Parse field of JSON object as expression.
     * This method will read the JSON value from the JSON object identified by
     * the key parameter, and will then use
     * {@link #parseExpression(ContextValue, JsonValue, Map)} to parse this
     * value. If the field value does not exist (and also if the expression is
     * invalid), and {@link EPMCException} will be thrown. The context,
     * object, and key paramters may not be {@code null}, while the identifier
     * parameter may be {@code null}, with the effect described in
     * {@link #parseExpression(ContextValue, JsonValue, Map)}.
     * 
     * @param context expression context of resulting expressions
     * @param object object from the key field which to read the expression
     * @param key field containing the expression
     * @param identifiers  map of identifiers, or {@code null}
     * @return parsed expression
     */
    public static Expression getExpression(ModelJANI model, JsonObject object, String key, Map<String,JANIIdentifier> identifiers) {
        assert model != null;
        if (identifiers != null) {
            for (Entry<String, JANIIdentifier> entry : identifiers.entrySet()) {
                assert entry.getKey() != null;
                assert entry.getValue() != null;
                assert entry.getKey().equals(entry.getValue().getName());
            }
        }
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        return parseExpression(model, object.get(key), identifiers);
    }

    /**
     * Transforms an EPMC {@link Expression} to its JANI JSON representation.
     * The parameter may not be {@code null}.
     * 
     * @param expression expression to be transformed
     * @return JANI representation
     */
    public static JsonValue generateExpression(ModelJANI model, Expression expression) {
        assert model != null : expression;
        assert expression != null;
        ExpressionParser parser = new ExpressionParser(model, Collections.emptyMap(), false);
        return parser.matchExpression(model, expression).generate();
    }

    /**
     * Transforms an EPMC {@link Value} to its JANI JSON representation.
     * The parameter may not be {@code null}.
     * 
     * @param value value to be transformed
     * @return JANI representation
     */
    public static JsonValue generateValue(Value value) {
        assert value != null;
        JsonObjectBuilder result = Json.createObjectBuilder();
        /* As there seems to be no way to directly generate simple JSON values
         * which are strings or numbers, we generate a JSON object, add an
         * element, and return this element as a result. If there turns out
         * to be a more elegant way with the same effect, the following code
         * should thus be changed. */
        if (ValueBoolean.isTrue(value)) {
            result.add(ARBITRARY, true);
            return result.build().get(ARBITRARY);
        } else if (ValueBoolean.isFalse(value)) {
            result.add(ARBITRARY, false);
            return result.build().get(ARBITRARY);			
        } else if (ValueInteger.is(value)) {
            result.add(ARBITRARY, ValueInteger.as(value).getInt());
            return result.build().get(ARBITRARY);
        } else if (ValueReal.is(value)) {
            result.add(ARBITRARY, new BigDecimal(value.toString()));
            return result.build().get(ARBITRARY);
        } else {
            assert false;
            return null;
        }
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }
}

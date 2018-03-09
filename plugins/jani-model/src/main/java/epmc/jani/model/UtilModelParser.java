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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.error.Positional.Builder;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionTypeBoolean;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.JANITypeBounded;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorLe;
import epmc.util.Util;
import epmc.util.UtilJSON;

/**
 * Auxiliary methods to parse JANI models and properties.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilModelParser {
    private final static String TRUE = "true";
    private final static String X_POSITIONAL = "x-positional";
    private final static String POSITIONAL_PART = "part";
    private final static String POSITIONAL_LINE = "line";
    private final static String POSITIONAL_COLUMN = "column";
    private final static String POSITIONAL_CONTENT = "content";
    
    @FunctionalInterface
    public interface NodeProvider <T extends JANINode> {
        T provide();
    }

    /**
     * Parse a (non-optional) JANI model node.
     * The object parameter must contain a field denoted by the key parameter.
     * If this is not the case, an {@link EPMCException} will be thrown.
     * Otherwise, the field will be read and
     * {@link JANINode#parse(ModelJANI, JsonValue)} of the result parameter
     * will called on the field read and the model parameter to parse the model
     * node.
     * None of the parameters may be {@code null}.
     * 
     * @param model JANI model the node is part of
     * @param result JANI model node to parse
     * @param object 
     * @param key
     */
    public static void parse(ModelJANI model, JANINode result, JsonObject object, String key) {
        assert result != null;
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        result.setModel(model);
        result.parse(object.get(key));
    }

    public static <T extends JANINode> T parse(ModelJANI model, Class<T> resultClass, JsonObject object, String key) {
        assert resultClass != null;
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        JsonValue got = object.get(key);
        T result = null;
        if (got != null) {
            result = Util.getInstance(resultClass);
            result.setModel(model);
            result.parse(object.get(key));			
        }
        return result;
    }

    public static <T extends JANINode> T parse(ModelJANI model, NodeProvider<T> provider, JsonObject object, String key) {
        assert provider != null;
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        JsonValue got = object.get(key);
        T result = null;
        if (got != null) {
            result = provider.provide();
            result.setModel(model);
            result.parse(object.get(key));			
        }
        return result;
    }

    public static <T extends JANINode> T parse(ModelJANI model, Class<T> resultClass, JsonValue value) {
        assert model != null;
        assert resultClass != null;
        assert value != null;
        T result = null;
        result = Util.getInstance(resultClass);
        result.setModel(model);
        result.parse(value);			
        return result;
    }

    public static <T extends JANINode> T parse(ModelJANI model, NodeProvider<T> provider, JsonValue value) {
        assert model != null;
        assert provider != null;
        assert value != null;
        T result = null;
        result = provider.provide();
        result.setModel(model);
        result.parse(value);			
        return result;
    }

    /**
     * Parse an optional JANI model node.
     * If the object parameter contains a field denoted by the key parameter,
     * the field will be read and
     * {@link JANINode#parse(ModelJANI, JsonValue)} of the result parameter
     * will called on the field read and the model parameter to parse the model
     * node.
     * If a field denoted by the key parameter is not present in the object
     * parameter, instead {@link JANINode#setParsed(ModelJANI)} will be
     * called.
     * None of the parameters may be {@code null}.
     * 
     * @param model JANI model the node is part of
     * @param result JANI model node to parse
     * @param object object to read element to be parsed from
     * @param key name of field to be read from object parameter
     */
    public static void parseOptional(ModelJANI model, JANINode result, JsonObject object, String key) {
        assert result != null;
        assert object != null;
        assert key != null;
        JsonValue got = object.get(key);
        if (got != null) {
            result.setModel(model);
            result.parse(object.get(key));			
        }
    }

    public static <T extends JANINode> T parseOptional(ModelJANI model, NodeProvider<T> provider, JsonObject object, String key) {
        assert provider != null;
        assert object != null;
        assert key != null;
        JsonValue got = object.get(key);
        T result = null;
        if (got != null) {
            result = provider.provide();
            result.setModel(model);
            result.parse(object.get(key));
        }
        return result;
    }

    public static <T extends JANINode> T parseOptional(ModelJANI model, Class<T> resultClass, JsonObject object, String key) {
        assert resultClass != null;
        assert object != null;
        assert key != null;
        JsonValue got = object.get(key);
        T result = null;
        if (got != null) {
            result = Util.getInstance(resultClass);
            result.setModel(model);
            result.parse(object.get(key));			
        }
        return result;
    }

    public static JsonObjectBuilder addOptional(JsonObjectBuilder object, String key, JANINode value) {
        assert object != null;
        assert key != null;
        if (value == null) {
            return object;
        }
        object.add(key, value.generate());
        return object;
    }

    /**
     * Method to implement {@code toString()} in JANI model node classes.
     * The method uses {@link JANINode#generate()} to generate a JSON
     * node, which is transformed to a {@link String} using
     * {@link JsonValue#toString()} and returned.
     * The node parameter must not be {@code null}.
     * 
     * @param node node to generate string representation of
     * @return string representation of node
     */
    public static String toString(JANINode node) {
        assert node != null;
        return node.generate().toString();
    }

    // TODO is this the right place for this function?
    public static Expression restrictToVariableRange(Iterable<Variable> variables) {
        assert variables != null;
        Expression result = null;
        for (Variable variable : variables) {
            JANIType type = variable.getType();
            Expression lower = null;
            Expression upper = null;
            if (type instanceof JANITypeBounded) {
                JANITypeBounded typeBounded = (JANITypeBounded) type;
                lower = new ExpressionOperator.Builder()
                        .setOperator(OperatorGe.GE)
                        .setOperands(variable.getIdentifier(), typeBounded.getLowerBound())
                        .build();
                upper = new ExpressionOperator.Builder()
                        .setOperator(OperatorLe.LE)
                        .setOperands(variable.getIdentifier(), typeBounded.getUpperBound())
                        .build();
            }
            Expression bound = null;
            if (lower == null && upper != null) {
                bound = upper;
            } else if (lower != null && upper == null) {
                bound = lower;
            } else if (lower != null && upper != null) {
                bound = UtilExpressionStandard.opAnd(lower, upper);				
            }
            if (bound != null) {
                result = (result == null) ? bound : UtilExpressionStandard.opAnd(result, bound);
            }
        }
        result = (result == null) ? new ExpressionLiteral.Builder()
                .setValue(TRUE)
                .setType(ExpressionTypeBoolean.TYPE_BOOLEAN)
                .build() : result;
                return result;
    }

    public static String prettyString(JANINode node) {
        assert node != null;
        return UtilJSON.prettyString(node.generate());
    }
    
    public static void addPositional(JsonObjectBuilder builder, Positional positional) {
        assert builder != null;
        if (positional == null) {
            return;
        }
        JsonObjectBuilder positionalBuilder = Json.createObjectBuilder();
        if (positional.getPart() != null) {
            positionalBuilder.add(POSITIONAL_PART, positional.getPart().toString());
        }
        if (positional.getLine() > 0) {
            positionalBuilder.add(POSITIONAL_LINE, positional.getLine());
        }
        if (positional.getColumn() > 0) {
            positionalBuilder.add(POSITIONAL_COLUMN, positional.getColumn());
        }
        if (positional.getContent() != null) {
            positionalBuilder.add(POSITIONAL_CONTENT, positional.getContent());
        }
        builder.add(X_POSITIONAL, positionalBuilder);
    }

    public static Positional getPositional(JsonValue value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonObject)) {
            return null;
        }
        JsonObject object = (JsonObject) value;
        if (!object.containsKey(X_POSITIONAL)) {
            return null;
        }
        JsonValue positionalValue = object.get(X_POSITIONAL);
        if (!(positionalValue instanceof JsonObject)) {
            return null;
        }
        JsonObject positionalObject = (JsonObject) positionalValue;
        Builder builder = new Positional.Builder();
        String part = UtilJSON.getStringOrNull(positionalObject, POSITIONAL_PART);
        if (part != null) {
            builder.setPart(part);
        }
        Long line = UtilJSON.getLongOrNull(positionalObject, POSITIONAL_LINE);
        if (line != null) {
            builder.setLine(line);
        }
        Long column = UtilJSON.getLongOrNull(positionalObject, POSITIONAL_COLUMN);
        if (column != null) {
            builder.setColumn(column);
        }
        String content = UtilJSON.getStringOrNull(positionalObject, POSITIONAL_CONTENT);
        if (content != null) {
            builder.setContent(content);
        }
        return builder.build();
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilModelParser() {
    }
}

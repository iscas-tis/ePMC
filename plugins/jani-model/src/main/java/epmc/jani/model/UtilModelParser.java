package epmc.jani.model;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.util.Util;
import epmc.util.UtilJSON;
import epmc.value.ContextValue;
import epmc.value.OperatorGe;
import epmc.value.OperatorLe;
import epmc.value.TypeBoolean;
import epmc.value.TypeBounded;
import epmc.value.Value;

/**
 * Auxiliary methods to parse JANI models and properties.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilModelParser {
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
	 * @throws EPMCException
	 */
	public static void parse(ModelJANI model, JANINode result, JsonObject object, String key) throws EPMCException {
		assert result != null;
		assert object != null;
		assert key != null;
		UtilJSON.ensurePresent(object.get(key), key);
		result.setModel(model);
		result.parse(object.get(key));
	}

	public static <T extends JANINode> T parse(ModelJANI model, Class<T> resultClass, JsonObject object, String key) throws EPMCException {
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

	public static <T extends JANINode> T parse(ModelJANI model, NodeProvider<T> provider, JsonObject object, String key) throws EPMCException {
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

	public static <T extends JANINode> T parse(ModelJANI model, Class<T> resultClass, JsonValue value) throws EPMCException {
		assert model != null;
		assert resultClass != null;
		assert value != null;
		T result = null;
		result = Util.getInstance(resultClass);
		result.setModel(model);
		result.parse(value);			
		return result;
	}
	
	public static <T extends JANINode> T parse(ModelJANI model, NodeProvider<T> provider, JsonValue value) throws EPMCException {
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
	 * @throws EPMCException thrown in case of problems during parsing
	 */
	public static void parseOptional(ModelJANI model, JANINode result, JsonObject object, String key) throws EPMCException {
		assert result != null;
		assert object != null;
		assert key != null;
		JsonValue got = object.get(key);
		if (got != null) {
			result.setModel(model);
			result.parse(object.get(key));			
		}
	}

	public static <T extends JANINode> T parseOptional(ModelJANI model, NodeProvider<T> provider, JsonObject object, String key) throws EPMCException {
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

	public static <T extends JANINode> T parseOptional(ModelJANI model, Class<T> resultClass, JsonObject object, String key) throws EPMCException {
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
	
	public static JsonObjectBuilder addOptional(JsonObjectBuilder object, String key, JANINode value) throws EPMCException {
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
		try {
			return node.generate().toString();
		} catch (EPMCException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Expression restrictToVariableRange(ContextValue contextValue,
			Iterable<Variable> variables) throws EPMCException {
		assert contextValue != null;
		assert variables != null;
		Expression result = null;
		for (Variable variable : variables) {
			Value lowerValue = TypeBounded.getLower(variable.toType());
			Expression lower = null;
			if (lowerValue != null) {
				Expression lowerValueExpr = new ExpressionLiteral.Builder()
						.setValue(lowerValue)
						.build();
				lower = new ExpressionOperator.Builder()
						.setOperator(contextValue.getOperator(OperatorGe.IDENTIFIER))
						.setOperands(variable.getIdentifier(), lowerValueExpr)
						.build();
			}
			Value upperValue = TypeBounded.getUpper(variable.toType());
			Expression upper = null;
			if (upperValue != null) {
				Expression upperValueExpr = new ExpressionLiteral.Builder()
						.setValue(upperValue)
						.build();
				upper = new ExpressionOperator.Builder()
						.setOperator(contextValue.getOperator(OperatorLe.IDENTIFIER))
						.setOperands(variable.getIdentifier(), upperValueExpr)
						.build();
			}
			Expression bound = null;
			if (lower == null && upper != null) {
				bound = upper;
			} else if (lower != null && upper == null) {
				bound = lower;
			} else if (lower != null && upper != null) {
				bound = UtilExpressionStandard.opAnd(contextValue, lower, upper);				
			}
			if (bound != null) {
				result = (result == null) ? bound : UtilExpressionStandard.opAnd(contextValue, result, bound);
			}
		}
		TypeBoolean typeBoolean = TypeBoolean.get(contextValue);
		result = (result == null) ? new ExpressionLiteral.Builder()
				.setValue(typeBoolean.getTrue())
				.build() : result;
		return result;
	}
	
	/**
	 * Private constructor to prevent instantiation of this class.
	 */
	private UtilModelParser() {
	}

	public static String prettyString(JANINode node) throws EPMCException {
		assert node != null;
		return UtilJSON.prettyString(node.generate());
	}
}

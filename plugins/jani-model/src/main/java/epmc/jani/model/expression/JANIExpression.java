package epmc.jani.model.expression;

import java.util.Map;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;

/**
 * JANI representation of expressions and expression parser.
 * 
 * @author Ernst Moritz Hahn
 */
public interface JANIExpression extends JANINode {
	default void setForProperty(boolean forProperty) {
	}
	
	Expression getExpression() throws EPMCException;
	
	/**
	 * Match the given expression and returns the corresponding {@link epmc.jani.model.expression.JANIExpression}.
	 * This function tries to match the given expression in the  context of the given model; in case of successful matching, the corresponding  
	 * {@link epmc.jani.model.expression.JANIExpression} is returned, {@code null} otherwise.
	 * 
	 * @param model the model to use as reference
	 * @param expression the expression to match
	 * @return the {@link epmc.jani.model.expression.JANIExpression} representing the expression, {@code null} if no matching is possible
	 */
	JANIExpression matchExpression(ModelJANI model, Expression expression) throws EPMCException;
	
	JANIExpression parseAsJANIExpression(JsonValue value) throws EPMCException;
	
	void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers);
}

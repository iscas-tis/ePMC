package epmc.jani.model;

import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.jani.model.type.JANIType;

/**
 * Entity in a JANI model which is either a variable or a constant.
 * 
 * @author Ernst Moritz Hahn
 */
public interface JANIIdentifier {
	/**
	 * Get identifier expression of this JANI identifier.
	 * 
	 * @return expression identifier of this JANI identifier.
	 */
	ExpressionIdentifierStandard getIdentifier();

	/**
	 * Get JANI type of this identifier.
	 * 
	 * @return JANI type of this identifier
	 */
	JANIType getType();

	/**
	 * Get name of this identifier.
	 * 
	 * @return name of this identifier
	 */
	String getName();
}

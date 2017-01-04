package epmc.jani.model;

import epmc.error.EPMCException;
import epmc.modelchecker.Model;

// TODO documentation
// TODO use this for PRISM, QMC, and RDDL models
/**
 * Model which can be converted to a JANI model.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ModelJANIConverter extends Model {
	/**
	 * Convert model to equivalent JANI representation.
	 * An exception might be thrown in case the concrete model instance uses
	 * features which cannot be converted to a JANI model (at present).
	 * 
	 * @return JANI representation of model
	 * @throws EPMCException thrown in case of problems
	 */
	ModelJANI toJANI() throws EPMCException;
}

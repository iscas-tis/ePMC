package epmc.jani.model;

import javax.json.JsonValue;

import epmc.error.EPMCException;

/**
 * Interface for JANI model parts which can be read and written from/to JANI.
 * 
 * @author Ernst Moritz Hahn
 */
public interface JANINode {
	/**
	 * Set model which this node belongs to.
	 * For some nodes implementing this interface, it might be necessary to call
	 * this function before parsing and similar functionality.
	 * 
	 * @param model model this node is part of
	 */
	void setModel(ModelJANI model);

	ModelJANI getModel();

	/**
	 * Parse JSON to JANI node.
	 * By performing this function, the JANI node will be filled with content
	 * and can be used afterwards. The method may only be called once. It must
	 * not be called with any {@code null} parameters. Before performing this
	 * method, additional calls to set context objects may be necessary. After
	 * this call, the object should become immutable. The method may only be
	 * called once.
	 * 
	 * @param model model to which this node belongs
	 * @param value JSON to convert to JANI
	 * @return the JANINode corresponding to the given value, or {@code null} if the parsing is not possible
	 * @throws EPMCException in case of incorrect JSON input
	 */
	JANINode parse(JsonValue value) throws EPMCException;
	
	/**
	 * Generate JSON from this JANI node.
	 * Must only be called after {@link #parseExpression(Parser, JsonValue)} has been
	 * called successfully.
	 * 
	 * @return JSON representing this JANI node
	 */
	JsonValue generate() throws EPMCException;
}

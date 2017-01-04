package epmc.jani.model;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;

/**
 * Extension for JANI models.
 * This interface is used for classes which extend the parsing process of JANI
 * models.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ModelExtension {
	String getIdentifier();
	
	default void setModel(ModelJANI model) throws EPMCException {
	}
	

	default ModelJANI getModel() {
		return null;
	}

	
	default void setNode(JANINode node) throws EPMCException {
	}
	
	default void setJsonValue(JsonValue value) throws EPMCException {
	}

	default void parseBefore() throws EPMCException {
	}
	
	default void parseAfter() throws EPMCException {
	}
	
	default void generate(JsonObjectBuilder generate) throws EPMCException {
	}
}

package epmc.jani.model;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.util.UtilJSON;

/**
 * Specifies an action in a model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Action implements JANINode {
	/** Identifies the name of a given action. */
	private final static String NAME = "name";
	private final static String COMMENT = "comment";
	
	/** Name of the action. */
	private String name;

	private ModelJANI model;
	
	private String comment;
	
	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}
	
	@Override
	public ModelJANI getModel() {
		return model;
	}
	
	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		assert model != null;
		assert value != null;
		JsonObject object = UtilJSON.toObject(value);
		name = UtilJSON.getString(object, NAME);
		comment = UtilJSON.getStringOrNull(object, COMMENT);
		return this;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}
	
	@Override
	public JsonValue generate() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(NAME, getName()).build();
		UtilJSON.addOptional(builder, COMMENT, comment);
		return builder.build();
	}
	
	/**
	 * Gets the name of the action.
	 * 
	 * @return name of the action
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}

}

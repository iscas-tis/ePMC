package epmc.jani.model.component;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import epmc.error.EPMCException;
import epmc.jani.model.Action;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.util.UtilJSON;

public final class SynchronisationVectorSync implements JANINode {
	private final static String SYNCHRONISE = "synchronise";
	private final static String RESULT = "result";
	private final static String COMMENT = "comment";
	
	private ModelJANI model;
	private List<Action> synchronise;
	private Action result;
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
		assert value != null;
		JsonObject object = UtilJSON.toObject(value);
		JsonArray synchronise = UtilJSON.getArray(object, SYNCHRONISE);
		this.synchronise = new ArrayList<>();
		for (JsonValue sync : synchronise) {
			if (sync.getValueType() == ValueType.NULL) {
				this.synchronise.add(null);
			} else {
				this.synchronise.add(UtilJSON.toOneOf(sync, model.getActions()));
			}
		}
		result = UtilJSON.toOneOfOrNull(object, RESULT, model.getActions());
		//AT: from 2016-09-21 the result is optional; if omitted, it is the silent action
		//TODO: check if the silent action has to be added to the model
		if (result == null) {
			result = model.getSilentAction();
		}
		comment = UtilJSON.getStringOrNull(object, COMMENT);
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JsonArrayBuilder synchronise = Json.createArrayBuilder();
		for (Action sync : this.synchronise) {
			if (sync == null) {
				synchronise.addNull();
			} else {
				synchronise.add(sync.getName());
			}
		}
		builder.add(SYNCHRONISE, synchronise);
		builder.add(RESULT, result.getName());
		UtilJSON.addOptional(builder, COMMENT, comment);
		return builder.build();
	}

	public void setSynchronise(List<Action> synchronise) {
		this.synchronise = synchronise;
	}
	
	public List<Action> getSynchronise() {
		return synchronise;
	}
	
	public void setResult(Action result) {
		this.result = result;
	}
	
	public Action getResult() {
		return result;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}
	
}

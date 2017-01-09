package epmc.jani.model.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.ExpressionParser;
import epmc.util.UtilJSON;

/**
 * JANI property interval.
 * 
 * @author Andrea Turrini
 */
public final class JANIPropertyRewardInstant implements JANINode {
	/** Identifier of this JANI expression type. */
	public final static String IDENTIFIER = "property-reward-instant";

	private final static String EXP = "exp";
	private final static String ACCUMULATE = "accumulate";
	private final static String INSTANTS = "instants";
	
	private Map<String, ? extends JANIIdentifier> validIdentifiers;
	private boolean forProperty;
	private ModelJANI model;
	
	private boolean initialized;
	private Expression exp;
	private List<JANIPropertyAccumulateValue> accumulate;
	private Expression instants;
	
	private void resetFields() {
		initialized = false;
		exp = null;
		accumulate = null;
		instants = null;
	}
	
	public JANIPropertyRewardInstant() {
		resetFields();
	}
	
	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		assert model != null;
		assert validIdentifiers != null;
		assert value != null;
		resetFields();
		if (!forProperty) {
			return null;
		}
		if (!(value instanceof JsonObject)) {
			return null;
		}
		JsonObject object = (JsonObject) value;
		if (!object.containsKey(EXP)) {
			return null;
		}
		if (!object.containsKey(ACCUMULATE)) {
			return null;
		}
		if (!object.containsKey(INSTANTS)) {
			return null;
		}
		exp = ExpressionParser.parseExpression(model, object.get(EXP), validIdentifiers);
		JsonArray accumulate = UtilJSON.getArray(object, ACCUMULATE);
		this.accumulate = new ArrayList<>(accumulate.size());
		for (JsonValue acc : accumulate) {
			this.accumulate.add(UtilJSON.toOneOf(acc, JANIPropertyAccumulateValue.getAccumulateValues()));
		}
		instants = ExpressionParser.parseExpression(model, object.get(INSTANTS), validIdentifiers);
		initialized = (exp != null) && (accumulate != null) && (instants != null);
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		assert initialized;
		assert model != null;
		assert validIdentifiers != null;
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(EXP, ExpressionParser.generateExpression(model, exp));
		if (accumulate != null) {
			JsonArrayBuilder accumulateBuilder = Json.createArrayBuilder();
			for (JANIPropertyAccumulateValue acc : accumulate) {
				accumulateBuilder.add(acc.toString());
			}
			builder.add(ACCUMULATE, accumulateBuilder);
		}
		builder.add(INSTANTS, ExpressionParser.generateExpression(model, instants));
		return builder.build();
	}

	public void setRef(Expression ref) {
		this.exp = ref;
		initialized = (ref != null) && (accumulate != null) && (instants != null);
	}
	
	public Expression getRef() {
		return exp;
	}
	
	public void setInstants(Expression instants) {
		this.instants = instants;
		initialized = (exp != null) && (accumulate != null) && (instants != null);
	}
	
	public Expression getInstants() {
		return instants;
	}
	
	public void setAccumulate(List<JANIPropertyAccumulateValue> accumulate) {
		this.accumulate = accumulate;
		initialized = (exp != null) && (accumulate != null) && (instants != null);
	}
	
	public List<JANIPropertyAccumulateValue> getAccumulate() {
		return accumulate;
	}
	
	public void setForProperty(boolean forProperty) {
		this.forProperty = forProperty;
	}
	
	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}

	@Override
	public ModelJANI getModel() {
		return model;
	}
	
	public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
		this.validIdentifiers = identifiers;
	}
	
	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
}
